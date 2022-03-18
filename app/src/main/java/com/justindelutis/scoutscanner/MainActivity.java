package com.justindelutis.scoutscanner;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private TextView scannedList;
    private Button importButton;
    private ArrayList<String> matchScoutInfo;
    private final String SCAN_INTENT = "com.justindelutis.scoutscanner.SCAN";
    private final String SCAN_DATA = "com.symbol.datawedge.data_string";
    private final String LABEL_TYPE = "com.symbol.datawedge.label_type";
    private final String QR_LABEL = "LABEL-TYPE-QRCODE";
    private final String SCAN_TEXT = "com.justindelutis.scoutscanner.SCANNED_SCOUT_INFO";
    private final String SCOUT_ARRAY = "com.justindelutis.scoutscanner.SCOUTING_ARRAY_LIST";
    private final String BUTTON_STATUS = "com.justindelutis.scoutscanner.BUTTON_STATUS";

    private String CSV_FILE = "/scout.csv";
    private  String EXPORT_PATH = Environment.DIRECTORY_DOWNLOADS + CSV_FILE;

    private boolean writeStoragePerms;

    private final Pattern FORMAT_PATTERN = Pattern.compile("^(s=[\\w\\s]+;e=\\w+;l=\\w+;m=\\d+;r=[rb][1-3];t=\\d+;as=\\[\\w+\\];at=[YN];au=\\d+;al=\\d+;ac=[YN];tu=\\d+;tl=\\d+;wd=[YN];wbt=[YN];cif=\\w+;ss=(\\[(\\d+,?)*\\])?;c=\\w+;lsr=\\w+;be=[YN];cn=\\w+;ds=\\w+;dr=\\w+;ba=[YN];d=[YN];cf=[YN];all=[YN];co=[\\w\\s]*;cnf=\\w+)");
    private Pattern DATA_PATTERN;

    private static final String TAG = "MainActivity";

    private ActivityResultLauncher<String> reqPermLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if(!isGranted) {
            Toast.makeText(this, "This App Requires Storage Permissions to Work!", Toast.LENGTH_LONG).show();
            writeStoragePerms = false;
        } else {
            writeStoragePerms = true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scannedList = findViewById(R.id.scanned_view);
        importButton = findViewById(R.id.import_button);
        ImageButton deleteButton = findViewById(R.id.deleteButton);
        matchScoutInfo = new ArrayList<String>();

        importButton.setEnabled(false);
        scannedList.setMovementMethod(new ScrollingMovementMethod());
//        importButton.setOnClickListener(importScanned());

        boolean storage = verifyStoragePermissions(this);

        String dataPattern = "";


        dataPattern += "(?:=)([^;]*)(?:;)?";

        DATA_PATTERN = Pattern.compile(dataPattern);

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    exportData(matchScoutInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(view.getContext(), "Exported to /sdcard/Download/scout.csv", Toast.LENGTH_SHORT).show();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.AlertDialog.Builder build = new AlertDialog.Builder(view.getContext())
                    .setCancelable(true)
                    .setTitle(R.string.delete_title)
                    .setMessage(R.string.delete_message)
                    .setPositiveButton(R.string.confirm_delete_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    File DOWNLOADS_EXPORT = Environment.getExternalStoragePublicDirectory(EXPORT_PATH);
                                    boolean deleted = DOWNLOADS_EXPORT.delete();
                                    scannedList.setText("");
                                    Log.d(TAG, "onClick: Delete Clicked: deleted=" + deleted);
                                    if (deleted) {
                                        Toast.makeText(view.getContext(), "CSV File Deleted!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(view.getContext(), "CSV File Does Not Exist!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                    .setNegativeButton(R.string.cancel_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.d(TAG, "onClick: Delete Action Cancelled!");
                                }
                            });
                android.app.AlertDialog dialog = build.create();
                dialog.show();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent i) {
        if(i.getAction().equals(SCAN_INTENT)) {
            String[] scanData = handleScanAction(i);
            if(scanData!=null) {
                scannedList.setText(scannedList.getText() + "Name: " + scanData[0] + "\t" + "Team: " + scanData[5] + "\t" + "Match: " + scanData[3] + "\n");
                matchScoutInfo.add(String.join(",", scanData));
            }
            importButton.setEnabled(true);
        } else {
            super.onNewIntent(i);
        }
    }

    private String[] handleScanAction(Intent i ) {
        String[] data = new String[29];
        String scannedData = i.getStringExtra(SCAN_DATA);
        String scannedType = i.getStringExtra(LABEL_TYPE);
        if(!(scannedData==null || scannedData.equals("")) && scannedType.equals(QR_LABEL)) {
            Matcher matchForm = FORMAT_PATTERN.matcher(scannedData);
            if(matchForm.find()) {
                Matcher matchData = DATA_PATTERN.matcher(scannedData);
                    for(int j=0; j<29; j++) {
                        if(matchData.find()) {
                            data[j] = matchData.group(1);
                            if(j==16) {
                                data[j] = "\"" + data[j] + "\"";
                            }
                        }
                    }
                    return data;
            } else {
                scannedList.setText(scannedList.getText() + "\nInvalid Barcode");
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        String textData = scannedList.getText().toString();
        outState.putString(SCAN_TEXT, textData);
        outState.putBoolean(BUTTON_STATUS, importButton.isEnabled());
        outState.putStringArrayList(SCOUT_ARRAY, matchScoutInfo);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String textData = savedInstanceState.getString(SCAN_TEXT);
        boolean buttonStatus = savedInstanceState.getBoolean(BUTTON_STATUS);
        ArrayList<String> list = savedInstanceState.getStringArrayList(SCOUT_ARRAY);
        scannedList.setText(textData);
        importButton.setEnabled(buttonStatus);
        matchScoutInfo = list;
    }

    private void exportData(ArrayList<String> list) throws IOException {
        File DOWNLOADS_EXPORT = Environment.getExternalStoragePublicDirectory(EXPORT_PATH);
        FileWriter fw = null;
        if(DOWNLOADS_EXPORT.exists()) {
            fw = new FileWriter(DOWNLOADS_EXPORT, true);
        } else {
            fw = new FileWriter(DOWNLOADS_EXPORT);
        }
        scannedList.setText("");
        importButton.setEnabled(false);
        Iterator<String> csvIterator = matchScoutInfo.iterator();
        while(csvIterator.hasNext()) {
            fw.append(csvIterator.next() + "\n");
        }
            fw.close();
        matchScoutInfo.clear();
    }

    private boolean verifyStoragePermissions(Activity act) {
        int perm = ContextCompat.checkSelfPermission(act, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(perm != PackageManager.PERMISSION_GRANTED) {
            reqPermLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return writeStoragePerms;
    }
}
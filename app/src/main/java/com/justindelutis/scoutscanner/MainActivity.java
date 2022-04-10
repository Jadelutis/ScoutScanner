package com.justindelutis.scoutscanner;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private TextView scannedList;
    private Button importButton;
    private ArrayList<String> matchScoutInfo;
    private ListView scoutView;
    private ScoutAdapter scoutAdapter;
    private final String SCAN_INTENT = "com.justindelutis.scoutscanner.SCAN";
    private final String SCAN_DATA = "com.symbol.datawedge.data_string";
    private final String LABEL_TYPE = "com.symbol.datawedge.label_type";
    private final String QR_LABEL = "LABEL-TYPE-QRCODE";
    private final String SCAN_TEXT = "com.justindelutis.scoutscanner.SCANNED_SCOUT_INFO";
    private final String SCOUT_ARRAY = "com.justindelutis.scoutscanner.SCOUTING_ARRAY_LIST";
    private final String BUTTON_STATUS = "com.justindelutis.scoutscanner.BUTTON_STATUS";
    private ArrayList<ScoutRecord> scoutingArray;
    private final String CSV_FILE = "/scout.csv";
    private final String EXPORT_PATH = Environment.DIRECTORY_DOWNLOADS + CSV_FILE;
    private boolean writeStoragePerms;
    private final Pattern FORMAT_PATTERN = Pattern.compile("^(s=[^;]*;.*)");
    private static final String TAG = "MainActivity";

    private final ActivityResultLauncher<String> reqPermLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
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
        this.scoutingArray = new ArrayList<ScoutRecord>();
        this.scoutAdapter = new ScoutAdapter(this, R.layout.scouting_record, scoutingArray);
        this.scoutView = findViewById(R.id.scoutView);
        scoutView.setAdapter(scoutAdapter);
        importButton = findViewById(R.id.import_button);
        ImageButton deleteButton = findViewById(R.id.deleteButton);

        //DISABLE DELETE BUTTON FOR USE IN STANDS
        deleteButton.setEnabled(false);


        importButton.setEnabled(false);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    exportData(scoutingArray);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            String scannedData = i.getStringExtra(SCAN_DATA);
            String scannedType = i.getStringExtra(LABEL_TYPE);
            if(!(scannedData==null || scannedData.equals("")) && scannedType.equals(QR_LABEL)) {
                Matcher matchForm = FORMAT_PATTERN.matcher(scannedData);
                if(matchForm.find()) {
                    ScoutRecord newRecord = new ScoutRecord(scannedData);
                    scoutingArray.add(newRecord);
                    Log.d(TAG, "handleScanAction: New Scout Record Added!\n" + newRecord);
                    this.scoutAdapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(MainActivity.this, "Invalid Barcode, Please Try Again!", Toast.LENGTH_SHORT).show();
            }
            importButton.setEnabled(true);
        } else {
            super.onNewIntent(i);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(BUTTON_STATUS, importButton.isEnabled());
        String[] saveArrList = new String[scoutingArray.size()];
        for(int i = 0; i<scoutingArray.size(); i++) {
            saveArrList[i] = scoutingArray.get(i).getOriginalScan();
        }
        outState.putStringArray(SCOUT_ARRAY, saveArrList);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean buttonStatus = savedInstanceState.getBoolean(BUTTON_STATUS);
        String[] restArray = savedInstanceState.getStringArray(SCOUT_ARRAY);
        importButton.setEnabled(buttonStatus);
        Iterator<String> stringIterator = Arrays.stream(restArray).iterator();
        while(stringIterator.hasNext()) {
            scoutingArray.add(new ScoutRecord(stringIterator.next()));
        }
        scoutAdapter.notifyDataSetChanged();
    }

    private void exportData(ArrayList<ScoutRecord> list) throws IOException {
        File DOWNLOADS_EXPORT = Environment.getExternalStoragePublicDirectory(EXPORT_PATH);
        FileWriter fw = null;
        if(DOWNLOADS_EXPORT.exists()) {
            fw = new FileWriter(DOWNLOADS_EXPORT, true);
        } else {
            fw = new FileWriter(DOWNLOADS_EXPORT);
        }
        for (ScoutRecord scoutRecord : list) {
            fw.append(scoutRecord.getExportable());
        }
        fw.close();
        list.clear();
        scoutAdapter.notifyDataSetChanged();
        importButton.setEnabled(false);
        Toast.makeText(MainActivity.this, "Exported to /sdcard/Download/scout.csv", Toast.LENGTH_SHORT).show();
    }

    private boolean verifyStoragePermissions(Activity act) {
        int perm = ContextCompat.checkSelfPermission(act, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(perm != PackageManager.PERMISSION_GRANTED) {
            reqPermLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return writeStoragePerms;
    }
}
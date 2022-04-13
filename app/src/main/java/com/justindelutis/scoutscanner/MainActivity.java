package com.justindelutis.scoutscanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private Button importButton;
    private ListView scoutView;
    private ScoutAdapter scoutAdapter;
    private final String SCAN_INTENT = "com.justindelutis.scoutscanner.SCAN";
    private final String SCAN_DATA = "com.symbol.datawedge.data_string";
    private final String LABEL_TYPE = "com.symbol.datawedge.label_type";
    private final String QR_LABEL = "LABEL-TYPE-QRCODE";
    private final String SCOUT_ARRAY = "com.justindelutis.scoutscanner.SCOUTING_ARRAY_LIST";
    private final String BUTTON_STATUS = "com.justindelutis.scoutscanner.BUTTON_STATUS";
    public static final String RECORD_INFO_ARRAY = "com.justindelutis.scoutscanner.RECORD_INFO_ARRAY";
    public static final String RECORD_POSITION = "com.justindelutis.scoutscanner.RECORD_POSITION";
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
        TextView appTitle = findViewById(R.id.app_title);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.scoutingArray = new ArrayList<ScoutRecord>();
        this.scoutAdapter = new ScoutAdapter(this, R.layout.scouting_record, scoutingArray);
        this.scoutView = findViewById(R.id.scoutView);
        scoutView.setAdapter(scoutAdapter);
        scoutView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent i = new Intent();
                i.setClass(getApplicationContext(), RecordInfo.class);
                i.putExtra(RECORD_INFO_ARRAY, scoutingArray.get(position));
                i.putExtra(RECORD_POSITION, position);
                startActivityForResult(i, 411);
            }
        });

        importButton = findViewById(R.id.import_button);
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

        ImageButton settings = findViewById(R.id.settingsButton);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //something
            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 411) {
            assert data != null;
            if (!data.getBooleanExtra(RecordInfo.DELETE_FLAG, false)) {
                return;
            }
            int deletePosition = data.getIntExtra(RecordInfo.RECORD_POSITION_DELETE, -1);
            if(deletePosition != -1) {
                scoutingArray.remove(deletePosition);
                if(scoutingArray.size() == 0) {
                    importButton.setEnabled(false);
                }
                scoutAdapter.notifyDataSetChanged();
            }
        }
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
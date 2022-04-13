package com.justindelutis.scoutscanner;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class RecordInfo extends AppCompatActivity {
    public static final String DELETE_FLAG = "com.justindelutis.scoutscanner.DELETE_FLAG";
    public static final String RECORD_POSITION_DELETE = "com.justindelutis.scoutscanner.RECORD_POSITION_DELETE";

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_info);

        ImageButton delete = findViewById(R.id.deleteRecord);

        Intent i = getIntent();
        ScoutRecord data = (ScoutRecord) i.getSerializableExtra(MainActivity.RECORD_INFO_ARRAY);
        int position = i.getExtras().getInt(MainActivity.RECORD_POSITION);
        String origString = data.getOriginalScan();
        TextView tv = findViewById(R.id.infoDisplay);
        TextView teamDisplay = findViewById(R.id.teamDisplay);
        TextView matchDisplay = findViewById(R.id.matchDisplay);
        String[] splitData = origString.split(";");
        for(String j:splitData) {
            tv.append(j + "\n");
        }
        teamDisplay.setText(String.format("Team: %d", data.getTeam()));
        matchDisplay.setText(String.format("Match: %d", data.getMatch()));

        tv.setMovementMethod(new ScrollingMovementMethod());

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent result = new Intent();
                new AlertDialog.Builder(RecordInfo.this).setCancelable(true).setCancelable(true)
                        .setTitle("Delete This Record?")
                        .setMessage("Press delete to delete this record. This cannot be undone!")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                result.putExtra(RECORD_POSITION_DELETE, position);
                                result.putExtra(DELETE_FLAG, true);
                                setResult(RESULT_OK, result);
                                finish();
                            }
                        }).create().show();
            }
        });
    }
}
package com.justindelutis.scoutscanner;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class ScoutRecord implements Serializable {
    private static final String TAG = "ScoutRecord";
    private String[] scoutData;
    private final String originalScan;

    public ScoutRecord(String scoutData) {
        this.originalScan = scoutData;
        parseData();
    }

    public void parseData() {
        String[] semiSplit = this.originalScan.split(";");
        this.scoutData = new String[semiSplit.length];
        for(int i = 0; i<semiSplit.length; i++) {
            if(semiSplit[i].indexOf("=") != semiSplit[i].length()-1) {
                this.scoutData[i] = semiSplit[i].split("=")[1];
            } else {
                this.scoutData[i] = "";
            }
        }
    }

    public String getOriginalScan() {
        return this.originalScan;
    }

    public String getScouter() {
        return this.scoutData[0];
    }

    public String getEvent() {
        return this.scoutData[1];
    }

    public int getMatch() {
        Log.d(TAG, "getMatch: Match: " + this.scoutData[3]);
        return Integer.parseInt(this.scoutData[3]);
    }

    public String getRobotPosition() {
        char[] charPosi = this.scoutData[4].toCharArray();
        String robotPosition = "";
        switch(charPosi[0]) {
            case 'b':
                robotPosition += "Blue ";
                break;
            case 'r':
                robotPosition += "Red ";
                break;
            default:
                robotPosition += "Unknown ";
                break;
        }

        robotPosition += charPosi[1];
        return robotPosition;
    }

    public int getTeam() {
        return Integer.parseInt(this.scoutData[5]);
    }

    @NonNull
    @Override
    public String toString() {
        return(this.getScouter() + ", " + this.getEvent() + ", " + this.getMatch() + ", " + this.getRobotPosition() + ", " + this.getTeam());
    }

    public String getExportable() {
        StringBuilder expBuild = new StringBuilder();
        for(int i = 0; i<scoutData.length; i++) {
            String quoted = "\"" + scoutData[i] +"\"";
            expBuild.append(quoted);
            if(i != scoutData.length-1) {
                expBuild.append(",");
            } else {
                expBuild.append("\n");
            }
        }
        return expBuild.toString();
    }
}

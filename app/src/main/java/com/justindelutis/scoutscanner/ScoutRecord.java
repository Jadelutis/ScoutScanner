package com.justindelutis.scoutscanner;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.HashMap;

public class ScoutRecord implements Serializable {
    private static final String[] categoryNames = {"Scouter", "Event", "Level", "Match", "Robot", "Team #", "Auto Position", "Taxi", "Auto Upper", "Auto Lower", "Auto Acquired",
                                                    "Teleop Upper", "Teleop Lower", "Defended", "Cargo Intake", "Shooting Spot", "Climb", "Climb Before End", "Num Climbed",
                                                    "Driver Skill", "Defense Rate", "Spicy", "Died", "Foul", "Partner", "Comments", "Confidence"};
    private static final String[] barcodeKeys = {"s", "e", "l", "m", "r", "t", "as", "at", "au", "al", "ac", "tu", "tl", "wd", "cif", "ss", "c", "be", "cn", "ds", "dr", "ba", "d", "cf",
                                                    "all", "co", "cnf"};
    private static final String TAG = "ScoutRecord";
    private String[] scoutData;
    private final String originalScan;
    private HashMap<String, String> scoutHashData;

    public ScoutRecord(String scoutData) {
        this.originalScan = scoutData;
        this.scoutHashData = new HashMap<String, String>();
        for(String i:barcodeKeys) {
            scoutHashData.put(i, "");
        }
        parseData();
    }

    public void parseData() {
//        String[] semiSplit = this.originalScan.split(";");
//        this.scoutData = new String[semiSplit.length];
//        for(int i = 0; i<semiSplit.length; i++) {
//            if(semiSplit[i].indexOf("=") != semiSplit[i].length()-1) {
//                this.scoutData[i] = semiSplit[i].split("=")[1];
//            } else {
//                this.scoutData[i] = "";
//            }
//        }

        String[] semiSplit = this.originalScan.split(";");
        for(int i = 0; i<semiSplit.length; i++) {
            if(semiSplit[i].indexOf("=") != semiSplit[i].length()-1) {
                String[] splitEquals = semiSplit[i].split("=");
                scoutHashData.put(splitEquals[0], splitEquals[1]);
            }
        }


    }

    public String getOriginalScan() {
        return this.originalScan;
    }

    public String getScouter() {
        return this.scoutHashData.get(barcodeKeys[0]);
    }

    public String getEvent() {
        return this.scoutHashData.get(barcodeKeys[1]);
    }

    public int getMatch() {
        Log.d(TAG, "getMatch: Match: " + this.scoutHashData.get(barcodeKeys[3]));
        return Integer.parseInt(this.scoutHashData.get(barcodeKeys[3]));
    }

    public String getRobotPosition() {
        char[] charPosi = this.scoutHashData.get(barcodeKeys[4]).toCharArray();
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
        return Integer.parseInt(this.scoutHashData.get(barcodeKeys[5]));
    }

    @NonNull
    @Override
    public String toString() {
        return(this.getScouter() + ", " + this.getEvent() + ", " + this.getMatch() + ", " + this.getRobotPosition() + ", " + this.getTeam());
    }

    public String getExportable() {
        StringBuilder expBuild = new StringBuilder();
        for(int i = 0; i<barcodeKeys.length; i++) {
            String quoted = "\"" + scoutHashData.get(barcodeKeys[i]) +"\"";
            expBuild.append(quoted);
            if(i != barcodeKeys.length-1) {
                expBuild.append(",");
            } else {
                expBuild.append("\n");
            }
        }
        return expBuild.toString();
    }
}

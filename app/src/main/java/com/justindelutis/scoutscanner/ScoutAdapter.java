package com.justindelutis.scoutscanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ScoutAdapter extends ArrayAdapter {
    private static final String TAG = "ScoutAdapter";
    private final int layoutResource;
    private final LayoutInflater layoutInflater;
    private ArrayList<ScoutRecord> scoutRecords;

    public ScoutAdapter(Context context, int resource, ArrayList<ScoutRecord> scoutRecords) {
        super(context, resource);
        this.layoutResource = resource;
        this.layoutInflater = LayoutInflater.from(context);
        this.scoutRecords = scoutRecords;
    }

    @Override
    public int getCount() {
        return scoutRecords.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            convertView = layoutInflater.inflate(this.layoutResource, parent, false);

            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ScoutRecord currRecord = scoutRecords.get(position);
        viewHolder.teamNum.setText(String.format("%d", currRecord.getTeam()));
        String infoString = String.format("Scouter: %s, Match: %d, Position: %s", currRecord.getScouter(), currRecord.getMatch(), currRecord.getRobotPosition());
        viewHolder.recordInfo.setText(infoString);

        return convertView;
    }

    private class ViewHolder {
        final TextView teamNum;
        final TextView recordInfo;

        ViewHolder(View v) {
            this.teamNum = v.findViewById(R.id.teamNum);
            this.recordInfo = v.findViewById(R.id.recordInfo);
        }
    }
}

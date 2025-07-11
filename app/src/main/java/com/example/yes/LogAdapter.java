package com.example.yes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class LogAdapter extends ArrayAdapter<LogEntry> {
    private Context context;
    private List<LogEntry> logList;

    public LogAdapter(Context context, List<LogEntry> logList) {
        // Pass context, layout, and list to the ArrayAdapter constructor
        super(context, android.R.layout.simple_list_item_2, logList);
        this.context = context;
        this.logList = logList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        LogEntry logEntry = logList.get(position);

        TextView messageTextView = convertView.findViewById(android.R.id.text1);
        TextView timestampTextView = convertView.findViewById(android.R.id.text2);

        messageTextView.setText(logEntry.getMessage());
        timestampTextView.setText(logEntry.getTimestamp());

        return convertView;
    }

}

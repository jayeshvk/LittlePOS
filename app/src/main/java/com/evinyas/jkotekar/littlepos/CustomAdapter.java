package com.evinyas.jkotekar.littlepos;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class CustomAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;
    private LayoutInflater inflater = null;
    private int[] colors = new int[]{Color.parseColor("#F0F0F0"), Color.parseColor("#D2E4FC")};

    public CustomAdapter(Context context, String[] values) {
        super(context, R.layout.report_date_view_item, values);
        this.context = context;
        this.values = values;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {

            View rowView = inflater.inflate(R.layout.report_date_view_item, parent, false);
            int colorPos = position % colors.length;
            rowView.setBackgroundColor(colors[colorPos]);
        }
        return super.getView(position, convertView, parent);
    }
}

package com.evinyas.jkotekar.littlepos;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private OnDataPass dataPasser;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = null;

        if (context instanceof Activity){
            activity=(Activity) context;
        }
        try {
            dataPasser = (OnDataPass) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DATE);
        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        String date = fmt.format(calendar.getTime());
        boolean stat;
        if (getArguments() != null)
            stat = getArguments().getBoolean("stat");
        else stat = false;
        dataPasser.onDateReceive(date, stat);
    }

    public interface OnDataPass {
        void onDateReceive(String date, Boolean stat);
    }


}
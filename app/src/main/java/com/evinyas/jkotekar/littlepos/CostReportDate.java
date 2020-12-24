package com.evinyas.jkotekar.littlepos;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.evinyas.jkotekar.littlepos.model.UHelper;
import com.evinyas.jkotekar.littlepos.model.salesData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class CostReportDate extends AppCompatActivity implements DatePickerFragment.OnDataPass {

    private SimpleAdapter adapter;
    private List<salesData> CustomListViewCostData = new ArrayList<salesData>();
    private DatabaseHelper databaseHelper;
    private TextView fromDate;
    private TextView toDate;
    private boolean fromTouched;
    private boolean toTouched;
    private ArrayList<HashMap<String, String>> feedList;
    private final String DATE = "date";
    private final String TOTAL = "total";
    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cost_report_date);

        feedList = new ArrayList<>();
        databaseHelper = DatabaseHelper.getInstance(getApplication());

        ListView list = (ListView) findViewById(R.id.reportsDateList);
        adapter = new SimpleAdapter(getApplicationContext(), feedList, R.layout.cost_report_date_view_item, new String[]{DATE, TOTAL}, new int[]{R.id.textViewDate, R.id.textViewTotal});

        list.setAdapter(adapter);


        fromDate = (TextView) findViewById(R.id.editText_rl_from);
        fromDate.setText(UHelper.setPresentDateddMMyyyy());
        fromDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fromTouched = true;
                toTouched = false;
                showdatePicker();
                return false;
            }
        });
        toDate = (TextView) findViewById(R.id.editText_rl_to);
        toDate.setText(UHelper.setPresentDateddMMyyyy());
        toDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fromTouched = false;
                toTouched = true;
                showdatePicker();
                return false;
            }
        });
        refreshList();
    }

    private void refreshList() {
        feedList.clear();
        double sumtotals = 0;
        List<String> date = databaseHelper.getCostDate(UHelper.dateFormatdmyTOymd(fromDate.getText().toString()), UHelper.dateFormatdmyTOymd(toDate.getText().toString()));
        System.out.println(date);
        if (date.size() > 0) {
            for (int i = 0; i < date.size(); i++) {
                List<String> data = databaseHelper.getCostSumbyDate(date.get(i), date.get(i));
                HashMap<String, String> temp = new HashMap<>();
                temp.put(DATE, UHelper.dateFormatymdTOdmy(date.get(i)));
                temp.put(TOTAL, data.get(0));
                feedList.add(temp);
                sumtotals = sumtotals + UHelper.parseDouble(data.get(0));
            }
        }

        TextView sumTotalText = (TextView) findViewById(R.id.rdrTotal);
        sumTotalText.setText(String.format("Sum Totals %.2f", sumtotals));
        adapter.notifyDataSetChanged();
    }

    private void refreshLista() {
        feedList.clear();
        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        long diff = 0;
        long days = 0;
        try {
            Date fromdate = myFormat.parse(fromDate.getText().toString());
            Date todate = myFormat.parse(toDate.getText().toString());
            diff = todate.getTime() - fromdate.getTime();
            days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            System.out.println("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

        for (int i = 0; i <= days; i++) {
            try {
                cal.setTime(dateFormat.parse(fromDate.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cal.add(Calendar.DATE, i);

            String date = UHelper.dateFormatdmyTOymd(myFormat.format(cal.getTime()) + "");
            System.out.println(myFormat.format(cal.getTime()) + " Format " + cal.getTime() + "****" + date);

            List<String> data = databaseHelper.getCostSumbyDate(date, date);
            if (data.get(0) == null)
                continue;
            HashMap<String, String> temp = new HashMap<>();
            temp.put(DATE, myFormat.format(cal.getTime()));
            temp.put(TOTAL, data.get(0));
            feedList.add(temp);
        }

        adapter.notifyDataSetChanged();
/*        CustomListViewCostData = databaseHelper.getSalesReportbyDate(cusID, UHelper.dateFormatdmyTOymd(fromDate.getText().toString()), UHelper.dateFormatdmyTOymd(toDate.getText().toString()));
        if (CustomListViewCostData != null) {
            //rep();

        }*/
    }

    public void showdatePicker() {
        flag = true;
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onDateReceive(String date, Boolean stat) {
        if (flag) {
            if (fromTouched) {
                fromDate.setText(date);
                if (toDate != null)
                    refreshList();
                System.out.println("In From");
            }
            if (toTouched) {
                toDate.setText(date);
                if (fromDate != null) {
                    refreshList();
                }
                System.out.println("In To");

            }
            flag = false;
        }
    }
}

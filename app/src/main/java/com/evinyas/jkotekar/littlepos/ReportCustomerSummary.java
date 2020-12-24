package com.evinyas.jkotekar.littlepos;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.evinyas.jkotekar.littlepos.model.CustomerProduct;
import com.evinyas.jkotekar.littlepos.model.UHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ReportCustomerSummary extends AppCompatActivity implements DatePickerFragment.OnDataPass {

    private SimpleAdapter adapter;
    private List<CustomerProduct> CPList = null;
    private DatabaseHelper databaseHelper;
    private ArrayList<HashMap<String, String>> feedList, tempList;
    private final String NAME = "name";
    private final String TOTAL = "total";
    private final String PAYMENTTOTAL = "payment";
    private final String DUE = "due";

    private TextView sales, paid, due, fromDate, toDate;
    private boolean fromTouched;
    private boolean toTouched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reportcustomersummary);

        sales = (TextView) findViewById(R.id.rcsSales);
        paid = (TextView) findViewById(R.id.rcsPaid);
        due = (TextView) findViewById(R.id.rcsDue);

        feedList = new ArrayList<>();
        tempList = new ArrayList<>();
        databaseHelper = DatabaseHelper.getInstance(getApplication());
        CPList = databaseHelper.getAllcustomerProductName(0);

        fillFeedList();

        List<String> data = databaseHelper.getSalesSumbyCustomer(0, "", "");
        sales.setText(UHelper.stringDouble(data.get(0)));
        paid.setText(UHelper.stringDouble(data.get(1)));
        due.setText(UHelper.stringDouble(UHelper.parseDouble(data.get(0)) - UHelper.parseDouble(data.get(1)) + ""));

        ListView list = (ListView) findViewById(R.id.reportsCutomerSummaryList);
        adapter = new SimpleAdapter(getApplicationContext(), feedList, R.layout.reportcustomersummaryitem,
                new String[]{NAME, TOTAL, PAYMENTTOTAL, DUE},
                new int[]{R.id.rcsiCustomer, R.id.rcsiSales, R.id.rcsiPaid, R.id.rcsiDue});

        list.setAdapter(adapter);

        fromDate = (TextView) findViewById(R.id.fromDate);
        fromDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fromTouched = true;
                toTouched = false;
                showdatePicker();
                return false;
            }
        });
        toDate = (TextView) findViewById(R.id.toDate);
        toDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fromTouched = false;
                toTouched = true;
                showdatePicker();
                return false;
            }
        });
    }


    private void fillFeedList() {
        int previousID = -1;
        for (CustomerProduct customer : CPList) {

            double d;

            if (customer.getCustomerID() != previousID) {
                List<String> salesDt = databaseHelper.getSalesSumbyCustomer(customer.getCustomerID(), "", "");
                d = UHelper.parseDouble(salesDt.get(0)) - UHelper.parseDouble(salesDt.get(1));

                HashMap<String, String> data = new HashMap<>();
                data.put(NAME, customer.getCustomerName());
                data.put(TOTAL, UHelper.stringDouble(salesDt.get(0)));
                data.put(PAYMENTTOTAL, UHelper.stringDouble(salesDt.get(1)));
                data.put(DUE, UHelper.stringDouble(d + ""));
                feedList.add(data);
                tempList.add(data);

            }
            previousID = customer.getCustomerID();
        }

    }

    @Override
    public void onDateReceive(String date, Boolean stat) {
        if (fromTouched) {
            fromDate.setText(date);
            if (toDate != null)
                refreshList();
        }
        if (toTouched) {
            toDate.setText(date);
            if (fromDate != null) {
                refreshList();
            }
        }
    }

    private void refreshList() {
        feedList.clear();
        int previousID = -1;
        double total = 0, payment = 0, dueAmount = 0;
        ;
        for (CustomerProduct customer : CPList) {
            if (customer.getCustomerID() != previousID) {
                List<String> salesDt = databaseHelper.getSalesSumbyCustomer(customer.getCustomerID(), UHelper.dateFormatdmyTOymd(fromDate.getText().toString()), UHelper.dateFormatdmyTOymd(toDate.getText().toString()));
                if (salesDt.get(0) == null && salesDt.get(1) == null)
                    continue;
                total = total + UHelper.parseDouble(salesDt.get(0));
                payment = payment + UHelper.parseDouble(salesDt.get(1));
                HashMap<String, String> data = new HashMap<>();
                data.put(NAME, customer.getCustomerName());
                data.put(TOTAL, UHelper.stringDouble(salesDt.get(0)));
                data.put(PAYMENTTOTAL, UHelper.stringDouble(salesDt.get(1)));
                for (int i = 0; i < tempList.size(); i++) {
                    if (tempList.get(i).get(NAME).contains(customer.getCustomerName())) {
                        data.put(DUE, UHelper.stringDouble(tempList.get(i).get(DUE)));
                        dueAmount = dueAmount + UHelper.parseDouble(tempList.get(i).get(DUE));
                    }
                }
                feedList.add(data);
            }
            previousID = customer.getCustomerID();
        }
        adapter.notifyDataSetChanged();
        sales.setText(UHelper.stringDouble(total + ""));
        paid.setText(UHelper.stringDouble(payment + ""));
        due.setText(UHelper.stringDouble(dueAmount + ""));

    }


    public void showdatePicker() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
}

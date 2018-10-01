package com.evinyas.jkotekar.littlepos;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.evinyas.jkotekar.littlepos.model.CustomerProduct;
import com.evinyas.jkotekar.littlepos.model.SpinnerItemID;
import com.evinyas.jkotekar.littlepos.model.UHelper;
import com.evinyas.jkotekar.littlepos.model.salesData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ReportDate extends AppCompatActivity implements AdapterView.OnItemSelectedListener, DatePickerFragment.OnDataPass {

    private SimpleAdapter adapter;
    private List<salesData> CustomListViewSalesData = new ArrayList<salesData>();
    private List<CustomerProduct> CPList = null;
    private DatabaseHelper databaseHelper;
    private TextView fromDate;
    private TextView toDate;
    private boolean fromTouched;
    private boolean toTouched;
    private int cusID;
    private ArrayList<HashMap<String, String>> feedList;
    private final String DATE = "date";
    private final String TOTAL = "total";
    private final String PAYMENTTOTAL = "payment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_date);

        feedList = new ArrayList<>();
        databaseHelper = DatabaseHelper.getInstance(getApplication());
        CPList = databaseHelper.getAllcustomerProductName(0);

        loadCustomer();

        ListView list = (ListView) findViewById(R.id.reportsDateList);
        adapter = new SimpleAdapter(getApplicationContext(), feedList, R.layout.report_date_view_item, new String[]{DATE, TOTAL, PAYMENTTOTAL}, new int[]{R.id.textViewDate, R.id.textViewTotal, R.id.textViewPayments});
        list.setAdapter(adapter);
        list.setClickable(true);

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
    }

    private void loadCustomer() {
        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.rl_spinner);

        spinner.setOnItemSelectedListener(this);
        ArrayList<SpinnerItemID> spinnerItemIDs = new ArrayList<>();
        // Spinner Drop down elements

        int previousID = -1;
        spinnerItemIDs.add(new SpinnerItemID(0, "All"));
        for (CustomerProduct c : CPList) {
            if (c.getCustomerID() != previousID)
                spinnerItemIDs.add(new SpinnerItemID(c.getCustomerID(), c.getCustomerName()));
            previousID = c.getCustomerID();
        }
        // Creating adapter for spinner
        ArrayAdapter<SpinnerItemID> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerItemIDs);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        SpinnerItemID itemID = (SpinnerItemID) parent.getSelectedItem();
        cusID = itemID.getId();
        refreshList();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void refreshList() {
        CustomListViewSalesData = databaseHelper.getSalesReportbyDate(cusID, UHelper.dateFormatdmyTOymd(fromDate.getText().toString()), UHelper.dateFormatdmyTOymd(toDate.getText().toString()));
        if (CustomListViewSalesData != null) {
            rep();
            adapter.notifyDataSetChanged();
        }
    }

    private void rep() {
        feedList.clear();
        double total = 0;
        double payment = 0;

        String tempDate = "";
        if (CustomListViewSalesData != null && CustomListViewSalesData.size() > 0)
            tempDate = UHelper.dateFormatymdhmsTOddmyyyy(CustomListViewSalesData.get(0).getDate());
        for (salesData s : CustomListViewSalesData) {
            String d = UHelper.dateFormatymdhmsTOddmyyyy(s.getDate());
            if (d.contains(tempDate)) {
                total = total + UHelper.parseDouble(s.getAmount());
                payment = payment + UHelper.parseDouble(s.getReceived());
            } else {
                HashMap<String, String> data = new HashMap<>();
                data.put(DATE, tempDate);
                data.put(TOTAL, String.format("%.2f", total));
                data.put(PAYMENTTOTAL, String.format("%.2f", payment));
                feedList.add(data);
                total = UHelper.parseDouble(s.getAmount());
                payment = UHelper.parseDouble(s.getReceived());
            }
            tempDate = UHelper.dateFormatymdhmsTOddmyyyy(s.getDate());
        }
        HashMap<String, String> data = new HashMap<>();
        data.put(DATE, tempDate);
        data.put(TOTAL, String.format("%.2f", total));
        data.put(PAYMENTTOTAL, String.format("%.2f", payment));

        feedList.add(data);

        double sumTotal = 0;
        double sumPayment = 0;
        for (int i = 0; i < feedList.size(); i++) {
            sumTotal = sumTotal + UHelper.parseDouble(feedList.get(i).get(TOTAL));
            sumPayment = sumPayment + UHelper.parseDouble(feedList.get(i).get(PAYMENTTOTAL));
        }
        TextView sumTotalText = (TextView) findViewById(R.id.rdrTotal);
        sumTotalText.setText(String.format("Sum Totals %.2f", sumTotal));
        TextView sumPaymentsText = (TextView) findViewById(R.id.rdrReceived);
        sumPaymentsText.setText(String.format("Sum Payments %.2f", sumPayment));
    }

    public void showdatePicker() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
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
}

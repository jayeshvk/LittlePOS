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


public class ItemTotal extends AppCompatActivity implements AdapterView.OnItemSelectedListener, DatePickerFragment.OnDataPass {

    private SimpleAdapter adapter;
    private List<salesData> CustomListViewSalesData = new ArrayList<salesData>();
    private List<CustomerProduct> CPList = null;
    private DatabaseHelper databaseHelper;
    private TextView fromDate;
    private TextView toDate;
    private TextView totalAmount;
    private boolean fromTouched;
    private boolean toTouched;
    private int cusID;
    private ArrayList<HashMap<String, String>> feedList;
    private final String PRODUCTNAME = "productname";
    private final String SUMQTY = "sumqty";
    private final String AMOUNT = "amount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_item_total);

        feedList = new ArrayList<>();
        databaseHelper = DatabaseHelper.getInstance(getApplication());
        CPList = databaseHelper.getAllcustomerProductName(0);

        loadCustomer();

        ListView list = (ListView) findViewById(R.id.reportsItemTotaList);
        adapter = new SimpleAdapter(getApplicationContext(), feedList, R.layout.report_item_total_listitem, new String[]{PRODUCTNAME, SUMQTY, AMOUNT}, new int[]{R.id.tvProductName, R.id.tvSumQty, R.id.tvAmount});
        list.setAdapter(adapter);
        list.setClickable(true);

        totalAmount = (TextView) findViewById(R.id.tvTotalAmount);

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
        List<String> items = databaseHelper.getItemTotalItems(cusID);
        if (items != null) {
            rep(items);
            adapter.notifyDataSetChanged();
        }
        CustomListViewSalesData = databaseHelper.getSalesReportbyDate(cusID, UHelper.dateFormatdmyTOymd(fromDate.getText().toString()), UHelper.dateFormatdmyTOymd(toDate.getText().toString()));

    }

    private void rep(List<String> items) {
        feedList.clear();
        double totalamount = 0;
        for (int i = 0; i < items.size(); i++) {
            int pid = UHelper.parseInt(items.get(i));
            List<String> data = databaseHelper.getItemTotal(cusID, pid, UHelper.dateFormatdmyTOymd(fromDate.getText().toString()), UHelper.dateFormatdmyTOymd(toDate.getText().toString()));
            System.out.println("Product ID " + data);

            HashMap<String, String> list = new HashMap<>();
            list.put(PRODUCTNAME, data.get(0));
            list.put(SUMQTY, data.get(1));
            list.put(AMOUNT, data.get(2));
            totalamount = totalamount + UHelper.parseDouble(data.get(2));

            feedList.add(list);
        }
        totalAmount.setText(String.format("Sum Totals %.2f", totalamount));


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

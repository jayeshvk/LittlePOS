package com.evinyas.jkotekar.littlepos;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.evinyas.jkotekar.littlepos.model.CustomAdapter;
import com.evinyas.jkotekar.littlepos.model.CustomerProduct;
import com.evinyas.jkotekar.littlepos.model.SpinnerItemID;
import com.evinyas.jkotekar.littlepos.model.UHelper;
import com.evinyas.jkotekar.littlepos.model.salesData;

import java.util.ArrayList;
import java.util.List;


public class ReportItem extends AppCompatActivity implements AdapterView.OnItemSelectedListener, DatePickerFragment.OnDataPass {

    private CustomAdapter adapter;
    private List<salesData> CustomListViewValuesArr = new ArrayList<salesData>();
    private List<CustomerProduct> CPList = null;
    private DatabaseHelper databaseHelper;
    //private String title = "Tap to Select Customer";
    private TextView fromDate;
    private TextView toDate;
    private boolean fromTouched;
    private boolean toTouched;
    private int cusID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_item);
        ReportItem customListView = this;

        databaseHelper = DatabaseHelper.getInstance(getApplication());
        CPList = databaseHelper.getAllcustomerProductName(0);
        loadCustomer();

        Resources res = getResources();
        ListView list = (ListView) findViewById(R.id.listViewr);

        /**************** Create Custom Adapter *********/
        adapter = new CustomAdapter(customListView, CustomListViewValuesArr, res);
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
    }

    /*****************
     * This function used by adapter
     ****************/
    public void onItemClick(int mPosition) {
        salesData tempValues;
        if (CustomListViewValuesArr.size() != 0) {
            tempValues = CustomListViewValuesArr.get(mPosition);
            if (tempValues.getComments().length() > 0)
                Toast.makeText(this, tempValues.getComments(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadCustomer() {
        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.rl_spinner);

        spinner.setOnItemSelectedListener(this);
        ArrayList<SpinnerItemID> spinnerItemIDs = new ArrayList<>();
        // Spinner Drop down elements

        int previousID = -1;
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

    public void showdatePicker() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
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
        CustomListViewValuesArr = databaseHelper.getSalesReportbyDate(cusID, UHelper.dateFormatdmyTOymd(fromDate.getText().toString()), UHelper.dateFormatdmyTOymd(toDate.getText().toString()));
        List<String> data = databaseHelper.getSalesSumbyCustomer(cusID, UHelper.dateFormatdmyTOymd(fromDate.getText().toString()), UHelper.dateFormatdmyTOymd(toDate.getText().toString()));
        if (data != null) {
            TextView sales = (TextView) findViewById(R.id.rdrTotal);
            sales.setText(String.format("Sales %s", UHelper.parseDouble(data.get(0))));
            TextView payments = (TextView) findViewById(R.id.rdrReceived);
            payments.setText(String.format("Payments %s", UHelper.parseDouble(data.get(1))));
            TextView balance = (TextView) findViewById(R.id.rdrBalance);
            balance.setText(String.format("Due %s", UHelper.stringDouble(UHelper.parseDouble(data.get(0)) - UHelper.parseDouble(data.get(1)) + "")));
        }
        adapter.updateReceiptsList(CustomListViewValuesArr);
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

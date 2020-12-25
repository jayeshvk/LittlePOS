package com.evinyas.jkotekar.littlepos;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.evinyas.jkotekar.littlepos.model.UHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class Reports extends AppCompatActivity {

    final String SALES = "Sales";
    final String PAYMENTTOTAL = "Payments";
    final String DUE = "Dues";
    final String YEARS = "Year";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reports);
    }

    public void reportItem(View view) {
        startActivity(new Intent(this, ReportItem.class));
    }

    public void reportDate(View view) {
        startActivity(new Intent(this, ReportDate.class));
    }

    public void totalBusiness(View view) {
        DatabaseHelper databaseHelper;
        databaseHelper = DatabaseHelper.getInstance(getApplication());
        ArrayList<HashMap<String, String>> feedList = new ArrayList<>();

        List<String> data = databaseHelper.getSalesSumbyCustomer(0, "", "");
        List<String> cdata = databaseHelper.getCostSumbyDate(null, null);

        String first = databaseHelper.getFirstEntry().substring(0, 4);
        int toYear = Calendar.getInstance().get(Calendar.YEAR);
        int fromYear = Integer.parseInt(first);
        System.out.println(fromYear + "*" + toYear);

        for (int i = fromYear; i <= toYear; i++) {
            HashMap<String, String> tmp = new HashMap<>();
            List<String> salesByYear = databaseHelper.getSalesByYear(i + "");
            if (salesByYear.get(0) != null || salesByYear.get(1) != null) {
                System.out.println(i + "=" + "Sales " + salesByYear.get(0) + "Received" + salesByYear.get(1) + "\n");
                tmp.put(YEARS, i + "");
                tmp.put(SALES, salesByYear.get(0));
                tmp.put(PAYMENTTOTAL, salesByYear.get(1));
                tmp.put(DUE, (Double.parseDouble(salesByYear.get(0)) - Double.parseDouble(salesByYear.get(1)) + ""));
                feedList.add(tmp);
            }

        }
        if (data.size() > 0) {
            showSales(cdata.get(0), feedList);
        }

    }

    public void customerSalesSummary(View view) {
        startActivity(new Intent(this, ReportCustomerSummary.class));
    }

    public void costReportDate(View view) {
        startActivity(new Intent(this, CostReportDate.class));
    }

    public void itemTotal(View view) {
        startActivity(new Intent(this, ItemTotal.class));
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    //sub methods
    protected void showSales(String cdata, ArrayList<HashMap<String, String>> busdata) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.totalsalespopup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        ListView list = promptView.findViewById(R.id.totalBusinessList);
        SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), busdata, R.layout.totalbusinesslistitem,
                new String[]{YEARS, SALES, PAYMENTTOTAL, DUE},
                new int[]{R.id.rcsiYear, R.id.rcsiSales, R.id.rcsiPaid, R.id.rcsiDue});
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        TextView totalCost = (TextView) promptView.findViewById(R.id.popupTotalCost);
        totalCost.setText(UHelper.stringDouble(cdata));
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
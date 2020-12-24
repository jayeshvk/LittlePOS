package com.evinyas.jkotekar.littlepos;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.evinyas.jkotekar.littlepos.model.UHelper;


import java.util.List;

public class Reports extends AppCompatActivity {



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
        List<String> data = databaseHelper.getSalesSumbyCustomer(0, "", "");
        List<String> cdata = databaseHelper.getCostSumbyDate(null, null);

        if (data.size() > 0) {
            System.out.println("Data " + data.get(0) + " bata " + data.get(1));
            showSales(data, cdata.get(0));
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
    protected void showSales(List<String> data, String cdata) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.totalsalespopup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        TextView totalSales = (TextView) promptView.findViewById(R.id.popupTotalSales);
        TextView totalCost = (TextView) promptView.findViewById(R.id.popupTotalCost);
        TextView totalProfit = (TextView) promptView.findViewById(R.id.popupTotalProfit);
        TextView totalPayments = (TextView) promptView.findViewById(R.id.popupTotalPayments);
        TextView totalDues = (TextView) promptView.findViewById(R.id.popupTotalDues);

        totalSales.setText(UHelper.stringDouble(data.get(0)));
        totalCost.setText(UHelper.stringDouble(cdata));
        totalProfit.setText(UHelper.stringDouble(UHelper.parseDouble(data.get(0)) - UHelper.parseDouble(cdata) + ""));
        totalPayments.setText(UHelper.stringDouble(data.get(1)));
        totalDues.setText(UHelper.stringDouble(UHelper.parseDouble(data.get(0)) - UHelper.parseDouble(data.get(1)) + ""));

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
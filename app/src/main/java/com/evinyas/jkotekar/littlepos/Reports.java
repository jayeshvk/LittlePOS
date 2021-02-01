package com.evinyas.jkotekar.littlepos;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.evinyas.jkotekar.littlepos.model.UHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Reports extends AppCompatActivity {

    private static final String COST = "Costs";
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


        String first = databaseHelper.getFirstEntry().substring(0, 4);
        int toYear = Calendar.getInstance().get(Calendar.YEAR);
        int fromYear = Integer.parseInt(first);
        for (int i = fromYear; i <= toYear; i++) {
            HashMap<String, String> tmp = new HashMap<>();
            Calendar cal = Calendar.getInstance();
            int m = UHelper.parseInt(readSharedPref("STARTMONTH"));
            cal.set(i - 1, m, 1);
            cal.add(Calendar.MONTH, 11);
            String fy = (i - 1) + "";
            String ty = cal.get(Calendar.YEAR) + "";
            String fm = String.format("%02d", (m + 1));
            String tm = String.format("%02d", cal.get(Calendar.MONTH) + 1);

            List<Double> salesByYear = databaseHelper.getSalesByYear(fy, ty, fm, tm, cal.getActualMaximum(Calendar.DATE) + "", 0);
            List<String> cdata = databaseHelper.getCostSumbyDate(fy + "-" + m + "-" + "01", ty + "-" + tm + "-" + "01");

            String yrs = fy.substring(2, 4) + "-" + ty.substring(2, 4);

            if (salesByYear.get(0) != null || salesByYear.get(1) != null) {

                System.out.println(i + "=" + "Sales " + salesByYear.get(0) + "Received" + salesByYear.get(1) + "\n");
                tmp.put(YEARS, yrs);
                NumberFormat formatter = NumberFormat.getIntegerInstance(new Locale("en", "IN"));
                tmp.put(SALES, formatter.format(salesByYear.get(0)));
                tmp.put(PAYMENTTOTAL, formatter.format(salesByYear.get(1)));
                tmp.put(DUE, formatter.format(salesByYear.get(0) - salesByYear.get(1)));
                if (cdata.get(0) == null)
                    cdata.set(0, "0");
                tmp.put(COST, formatter.format(UHelper.parseDouble(cdata.get(0))));
                feedList.add(tmp);
            }
        }
        if (data.size() > 0) {
            showSales(feedList);
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
    protected void showSales(ArrayList<HashMap<String, String>> busdata) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.totalsalespopup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(promptView);

        ListView list = promptView.findViewById(R.id.totalBusinessList);
        SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), busdata, R.layout.totalbusinesslistitem,
                new String[]{YEARS, SALES, PAYMENTTOTAL, DUE, COST},
                new int[]{R.id.rcsiYear, R.id.rcsiSales, R.id.rcsiPaid, R.id.rcsiDue, R.id.rcsiCost});
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();

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

    private String readSharedPref(String KEY) {
        String returnData = null;
        String SHAREDPREFNAME = "LittlePOSPrefs";
        SharedPreferences settings = getSharedPreferences(SHAREDPREFNAME, 0);
        return settings.getString("STARTMONTH", 0 + "");
    }

}
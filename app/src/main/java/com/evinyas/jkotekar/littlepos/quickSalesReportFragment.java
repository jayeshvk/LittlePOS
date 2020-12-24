package com.evinyas.jkotekar.littlepos;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.evinyas.jkotekar.littlepos.model.CustomAdapter;
import com.evinyas.jkotekar.littlepos.model.UHelper;
import com.evinyas.jkotekar.littlepos.model.salesData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jkotekar on 6/2/2016.
 */
public class quickSalesReportFragment extends DialogFragment {

    private TextView gross;
    private TextView payments;
    private TextView net;
    private DatabaseHelper databaseHelper;
    private TextView fromDate;
    private TextView toDate;
    private TextView totalForTheDay;
    private boolean fromTouched;
    private boolean toTouched;
    private CustomAdapter adapter;
    private List<salesData> CustomListViewValuesArr = new ArrayList<>();
    private int cusID;
    private ImageView salesPopUp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.quick_sales_report_fragment, container, false);
        fromDate = (TextView) view.findViewById(R.id.textView_from_POSR);
        fromDate.setText(UHelper.setPresentDateddMMyyyy());
        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromTouched = true;
                toTouched = false;
                showdatePicker();
            }
        });

        toDate = (TextView) view.findViewById(R.id.textView_to_POSR);
        toDate.setText(UHelper.setPresentDateddMMyyyy());
        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromTouched = false;
                toTouched = true;
                showdatePicker();
            }
        });

        String customer = getArguments().getString("customer");

        databaseHelper = DatabaseHelper.getInstance(getActivity());

        salesPopUp = (ImageView) view.findViewById(R.id.salesPopUpButton);
        salesPopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salesPopUp();
            }
        });

        cusID = getArguments().getInt("cusid");
        TextView cusName = (TextView) view.findViewById(R.id.salesreport_customername);
        cusName.setText(String.format("Sales for %s", customer));

        ListView list = (ListView) view.findViewById(R.id.listViewPOSR);
        Resources res = getResources();
        adapter = new CustomAdapter(getActivity(), CustomListViewValuesArr, res, this);
        list.setAdapter(adapter);
        gross = (TextView) view.findViewById(R.id.salesReport_gross);
        payments = (TextView) view.findViewById(R.id.salesReport_payments);
        net = (TextView) view.findViewById(R.id.salesReport_net);
        totalForTheDay = (TextView) view.findViewById(R.id.totalForDay);
        getGrandTotal();
        refreshList();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.MY_DIALOG);
    }

    private void getGrandTotal() {
        List<String> data = databaseHelper.getSalesSumbyCustomer(cusID,
                UHelper.dateFormatdmyTOymd(fromDate.getText().toString()),
                UHelper.dateFormatdmyTOymd(toDate.getText().toString()));
        gross.setText(String.format("%s", UHelper.parseDouble(data.get(0))));
        payments.setText(String.format("%s", UHelper.parseDouble(data.get(1))));
        net.setText(String.format("%s", UHelper.parseDouble(data.get(0)) - UHelper.parseDouble(data.get(1))));

        List<String> sumDay = databaseHelper.getSalessumfortheDay(cusID, UHelper.setPresentDateyyyyMMddCP());
        totalForTheDay.setText(String.format("Σ %s, Received %s for Today", UHelper.parseDouble(sumDay.get(0)), UHelper.parseDouble(sumDay.get(1))));
    }

    public void showdatePicker() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void pass(String data) {
        if (fromTouched) {
            fromDate.setText(data);
            if (toDate != null)
                refreshList();
        }
        if (toTouched) {
            toDate.setText(data);
            if (fromDate != null) {
                refreshList();
            }
        }
    }

    public void refreshList() {
        CustomListViewValuesArr = databaseHelper.getSalesReportbyDate(cusID, UHelper.dateFormatdmyTOymd(fromDate.getText().toString()), UHelper.dateFormatdmyTOymd(toDate.getText().toString()));
        adapter.updateReceiptsList(CustomListViewValuesArr);
        getGrandTotal();
    }

    /*****************
     * This function used by adapter
     ****************/
    public void onItemClick(int mPosition) {
        salesData tempSalesData;
        if (CustomListViewValuesArr.size() != 0) {
            tempSalesData = CustomListViewValuesArr.get(mPosition);
            if (tempSalesData.getComments().length() != 0)
                Toast.makeText(getActivity(), tempSalesData.getComments(), Toast.LENGTH_SHORT).show();
        }
    }

    public void OnItemLongClicked(int mPosition) {
        salesData tempSalesData;
        if (CustomListViewValuesArr.size() != 0) {
            tempSalesData = CustomListViewValuesArr.get(mPosition);
            showInputDialog(tempSalesData);
        }
    }

    protected void showInputDialog(final salesData temp) {
        LayoutInflater layoutInflater = LayoutInflater.from(quickSalesReportFragment.this.getActivity());
        View promptView = layoutInflater.inflate(R.layout.prompts, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                quickSalesReportFragment.this.getActivity());
        alertDialogBuilder.setView(promptView);

        final EditText editTextQty = (EditText) promptView.findViewById(R.id.editTextDialogUserInput);
        if (temp.getPrice() == null) editTextQty.setEnabled(false);
        editTextQty.setText(temp.getQuantity());
        final EditText editTextRcvd = (EditText) promptView.findViewById(R.id.inputReceived);
        editTextRcvd.setText(temp.getReceived());
        final EditText editTextComments = (EditText) promptView.findViewById(R.id.inputComments);
        editTextComments.setText(temp.getComments());

        alertDialogBuilder
                .setCancelable(false)
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRow(temp.getSlno());
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String quantity = editTextQty.getText().toString();
                        String received = editTextRcvd.getText().toString();
                        String comments = editTextComments.getText().toString();
                        if (received.length() == 0)
                            received = "0";
                        if (quantity.length() == 0)
                            quantity = "0";
                        if (quantity.length() != 0 || received.length() != 0) {
                            updateDBwithQuantity(quantity, temp.getPrice(), received, comments, temp.getSlno());
                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void updateDBwithQuantity(String quantity, String price, String received, String comments, String slno) {
        Double amt = UHelper.parseDouble(quantity) * UHelper.parseDouble(price);
        String[] values = {quantity, String.valueOf(amt), received, comments, slno};
        databaseHelper.updateQtyRcvd(values);
        refreshList();
    }

    private void deleteRow(String slno) {
        databaseHelper.salesRow(slno);
        refreshList();
    }

    public void salesPopUp() {
        List<String> data = databaseHelper.getSalesSumbyCustomer(cusID, "", "");

        LayoutInflater layoutInflater = LayoutInflater.from(quickSalesReportFragment.this.getActivity());
        View promptView = layoutInflater.inflate(R.layout.totalsalespopup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(quickSalesReportFragment.this.getActivity());
        alertDialogBuilder.setView(promptView);

        TextView totalSales = (TextView) promptView.findViewById(R.id.popupTotalSales);
        TextView totalPayments = (TextView) promptView.findViewById(R.id.popupTotalPayments);
        TextView totalDues = (TextView) promptView.findViewById(R.id.popupTotalDues);

        totalSales.setText(UHelper.stringDouble(data.get(0)));
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

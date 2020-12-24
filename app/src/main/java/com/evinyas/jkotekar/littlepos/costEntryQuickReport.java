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

import com.evinyas.jkotekar.littlepos.model.CostCustomAdapter;
import com.evinyas.jkotekar.littlepos.model.CostData;
import com.evinyas.jkotekar.littlepos.model.UHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jkotekar on 6/2/2016.
 */
public class costEntryQuickReport extends DialogFragment {

    private TextView gross;
    private TextView payments;
    private TextView net;
    private DatabaseHelper databaseHelper;
    private TextView fromDate;
    private TextView toDate;
    private TextView totalForTheDay;
    private boolean fromTouched;
    private boolean toTouched;
    private CostCustomAdapter adapter;
    private List<CostData> CustomListViewValuesArr = new ArrayList<>();
    private int cusID;
    private ImageView salesPopUp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cost_entry_quick_report, container, false);
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

        databaseHelper = DatabaseHelper.getInstance(getActivity());

        salesPopUp = (ImageView) view.findViewById(R.id.salesPopUpButton);
        salesPopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salesPopUp();
            }
        });

        ListView list = (ListView) view.findViewById(R.id.listViewPOSR);
        Resources res = getResources();
        adapter = new CostCustomAdapter(getActivity(), CustomListViewValuesArr, res, this);
        list.setAdapter(adapter);
        gross = (TextView) view.findViewById(R.id.salesReport_gross);
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
        List<String> data = databaseHelper.getCostSumbyDate(
                UHelper.dateFormatdmyTOymd(fromDate.getText().toString()),
                UHelper.dateFormatdmyTOymd(toDate.getText().toString()));
        gross.setText(String.format("%s", UHelper.parseDouble(data.get(0))));

        List<String> sumDay = databaseHelper.getCostSumbyDate(UHelper.setPresentDateyyyyMMdd(), UHelper.setPresentDateyyyyMMdd());
        totalForTheDay.setText(String.format("Total expenses for today %s", UHelper.parseDouble(sumDay.get(0))));
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
        CustomListViewValuesArr = databaseHelper.getCostReportbyDate(UHelper.dateFormatdmyTOymd(fromDate.getText().toString()), UHelper.dateFormatdmyTOymd(toDate.getText().toString()));
        adapter.updateReceiptsList(CustomListViewValuesArr);
        getGrandTotal();
    }

    /*****************
     * This function used by adapter
     ****************/
    public void onItemClick(int mPosition) {
        CostData tempCostData;
        if (CustomListViewValuesArr.size() != 0) {
            tempCostData = CustomListViewValuesArr.get(mPosition);
            if (tempCostData.getComments().length() != 0)
                Toast.makeText(getActivity(), tempCostData.getComments(), Toast.LENGTH_SHORT).show();
        }
    }

    public void OnItemLongClicked(int mPosition) {
        CostData tempCostData;
        if (CustomListViewValuesArr.size() != 0) {
            tempCostData = CustomListViewValuesArr.get(mPosition);
            showInputDialog(tempCostData);
        }
    }

    protected void showInputDialog(final CostData temp) {
        LayoutInflater layoutInflater = LayoutInflater.from(costEntryQuickReport.this.getActivity());
        View promptView = layoutInflater.inflate(R.layout.prompts, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                costEntryQuickReport.this.getActivity());
        alertDialogBuilder.setView(promptView);

        final TextView paydisc = (TextView) promptView.findViewById(R.id.titlePayDisc);
        paydisc.setVisibility(View.INVISIBLE);
        final EditText editTextRcvd = (EditText) promptView.findViewById(R.id.inputReceived);
        editTextRcvd.setVisibility(View.INVISIBLE);

        final EditText editTextQty = (EditText) promptView.findViewById(R.id.editTextDialogUserInput);
        if (temp.getCost() == null)
            editTextQty.setEnabled(false);
        editTextQty.setText(temp.getQuantity());

        final EditText editTextComments = (EditText) promptView.findViewById(R.id.inputComments);
        editTextComments.setText(temp.getComments());

        alertDialogBuilder
                .setCancelable(false)
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseHelper.deleteCostRow(temp.getSlno());
                        refreshList();
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String quantity = editTextQty.getText().toString();
                        String comments = editTextComments.getText().toString();
                        if (quantity.length() == 0)
                            quantity = "0";
                        else
                            updateDBwithQuantity(quantity, temp.getCost(), comments, temp.getSlno());

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

    private void updateDBwithQuantity(String quantity, String cost, String comments, String slno) {
        Double amt = UHelper.parseDouble(quantity) * UHelper.parseDouble(cost);
        String[] values = {quantity, String.valueOf(amt), comments, slno};
        databaseHelper.updateCostQtyRcvd(values);
        refreshList();
    }

    public void salesPopUp() {
        List<String> data = databaseHelper.getSalesSumbyCustomer(cusID, "", "");
        List<String> cdata = databaseHelper.getCostSumbyDate(null, null);

        LayoutInflater layoutInflater = LayoutInflater.from(costEntryQuickReport.this.getActivity());
        View promptView = layoutInflater.inflate(R.layout.totalsalespopup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(costEntryQuickReport.this.getActivity());
        alertDialogBuilder.setView(promptView);

        TextView totalSales = (TextView) promptView.findViewById(R.id.popupTotalSales);
        TextView totalPayments = (TextView) promptView.findViewById(R.id.popupTotalPayments);
        TextView totalDues = (TextView) promptView.findViewById(R.id.popupTotalDues);
        TextView totalCost = (TextView) promptView.findViewById(R.id.popupTotalCost);
        TextView totalProfit = (TextView) promptView.findViewById(R.id.popupTotalProfit);

        totalSales.setText(UHelper.stringDouble(data.get(0)));
        totalPayments.setText(UHelper.stringDouble(data.get(1)));
        totalDues.setText(UHelper.stringDouble(UHelper.parseDouble(data.get(0)) - UHelper.parseDouble(data.get(1)) + ""));
        totalCost.setText(UHelper.stringDouble(cdata.get(0)));
        totalProfit.setText(UHelper.stringDouble(UHelper.parseDouble(data.get(0)) - UHelper.parseDouble(cdata.get(0)) + ""));

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

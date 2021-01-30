package com.evinyas.jkotekar.littlepos;


import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.cie.btp.CieBluetoothPrinter;
import com.cie.btp.DebugLog;
import com.cie.btp.PrintDensity;
import com.cie.btp.PrinterWidth;
import com.evinyas.jkotekar.littlepos.model.CustomAdapter;
import com.evinyas.jkotekar.littlepos.model.UHelper;
import com.evinyas.jkotekar.littlepos.model.salesData;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_DEVICE_NAME;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_CONNECTED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_CONNECTING;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_LISTEN;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_NONE;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_MESSAGES;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOTIFICATION_ERROR_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOTIFICATION_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOT_CONNECTED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOT_FOUND;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_SAVED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_STATUS;

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

    // to show total busines per customer per year
    final String SALES = "Sales";
    final String PAYMENTTOTAL = "Payments";
    final String DUE = "Dues";
    final String YEARS = "Year";

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public CieBluetoothPrinter mPrinter = CieBluetoothPrinter.INSTANCE;
    private static final int LEFT = 1;
    private static final int RIGHT = -1;
    private static final int CENTER = 0;
    public static final int REQUEST_ENABLE_BT = 9;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.quick_sales_report_activity, container, false);
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

        ImageView salesPopUp = (ImageView) view.findViewById(R.id.salesPopUpButton);
        salesPopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salesPopUp();
            }
        });

        ImageView print = (ImageView) view.findViewById(R.id.printImage);
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CustomListViewValuesArr.size() > 0)
                    connect();
            }
        });

        cusID = getArguments().getInt("cusid");
        TextView cusName = (TextView) view.findViewById(R.id.salesreport_customername);
        cusName.setText(String.format("Sales for %s", customer));

        ListView list = (ListView) view.findViewById(R.id.listViewPOSR);
        Resources res = getResources();
        //adapter = new CustomAdapter(getActivity(), CustomListViewValuesArr, res, this);
        list.setAdapter(adapter);
        gross = (TextView) view.findViewById(R.id.salesReport_gross);
        payments = (TextView) view.findViewById(R.id.salesReport_payments);
        net = (TextView) view.findViewById(R.id.salesReport_net);
        totalForTheDay = (TextView) view.findViewById(R.id.totalForDay);
        getGrandTotal();
        refreshList();
        return view;
    }

    private void connectPrinter() {

        if (!isBluetoothEnabled()) {
            toast("Bluetooth is not switched on");

        } else {
            connect();
        }
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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIPT_PRINTER_MESSAGES);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(ReceiptPrinterMessageReceiver, intentFilter);
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
        ArrayList<HashMap<String, String>> feedList = new ArrayList<>();

        //List<String> data = databaseHelper.getSalesSumbyCustomer(cusID, "", "");
        //start of new fucntionality to get total business per customer per year
        List<String> cdata = databaseHelper.getCostSumbyDate(null, null);

        String first = databaseHelper.getFirstEntry().substring(0, 4);
        int toYear = Calendar.getInstance().get(Calendar.YEAR);
        int fromYear = Integer.parseInt(first);
        System.out.println(fromYear + "*" + toYear);

        for (int i = fromYear; i <= toYear; i++) {
            HashMap<String, String> tmp = new HashMap<>();
            List<Double> salesByYear = databaseHelper.getSalesByYear(i + "", cusID);
            if (salesByYear.get(0) != null || salesByYear.get(1) != null) {
                System.out.println(i + "=" + "Sales " + salesByYear.get(0) + "Received" + salesByYear.get(1) + "\n");
                tmp.put(YEARS, i + "");
                NumberFormat formatter = NumberFormat.getIntegerInstance(new Locale("en", "IN"));
                tmp.put(SALES, formatter.format(salesByYear.get(0)));
                tmp.put(PAYMENTTOTAL, formatter.format(salesByYear.get(1)));
                tmp.put(DUE, formatter.format(salesByYear.get(0) - salesByYear.get(1)));
                feedList.add(tmp);
            }
        }


        LayoutInflater layoutInflater = LayoutInflater.from(quickSalesReportFragment.this.getActivity());
        View promptView = layoutInflater.inflate(R.layout.totalsalespopup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(quickSalesReportFragment.this.getActivity());
        alertDialogBuilder.setView(promptView);

        ListView list = promptView.findViewById(R.id.totalBusinessList);
        SimpleAdapter adapter = new SimpleAdapter(quickSalesReportFragment.this.getActivity().getApplicationContext(), feedList, R.layout.totalbusinesslistitem,
                new String[]{YEARS, SALES, PAYMENTTOTAL, DUE},
                new int[]{R.id.rcsiYear, R.id.rcsiSales, R.id.rcsiPaid, R.id.rcsiDue});
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        TextView totalCost = (TextView) promptView.findViewById(R.id.popupTotalCost);
        totalCost.setText(UHelper.stringDouble(cdata.get(0)));
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

        /*TextView totalSales = (TextView) promptView.findViewById(R.id.popupTotalSales);
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
        alert.show();*/
    }

    private boolean isBluetoothEnabled() {
        if (mBluetoothAdapter == null) {
            toast("Device does not support bluetooth");
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                toast("Switch on Bluetooth to use printer");
                return false;
            } else {
                return true;
            }
        }
    }

    void connect() {

        try {
            mPrinter.initService(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPrinter.connectToPrinter("D8:80:39:F8:37:A5");
    }

    private final BroadcastReceiver ReceiptPrinterMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DebugLog.logTrace("Printer Message Received");
            Bundle b = intent.getExtras();
            switch (b.getInt(RECEIPT_PRINTER_STATUS)) {
                case RECEIPT_PRINTER_CONN_STATE_NONE:
                    toast("Printer Not Connected");
                    //checkFinish();
                    break;
                case RECEIPT_PRINTER_CONN_STATE_LISTEN:
                    //toast(R.string.ready_for_conn);
                    break;
                case RECEIPT_PRINTER_CONN_STATE_CONNECTING:
                    //toast(R.string.printer_connecting);
                    break;
                case RECEIPT_PRINTER_CONN_STATE_CONNECTED:
                    toast("Printer Connected");
                    new AsyncPrint().execute();
                    break;
                case RECEIPT_PRINTER_CONN_DEVICE_NAME:
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_ERROR_MSG:
                    String n = b.getString(RECEIPT_PRINTER_MSG);
                    //toast(n);
                    //checkFinish();
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_MSG:
                    String m = b.getString(RECEIPT_PRINTER_MSG);
                    //toast(m);
                    break;
                case RECEIPT_PRINTER_NOT_CONNECTED:
                    toast("Status : Printer Not Connected");
                    //checkFinish();
                    break;
                case RECEIPT_PRINTER_NOT_FOUND:
                    toast("Status : Printer Not Found");
                    //checkFinish();
                    break;
                case RECEIPT_PRINTER_SAVED:
                    toast("Printer Saved as Favourite");
                    break;
            }
        }
    };

    private class AsyncPrint extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            print("0123");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //wait for printing to complete
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //mPrinter.disconnectFromPrinter();


        }
    }

    private void print(String receiptNo) {
        mPrinter.setPrinterWidth(PrinterWidth.PRINT_WIDTH_48MM);
        mPrinter.setAlignmentCenter();
        int textSize = 32;
        int pixellineFeed = 150;
        mPrinter.setPrintDensity(PrintDensity.FADE);
        //Print logo
        //Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.logo);
        //mPrinter.printGrayScaleImage(logo, 1);
        mPrinter.setPrintDensity(PrintDensity.NORMAL);
        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER);

        //Print receipt number
        Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/ErasBoldITC.ttf");
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(125);
        textPaint.setTypeface(custom_font);
        mPrinter.printUnicodeText(receiptNo, Layout.Alignment.ALIGN_CENTER, textPaint);

        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER);
        //mPrintUnicodeText("ನಮ್ಮಲ್ಲಿ ಸುಂದರವಾದ ಶಿರಸಿಯ ಗಣಪತಿ ಮೂರ್ತಿಗಳು ಸಿಗುತ್ತವೆ", 22, CENTER);
        //mPrintUnicodeText(readSharedPref("text1"), 22, CENTER);
        mPrinter.printTextLine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        mPrintUnicodeText("Dt:____________", textSize, RIGHT);
        mPrintUnicodeText("Name :", textSize, LEFT);
        mPrinter.printLineFeed();
        mPrintUnicodeText("Mob  :", textSize, LEFT);
        mPrinter.printLineFeed();
        mPrintUnicodeText("Model: ", textSize, LEFT);
        mPrinter.printLineFeed();
        mPrintUnicodeText("City :", textSize, LEFT);
        //mPrintUnicodeText("Price:₹", textSize, LEFT);
        //mPrintUnicodeText("Advnc:₹", textSize, LEFT);
        mPrintUnicodeText("Bal  :₹", textSize, LEFT);
        mPrinter.printLineFeed();
        //mPrintUnicodeText("Text :", textSize, LEFT);
        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER);
        //mPrintUnicodeText("ವಿಶೇಷ ಸೂಚನೆ : ಗಣಪತಿ ಮೂರ್ತಿಯನ್ನು ಚವತಿಯ ದಿವಸ ಮಧ್ಯಾನ್ಹ 12 ಘಂಟೆಯ ಒಳಗೆ ವಯ್ಯಬೇಕು. ಬರುವಾಗ ಇ ಚೀಟಿಯನ್ನುತಪ್ಪದೆ ತರಬೇಕು.", 22, CENTER);
        //mPrintUnicodeText(readSharedPref("text2"), 22, CENTER);
        mPrinter.printTextLine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        //mPrintUnicodeText("ತಯಾರಕರು : ಸಿ. ವಿ. ಚಿತ್ರಗಾರ, ಮರಾಠಿಕೊಪ್ಪ, ಶಿರಸಿ.", 22, CENTER);
        //mPrintUnicodeText(readSharedPref("text3"), 22, CENTER);
        //mPrintUnicodeText("9448629160/9916278538/9141646176", 20, CENTER);
        // mPrintUnicodeText(readSharedPref("text4"), 20, CENTER);
        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER);

        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.resetPrinter();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPrinter.onActivityRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void mPrintUnicodeText(String text, int size, int almnt) {
        Layout.Alignment alignment = null;
        switch (almnt) {
            case 0:
                alignment = Layout.Alignment.ALIGN_CENTER;
                break;
            case 1:
                alignment = Layout.Alignment.ALIGN_NORMAL;
                break;
            case -1:
                alignment = Layout.Alignment.ALIGN_OPPOSITE;
                break;
        }
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Cousine-Regular.ttf");
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(size);
        textPaint.setTypeface(font);
        System.out.println("Print status **" + mPrinter.printUnicodeText(text, alignment, textPaint));
        System.out.println("Print status Stat**" + mPrinter.getPrinterStatus());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        System.out.println(requestCode + "*" + resultCode + "*" + data);


        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    connect();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    toast("Bluetooth not switched on");
                    //checkFinish();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    void toast(int message) {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    void toast(String message) {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        DebugLog.logTrace();
        mPrinter.onActivityResume();
        super.onResume();
    }

    @Override
    public void onStop() {
        System.out.println("On stop");
        super.onStop();
        try {
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(ReceiptPrinterMessageReceiver);
        } catch (Exception e) {
            DebugLog.logException(e);
        }
    }

    @Override
    public void onPause() {
        DebugLog.logTrace();
        mPrinter.onActivityPause();
        System.out.println("On pause");
        super.onPause();
        //this.unregisterReceiver(this.mReceiver);
    }

    @Override
    public void onDestroy() {
        DebugLog.logTrace("onDestroy");
        mPrinter.onActivityDestroy();
        super.onDestroy();
    }


}

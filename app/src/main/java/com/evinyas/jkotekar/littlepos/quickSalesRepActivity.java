package com.evinyas.jkotekar.littlepos;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import static com.cie.btp.BtpConsts.PRINTER_DISCONNECTED;
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

public class quickSalesRepActivity extends AppCompatActivity implements DatePickerFragment.OnDataPass {
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
    static final String COST = "Costs";
    final String SALES = "Sales";
    final String PAYMENTTOTAL = "Payments";
    final String DUE = "Dues";
    final String YEARS = "Year";

    String customer;
    double sum, rec;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public CieBluetoothPrinter mPrinter = CieBluetoothPrinter.INSTANCE;
    private static final int LEFT = 1;
    private static final int RIGHT = -1;
    private static final int CENTER = 0;
    public static final int REQUEST_ENABLE_BT = 9;
    boolean printerConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_sales_report_activity);

        fromDate = (TextView) findViewById(R.id.textView_from_POSR);
        fromDate.setText(UHelper.setPresentDateddMMyyyy());
        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromTouched = true;
                toTouched = false;
                showdatePicker();
            }
        });

        toDate = (TextView) findViewById(R.id.textView_to_POSR);
        toDate.setText(UHelper.setPresentDateddMMyyyy());
        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromTouched = false;
                toTouched = true;
                showdatePicker();
            }
        });

        Bundle extras = getIntent().getExtras();

        assert extras != null;
        customer = extras.getString("customer");

        databaseHelper = DatabaseHelper.getInstance(this);

        ImageView salesPopUp = (ImageView) findViewById(R.id.salesPopUpButton);
        salesPopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salesPopUp();
            }
        });

        final ImageView print = (ImageView) findViewById(R.id.printImage);
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (printerConnected)
                    print();
                else connectPrinter();

            }
        });

        cusID = extras.getInt("cusid");
        TextView cusName = (TextView) findViewById(R.id.salesreport_customername);
        cusName.setText(String.format("Sales for %s", customer));

        ListView list = (ListView) findViewById(R.id.listViewPOSR);
        Resources res = getResources();
        adapter = new CustomAdapter(quickSalesRepActivity.this, CustomListViewValuesArr, res, this);
        list.setAdapter(adapter);
        gross = (TextView) findViewById(R.id.salesReport_gross);
        payments = (TextView) findViewById(R.id.salesReport_payments);
        net = (TextView) findViewById(R.id.salesReport_net);
        totalForTheDay = (TextView) findViewById(R.id.totalForDay);
        getGrandTotal();
        refreshList();
    }

    private void connectPrinter() {
        if (!isBluetoothEnabled()) {
            toast("Bluetooth is not switched on");

        } else {
            if (!printerConnected)
                connect();
        }
    }

    private void getGrandTotal() {
        List<String> data = databaseHelper.getSalesSumbyCustomer(cusID,
                UHelper.dateFormatdmyTOymd(fromDate.getText().toString()),
                UHelper.dateFormatdmyTOymd(toDate.getText().toString()));
        gross.setText(String.format("%s", UHelper.parseDouble(data.get(0))));
        payments.setText(String.format("%s", UHelper.parseDouble(data.get(1))));
        net.setText(String.format("%s", UHelper.parseDouble(data.get(0)) - UHelper.parseDouble(data.get(1))));

        List<String> sumDay = databaseHelper.getSalessumfortheDay(cusID, UHelper.setPresentDateyyyyMMddCP());
        sum = UHelper.parseDouble(sumDay.get(0));
        rec = UHelper.parseDouble(sumDay.get(1));
        totalForTheDay.setText(String.format("Σ %s, Received %s for Today", UHelper.parseDouble(sumDay.get(0)), UHelper.parseDouble(sumDay.get(1))));
    }

    public void showdatePicker() {
        Bundle b = new Bundle();
        b.putBoolean("stat", true);
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.setArguments(b);
        newFragment.show(getSupportFragmentManager(), "datePicker");
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

    private void getListTotal() {
        sum = 0;
        rec = 0;
        for (salesData sd : CustomListViewValuesArr) {
            sum = sum + UHelper.parseDouble(sd.getAmount());
            rec = rec + UHelper.parseDouble(sd.getReceived());
        }
    }

    /*****************
     * This function used by adapter
     ****************/
    public void onItemClick(int mPosition) {
        salesData tempSalesData;
        if (CustomListViewValuesArr.size() != 0) {
            tempSalesData = CustomListViewValuesArr.get(mPosition);
            if (tempSalesData.getComments().length() != 0)
                Toast.makeText(this.getApplicationContext(), tempSalesData.getComments(), Toast.LENGTH_SHORT).show();
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
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.prompts, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                quickSalesRepActivity.this);
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

        String first = databaseHelper.getFirstEntry().substring(0, 4);
        int toYear = Calendar.getInstance().get(Calendar.YEAR);
        int fromYear = Integer.parseInt(first);
        System.out.println(fromYear + "*" + toYear);

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

            List<Double> salesByYear = databaseHelper.getSalesByYear(fy, ty, fm, tm, cal.getActualMaximum(Calendar.DATE) + "", cusID);
            List<String> cdata = databaseHelper.getCostSumbyDate(fy + "-" + m + "-" + "01", ty + "-" + tm + "-" + "01");

            String yrs = fy.substring(2, 4) + "-" + ty.substring(2, 4);
            if (salesByYear.get(0) != null || salesByYear.get(1) != null) {
                System.out.println(yrs + "=" + "Sales " + salesByYear.get(0) + "Received" + salesByYear.get(1) + "\n");
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

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.totalsalespopup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        ListView list = promptView.findViewById(R.id.totalBusinessList);
        SimpleAdapter adapter = new SimpleAdapter(this, feedList, R.layout.totalbusinesslistitem,
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
        toast("Connecting to Printer");
        try {
            mPrinter.initService(this);
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
                    //toast("Printer Not Connected");
                    printerConnected = false;
                    break;
                case RECEIPT_PRINTER_CONN_STATE_LISTEN:
                    break;
                case RECEIPT_PRINTER_CONN_STATE_CONNECTING:
                    toast("Connecting to Printer, please wait");
                    break;
                case RECEIPT_PRINTER_CONN_STATE_CONNECTED:
                    printerConnected = true;
                    toast("Printer Connected");
                    print();
                    break;
                case RECEIPT_PRINTER_CONN_DEVICE_NAME:
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_ERROR_MSG:
                    String n = b.getString(RECEIPT_PRINTER_MSG);
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_MSG:
                    String m = b.getString(RECEIPT_PRINTER_MSG);
                    //toast(m);
                    break;
                case RECEIPT_PRINTER_NOT_CONNECTED:
                    //toast("Status : Printer Not Connected");
                    printerConnected = false;
                    break;
                case RECEIPT_PRINTER_NOT_FOUND:
                    toast("Printer Not Found");
                    printerConnected = false;
                    break;
                case RECEIPT_PRINTER_SAVED:
                    //toast("Printer Saved as Favourite");
                    break;
                case PRINTER_DISCONNECTED:
                    toast("Printer Disconnected");
                    break;
            }
        }
    };

    @Override
    public void onDateReceive(String date, Boolean stat) {
        pass(date);
    }

    private void print() {
        getListTotal();
        mPrinter.setPrinterWidth(PrinterWidth.PRINT_WIDTH_48MM);
        mPrinter.setAlignmentCenter();
        int textSize = 20;
        mPrinter.setPrintDensity(PrintDensity.FADE);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/ErasBoldITC.ttf");
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
        textPaint.setTypeface(custom_font);
        mPrinter.printUnicodeText("Cream Basket", Layout.Alignment.ALIGN_CENTER, textPaint);
        mPrinter.setPrintDensity(PrintDensity.NORMAL);
        mPrintUnicodeText(UHelper.setPresentDateddMMyyyy(), 30, CENTER, Typeface.NORMAL);
        mPrintUnicodeText("Name : " + customer, 22, LEFT, Typeface.BOLD_ITALIC);
        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER, Typeface.NORMAL);
        mPrintUnicodeText("Product Name   " + " " + "Quanty" + " " + "  Amount", textSize, LEFT, Typeface.NORMAL);
        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER, Typeface.NORMAL);
        for (int i = 0; i < CustomListViewValuesArr.size(); i++) {
            String pName = CustomListViewValuesArr.get(i).getProductName();
            String qty = CustomListViewValuesArr.get(i).getQuantity();
            String amt = UHelper.stringDouble(CustomListViewValuesArr.get(i).getAmount());
            if (qty == null) {
                pName = "Payments";
                qty = "";
                amt = "-" + UHelper.stringDouble(CustomListViewValuesArr.get(i).getReceived());
            }
            pName = pName != null ? pName : "Payments";
            pName = rightpad(pName, 16);
            qty = leftpad(qty, 6);
            amt = leftpad(amt, 8);

            mPrintUnicodeText(pName + " " + qty + " " + amt, textSize, LEFT, Typeface.NORMAL);
        }
        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER, Typeface.NORMAL);
        mPrintUnicodeText(rightpad("Total", 20) + leftpad(UHelper.stringDouble(sum + ""), 12), textSize, LEFT, Typeface.BOLD);
        mPrintUnicodeText(rightpad("Received", 20) + leftpad("-" + UHelper.stringDouble(rec + ""), 12), textSize, LEFT, Typeface.BOLD);
        mPrintUnicodeText(rightpad("Balance", 20) + leftpad(UHelper.stringDouble((sum - rec) + ""), 12), textSize, LEFT, Typeface.BOLD);

        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.resetPrinter();
    }

    private String rightpad(String text, int length) {
        return String.format("%-" + length + "." + length + "s", text);
    }

    private String leftpad(String text, int length) {
        return String.format("%" + length + "." + length + "s", text);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPrinter.onActivityRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void mPrintUnicodeText(String text, int size, int almnt, int typefaceType) {
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
        Typeface plain = Typeface.createFromAsset(getAssets(), "fonts/Cousine-Regular.ttf");
        Typeface typeface = Typeface.create(plain, typefaceType);
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(size);
        textPaint.setTypeface(typeface);
        System.out.println("Print status **" + mPrinter.printUnicodeText(text, alignment, textPaint));
        System.out.println("Print status Stat**" + mPrinter.getPrinterStatus());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

    void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        DebugLog.logTrace();
        mPrinter.onActivityResume();
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIPT_PRINTER_MESSAGES);
        LocalBroadcastManager.getInstance(this).registerReceiver(ReceiptPrinterMessageReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(ReceiptPrinterMessageReceiver);
        } catch (Exception e) {
            DebugLog.logException(e);
        }
    }

    @Override
    protected void onPause() {
        mPrinter.onActivityPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        DebugLog.logTrace("onDestroy");
        mPrinter.onActivityDestroy();
        super.onDestroy();
    }

    private String readSharedPref(String KEY) {
        String returnData = null;
        String SHAREDPREFNAME = "LittlePOSPrefs";
        SharedPreferences settings = getSharedPreferences(SHAREDPREFNAME, 0);
        return settings.getString("STARTMONTH", 0 + "");
    }

}
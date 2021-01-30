package com.evinyas.jkotekar.littlepos;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.evinyas.jkotekar.littlepos.model.CustomerProduct;
import com.evinyas.jkotekar.littlepos.model.SpinnerItemID;
import com.evinyas.jkotekar.littlepos.model.UHelper;
import com.evinyas.jkotekar.littlepos.model.salesData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//Completed Backup to sd and Gmail file send on 6/6/2016


public class POS extends AppCompatActivity implements DatePickerFragment.OnDataPass {

    private static final int PRODUCT = 0;
    private static final int PRICE = 1;
    private static final int QUANTITY = 2;
    private static final int AMOUNT = 3;
    private List<CustomerProduct> CPList = null;
    private TextView date;
    private TextView gross;
    private TextView net;
    private boolean writePayments = true;
    private boolean writeDiscount = false;
    private boolean writeComments = true;
    private LinearLayout holder;
    private String selectedCustomer = null;
    private Integer cusID = -1;
    private DatabaseHelper databaseHelper;
    Button salesReportButton;
    Button extras;
    Spinner spinner;

    private double payments, surcharge, discount;
    double netPrice = 0, sum = 0;
    String comment = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pos);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        holder = (LinearLayout) findViewById(R.id.holder);
        databaseHelper = DatabaseHelper.getInstance(getApplication());
        CPList = databaseHelper.getAllcustomerProductName(0);
        salesReportButton = (Button) findViewById(R.id.sl_button_sales);
        extras = (Button) findViewById(R.id.pos_button_extras);
        spinner = (Spinner) findViewById(R.id.sl_spinner);

        date = (TextView) findViewById(R.id.sl_editText_date);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                b.putBoolean("stat", true);
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.setArguments(b);
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });


        gross = (TextView) findViewById(R.id.sales_gross);
        net = (TextView) findViewById(R.id.sales_net);

        loadCustomer();
        Button saveButton = (Button) findViewById(R.id.sl_button_save);
        if (saveButton != null) {
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (payments > 0)
                        writePayments = true;
                    if (comment.length() != 0)
                        writeComments = true;
                    //if only payment is entered
                    if (payments > 0 || surcharge > 0 || discount > 0 && UHelper.parseDouble(gross.getText().toString()) <= 0) {
                        salesData sls = new salesData();
                        if (isDateSame())
                            sls.setDate(UHelper.setPresentDateyyyyMMddhhmmss());
                        else
                            sls.setDate(UHelper.dateFormatdmyTOymdhms(date.getText().toString()));
                        sls.setCustID(cusID);
                        if (payments > 0)
                            sls.setReceived(payments + "");
                        if (surcharge > 0)
                            sls.setAmount(surcharge + "");
                        if (discount > 0)
                            sls.setAmount("-" + discount);
                        if (comment.length() != 0)
                            sls.setComments(comment);

                        if (databaseHelper.addSales(sls) > 0)
                            showToast("Transaction Saved Successfully");
                        else
                            showToast("Transaction failed to save!");
                        payments = 0;
                        surcharge = 0;
                        discount = 0;
                        comment = "";
                        calculateGross();
                        calculateNet();
                        return;
                    }
                    if (UHelper.parseDouble(gross.getText().toString()) > 0) {
                        saveData();
                        payments = 0;
                        surcharge = 0;
                        discount = 0;
                        comment = "";
                        resetQuantity();
                        calculateGross();
                        calculateNet();
                    }
                }
            });
        }

        Button closeButton = (Button) findViewById(R.id.sl_button_close);
        if (closeButton != null) {
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        salesReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cusID != -1) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("cusid", cusID);
                    bundle.putString("grandtotal", "1000");
                    bundle.putString("customer", selectedCustomer);

                    Intent myIntent = new Intent(POS.this, quickSalesRepActivity.class);
                    myIntent.putExtras(bundle);
                    startActivity(myIntent);
                    //quickSalesReportFragment report = new quickSalesReportFragment();
                    //report.setArguments(bundle);
                    //report.show(getSupportFragmentManager(), "salesReport");
                }
            }
        });

        extras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupExtras();
            }
        });

    }

    private void calculateNet() {
        //Net cost = Gross - payments+surcharges
        netPrice = sum - payments + surcharge - discount;
        net.setText(String.format(Locale.ENGLISH, "%.2f", netPrice));
    }

    private void loadCustomer() {
        ArrayList<SpinnerItemID> spinnerItemIDs = new ArrayList<>();
        int previousID = -1;
        for (CustomerProduct c : CPList) {
            if (c.getCustomerID() != previousID)
                spinnerItemIDs.add(new SpinnerItemID(c.getCustomerID(), c.getCustomerName()));
            previousID = c.getCustomerID();
        }

        ArrayAdapter<SpinnerItemID> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerItemIDs);

        dataAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerItemID itemID = (SpinnerItemID) parent.getSelectedItem();
                {
                    selectedCustomer = itemID.getText();
                    cusID = itemID.getId();
                    date.setText(UHelper.setPresentDateddMMyyyy());
                    removeProduct();
                    loadProducts();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void loadProducts() {
        if (selectedCustomer != null) {
            CPList = null;
            CPList = databaseHelper.getAllcustomerProductName(cusID);
            float textSize = 16;
            int textColor = R.color.Yellow600;

            for (int i = 0; i < CPList.size(); i++) {
                int productID = CPList.get(i).getProductID();
                String pName = CPList.get(i).getProductName();
                String pPrice = CPList.get(i).getCustomerPrice();
                //If customer cost is zero then take cost from default cost
                if (UHelper.parseDouble(pPrice) == 0)
                    pPrice = String.format("%s", databaseHelper.getProductPricebyID(productID));

                final TextView amount = new TextView(this);

                LinearLayout L = new LinearLayout(this);
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                L.setOrientation(LinearLayout.HORIZONTAL);
                L.setLayoutParams(param);

                //Product Name
                TextView product = new TextView(this);
                LinearLayout.LayoutParams PNparam = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                PNparam.weight = 2.5f;
                product.setLayoutParams(PNparam);
                product.setText(pName);
                product.setId(productID);
                product.setGravity(Gravity.END);
                product.setTypeface(null, Typeface.BOLD);
                product.setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                product.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                L.addView(product);

                //Product Price
                final TextView price = new TextView(this);
                price.setText(UHelper.parseDouble(pPrice) + "");
                LinearLayout.LayoutParams priceparam = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                priceparam.weight = 1f;
                price.setLayoutParams(priceparam);
                price.setEnabled(false);
                price.setTypeface(null, Typeface.BOLD);
                price.setGravity(Gravity.END);
                price.setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                price.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

                L.addView(price);

                //quantity
                final EditText quantity = new EditText(this);
                quantity.setHint("00");
                LinearLayout.LayoutParams quantityparam = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                quantityparam.weight = 1f;
                quantity.setGravity(Gravity.END);
                quantity.setLayoutParams(quantityparam);
                quantity.setSingleLine();
                quantity.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                quantity.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                quantity.setTypeface(null, Typeface.BOLD);
                quantity.setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                //set the max length programatically
                int maxLength = 7;
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(maxLength);
                quantity.setFilters(FilterArray);
                // set the max length END
                quantity.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        double val;
                        if (s.length() == 0) {
                            amount.setText("0.00");
                        } else {
                            double prc = UHelper.parseDouble(price.getText().toString());
                            double qty = UHelper.parseDouble(quantity.getText().toString());
                            val = prc * qty;
                            amount.setText(String.format(Locale.ENGLISH, "%.2f", UHelper.parseDouble(val + "")));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                L.addView(quantity);

                //Amount
                amount.setHint("00");
                LinearLayout.LayoutParams amountparam = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                amountparam.weight = 1.5f;
                amount.setGravity(Gravity.END);
                amount.setLayoutParams(amountparam);
                amount.setEnabled(false);
                amount.setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                amount.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                amount.setTypeface(null, Typeface.BOLD);
                amount.setPadding(0, 0, 5, 0);
                amount.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        calculateSum();
                        calculateNet();
                        calculateGross();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                L.addView(amount);
                holder.addView(L);
            }
        }
    }

    private void calculateGross() {
        gross.setText(String.format(Locale.ENGLISH, "%.2f", sum));
    }

    private void saveData() {
        for (int i = 0; i < holder.getChildCount(); i++) {
            salesData sls = new salesData();
            View child = holder.getChildAt(i);
            ViewGroup group = (ViewGroup) child;
            for (int j = 0; j < group.getChildCount(); j++) {
                View v = group.getChildAt(j);
                double amount = 0;
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    if (j == PRODUCT) {
                        sls.setCustID(cusID);
                        sls.setProdID(tv.getId() + "");
                    } else if (j == PRICE)
                        sls.setPrice(tv.getText().toString());
                    else if (j == AMOUNT) {
                        sls.setAmount(tv.getText().toString());
                        amount = UHelper.parseDouble(tv);
                    }
                }

                if (v instanceof EditText) {
                    EditText et = (EditText) v;
                    if (j == QUANTITY)
                        sls.setQuantity(et.getText().toString());
                }
                //write to database when last field value is read and when amount is not ZERO
                if (j == AMOUNT && amount != 0) {
                    //if the from date and to date in is not same only then write to DB format date into 30-12-9999
                    if (isDateSame())
                        sls.setDate(UHelper.setPresentDateyyyyMMddhhmmss());
                    else sls.setDate(UHelper.dateFormatdmyTOymdhms(date.getText().toString()));
                    //check if write payments in true to write to DB only once in a transaction
/*                    if (writePayments && payments > 0) {
                        sls.setReceived(payments + "");
                        writePayments = false;
                    }*/
/*                    if (writeDiscount && discount > 0) {
                        sls.setReceived(discount + "");
                        writeDiscount = false;
                    }*/
                    //check if write payments in true to write to DB only once in a transaction
                    if (writeComments && comment.length() != 0) {
                        sls.setComments(comment);
                        writeComments = false;
                    }

                    if (databaseHelper.addSales(sls) > 0)
                        showToast("Transaction Saved Successfully");
                    else showToast("Transaction failed to save !");
                }
            }
        }
    }

    private boolean isDateSame() {
        String presentdate = UHelper.setPresentDateddMMyyyy();
        String source = date.getText().toString();
        return source.contains(presentdate);
    }

    private void removeProduct() {
        holder.removeAllViews();
    }

    private double calculateSum() {
        double xsum = 0;
        sum = 0;
        for (int i = 0; i < holder.getChildCount(); i++) {
            View child = holder.getChildAt(i);
            ViewGroup group = (ViewGroup) child;
            for (int j = 0; j < group.getChildCount(); j++) {
                View v = group.getChildAt(j);
                if (v instanceof TextView && j == AMOUNT) {
                    TextView et = (TextView) v;
                    xsum += UHelper.parseDouble(et.getText().toString());
                }
            }
        }
        sum = xsum;
        return xsum;
    }


    @Override
    public void onDateReceive(String date, Boolean stat) {
        if (stat)
            this.date.setText(date);
        if (!stat) {
            FragmentManager fm = getSupportFragmentManager();
            quickSalesReportFragment frag = (quickSalesReportFragment) fm.findFragmentByTag("salesReport");
            frag.pass(date);
        }
    }

    private void showToast(String text) {
        final Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 1000);
    }

    protected void popupExtras() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.extras, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        final EditText et_payments = (EditText) promptView.findViewById(R.id.pos_payments);
        final EditText et_discounts = (EditText) promptView.findViewById(R.id.pos_discount);
        final EditText et_comments = (EditText) promptView.findViewById(R.id.pos_comments);
        final EditText et_surcharge = (EditText) promptView.findViewById(R.id.pos_surcharges);

        et_comments.setText(comment);
        if (surcharge > 0)
            et_surcharge.setText(String.format(Locale.ENGLISH, "%s", surcharge));
        if (payments > 0)
            et_payments.setText(String.format(Locale.ENGLISH, "%s", payments));
        if (discount > 0)
            et_discounts.setText(String.format(Locale.ENGLISH, "%s", discount));


        if (sum <= 0) {
            et_payments.setEnabled(true);
            et_surcharge.setEnabled(true);
            et_discounts.setEnabled(true);
        } else {
            et_payments.setEnabled(false);
            et_surcharge.setEnabled(false);
            et_discounts.setEnabled(false);
        }

        et_discounts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (UHelper.parseDouble(s.toString()) > 0) {
                    et_comments.setText(String.format("Discount of %s", s));
                    et_surcharge.setEnabled(false);
                } else {
                    et_comments.setText(null);
                    et_surcharge.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_surcharge.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (UHelper.parseDouble(s.toString()) > 0) {
                    et_comments.setText(String.format("Surcharge of %s", s));
                    et_discounts.setEnabled(false);
                } else {
                    et_comments.setText(null);
                    et_discounts.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        POS.this.payments = UHelper.parseDouble(et_payments.getText().toString());
                        surcharge = UHelper.parseDouble(et_surcharge.getText().toString());
                        comment = et_comments.getText().toString();
                        discount = UHelper.parseDouble(et_discounts.getText().toString());
                        writeDiscount = discount > 0;
                        calculateNet();
                        calculateGross();
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

    private void resetQuantity() {
        for (int i = 0; i < holder.getChildCount(); i++) {
            View child = holder.getChildAt(i);
            ViewGroup group = (ViewGroup) child;
            for (int j = 0; j < group.getChildCount(); j++) {
                View v = group.getChildAt(j);
                if (v instanceof EditText && j == QUANTITY) {
                    EditText et = (EditText) v;
                    et.setText(null);

                }
            }
        }
    }
}

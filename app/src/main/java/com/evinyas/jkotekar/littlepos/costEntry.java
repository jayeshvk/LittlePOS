package com.evinyas.jkotekar.littlepos;

import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.evinyas.jkotekar.littlepos.model.Cost;
import com.evinyas.jkotekar.littlepos.model.CostData;
import com.evinyas.jkotekar.littlepos.model.CustomerProduct;
import com.evinyas.jkotekar.littlepos.model.UHelper;
import com.evinyas.jkotekar.littlepos.model.salesData;

import java.util.List;
import java.util.Locale;

public class costEntry extends AppCompatActivity implements DatePickerFragment.OnDataPass {
    private static final int PRODUCT = 0;
    private static final int PRICE = 1;
    private static final int QUANTITY = 2;
    private static final int AMOUNT = 3;
    private List<Cost> costList = null;
    private TextView totalCost;
    private TextView date;
    private LinearLayout holder;
    private DatabaseHelper databaseHelper;
    double sum = 0;
    private EditText comment;
    private boolean writeComments = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cost_entry);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        holder = (LinearLayout) findViewById(R.id.holder);
        databaseHelper = DatabaseHelper.getInstance(getApplication());
        comment = (EditText) findViewById(R.id.etComment);
        totalCost = (TextView) findViewById(R.id.totalCost);

        date = (TextView) findViewById(R.id.sl_editText_date);
        date.setText(UHelper.setPresentDateddMMyyyy());
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
        loadProducts();

        Button saveButton = (Button) findViewById(R.id.sl_button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UHelper.parseDouble(totalCost.getText().toString()) > 0) {
                    saveData();
                    resetQuantity();
                }
            }
        });
    }

    @Override
    public void onDateReceive(String date, Boolean stat) {
        if (stat)
            this.date.setText(date);
        if (!stat) {
            FragmentManager fm = getSupportFragmentManager();
            costEntryQuickReport frag = (costEntryQuickReport) fm.findFragmentByTag("costReport");
            frag.pass(date);
        }
    }

    private void loadProducts() {
        costList = databaseHelper.getAllCostItem();
        float textSize = 16;
        int textColor = R.color.Yellow600;

        for (int i = 0; i < costList.size(); i++) {
            int productID = costList.get(i).getId();
            String pName = costList.get(i).getCostName();
            String pPrice = costList.get(i).getCost();

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
                    totalCost.setText(String.format(Locale.ENGLISH, "%.2f", sum));

                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            L.addView(amount);
            holder.addView(L);
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

    private boolean isDateSame() {
        String presentdate = UHelper.setPresentDateddMMyyyy();
        String source = date.getText().toString();
        return source.contains(presentdate);
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
        comment.setText(null);
    }

    private void saveData() {
        for (int i = 0; i < holder.getChildCount(); i++) {
            CostData sls = new CostData();
            View child = holder.getChildAt(i);
            ViewGroup group = (ViewGroup) child;
            for (int j = 0; j < group.getChildCount(); j++) {
                View v = group.getChildAt(j);
                double amount = 0;
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    if (j == PRODUCT) {
                        sls.setCostId(tv.getId() + "");
                    } else if (j == PRICE)
                        sls.setCost(tv.getText().toString());
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
                    if (writeComments && comment.length() != 0) {
                        sls.setComments(comment.getText().toString());
                        writeComments = false;
                    }
                    if (databaseHelper.addCostData(sls) > 0)
                        showToast("Transaction Saved Successfully");
                    else showToast("Transaction failed to save !");
                }
            }
        }
        writeComments = true;
    }

    public void costEntryQuickReport(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("grandtotal", "1000");
        costEntryQuickReport report = new costEntryQuickReport();
        report.setArguments(bundle);
        report.show(getSupportFragmentManager(), "costReport");
    }


    public void close(View view) {
        finish();
    }
}

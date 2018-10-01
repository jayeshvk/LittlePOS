package com.evinyas.jkotekar.littlepos;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.evinyas.jkotekar.littlepos.model.CustomerProduct;
import com.evinyas.jkotekar.littlepos.model.SpinnerItemID;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jkotekar on 4/30/2016.
 * <p/>
 * Add customer price class core database manager
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class addCustomerPrice extends DialogFragment implements AdapterView.OnItemSelectedListener {

    private LinearLayout linearLayout;
    private List<CustomerProduct> CPList = null;
    private String selectedCustomer = null;
    private DatabaseHelper databaseHelper;
    private Integer cusID;
    //private String title = "SELECT A CUSTOMER  " + UHelper.getEmijoByUnicode(0x1F53B);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_customer_price, container, false);

        getDialog().setTitle("Create/Update Customer Price");
        getDialog().setCanceledOnTouchOutside(false);

        // Get singleton instance of database
        databaseHelper = DatabaseHelper.getInstance(getActivity());

        linearLayout = (LinearLayout) rootView.findViewById(R.id.productPriceLayout);
        CPList = databaseHelper.getAllcustomerProductName(0);

        loadCustomer(rootView);

        ImageButton saveButton = (ImageButton) rootView.findViewById(R.id.buttonACPSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCustomerPricetoDB();
                toast("Changes Saved");
            }
        });

        ImageButton btnClose = (ImageButton) rootView.findViewById(R.id.buttonACPClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(addCustomerPrice.this).commit();
            }
        });

        return rootView;
    }

    private void loadCustomer(View rootView) {
        // Spinner element
        Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);


        ArrayList<SpinnerItemID> spinnerItemIDs = new ArrayList<>();
        // Spinner Drop down elements
        int previousID = -1;
        for (CustomerProduct c : CPList) {
            if (c.getCustomerID() != previousID)
                spinnerItemIDs.add(new SpinnerItemID(c.getCustomerID(), c.getCustomerName()));
            previousID = c.getCustomerID();
        }
        //spinnerItemIDs.add(0, new SpinnerItemID(0, title));
        ArrayAdapter<SpinnerItemID> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item_black, spinnerItemIDs);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }

    private void loadCustomerProducts() {
        float textSize = 16;
        if (selectedCustomer != null) {
            CPList = null;
            CPList = databaseHelper.getAllcustomerProductName(cusID);
            for (int i = 0; i < CPList.size(); i++) {
                if (CPList.get(i).getCustomerID() == cusID) {

                    LinearLayout L = new LinearLayout(getActivity());
                    L.setOrientation(LinearLayout.HORIZONTAL);
                    L.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TextView productName = new TextView(getActivity());
                    productName.setText(CPList.get(i).getProductName());
                    productName.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                    productName.setTextColor(Color.parseColor("#000000"));
                    productName.setGravity(Gravity.END);
                    productName.setEms(5);

                    EditText productPrice = new EditText(getActivity());
                    productPrice.setHint("0.00");
                    productPrice.setEms(5);
                    productPrice.setId(CPList.get(i).getProductID());
                    productPrice.setText(CPList.get(i).getCustomerPrice());
                    productPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                    productPrice.setTextColor(Color.parseColor("#000000"));
                    productPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    productPrice.setGravity(Gravity.END);

                    L.addView(productName);
                    L.addView(productPrice);
                    linearLayout.addView(L);
                }
            }
        }
    }

    private void removeProduct() {
        linearLayout.removeAllViews();
    }

    private void saveCustomerPricetoDB() {
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View child = linearLayout.getChildAt(i);
            LinearLayout cb = (LinearLayout) child;
            for (int j = 0; j < cb.getChildCount(); j++) {
                View childtochild = cb.getChildAt(j);
                if (childtochild instanceof EditText) {
                    EditText et = (EditText) childtochild;
                    CustomerProduct cprice = new CustomerProduct(cusID, et.getId(), et.getText().toString());
                    databaseHelper.addCustomerPrice(cprice);
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        SpinnerItemID itemID = (SpinnerItemID) parent.getSelectedItem();
/*        if (itemID.getText().contains(title)) {
            cusID = -1;
            removeProduct();
        } else*/
        {
            selectedCustomer = itemID.getText();
            cusID = itemID.getId();
            removeProduct();
            loadCustomerProducts();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}

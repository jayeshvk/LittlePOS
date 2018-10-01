package com.evinyas.jkotekar.littlepos;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.evinyas.jkotekar.littlepos.model.Customer;
import com.evinyas.jkotekar.littlepos.model.CustomerProduct;
import com.evinyas.jkotekar.littlepos.model.Product;
import com.evinyas.jkotekar.littlepos.model.SpinnerItemID;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jkotekar on 4/30/2016.
 * adding customer products
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class addCustomerProducts extends DialogFragment implements AdapterView.OnItemSelectedListener {

    private LinearLayout linearLayout;
    private List<Product> productList;
    private List<Customer> customerList;
    private String selectedCustomer;
    private DatabaseHelper databaseHelper;
    private Integer cusID;
    //private String title = "SELECT A CUSTOMER  " + UHelper.getEmijoByUnicode(0x1F53B);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_customer_products, container, false);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setTitle("Add Customer Products");

        // Get singleton instance of database
        databaseHelper = DatabaseHelper.getInstance(getActivity());

        customerList = databaseHelper.getAllCustomers();
        // If no cusotmer then dont show the products in the list
        if (customerList.size() >= 0)
            productList = databaseHelper.getAllProducts();

        linearLayout = (LinearLayout) rootView.findViewById(R.id.svLayout);

        loadCustomer(rootView);
        loadProductList();

        ImageButton saveButton = (ImageButton) rootView.findViewById(R.id.buttonACPdSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCustomerProduct();
                toast("Changes Saved");

            }
        });

        ImageButton btnClose = (ImageButton) rootView.findViewById(R.id.buttonACPdClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(addCustomerProducts.this).commit();
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
        for (Customer c : customerList) {
            if (c.getId() != previousID)
                spinnerItemIDs.add(new SpinnerItemID(c.getId(), c.getCustomerName()));
            previousID = c.getId();
        }
        //spinnerItemIDs.add(0, new SpinnerItemID(0, title));
        ArrayAdapter<SpinnerItemID> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item_black, spinnerItemIDs);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }

    private void loadProductList() {
        float textSize = 16;
        for (int i = 0; i < productList.size(); i++) {
            CheckBox cb = new CheckBox(getActivity());
            cb.setText(productList.get(i).getProductName());
            cb.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            cb.setId(productList.get(i).getId());
            linearLayout.addView(cb);
        }
    }

    private void loadCustomerProductList() {
        List<CustomerProduct> cp = databaseHelper.getAllcustomerProduct(cusID);
        for (int j = 0; j < linearLayout.getChildCount(); j++) {
            View child = linearLayout.getChildAt(j);
            CheckBox cb = (CheckBox) child;
            for (CustomerProduct k : cp) {
                if (cb.getId() == k.getProductID()) {
                    cb.setChecked(true);
                    break;
                } else
                    cb.setChecked(false);
            }
        }
    }

    private void saveCustomerProduct() {
        if (selectedCustomer != null) {
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                View child = linearLayout.getChildAt(i);
                CheckBox cb = (CheckBox) child;
                if (cb.isChecked()) {
                    CustomerProduct cp = new CustomerProduct();
                    cp.setCustomerID(cusID);
                    cp.setProductID(cb.getId());
                    cp.setCustomerPrice("");

                    databaseHelper.addCustomerProducts(cp);
                } else if (!cb.isChecked()) {
                    //delete the customer and selected customer.
                    databaseHelper.deleteCustomerProduct(cusID + "", cb.getId() + "");
                }

            }
        }
    }

    private void clearCheckBox() {
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View child = linearLayout.getChildAt(i);
            if (child instanceof CheckBox) {
                //Support for Checkboxes
                CheckBox cb = (CheckBox) child;
                cb.setChecked(false);
            }
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        SpinnerItemID item = (SpinnerItemID) parent.getItemAtPosition(position);
        /*if (item.getText().contains(title)) {
            selectedCustomer = null;
            cusID = -1;
        } else*/
        {
            selectedCustomer = item.getText();
            cusID = item.getId();
            clearCheckBox();
            loadCustomerProductList();
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

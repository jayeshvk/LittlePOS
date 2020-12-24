package com.evinyas.jkotekar.littlepos;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MasterData extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.master_data);
    }

    public void addCustomer(View view) {
        FragmentManager fm = getFragmentManager();
        addCustomer dialogFragment = new addCustomer();
        dialogFragment.show(fm, "addCustomer");

    }

    public void addProduct(View view) {
        FragmentManager fm = getFragmentManager();
        addProduct dialogFragment = new addProduct();
        dialogFragment.show(fm, "addProduct");
    }

    public void addCustomerProduct(View view) {
        FragmentManager fm = getFragmentManager();
        addCustomerProducts dialogFragment = new addCustomerProducts();
        dialogFragment.show(fm, "addCustomerProducts");

    }

    public void addCustomerPrice(View view) {
        FragmentManager fm = getFragmentManager();
        addCustomerPrice dialogFragment = new addCustomerPrice();
        dialogFragment.show(fm, "addCustomerPrice");
    }

    public void addCostItem(View view) {
        FragmentManager fm = getFragmentManager();
        addCostItem dialogFragment = new addCostItem();
        dialogFragment.show(fm, "addCostItem");
    }
}

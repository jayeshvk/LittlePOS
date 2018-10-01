package com.evinyas.jkotekar.littlepos;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.evinyas.jkotekar.littlepos.model.Customer;
import com.evinyas.jkotekar.littlepos.model.UHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jkotekar on 4/30/2016.
 * Customer Master Dialog interface
 */
@SuppressLint("NewApi")
public class addCustomer extends DialogFragment {

    private final String NAME = "name";
    private final String PHONE = "phone";
    private final String ID = "id";
    ImageButton add;
    ImageButton modify;
    ImageButton delete;
    ImageButton reset;
    ImageButton close;
    private EditText customerName;
    private EditText customerPhone;
    private EditText cid;
    private SimpleAdapter adapter;
    private ArrayList<HashMap<String, String>> feedList;
    private ListView listView;

    private DatabaseHelper dbh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_customer, container, false);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setTitle("Customer Entry");

        listView = (ListView) rootView.findViewById(R.id.listViewx);
        listView.setFastScrollAlwaysVisible(true);

        add = (ImageButton) rootView.findViewById(R.id.button_add);
        modify = (ImageButton) rootView.findViewById(R.id.button_modify);
        delete = (ImageButton) rootView.findViewById(R.id.button_delete);
        reset = (ImageButton) rootView.findViewById(R.id.button_reset);
        close = (ImageButton) rootView.findViewById(R.id.button_close);


        customerName = (EditText) rootView.findViewById(R.id.customerName);
        customerPhone = (EditText) rootView.findViewById(R.id.customerPhone);
        cid = (EditText) rootView.findViewById(R.id.editTextId);
        cid.setEnabled(false);

        feedList = new ArrayList<>();

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dbh = DatabaseHelper.getInstance(getActivity());
        loadCustomer();

        adapter = new SimpleAdapter(getActivity(), feedList, R.layout.view_item, new String[]{NAME, PHONE, ID}, new int[]{R.id.textViewCName, R.id.textViewCPhone, R.id.textViewCID});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // ListView Clicked item value
                //String itemValue = (String) listViewtemp.getItemAtPosition(position);

                HashMap<String, String> itemValue = (HashMap<String, String>) listView.getItemAtPosition(position);
                customerName.setText(itemValue.get(NAME));
                customerPhone.setText(itemValue.get(PHONE));
                cid.setText(itemValue.get(ID));
                add.setClickable(false);
            }

        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //error if name is blank
                if (customerName.getText().toString().length() == 0) {
                    customerName.setError("Name is required!");
                    return;
                }
                //get details of customer and add to database
                Customer cust = new Customer(customerName.getText().toString(), customerPhone.getText().toString());
                Long cusID = dbh.addCustomer(cust);
                toast("Customer " + customerName.getText().toString() + " Added");
                Admin.DBRESET = false;
                HashMap<String, String> map = new HashMap<>();
                map.put(NAME, customerName.getText().toString());
                map.put(PHONE, customerPhone.getText().toString());
                map.put(ID, String.valueOf(cusID));
                feedList.add(map);

                resetItems();
                adapter.notifyDataSetChanged();
            }
        });

        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customerName.getText().toString().length() == 0) {
                    customerName.setError("Name is required!");
                    return;
                }
                if (cid.getText().toString().length() == 0) {
                    return;
                }
                int id = UHelper.parseInt(cid.getText().toString());
                Customer cus = new Customer(
                        id,
                        customerName.getText().toString(),
                        customerPhone.getText().toString());

                dbh.updateCustomer(cus);
                toast("Customer Modified to " + customerName.getText().toString());
                loadCustomer();
                adapter.notifyDataSetChanged();

                resetItems();
                add.setClickable(true);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customerName.getText().toString().length() == 0) {
                    customerName.setError("Name is required!");
                    return;
                }
                int id = UHelper.parseInt(cid.getText().toString());
                dbh.deleteCustomer(id);
                toast("Customer " + customerName.getText().toString() + " Deleted");
                loadCustomer();
                adapter.notifyDataSetChanged();
                resetItems();
                add.setClickable(true);
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetItems();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(addCustomer.this).commit();
            }
        });
    }

    private void loadCustomer() {
        feedList.clear();
        List<Customer> customer = dbh.getAllCustomers();
        for (Customer cust : customer) {
            HashMap<String, String> map = new HashMap<>();
            map.put(NAME, cust.getCustomerName());
            map.put(PHONE, cust.getCustomerPhone());
            map.put(ID, String.valueOf(cust.getId()));
            feedList.add(map);
        }
    }

    private void resetItems() {
        customerName.setText(null);
        customerPhone.setText(null);
        cid.setText(null);
        add.setClickable(true);
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}

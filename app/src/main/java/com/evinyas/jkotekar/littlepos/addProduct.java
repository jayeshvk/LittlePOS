package com.evinyas.jkotekar.littlepos;

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

import com.evinyas.jkotekar.littlepos.model.Product;
import com.evinyas.jkotekar.littlepos.model.UHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jkotekar on 4/30/2016.
 * Product Maser all function
 */
public class addProduct extends DialogFragment {

    private final String NAME = "name";
    private final String DPRICE = "dprice";
    private final String ID = "id";
    private ImageButton add;
    private ImageButton modify;
    private ImageButton delete;
    private ImageButton reset;
    private ImageButton close;
    private EditText productName;
    private EditText productDPrice;
    private EditText productId;
    private ListView listView;
    private SimpleAdapter adapter;
    private DatabaseHelper dbh;
    private ArrayList<HashMap<String, String>> feedList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_product, container, false);
        getDialog().setCanceledOnTouchOutside(false);

        getDialog().setTitle("Product Entry");

        listView = (ListView) rootView.findViewById(R.id.listViewy);
        listView.setFastScrollAlwaysVisible(true);

        add = (ImageButton) rootView.findViewById(R.id.button_padd);
        reset = (ImageButton) rootView.findViewById(R.id.button_preset);
        modify = (ImageButton) rootView.findViewById(R.id.button_pmodify);
        delete = (ImageButton) rootView.findViewById(R.id.button_pdelete);
        close = (ImageButton) rootView.findViewById(R.id.button_pclose);

        productName = (EditText) rootView.findViewById(R.id.productName);
        productDPrice = (EditText) rootView.findViewById(R.id.defaultPrice);
        productId = (EditText) rootView.findViewById(R.id.editTextPID);
        productId.setEnabled(false);

        feedList = new ArrayList<>();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dbh = DatabaseHelper.getInstance(getActivity());

        loadProduct();

        adapter = new SimpleAdapter(getActivity(), feedList, R.layout.view_item, new String[]{NAME, DPRICE, ID}, new int[]{R.id.textViewCName, R.id.textViewCPhone, R.id.textViewCID});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item value
                //String itemValue = (String) listViewtemp.getItemAtPosition(position);

                HashMap<String, String> itemValue = (HashMap<String, String>) listView.getItemAtPosition(position);

                productName.setText(itemValue.get(NAME));
                productDPrice.setText(itemValue.get(DPRICE));
                productId.setText(itemValue.get(ID));
                add.setClickable(false);

            }

        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //error if name is blank
                if (productName.getText().toString().length() == 0) {
                    productName.setError("Name is required!");
                    return;
                }

                //get details of customer and add to database
                Product prdt = new Product(productName.getText().toString(), productDPrice.getText().toString());
                Long prdtID = dbh.addProduct(prdt);
                toast("Product " + productName.getText().toString() + " Added");
                Admin.DBRESET = false;
                HashMap<String, String> map = new HashMap<>();
                map.put(NAME, productName.getText().toString());
                map.put(DPRICE, productDPrice.getText().toString());
                map.put(ID, String.valueOf(prdtID));
                feedList.add(map);

                resetItems();
                adapter.notifyDataSetChanged();

            }
        });
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (productName.getText().toString().length() == 0) {
                    productName.setError("Name is required!");
                    return;
                }
                if (productId.getText().toString().length() == 0) {
                    return;
                }
                int id = UHelper.parseInt(productId.getText().toString());
                Product prdt = new Product(
                        id,
                        productName.getText().toString(),
                        productDPrice.getText().toString());

                dbh.updateProduct(prdt);
                toast("Product Modified to " + productName.getText().toString());
                loadProduct();
                adapter.notifyDataSetChanged();

                resetItems();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (productName.getText().toString().length() == 0) {
                    productName.setError("Select Customer!");
                    return;
                }
                int id = Integer.parseInt(productId.getText().toString());
                dbh.deleteProduct(id);
                loadProduct();
                adapter.notifyDataSetChanged();
                resetItems();
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
                getActivity().getFragmentManager().beginTransaction().remove(addProduct.this).commit();
            }
        });
    }

    private void loadProduct() {
        feedList.clear();
        List<Product> product = dbh.getAllProducts();
        for (Product prdt : product) {
            HashMap<String, String> map = new HashMap<>();
            map.put(NAME, prdt.getProductName());
            map.put(DPRICE, prdt.getProductDPrice());
            map.put(ID, String.valueOf(prdt.getId()));
            feedList.add(map);
        }
    }

    private void resetItems() {
        productName.setText(null);
        productDPrice.setText(null);
        productId.setText(null);
        add.setClickable(true);
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}

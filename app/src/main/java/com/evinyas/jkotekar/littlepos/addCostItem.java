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


import com.evinyas.jkotekar.littlepos.model.Cost;
import com.evinyas.jkotekar.littlepos.model.UHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jkotekar on 4/30/2016.
 * Product Maser all function
 */
public class addCostItem extends DialogFragment {

    private final String NAME = "name";
    private final String COST = "cost";
    private final String ID = "id";
    private ImageButton add;
    private ImageButton modify;
    private ImageButton delete;
    private ImageButton reset;
    private ImageButton close;
    private EditText costItemName;
    private EditText costItemPrice;
    private EditText costID;
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
        View rootView = inflater.inflate(R.layout.add_cost_item, container, false);
        getDialog().setCanceledOnTouchOutside(false);

        getDialog().setTitle("Cost Item Entry");

        listView = (ListView) rootView.findViewById(R.id.listView);
        listView.setFastScrollAlwaysVisible(true);

        add = (ImageButton) rootView.findViewById(R.id.buttonAdd);
        reset = (ImageButton) rootView.findViewById(R.id.buttonReset);
        modify = (ImageButton) rootView.findViewById(R.id.buttonModify);
        delete = (ImageButton) rootView.findViewById(R.id.buttonDelete);
        close = (ImageButton) rootView.findViewById(R.id.buttonClose);

        costItemName = (EditText) rootView.findViewById(R.id.costName);
        costItemPrice = (EditText) rootView.findViewById(R.id.cost);
        costID = (EditText) rootView.findViewById(R.id.costId);
        costID.setEnabled(false);

        feedList = new ArrayList<>();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dbh = DatabaseHelper.getInstance(getActivity());

        loadProduct();

        adapter = new SimpleAdapter(getActivity(), feedList, R.layout.view_item, new String[]{NAME, COST, ID}, new int[]{R.id.textViewCName, R.id.textViewCPhone, R.id.textViewCID});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item value
                //String itemValue = (String) listViewtemp.getItemAtPosition(position);

                HashMap<String, String> itemValue = (HashMap<String, String>) listView.getItemAtPosition(position);

                costItemName.setText(itemValue.get(NAME));
                costItemPrice.setText(itemValue.get(COST));
                costID.setText(itemValue.get(ID));
                add.setClickable(false);

            }

        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //error if name is blank
                if (costItemName.getText().toString().length() == 0) {
                    costItemName.setError("Description is required!");
                    return;
                }

                //get details of customer and add to database
                Cost cost = new Cost(costItemName.getText().toString(), costItemPrice.getText().toString());
                Long id = dbh.addCostItem(cost);
                toast("Cost Item " + costItemName.getText().toString() + " Added");
                Admin.DBRESET = false;
                HashMap<String, String> map = new HashMap<>();
                map.put(NAME, costItemName.getText().toString());
                map.put(COST, costItemPrice.getText().toString());
                map.put(ID, String.valueOf(id));
                feedList.add(map);

                resetItems();
                adapter.notifyDataSetChanged();

            }
        });
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (costItemName.getText().toString().length() == 0) {
                    costItemName.setError("Select a item!");
                    return;
                }
                if (costID.getText().toString().length() == 0) {
                    return;
                }
                int id = UHelper.parseInt(costID.getText().toString());
                Cost cost = new Cost(
                        id,
                        costItemName.getText().toString(),
                        costItemPrice.getText().toString());

                dbh.updateCostItem(cost);
                toast("Cost Modified to " + costItemName.getText().toString());
                loadProduct();
                adapter.notifyDataSetChanged();

                resetItems();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (costItemName.getText().toString().length() == 0) {
                    costItemName.setError("Select a item!");
                    return;
                }
                int id = Integer.parseInt(costID.getText().toString());
                dbh.deleteCost(id);
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
                getActivity().getFragmentManager().beginTransaction().remove(addCostItem.this).commit();
            }
        });
    }

    private void loadProduct() {
        feedList.clear();
        List<Cost> costs = dbh.getAllCostItem();
        for (Cost cost : costs) {
            HashMap<String, String> map = new HashMap<>();
            map.put(NAME, cost.getCostName());
            map.put(COST, cost.getCost());
            map.put(ID, String.valueOf(cost.getId()));
            feedList.add(map);
        }
    }

    private void resetItems() {
        costItemName.setText(null);
        costItemPrice.setText(null);
        costID.setText(null);
        add.setClickable(true);
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}

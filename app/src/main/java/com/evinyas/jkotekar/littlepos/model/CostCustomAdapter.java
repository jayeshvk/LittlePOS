package com.evinyas.jkotekar.littlepos.model;

/**
 * Created by jkotekar on 6/14/2016.
 * adapter
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.evinyas.jkotekar.littlepos.R;
import com.evinyas.jkotekar.littlepos.ReportItem;
import com.evinyas.jkotekar.littlepos.costEntryQuickReport;

import java.util.List;

/*********
 * Adapter class extends with BaseAdapter and implements with OnClickListener
 ************/
public class CostCustomAdapter extends BaseAdapter implements View.OnClickListener {

    /***********
     * Declare Used Variables
     *********/
    private Activity activity;
    private costEntryQuickReport fragment;
    private List data;
    private static LayoutInflater inflater = null;
    public Resources res;
    private CostData tempValues = null;

    /*************
     * CustomAdapter Constructor
     *****************/
    public CostCustomAdapter(Activity a, List d, Resources resLocal) {

        /********** Take passed values **********/
        activity = a;
        data = d;
        res = resLocal;

        /***********  Layout inflator to call external xml layout () ***********/
        inflater = (LayoutInflater) activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public CostCustomAdapter(Activity a, List d, Resources resLocal, costEntryQuickReport f) {

        /********** Take passed values **********/
        activity = a;
        data = d;
        res = resLocal;
        fragment = f;

        /***********  Layout inflator to call external xml layout () ***********/
        inflater = (LayoutInflater) activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    /********
     * What is the size of Passed Arraylist Size
     ************/
    public int getCount() {

        if (data.size() <= 0)
            return 1;
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /*********
     * Create a holder Class to contain inflated xml file elements
     *********/
    public static class ViewHolder {
        public TextView slno;
        public TextView date;
        public TextView costItem;
        public TextView quantity;
        public TextView cost;
        public TextView amount;
        public TextView received;
    }

    /******
     * Depends upon data size called for each row , Create each ListView row
     *****/
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;
        if (convertView == null) {

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            vi = inflater.inflate(R.layout.custom_adapter_list_item, null, false);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.slno = (TextView) vi.findViewById(R.id.liSlno);
            holder.date = (TextView) vi.findViewById(R.id.liDate);
            holder.costItem = (TextView) vi.findViewById(R.id.liProduct);
            holder.cost = (TextView) vi.findViewById(R.id.liPrice);
            holder.quantity = (TextView) vi.findViewById(R.id.liQty);
            holder.amount = (TextView) vi.findViewById(R.id.liAmount);
            holder.received = (TextView) vi.findViewById(R.id.liReceived);
            /************  Set holder with LayoutInflater ************/
            vi.setTag(holder);
        } else
            holder = (ViewHolder) vi.getTag();

        if (data.size() <= 0) {

            /*holder.slno.setText(null);
            holder.date.setText("Date");
            holder.costItem.setText("Product");
            holder.cost.setText("0.00");
            holder.quantity.setText("00");
            holder.amount.setText("0.00");
            holder.received.setText("0.00");
            holder.received.setText("0.00");
*/
        } else {
            /***** Get each Model object from Arraylist ********/
            tempValues = null;
            tempValues = (CostData) data.get(position);
            vi.setBackgroundColor(Color.parseColor("#ffffff"));
            /************  Set Model values in Holder elements ***********/
            holder.slno.setText(tempValues.getSlno());
            holder.date.setText(UHelper.dateFormatymdhmsTOdmy(tempValues.getDate()));
            holder.costItem.setText(tempValues.getCostName());
            holder.cost.setText(tempValues.getCost());
            holder.quantity.setText(tempValues.getQuantity());
            holder.amount.setText(tempValues.getAmount());

            //if Comments exist insert the note EMoji Next to received field
            if (tempValues.getComments().length() != 0) {
                holder.received.setText(UHelper.getEmijoByUnicode(0x1F4C4));
            } else {
                holder.received.setText("");
            }

            //******** Set Item Click Listner for LayoutInflater for each row *******/
            vi.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.Pink300));
            vi.setOnClickListener(new OnItemClickListener(position));
            vi.setOnLongClickListener(new OnItemLongClickListener(position));
        }

        return vi;
    }

    @Override
    public void onClick(View v) {
        Log.v("CustomAdapter", "=====Row button clicked=====");
    }

    /*********
     * Called when Item click in ListView
     ************/
    private class OnItemClickListener implements View.OnClickListener {
        private int mPosition;

        OnItemClickListener(int position) {
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {
            if (fragment != null)
                fragment.onItemClick(mPosition);
        }
    }

    private class OnItemLongClickListener implements View.OnLongClickListener {
        private int mPosition;

        OnItemLongClickListener(int position) {
            mPosition = position;
        }

        @Override
        public boolean onLongClick(View v) {
            if (fragment != null)
                fragment.OnItemLongClicked(mPosition);
            return false;
        }
    }

    public void updateReceiptsList(List newlist) {
        System.out.println("Updated list received");
        data.clear();
        data.addAll(newlist);
        this.notifyDataSetChanged();
    }


}
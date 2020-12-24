package com.evinyas.jkotekar.littlepos;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.evinyas.jkotekar.littlepos.model.UHelper;

public class Admin_Reset extends AppCompatActivity {

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_reset);

        databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
    }

    public void clearTransactions(View view) {
        UHelper.showAlert(this, "Are you sure you want to clear, this can not be undone!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    if (databaseHelper.resetTransactions() > 0)
                        toast("All Transactios Cleared");
                    else
                        toast("Error clearing transactions data");
                }
            }
        });

    }

    public void clearCustomerPrice(View view) {
        UHelper.showAlert(this, "Are you sure you want to clear, this can not be undone!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    if (databaseHelper.resetCustomerPrice() > 0)
                        toast("Customer Price Cleared");
                    else
                        toast("Error clearing Cusotmer Price Data");
                }
            }
        });
    }

    public void clearCustomerProduct(View view) {
        UHelper.showAlert(this, "Are you sure you want to clear, this can not be undone!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    if (databaseHelper.resetCustomerProduct() > 0)
                        toast("Cusotmer Product Cleared");
                    else
                        toast("Error clearing Cusotmer Product Data");
                }
            }
        });
    }

    public void clearProduct(View view) {
        UHelper.showAlert(this, "Are you sure you want to clear, this can not be undone!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    if (databaseHelper.resetProduct() > 0)
                        toast("Product Master Cleared");
                    else
                        toast("Error clearing Product Master Data");
                }
            }
        });
    }

    public void clearCustomer(View view) {
        UHelper.showAlert(this, "Are you sure you want to clear, this can not be undone!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    if (databaseHelper.resetCustomer() > 0)
                        toast("All customer master data cleared");
                    else
                        toast("Error clearing customer master data");
                }
            }
        });
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}

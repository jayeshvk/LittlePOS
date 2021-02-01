package com.evinyas.jkotekar.littlepos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.multidex.MultiDex;

import com.evinyas.jkotekar.littlepos.model.UHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private Boolean autoBackup = false;
    private boolean isChecked = false;

    private String SHAREDPREFNAME = "LittlePOSPrefs";
    private static final String USERNAME = "username";
    private static final String AUTOBACKUP = "autobackup";

    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultiDex.install(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();

        TextView tvUSer = findViewById(R.id.user);
        tvUSer.setText(user.getEmail());

        if (readSharedPref(USERNAME).length() == 0)
            showHelp();

        //for mashmello check if storgae permisiion exist and ask if not exist
        isStoragePermissionGranted();

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Developed by EVINYAS", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/

        if (readSharedPref(USERNAME).length() == 0) {
            popUpMessage();
        }

        try {
            Admin.DBRESET = Boolean.parseBoolean(readSharedPref("DBRESET"));
        } catch (Exception e) {
            System.out.println("Reset boolean parse" + e);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_autobackup_check);
        item.setChecked(readSharedPref(AUTOBACKUP).contains("true"));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_autobackup_check:
                isChecked = !item.isChecked();
                item.setChecked(isChecked);
                if (isChecked)
                    writetoSharedPref(AUTOBACKUP, "true");
                else writetoSharedPref(AUTOBACKUP, "false");
                return true;
            case R.id.financial_period:
                setFinancialPeriod();
                return true;
            case R.id.action_user_name:
                Toast.makeText(getApplicationContext(), "User Name " + readSharedPref(USERNAME), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_logout:
                logout();
                return true;
            case R.id.instructions:
                showHelp();
                return true;
            case R.id.policy:
                String url = "http://jayesh.kotekar.com/littlepos.htm";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            case R.id.help:
                AlertDialog.Builder about = new AlertDialog.Builder(this);
                about.setTitle("About");
                about.setMessage("Version 1.5.0\nDeveloped by EVINYAS PVT LTD\nFor any issues and queries pleaase contact the developer at jayeshvk@gmail.com");
                AlertDialog aboutAlert = about.create();
                aboutAlert.show();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void setFinancialPeriod() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Start Period")
                .setSingleChoiceItems(R.array.months, UHelper.parseInt(readSharedPref("STARTMONTH")), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        writetoSharedPref("STARTMONTH", which + "");
                    }
                });

        builder.create().show();
    }

    public void sales(View view) {
        startActivity(new Intent(this, POS.class));
    }

    public void reports(View view) {
        startActivity(new Intent(this, Reports.class));
    }

    public void masterdata(View view) {
        startActivity(new Intent(this, MasterData.class));
    }

    public void admin(View view) {
        startActivity(new Intent(this, Admin.class));
    }

    public void cost(View view) {
        startActivity(new Intent(this, costEntry.class));
    }

    protected void popUpMessage() {
/*        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.promptsusername, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // get user input and set it to result
                // edit text
                // result.setText(userInput.getText());
                String text = userInput.getText().toString();
                if (text.length() == 0) {
                    System.exit(0);

                } else {
                    writetoSharedPref("username", text);
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                System.exit(0);
                //dialog.cancel();
            }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();*/

        writetoSharedPref("username", user.getEmail());

    }

    private void writetoSharedPref(String key, String text) {
        SharedPreferences preferences = getSharedPreferences(SHAREDPREFNAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, text);
        editor.apply();
    }

    private String readSharedPref(String KEY) {
        String returnData = null;
        String SHAREDPREFNAME = "LittlePOSPrefs";
        SharedPreferences settings = getSharedPreferences(SHAREDPREFNAME, 0);
        switch (KEY) {
            case USERNAME:
                return settings.getString(USERNAME, "");
            case AUTOBACKUP:
                return settings.getString(AUTOBACKUP, "none");
            case "DBRESET":
                return settings.getString("DBRESET", "true");
            case "STARTMONTH":
                return settings.getString("STARTMONTH", 0 + "");
            default:
                return settings.getString(KEY, null);
        }
    }

    private void showHelp() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.instructions, null);
        TextView tv = (TextView) view.findViewById(R.id.instructionsText);
        tv.setText(Html.fromHtml("<p>The Little POS application is a Point of Sale application to record your business transactions. It’s a very esy to use in few steps. To get started you must first setup the master data , assign products to your cusotmer data and start recording the sales transactions. This app is in the initial stage where separate tax calculation's and unit conversions not yet available. Later in future these features will be implemented.</p>\n" +
                "<p><u><b>Main Menu</b></u></p>\n" +
                "<strong>First Step</strong>\n" +
                "<p>Select the Master data and click on the Customer Master to create your customers or accounts. In this section you also can modify customers or delete customer is no transaction is recorded</p>\n" +
                "<strong>Second Step</strong>\n" +
                "<p>Select the costItem master and add the products you sell. The cost that you set here for the costItem is default selling cost for all the customers. Please also add the tax to the selling cost as there is no separate tax calculations.</p>\n" +
                "<strong>Third Step</strong>\n" +
                "<p>Select the Customer costItem option, select the customer from the dropdown list and tick on the products that you want to sell to your customer and save.\n" +
                "To remove a costItem from customer un check and save.\n" +
                "</p>\n" +
                "<strong>Fourth Step</strong>\n" +
                "<p>If you have different selling cost based on customer, set the customer costItem cost here. If nothing is set default cost will be applicable.</p>\n" +
                "<p>That is all needs to do. Now you can start your sales from the main Menu – Sales Entry option. Here you can select the customer and enter quantity. You will see following labels on screen</p>\n" +
                "<p><b>Gross: </b>This is the Total sales cost of the customer.</p>\n" +
                "<p><b>Net :</b>The calculated cost after discounts.</p>\n" +
                "<p><b>Save option :</b>Saves the transaction in the system.</p>\n" +
                "<p><b>Sales Entry Sales report :</b>Quick sales report for the day, long press on an item to modify or delete the transaction</p>\n" +
                "<p><b>Extras option :</b>: Here you can add payment, discount and surcharges. While adding discount, payment or surcharge there should not be any item quantity entered. If you received any payments or you want to add surcharge, this is where it can be done. Payments and surcharges should be added independently without and sales entry in the Sales Entry screen.</p>\n" +
                "<p><b>Sales option :</b>Quick view of the sales transaction done for today. You can also tap on the date to select the date range. Long press on item for additional information and to modify or delete the transaction and Tap on the item to view the comments.</p>\n" +
                "<p><u><b>Reports</b></u></p>\n" +
                "<p>Reports showing Sales report Item wise, Sales report day wise and Total Business. You can choose the customer, date range to view the report. Tap on the Item to view the comments.</p>\n" +
                "<p><u><b>Admin</b></u></p>\n" +
                "<p>Under admin you have option for Backup and restoring your data on SD card, Gdrve and exporting complete report as Excel file saved in your SD card and last option where you can clear the master data or transaction data at once</p>"));
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Instructions");
        alertDialog.setView(view);
        AlertDialog alert = alertDialog.create();
        alert.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("Reset Status : " + Admin.DBRESET + " Auto Backup Shared Pref" + readSharedPref(AUTOBACKUP) + " Auto backup var " + autoBackup);
        writetoSharedPref("DBRESET", String.valueOf(Admin.DBRESET));
        if (!Admin.DBRESET) {
            if (readSharedPref(AUTOBACKUP).contains("true"))
                startService(new Intent(this, BackupService.class));
            autoBackup = false;
        }

    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(getApplicationContext(), "Permission is granted", Toast.LENGTH_SHORT).show();
                return true;
            } else {
//                Toast.makeText(getApplicationContext(), "Permission is revoked", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            //Toast.makeText(getApplicationContext(), "Permission is granted", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(getApplicationContext(), "Permission: " + permissions[0] + "was " + grantResults[0], Toast.LENGTH_SHORT).show();
            //resume tasks needing this permission
        } else {
            Toast.makeText(getApplicationContext(), "Application Requires Access to Storage for backing up file", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }


    private void logout() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        writetoSharedPref(USERNAME, "");
        startActivity(new Intent(MainActivity.this, Login.class));
    }
}


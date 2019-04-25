package com.evinyas.jkotekar.littlepos;


import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.evinyas.jkotekar.littlepos.model.UHelper;
import com.evinyas.jkotekar.littlepos.model.salesData;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

public class Admin extends GdriveBase {

    private String userName;
    private ProgressDialog pDialog;
    private PopupWindow popupWindow;
    private NotificationCompat.Builder notification;
    private NotificationManager notificationManager;
    private Bitmap largeIcon;
    private DatabaseHelper databaseHelper;
    public static boolean DBRESET;

    private static final int REQUEST_CODE_OPENER = 3;

    private static final String TAG = "ADMIN";
    //private GoogleApiClient mGoogleApiClient;
    private DriveId mFolderDriveId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        databaseHelper = DatabaseHelper.getInstance(this);
        setContentView(R.layout.admin);
        userName = getsharedPref("username");

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.pos);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new NotificationCompat.Builder(getApplicationContext());

        try {
            Admin.DBRESET = Boolean.parseBoolean(readSharedPref("DBRESET"));
        } catch (Exception e) {
            System.out.println("Reset boolean parse" + e);
        }
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");


    }

    public void copyDBtoSD(View view) {
        File sd = Environment.getExternalStorageDirectory().getAbsoluteFile();
        FileChannel source;
        FileChannel destination;
        String currentDBPath = getApplication().getDatabasePath(DatabaseHelper.DATABASE_NAME).getPath();

        System.out.println("User Name : " + userName);
        String backupDBName = userName + UHelper.setPresentDateDDMMYYhhmm() + ".db";
        String backupDBPath; // = "/download/" + backupDBName;

        //create folder if not exist for Backingup data
        File folder = new File(sd + "/LittlePOSBackup");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            toast("Folder for backup in SD Card Created: LittlePOSBackup");
            backupDBPath = "/LittlePOSBackup/" + backupDBName;
            // Do something on success
        } else {
            toast("Unable to create Backup folder, DOWNLOAD folder will be used");
            backupDBPath = "/download/" + backupDBName;
            // Do something else on failure
        }

        File currentDB = new File(currentDBPath);
        File backupDB = new File(sd, backupDBPath);

        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            //Copy files
            destination.transferFrom(source, 0, source.size());
            //close Files
            source.close();
            destination.close();
            Toast.makeText(this, "File backed up to " + backupDBPath, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Backup failed, try again", Toast.LENGTH_LONG).show();
        }
    }

    public void restoreDBfromSD(View view) {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"db"};

        FilePickerDialog dialog = new FilePickerDialog(Admin.this, properties);

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                onSelectRestoreDBfromSD(files[0]);
            }

        });
        dialog.show();
    }

    private void onSelectRestoreDBfromSD(String selectedFilename) {

        String currentDBPath = getApplication().getDatabasePath(DatabaseHelper.DATABASE_NAME).getPath();

        File sdCardDBPath = new File(selectedFilename);
        File currentDB = new File(currentDBPath);
        try {
            InputStream myInput = new FileInputStream(sdCardDBPath);
            OutputStream myOutput = new FileOutputStream(currentDB);

            byte[] buffer = new byte[1024];
            int length;
            showpDialog();
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();
            hidepDialog();
            toast("Database Restored Successfully.");
        } catch (IOException e) {
            toast("Error Restoring from SD Card.");
            e.printStackTrace();
        }
    }

    public void attachDBtoEmail(View view) {

        //attached to gmail using Uri.parse, using this function gmail will accept file read from external cache
        FileChannel source;
        FileChannel tempCacheDest;

        String currentDBPath = getApplication().getDatabasePath(DatabaseHelper.DATABASE_NAME).getPath();

        File currentDB = new File(currentDBPath);
        File tempFile;

        try {
            tempFile = File.createTempFile(userName + UHelper.setPresentDateDDMMYYhhmm(), ".db", getApplicationContext().getExternalCacheDir());
            source = new FileInputStream(currentDB).getChannel();
            tempCacheDest = new FileOutputStream(tempFile).getChannel();
            //Copy files
            tempCacheDest.transferFrom(source, 0, source.size());
            //close Files
            source.close();
            tempCacheDest.close();
            Toast.makeText(this, "File attaching as email, select your email app", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            tempFile = null;
            e.printStackTrace();
            Toast.makeText(this, "Unable to get the database file", Toast.LENGTH_LONG).show();

        }
        if (tempFile != null) {
            Uri uri = Uri.fromFile(tempFile);
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822"); // use from live device
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{"enter email address"});
            i.putExtra(Intent.EXTRA_SUBJECT, "Sqlite Backup Email");
            i.putExtra(Intent.EXTRA_TEXT, "Do Not reply");
            i.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(i);
        }
    }

    public void resetDB(View view) {
        startActivity(new Intent(this, Admin_Reset.class));
    }

    public void exporttoExcel(View view) {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.DIR_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

        FilePickerDialog dialog = new FilePickerDialog(Admin.this, properties);

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                //files is the array of the paths of files selected by the Application User.
                if (files.length > 0)
                    new exportTOexcel().execute(files);
            }

        });
        dialog.show();

    }

    public void backupToGdrive(View view) {
        if (isInternetAvailable()) {
            if (getGoogleApiClient().isConnected()) {
                String driveid = getsharedPref("driveid");
                if (driveid == null) {
                    toast("Please try again,Connecting to google drive");
                    return;
                }
                //get drive id from saved preference
                DriveId id = DriveId.decodeFromString(getsharedPref("driveid"));
                DriveFolder folder = id.asDriveFolder();

                saveToDrive(folder, userName + "_LPBackup_" + UHelper.setPresentDateyyyyMMddhhmm() + ".db", "application/x-sqlite3");
            } else toast("Not Connected to Google Drive");
        } else
            toast("Internet not available, please connect to Internet");
    }

    public void restoreFromGdrive(View view) {
        pickFileFromGdrive();
    }

    public void changeGdriveAccount(View view) {
        if (isInternetAvailable()) {
            if (getGoogleApiClient().isConnected()) {
                Log.d(TAG, "Logged In");
                getGoogleApiClient().clearDefaultAccountAndReconnect();
                toast("Changing Google drive linked account");
            } else
                toast("Not Connected to Google Drive");
        } else
            toast("Internet not available, please connect to Internet");
    }

    class exportTOexcel extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            showpDialog();
        }

        @Override
        protected Void doInBackground(String... params) {
            String dir = params[0];

            List<salesData> salesdata = databaseHelper.getSalesReportbyDate(0, "1900-01-01", UHelper.setPresentDateyyyyMMdd());

            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet newSheet = workbook.createSheet("Sheet1");

            // Colum Headings
            String[] columNames = {"DATE", "CUSTOMER NAME", "PRODUCT", "QUANTITY", "SALES PRICE", "AMOUNT",
                    "PAYMENTS/DISCOUNTS", "COMMENTS"};
            // create columns for headings starting from column 0
            HSSFRow row = newSheet.createRow(0);
            for (int i = 0; i < columNames.length; i++) {
                HSSFCell cells = row.createCell(i);
                cells.setCellValue(columNames[i]);
            }

            // insert data
            for (int i = 0; i < salesdata.size(); i++) {
                HSSFRow datarow = newSheet.createRow(i + 1);

                HSSFCell date = datarow.createCell(0);
                date.setCellValue(UHelper.dateFormatymdhmsTOddmyyyy(salesdata.get(i).getDate()));

                HSSFCell customername = datarow.createCell(1);
                customername.setCellValue(salesdata.get(i).getCustomerName());

                HSSFCell productname = datarow.createCell(2);
                productname.setCellValue(salesdata.get(i).getProductName());

                HSSFCell quantity = datarow.createCell(3);
                quantity.setCellValue(salesdata.get(i).getQuantity());

                HSSFCell salesprice = datarow.createCell(4);
                salesprice.setCellValue(salesdata.get(i).getPrice());

                HSSFCell amount = datarow.createCell(5);
                amount.setCellValue(salesdata.get(i).getAmount());

                HSSFCell payordiscount = datarow.createCell(6);
                payordiscount.setCellValue(salesdata.get(i).getReceived());

                HSSFCell comments = datarow.createCell(7);
                comments.setCellValue(salesdata.get(i).getComments());
            }
            try {
                FileOutputStream fileOut = new FileOutputStream(dir + "/LittlePOSMySales" + UHelper.setPresentDateddMMyyyy() + ".xls");
                workbook.write(fileOut);
                fileOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            hidepDialog();
        }
    }

    //Helper methods
    private boolean isInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    private void showProgressBar(final boolean visibility) {

        runOnUiThread(new Runnable() {
            public void run() {
                if (visibility)
                    showpDialog();
                else hidepDialog();
            }
        });
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private String getsharedPref(String text) {
        String SHAREDPREFNAME = "LittlePOSPrefs";
        SharedPreferences settings = getSharedPreferences(SHAREDPREFNAME, 0);
        return settings.getString(text, null);
    }

    private String readSharedPref(String KEY) {
        String SHAREDPREFNAME = "LittlePOSPrefs";
        SharedPreferences settings = getSharedPreferences(SHAREDPREFNAME, 0);
        return settings.getString(KEY, "true");
    }

    private void setSharedPref(String key, String text) {
        SharedPreferences preferences = getSharedPreferences("LittlePOSPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, text);
        editor.apply();
        editor.commit();
    }
    /////////////////////////////Gdrive Integration

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        toast("Connected to Gdrive Account for Backup and Restore");
        DriveFolder filter = Drive.DriveApi.getRootFolder(getGoogleApiClient());
        Query query = new Query.Builder().addFilter(Filters.and(
                Filters.eq(SearchableField.TRASHED, false),
                Filters.contains(SearchableField.TITLE, "LittlePOSbackup"))).build();

        filter.queryChildren(getGoogleApiClient(), query).setResultCallback(childrenRetrievedCallback);
    }

    ResultCallback<DriveApi.MetadataBufferResult> childrenRetrievedCallback = new
            ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(@NonNull DriveApi.MetadataBufferResult result) {

                    if (!result.getStatus().isSuccess()) {
                        Log.i(TAG, "Problem while retrieving files");
                        return;
                    }
                    Log.i(TAG, "Get Metadata buffer : " + result.getMetadataBuffer().getCount());
                    //if folder does not exist create folder
                    if (result.getMetadataBuffer().getCount() == 0) {
                        //create folder if the folder name with trashed = false is ZERO
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle("LittlePOSbackup").build();
                        Drive.DriveApi.getRootFolder(getGoogleApiClient()).createFolder(getGoogleApiClient(), changeSet).setResultCallback(createFolderCallBack);
                        return;
                    }

                    //If folder exists update in shared Pref
                    mFolderDriveId = result.getMetadataBuffer().get(0).getDriveId();
                    setSharedPref("driveid", mFolderDriveId.encodeToString());
                    result.getMetadataBuffer().release();
                    result.release();
                }
            };
    final ResultCallback<DriveFolder.DriveFolderResult> createFolderCallBack = new ResultCallback<DriveFolder.DriveFolderResult>() {
        //Create folder if not exist results
        @Override
        public void onResult(@NonNull DriveFolder.DriveFolderResult result) {
            if (!result.getStatus().isSuccess()) {
                Toast.makeText(getApplicationContext(), "Error while trying to create Backup folder", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getApplicationContext(), "Created Backup folder LittlePOSbackup in your google drive", Toast.LENGTH_SHORT).show();
            mFolderDriveId = result.getDriveFolder().getDriveId();
            setSharedPref("driveid", mFolderDriveId.encodeToString());
        }
    };

    /******************************************************************
     * create file in GOODrive
     *
     * @param pFldr parent's ID
     * @param titl  file name
     * @param mime  file mime type  (application/x-sqlite3)
     */

    public void saveToDrive(final DriveFolder pFldr, final String titl,
                            final String mime) {
        showProgressBar(true);
        if (getGoogleApiClient() != null && pFldr != null && titl != null && mime != null)
            try {
                // create content from file
                Drive.DriveApi.newDriveContents(getGoogleApiClient()).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                        DriveContents cont = driveContentsResult.getStatus().isSuccess() ?
                                driveContentsResult.getDriveContents() : null;

                        // write file to content, chunk by chunk
                        if (cont != null) try {
                            OutputStream oos = cont.getOutputStream();
                            if (oos != null) try {
                                //InputStream is = getAssets().open("atest.xls");
                                File file = new File(getApplication().getDatabasePath(DatabaseHelper.DATABASE_NAME).getPath());

                                InputStream is = new FileInputStream(file);
                                byte[] buf = new byte[4096];
                                int c;
                                while ((c = is.read(buf, 0, buf.length)) > 0) {
                                    oos.write(buf, 0, c);
                                    oos.flush();
                                    System.out.println("Uploading " + c);
                                }
                            } finally {
                                oos.close();
                            }

                            // content's COOL, create metadata
                            MetadataChangeSet meta = new MetadataChangeSet.Builder().setTitle(titl).setMimeType(mime).build();

                            // now create file on GooDrive
                            pFldr.createFile(getGoogleApiClient(), meta, cont).setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                @Override
                                public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                                    if (driveFileResult.getStatus().isSuccess()) {
                                        // BINGO
                                        Log.i(TAG, "DB file created successfully");
                                        toast("Backup file will be stored in GDrive");
                                    } else {
                                        // report error
                                        Log.i(TAG, "Error backing up file, try again");
                                        toast("Error creating Backup up file, try again");
                                    }
                                }
                            });
                        } catch (Exception e) {
                            toast("Error occured try again " + e);
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                toast("Error " + e);
            }
        showProgressBar(false);
    }

    private void pickFileFromGdrive() {
        if (isInternetAvailable()) {
            if (getGoogleApiClient().isConnected()) {
                String driveid = getsharedPref("driveid");
                if (driveid == null) {
                    toast("Please try again,Connecting to google drive");
                    return;
                }
                DriveId id = DriveId.decodeFromString(getsharedPref("driveid"));
                IntentSender intentSender = Drive.DriveApi
                        .newOpenFileActivityBuilder()
                        .setActivityTitle("Select Backup File")
                        //.setMimeType(new String[]{"application/x-sqlite3"})
                        .setSelectionFilter(Filters.and(Filters.eq(SearchableField.TRASHED, false), Filters.contains(SearchableField.MIME_TYPE, "application/x-sqlite3")))
                        .setActivityStartFolder(id)
                        .build(getGoogleApiClient());
                try {
                    startIntentSenderForResult(
                            intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    Log.w(TAG, "Unable to send intent", e);
                }
            } else {
                toast("Not Connected to Google Drive");
            }
        } else
            toast("Internet not available, please connect to Internet");
    }

    private void downloadFileFromGdrive(DriveId driveID) {
        new RetrieveDriveFileContentsAsyncTask(this).execute(driveID);

    }

    final private class RetrieveDriveFileContentsAsyncTask
            extends ApiClientAsyncTask<DriveId, Boolean, String> {

        RetrieveDriveFileContentsAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected String doInBackgroundConnected(DriveId... params) {
            String contents;
            DriveFile file = params[0].asDriveFile();
            DriveApi.DriveContentsResult driveContentsResult =
                    file.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                return null;
            }
            DriveContents driveContents = driveContentsResult.getDriveContents();
            try {
                InputStream gdriveFile = driveContents.getInputStream();
                OutputStream myOutput = new FileOutputStream(new File(getApplication().getDatabasePath(DatabaseHelper.DATABASE_NAME).getPath()));

                byte[] buffer = new byte[1024];
                int length;
                while ((length = gdriveFile.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.flush();
                myOutput.close();
                contents = "Success";

            } catch (IOException e) {
                contents = null;
                Log.e(TAG, "IOException while reading from the stream", e);
            }
            driveContents.discard(getGoogleApiClient());
            return contents;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                showMessage("Error while reading from the file");
                return;
            }
            showMessage("Restore " + result);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_OPENER:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    downloadFileFromGdrive(driveId);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

}

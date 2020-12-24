package com.evinyas.jkotekar.littlepos;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by jkotekar on 7/9/2016. for services
 */
public class BackupService extends Service {
    NotificationCompat.Builder notification;
    NotificationManager notificationManager;
    int id = 30;
    private static final String USERNAME = "username";
    private static final String AUTOBACKUP = "autobackup";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("Reset - Backup Sgtarted");
        // Let it continue running until it is stopped.
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new NotificationCompat.Builder(getApplicationContext());
        notification.setContentTitle("Backup File to SD Card")
                .setContentText("Backup in progress")
                .setProgress(0, 0, true)
                .setSmallIcon(R.drawable.smallicon)
                .setLargeIcon(largeIcon);
        notificationManager.notify(id, notification.build());
        System.out.println("Reset - Before copyDB");
        copyDBtoSD();
        stopSelf();
        return START_NOT_STICKY;
    }

    public void copyDBtoSD() {

        File sd = Environment.getExternalStorageDirectory().getAbsoluteFile();
        FileChannel source;
        FileChannel destination;

        String currentDBPath = getApplication().getDatabasePath(DatabaseHelper.DATABASE_NAME).getPath();
        String backupDBName = "AutoBackup" + readSharedPref("username") + ".db";
        String backupDBPath;
        //create folder if not exist for Backingup data
        File folder = new File(sd + "/LittlePOSBackup");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            backupDBPath = "/LittlePOSBackup/" + backupDBName;
            // Do something on success
        } else {
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
            notification.setContentText("Backup complete : " + backupDBName).setProgress(0, 0, false);
            notificationManager.notify(id, notification.build());
        } catch (IOException e) {
            e.printStackTrace();
            notification.setContentText("Failed to Backup, try Manual backup").setProgress(0, 0, false);
            notificationManager.notify(id, notification.build());

        }

    }

    private String readSharedPref(String text) {
        String returnData = null;
        String SHAREDPREFNAME = "LittlePOSPrefs";
        SharedPreferences settings = getSharedPreferences(SHAREDPREFNAME, 0);
        switch (text) {
            case USERNAME:
                returnData = settings.getString(USERNAME, null);
                return returnData;
            case AUTOBACKUP:
                returnData = settings.getString(AUTOBACKUP, null);
        }
        return returnData;
    }

}

package com.evinyas.jkotekar.littlepos.model;

public class BackupFile {
    String fileName;
    String key;
    String value;
    String date;

    public BackupFile(String fileName, String date) {
        this.fileName = fileName;
        this.date = date;
    }

    public BackupFile() {
    }

    public BackupFile(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

package com.evinyas.jkotekar.littlepos.model;

/**
 * Created by jkotekar on 6/16/2016.
 *
 */
public class SpinnerItemID {

    private int id;
    private String text;

    public SpinnerItemID(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    //to display object as a string in spinner
    public String toString() {
        return text;
    }

}


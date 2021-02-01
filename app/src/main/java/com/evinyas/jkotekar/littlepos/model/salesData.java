package com.evinyas.jkotekar.littlepos.model;

/**
 * Created by jkotekar on 5/18/2016.
 * Model for Sales line item
 */
public class salesData {

    private String slno;
    private String date;
    private int custID;
    private String customerName;
    private String prodID;
    private String productName;
    private String quantity;
    private String price;
    private String amount;
    private String received;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    private String comments;

    public salesData() {

    }

    public salesData(String date, int custID, String prodID, String quantity, String price, String amount, String received) {
        this.date = date;
        this.custID = custID;
        this.prodID = prodID;
        this.quantity = quantity;
        this.price = price;
        this.amount = amount;
        this.received = received;
    }

    public salesData(int custID, String prodID, String price, String quantity, String amount) {

        this.amount = amount;
        this.custID = custID;
        this.price = price;
        this.prodID = prodID;
        this.quantity = quantity;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getCustID() {
        return custID;
    }

    public void setCustID(int custID) {
        this.custID = custID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getProdID() {
        return prodID;
    }

    public void setProdID(String prodID) {
        this.prodID = prodID;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getReceived() {
        return received;
    }

    public void setReceived(String received) {
        this.received = received;
    }

    public String getSlno() {
        return slno;
    }

    public void setSlno(String slno) {
        this.slno = slno;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}

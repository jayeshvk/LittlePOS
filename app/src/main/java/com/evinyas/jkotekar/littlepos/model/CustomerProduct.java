package com.evinyas.jkotekar.littlepos.model;

/**
 * Created by jkotekar on 5/1/2016.
 */
public class CustomerProduct {
    private int Id;
    private int productID;
    private int customerID;
    private String customerName;
    private String productName;
    private String customerPrice;

    public String getCustomerPrice() {
        return customerPrice;
    }

    public void setCustomerPrice(String customerPrice) {
        this.customerPrice = customerPrice;
    }

    public CustomerProduct() {
    }

    public CustomerProduct(String customerName, String productName) {
        this.customerName = customerName;
        this.productName = productName;
    }
    public CustomerProduct(int customerID,int productID, String customerPrice) {
        this.productID = productID;
        this.customerID = customerID;
        this.customerPrice = customerPrice;
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

    public CustomerProduct(int productID, int customerID) {

        this.productID = productID;
        this.customerID = customerID;
    }

    public int getId() {

        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }
}

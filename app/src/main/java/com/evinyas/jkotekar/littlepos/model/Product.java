package com.evinyas.jkotekar.littlepos.model;

/**
 * Created by jkotekar on 4/29/2016.
 */
public class Product {

    private int id;
    private String productName;
    private String productDPrice;

    public Product(int id, String productName, String productDPrice) {
        this.id = id;
        this.productName = productName;
        this.productDPrice = productDPrice;
    }

    public Product(String productName, String productDPrice) {
        this.productName = productName;
        this.productDPrice = productDPrice;
    }

    public Product() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDPrice() {
        return productDPrice;
    }

    public void setProductDPrice(String productDPrice) {
        this.productDPrice = productDPrice;
    }
}

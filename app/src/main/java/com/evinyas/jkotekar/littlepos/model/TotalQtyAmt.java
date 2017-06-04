package com.evinyas.jkotekar.littlepos.model;

public class TotalQtyAmt {

    private int id;
    private String productName;
    private String totalQuantity;
    private String amount;

    public TotalQtyAmt(int id, String productName, String totalQuantity, String amount) {
        this.id = id;
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.amount = amount;
    }

    public TotalQtyAmt(String productName, String totalQuantity, String amount) {
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.amount = amount;
    }
    public TotalQtyAmt() {

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

    public String getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(String totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}

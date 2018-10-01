package com.evinyas.jkotekar.littlepos.model;

/**
 * Created by jkotekar on 3/16/2017.
 */

public class CostData {


    private String slno;
    private String date;
    private String costId;
    private String quantity;
    private String cost;
    private String amount;
    private String comments;
    private String Data1;

    public String getCostName() {
        return costName;
    }

    public void setCostName(String costName) {
        this.costName = costName;
    }

    private String costName;


    public CostData() {

    }

    public CostData(
            String date,
            String costId,
            String quantity,
            String cost,
            String amount,
            String comments
    ) {
        this.date = date;
        this.costId = costId;
        this.quantity = quantity;
        this.cost = cost;
        this.amount = amount;
        this.comments = comments;

    }

    public String getSlno() {
        return slno;
    }

    public void setSlno(String slno) {
        this.slno = slno;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCostId() {
        return costId;
    }

    public void setCostId(String costId) {
        this.costId = costId;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}

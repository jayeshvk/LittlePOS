package com.evinyas.jkotekar.littlepos.model;

//04/03/2017
public class Cost {

    int id;
    String costName;
    String cost;

    public Cost(int id, String costName, String cost) {
        this.id = id;
        this.costName = costName;
        this.cost = cost;
    }

    public Cost(String costName, String cost) {
        this.costName = costName;
        this.cost = cost;
    }

    public Cost() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCostName() {
        return costName;
    }

    public void setCostName(String costName) {
        this.costName = costName;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }
}

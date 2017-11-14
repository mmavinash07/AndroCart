package com.androcart;

/**
 * Created by Avinash on 10/28/2017.
 */

public class Items {

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getItem_price() {
        return item_price;
    }

    public void setItem_price(String item_price) {
        this.item_price = item_price;
    }

    public int getItem_qty() {
        return item_qty;
    }

    public void setItem_qty(int item_qty) {
        this.item_qty = item_qty;
    }

    public String getItem_total() {
        return item_total;
    }

    public void setItem_total(String item_total) {
        this.item_total = item_total;
    }


    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    private int item_id;
    private int item_qty;
    private String item_total;
    private String item_price;
    private String item_name;
    private String item_category;
    private String item_unit_price;
    private String item_description;

    public String getItem_percent() {
        return item_percent;
    }

    public void setItem_percent(String item_percent) {
        this.item_percent = item_percent;
    }

    private String item_percent;

    public String getItem_unit_price() {
        return item_unit_price;
    }

    public void setItem_unit_price(String item_unit_price) {
        this.item_unit_price = item_unit_price;
    }

    public String getItem_description() {
        return item_description;
    }

    public void setItem_description(String item_description) {
        this.item_description = item_description;
    }

    public String getItem_number() {
        return item_number;
    }

    public void setItem_number(String item_number) {
        this.item_number = item_number;
    }

    private String item_number;


    public String getItem_category() {
        return item_category;
    }

    public void setItem_category(String item_category) {
        this.item_category = item_category;
    }


    public Items(int id,String name,int qty,String price,String category){
            item_qty = qty;
            item_price = price;
            item_name = name;
            item_category = category;
            item_id = id;
    }

    public Items(){

    }
}

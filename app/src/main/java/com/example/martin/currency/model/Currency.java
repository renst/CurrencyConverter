package com.example.martin.currency.model;

import java.io.Serializable;

/**
 * Created by Martin on 2017-11-04.
 */

public class Currency implements Serializable{

    private String currency;
    private double value;


    public Currency(String currency, double value) {
        this.currency = currency;
        this.value = value;
    }

    public double getRate() {
        return value;
    }

    public void setRate(double value) {
        this.value = value;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

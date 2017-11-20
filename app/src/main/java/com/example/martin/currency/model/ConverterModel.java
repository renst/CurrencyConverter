package com.example.martin.currency.model;

import android.content.Context;
import android.util.Log;

import com.example.martin.currency.R;
import com.example.martin.currency.SpinnerItem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Martin on 2017-11-05.
 */

public class ConverterModel implements Serializable {

    private Date dateUpdated;
    private ArrayList<Currency> currencies = new ArrayList<>();
    private  static ConverterModel model;

    public static ConverterModel getInstance(){
        if(model == null)
            model = new ConverterModel();
        return model;
    }

    public synchronized void setCurrencies(ArrayList<Currency> currencies){
        this.currencies = currencies;
    }

    /**
     * Take a string and check if currency exists in list
     * @param currency name
     * @return true if currency exists
     */

    public synchronized Currency getCurrencyFromString(String currency){
        for (Currency c: currencies
             ) {
            if(c.getCurrency() == currency)
                return c;
        }
        return null;
    }

    /**
     * Convert from one currency to another using it's rate
     * @param amount to convert
     * @param fromCurrency
     * @param toCurrency
     * @return converted amount
     */
    public synchronized double convertFromTo(double amount, String fromCurrency, String toCurrency){
        Currency fromCurr= getCurrencyFromString(fromCurrency);
        Currency toCurr = getCurrencyFromString(toCurrency);
        return amount/fromCurr.getRate() * toCurr.getRate();
    }

    /**
     * Takes all currency objects in list and gets their names
     * @return list of names of currencies
     */
    public synchronized ArrayList<String> getCurrenciesNames(){
        ArrayList<String> listOfCurrencies = new ArrayList<>();
        for (Currency c: currencies
             ) {
            listOfCurrencies.add(c.getCurrency());
        }
        return listOfCurrencies;
    }

    public synchronized ArrayList<SpinnerItem> getCurrencyListAsSpinnerItem(){
        ArrayList<SpinnerItem> listOfCurrencies = new ArrayList<>();
        for (Currency c: currencies
                ) {
            //listOfCurrencies.add(new SpinnerItem(c.getCurrency(), R.drawable.eur));
        }
        return listOfCurrencies;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public boolean isUpToDate(){
        return dateUpdated.getTime() > System.currentTimeMillis()-60*60*24*1000;
    }

    public void saveModel(Context context) throws IOException{
        ObjectOutputStream out = new ObjectOutputStream(context.openFileOutput("test8.ser", Context.MODE_PRIVATE));
        out.writeObject(ConverterModel.getInstance());
        out.close();
    }

    public void loadModel(Context fileContext, String fileName) throws IOException, ClassNotFoundException{
        ObjectInputStream in = new ObjectInputStream(fileContext.openFileInput(fileName));
        model = (ConverterModel) in.readObject();
        in.close();
    }
}

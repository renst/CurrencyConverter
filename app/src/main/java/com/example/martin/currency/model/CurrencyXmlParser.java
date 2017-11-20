package com.example.martin.currency.model;

import android.util.Log;
import android.util.Xml;

import com.example.martin.currency.MainActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 2017-11-04.
 * Taken from example in lecture notes
 */

public class CurrencyXmlParser {


    /**
     * Parse through xml document and retrieve currency values
     * @param in
     * @param task
     * @return Currencies
     * @throws XmlPullParserException
     * @throws IOException
     */
    public List parse(InputStream in, MainActivity.UpdateCurrenciesTask task) throws XmlPullParserException, IOException {

        ArrayList<Currency> currencies = new ArrayList<>();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, null);

            int parseEvent = parser.getEventType();
            while (parseEvent != XmlPullParser.END_DOCUMENT) {
                if(task.isCancelled())
                    return currencies;
                switch (parseEvent){
                    case XmlPullParser.START_DOCUMENT:
                        break;

                    case XmlPullParser.START_TAG:
                        Log.d("test", "start");

                        String tagName = parser.getName();
                        if(tagName.equalsIgnoreCase("Cube")){
                            Currency currency = parseItem(parser);
                            if(currency.getCurrency() != null)
                                currencies.add(currency);

                        }
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        return currencies;
                }
                parseEvent = parser.next();
            }
        } finally {
            in.close();
        }
        return currencies;
    }

    /**
     * Parse an items attributes and create a new currency object
     * @param parser
     * @return Currency object
     * @throws IOException
     * @throws XmlPullParserException
     */
    private Currency parseItem(XmlPullParser parser) throws IOException, XmlPullParserException{
        int parseEvent;
        String name, item=" ";
        Currency currency = new Currency();
        do{
            parseEvent = parser.next();
            name = parser.getName();
            if(name== null)
                break;
            if(name.equalsIgnoreCase("Cube")&& parser.getAttributeCount()==2){
               name=parser.getAttributeName(0);
                if(name.equalsIgnoreCase("currency")) {
                    currency.setCurrency(parser.getAttributeValue(null,"currency"));
                }
                name=parser.getAttributeName(1);
                if (name.equalsIgnoreCase("rate")){
                    currency.setRate(Double.parseDouble(parser.getAttributeValue(null, "rate")));
                }
            }
        }while (parseEvent != XmlPullParser.END_TAG || !name.equalsIgnoreCase("Cube"));
        return currency;
    }


}



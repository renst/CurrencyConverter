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
 */

public class CurrencyXmlParser {

    private static final String ns = null;

    public List parse(InputStream in, MainActivity.RetrieveFeedTask task) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser,task);
        } finally {
            in.close();
        }
    }

    private List readFeed(XmlPullParser parser, MainActivity.RetrieveFeedTask task) throws XmlPullParserException, IOException {
        List entries = new ArrayList();
        String currency = null;
        double rate = 0;

        parser.require(XmlPullParser.START_TAG, ns, "gesmes:Envelope");

        while (parser.next() != XmlPullParser.END_TAG) {
            if(task.isCancelled())
                break;
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("Cube")) {
                if (name.equals("Cube") && parser.getAttributeCount() == 2) {
                    currency = parser.getAttributeValue(null, "currency");
                    rate = Double.parseDouble(parser.getAttributeValue(null, "rate"));
                    entries.add(new Currency(currency, rate));
                    skip(parser);
                }
            }else{
                skip(parser);
            }
        }
        return entries;
    }

    //Google example parser
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}

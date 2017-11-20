package com.example.martin.currency;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.martin.currency.model.ConverterModel;
import com.example.martin.currency.model.Currency;
import com.example.martin.currency.model.CurrencyXmlParser;
import com.example.martin.currency.model.GuessModel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner;
    private Spinner toSpinner;
    private EditText inputView;
    private TextView convertedView;
    private static final String URL = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";


    // Given a string representation of a URL, sets up a connection and gets
// an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        java.net.URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    class RetrieveFeedTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                InputStream stream = downloadUrl(strings[0]);
                CurrencyXmlParser  parser = new CurrencyXmlParser();
                ArrayList<Currency> currencies = new ArrayList<>();
                currencies.addAll((ArrayList<Currency>) parser.parse(stream));
                //Since Eur doesnt exist in XML add it
                currencies.add(0,new Currency("EUR",1));
                ConverterModel.getInstance().setCurrencies(currencies);
                return "Success";
            }catch (Exception e) {
                //Get old values here
                Log.d("Test","Error ", e);
                return "Failed";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("Failed")){
                showToast("Failed to download values");
                return;
            }
            showToast("Downloaded values from XML");
            ConverterModel.getInstance().setDateUpdated(new Date());
            saveModel();
            loadCurrenciesToSpinners();
        }
    }

    private void saveModel(){
        try {
            ConverterModel.getInstance().saveModel(this);
        }
        catch (IOException e){
            showToast("Couldn't save data to file!");
        }
    }

    /**
     * Load currencies from converter model,
     * create adapter form values and load spinners
     */
    private void loadCurrenciesToSpinners(){
        //SpinnerAdapter adapter = new SpinnerAdapter(getApplicationContext(), ConverterModel.getInstance().getCurrencyListAsSpinnerItem());
        ArrayList<String> currencies = ConverterModel.getInstance().getCurrenciesNames();
        ArrayList<SpinnerItem> listOfCurrencies = new ArrayList<>();
        for (String c: currencies) {
            int resID = getResources().getIdentifier(c.substring(0,2).toLowerCase(), "drawable", "com.example.martin.currency");
            Log.d("test", c.substring(0,2).toLowerCase());
            listOfCurrencies.add(new SpinnerItem(c, resID));
        }
        SpinnerAdapter adapter = new SpinnerAdapter(getApplicationContext(), listOfCurrencies);
        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fromSpinner = findViewById(R.id.from_spinner);
        toSpinner = findViewById(R.id.to_spinner);
        inputView = findViewById(R.id.input_guess);
        convertedView = findViewById(R.id.converted_result);
        inputView.addTextChangedListener(new SubmitHandler());

        fromSpinner.setOnItemSelectedListener( new SpinnerHandler());
        toSpinner.setOnItemSelectedListener( new SpinnerHandler());


        //If no previous instance existed run as first time opened
        if(savedInstanceState == null) {
            try{
                ConverterModel.getInstance().loadModel(this, "test8.ser");
                if(ConverterModel.getInstance().isUpToDate()) {
                    showToast("Loaded from local files");
                    loadCurrenciesToSpinners();
                }
                else {
                    new RetrieveFeedTask().execute(URL);
                }
            }
            catch (Exception e){
                showToast("Couldn't load values from file");
                new RetrieveFeedTask().execute(URL);
            }

            /*
            if(loadSavedValues()){
                //If more than a day has passed
                if(ConverterModel.getInstance().getDateUpdated().getTime() < System.currentTimeMillis()-60*60*24*1000)
                    new RetrieveFeedTask().execute(URL);
                else {
                    loadCurrencies();
                    showToast("Loaded from local files");
                }
            }
            else {
                new RetrieveFeedTask().execute(URL);
            }
*/
        }
    }

    /**
     * In the event of leaving the app or rotating selected values
     * are stored in a state so it can be restored when app is
     * resumed
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("spinnerAdapter",(SpinnerAdapter)fromSpinner.getAdapter());
        outState.putInt("selectedFrom", fromSpinner.getSelectedItemPosition());
        outState.putInt("selectedTo", toSpinner.getSelectedItemPosition());
    }

    /**
     * When app is resumed restore previous saved view state
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fromSpinner.setAdapter((SpinnerAdapter) savedInstanceState.getSerializable("spinnerAdapter"));
        toSpinner.setAdapter((SpinnerAdapter) savedInstanceState.getSerializable("spinnerAdapter"));
        fromSpinner.setSelection(savedInstanceState.getInt("selectedFrom"));
        toSpinner.setSelection(savedInstanceState.getInt("selectedTo"));
    }

    private  class SpinnerHandler implements Spinner.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            updateConversion();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    /**
     * Gets the values from the view sends them to converter for calculation and then displays it
     */
    private void updateConversion(){
        String input = inputView.getText().toString();
        if(!input.isEmpty()) {
            Double amount = Double.parseDouble(input);
            DecimalFormat numberFormat = new DecimalFormat("#.00");
            try {
                if(toSpinner.getSelectedItem() == null)
                    return;
                Double convertedValue = ConverterModel.getInstance().convertFromTo
                        (amount, ((SpinnerItem)fromSpinner.getSelectedItem()).getText(), ((SpinnerItem)toSpinner.getSelectedItem()).getText());
                convertedView.setText(numberFormat.format(convertedValue));
            }
            catch (Exception e){
                showToast("An error occurred during conversion");
                e.printStackTrace();
            }
        }
        else
            convertedView.setText("");
    }

    private class SubmitHandler implements TextWatcher{
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            updateConversion();
        }
    }



    private void showToast(String msg){
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0,0);
        toast.show();

    }

}

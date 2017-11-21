package com.example.martin.currency;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.martin.currency.model.ConverterModel;
import com.example.martin.currency.model.Currency;
import com.example.martin.currency.model.CurrencyXmlParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner;
    private Spinner toSpinner;
    private EditText inputView;
    private TextView convertedView;

    public void btnSettings_onClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    public class UpdateCurrenciesTask extends AsyncTask<String, Void, String> {

        private Context context;

        public UpdateCurrenciesTask (Context context){
            this.context = context;
        }
        /**
         * Given a string representation of a URL,
         * sets up a connection and parse data
         * @param URL
         * @return  list of currencies parsed
         * @throws IOException
         */
        private ArrayList<Currency> updateCurrencies(String URL) throws IOException {
            URL url = new URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream stream = conn.getInputStream();
            CurrencyXmlParser  parser = new CurrencyXmlParser();
            ArrayList<Currency> currencies = new ArrayList<>();
            //Since EUR doesnt exist in XML add it
            currencies.add(0,new Currency("EUR",1));
            try {
                currencies.addAll((ArrayList<Currency>) parser.parse(stream, this ));
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return currencies;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                ConverterModel.getInstance().setCurrencies(updateCurrencies(getString(R.string.URL)));
                Log.d("test", ConverterModel.getInstance().getCurrenciesNames().toString());

                if(isCancelled())
                    return "Cancelled";

                return "Success";
            }catch (Exception e) {
                return "Failed";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            switch (result) {
                case "Failed":
                    showToast("Failed to download values");
                    return;
                case "Success":
                    showToast("Downloaded values from XML");
                    ConverterModel.getInstance().setDateUpdated(new Date());
                    try {
                        ConverterModel.getInstance().saveModel(context, getString(R.string.localFileName));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    loadCurrenciesToSpinners();
                    break;
                case "Cancelled":
                    showToast("Download cancelled");
                    return;
            }

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
            int resID = getResources().getIdentifier(c.substring(0,2).toLowerCase(), "drawable", getString(R.string.projectName));
            listOfCurrencies.add(new SpinnerItem(c, resID));
        }
        SpinnerAdapter adapter = new SpinnerAdapter(getApplicationContext(), listOfCurrencies);
        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);
    }

    private UpdateCurrenciesTask getDataTask = new UpdateCurrenciesTask(this) ;

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
            initializeConverter();
        }
    }


    /**
     * Load in currencies either from local file or XML
     * depending on internet connection and access to local file
     */
    private void initializeConverter(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Double syncTime = Double.parseDouble(sharedPreferences.getString("sync_frequency", "xxx"));
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        try{
            ConverterModel.getInstance().loadModel(this, getString(R.string.localFileName));
            if(connManager.getActiveNetworkInfo() == null) {
                showToast("No internet connection using outdated data");
                loadCurrenciesToSpinners();
                return;
            }
            if (wifi.isConnected())
                syncTime = syncTime* 10*1000;
            else
                syncTime = syncTime*60*1000;

            if (!ConverterModel.getInstance().isUpToDate(syncTime)) {
                getDataTask.execute();
                return;
            }
            showToast("Loaded values from file");
            loadCurrenciesToSpinners();
        }
        catch (Exception e){
            if(connManager.getActiveNetworkInfo() == null)
                showToast("No internet and no backup!");
            else
                getDataTask.execute();
        }
    }

    /**
     * When user leaves activity set cancel flag in background task to true
     */
    @Override
    protected void onPause() {
        super.onPause();
        getDataTask.cancel(true);
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
     * When app is resumed restore previous saved instance state
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
            Double amount;
            try {
                amount = Double.parseDouble(input);
            }
            catch (Exception e){
                showToast("Only numbers allowed!");
                inputView.setText("");
                return;
            }
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

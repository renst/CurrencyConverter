package com.example.martin.currency;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 2017-11-20.
 */

public class SpinnerAdapter extends BaseAdapter implements Serializable{
    int id;
    Context context;
    ArrayList<SpinnerItem> list;
    LayoutInflater inflater;

    public SpinnerAdapter( Context context,  ArrayList<SpinnerItem> list) {
        this.context = context;
        this.list = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    /**
     * Create a custom spinner view by filling custom_spinner.xml
     */
    public View getView(int pos, View convertView, ViewGroup viewGroup){
        View view = inflater.inflate(R.layout.custom_spinner, null);
        ImageView icon = (ImageView) view.findViewById(R.id.img);
        TextView textView = (TextView) view.findViewById(R.id.txt);

        icon.setImageResource(list.get(pos).getImageId());
        textView.setText(list.get(pos).getText());
        return view;
    }

}

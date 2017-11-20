package com.example.martin.currency;

import java.io.Serializable;

/**
 * Created by Martin on 2017-11-20.
 */

public class SpinnerItem implements Serializable{
    String text;
    int imageId;


    public SpinnerItem(String text, int imageId) {
        this.text = text;
        this.imageId = imageId;
    }

    public String getText() {
        return text;
    }

    public int getImageId() {
        return imageId;
    }


}

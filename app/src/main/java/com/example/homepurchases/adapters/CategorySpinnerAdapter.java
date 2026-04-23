package com.example.homepurchases.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

public class CategorySpinnerAdapter extends ArrayAdapter<String> {

    public CategorySpinnerAdapter(Context context, List<String> categoryNames) {
        super(context, android.R.layout.simple_spinner_item, categoryNames);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
}

package com.androcart;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Avinash on 10/30/2017.
 */

public class CategoryAdapter extends ArrayAdapter<CategoryCount> {

    private ArrayList<CategoryCount> dataSet;
    Context mContext;
    LayoutInflater inflater;

    private static class ViewHolder {
        ProgressBar progress;
        TextView txtCategory;
    }


    public CategoryAdapter(ArrayList<CategoryCount> data, Context context){
        super(context, R.layout.layout_list_cat_item, data);
        this.dataSet = data;
        this.mContext=context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        CategoryCount itemsModel = getItem(position);
        CategoryAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new CategoryAdapter.ViewHolder();
            convertView = inflater.inflate(R.layout.layout_list_cat_item, parent, false);
            viewHolder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
            viewHolder.txtCategory = (TextView) convertView.findViewById(R.id.txtCategory);
            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CategoryAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }

        Random rand = new Random();
        int r = rand.nextInt(255);
        int g = rand.nextInt(255);
        int b = rand.nextInt(255);
        int randomColor = Color.rgb(r,g,b);
        viewHolder.progress.setProgressTintList(ColorStateList.valueOf(randomColor));
        viewHolder.progress.setMax(itemsModel.getTotal());
        viewHolder.progress.setProgress((int)(Double.parseDouble(itemsModel.getCount())));
        viewHolder.txtCategory.setText(itemsModel.getCategory() + " - " + (int)(Double.parseDouble(itemsModel.getCount())));
        return result;
    }

}

package com.androcart;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Avinash on 10/28/2017.
 */

public class ItemAdapter extends ArrayAdapter<Items> implements View.OnClickListener {

    private ArrayList<Items> dataSet;
    Context mContext;
    LayoutInflater inflater;

    @Override
    public void onClick(View view) {
        int position=(Integer) view.getTag();
        Object object= getItem(position);
        Items item=(Items) object;

        switch (view.getId())
        {
            case R.id.btnremove:
                Items delItem = dataSet.get(position);
                if(delItem.getItem_qty() == 1) {
                    dataSet.remove(position);
                    notifyDataSetChanged();
                    FragmentManager fm = ((Activity) mContext).getFragmentManager();
                    Fragment fragment = fm.findFragmentById(R.id.fragment_cart);
                    if(fragment != null){
                        ((Fragment_Items)fragment).deleteItem(delItem,2);
                    }
                }
                else{
                    delItem.setItem_qty(delItem.getItem_qty() -1);
                    dataSet.set(position,delItem);
                    notifyDataSetChanged();
                    FragmentManager fm = ((Activity) mContext).getFragmentManager();
                    Fragment fragment = fm.findFragmentById(R.id.fragment_cart);
                    if(fragment != null){
                        ((Fragment_Items)fragment).updateQuantity(delItem,2);
                    }
                }

                break;
        }
    }

    private static class ViewHolder {
        TextView txtItem;
        TextView txtQty;
        TextView txtPrice;
        TextView txtTotal;
        ImageButton btnRemove;
    }


    public ItemAdapter(ArrayList<Items> data, Context context) {
        super(context, R.layout.layout_list_item, data);
        this.dataSet = data;
        this.mContext=context;
        inflater = LayoutInflater.from(context);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Items itemsModel = getItem(position);
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.layout_list_item, parent, false);
            viewHolder.txtItem = (TextView) convertView.findViewById(R.id.txtitem);
            viewHolder.txtQty = (TextView) convertView.findViewById(R.id.txtqty);
            viewHolder.txtPrice = (TextView) convertView.findViewById(R.id.txtprice);
            viewHolder.txtTotal = (TextView) convertView.findViewById(R.id.txttotal);
            viewHolder.btnRemove = (ImageButton) convertView.findViewById(R.id.btnremove);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.txtItem.setText(itemsModel.getItem_name());
        viewHolder.txtQty.setText(Integer.toString(itemsModel.getItem_qty()));
        viewHolder.txtPrice.setText(itemsModel.getItem_unit_price());
        viewHolder.txtTotal.setText(itemsModel.getItem_total());
        viewHolder.btnRemove.setOnClickListener(this);
        viewHolder.btnRemove.setTag(position);
        return result;
    }

}

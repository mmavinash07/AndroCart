package com.androcart;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Avinash on 10/30/2017.
 */

public class Fragment_Analytics extends Fragment {

    private TextView txttax;
    private TextView txttotalitems;
    private TextView txtnettotal;
    private double nettotal = 0.00;
    private double nettax = 0.00;
    private int netitems = 0;
    private ListView listView;
    private CategoryAdapter adapter;
    private ArrayList<CategoryCount> itemModels;



    public Fragment_Analytics() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Fragment_Analytics newInstance() {
        Fragment_Analytics fragment = new Fragment_Analytics();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);
        txttax = view.findViewById(R.id.txttax);
        txttotalitems = view.findViewById(R.id.txttotalitems);
        txtnettotal = view.findViewById(R.id.txtnettotal);
        listView=(ListView)view.findViewById(R.id.listcategory);
        itemModels= new ArrayList<>();
        adapter= new CategoryAdapter(itemModels,getActivity().getApplicationContext());
        listView.setAdapter(adapter);
        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void SetTotal(Items item,int action){
        switch (action){
            case 1:
                nettotal +=  Double.valueOf(item.getItem_unit_price());
                nettax += Double.valueOf(item.getItem_unit_price())*((Double.valueOf(item.getItem_percent()))/100.000);
                netitems += 1;
                break;
            case 2:
                nettotal -=  Double.valueOf(item.getItem_unit_price());
                nettax -= Double.valueOf(item.getItem_unit_price())*((Double.valueOf(item.getItem_percent()))/100.000);
                netitems -= 1;
                break;
        }
        txtnettotal.setText(Double.toString(nettotal));
        txttax.setText(Double.toString(nettax));
        txttotalitems.setText(Integer.toString(netitems));
    }

    public void UpdateCategory(ArrayList<CategoryCount> cat){
        itemModels.clear();
        itemModels.addAll(cat);
        adapter.notifyDataSetChanged();
    }

    public String getTotal(){
        return Double.toString(nettotal + nettax);
    }


}

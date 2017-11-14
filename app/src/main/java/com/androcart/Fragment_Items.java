package com.androcart;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.internal.zzagz.runOnUiThread;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Items.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Items#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Items extends Fragment {

    private ArrayList<Items> itemModels;
    private ArrayList<String> itemsAdded;
    private ListView listView;
    private ItemAdapter adapter;
    private GetItem getItemTask;
    private String sales_id;
    private int line_no = 0;
    private UpdatequantityTask updatequantityTask;
    private DeleteItemTask deleteItemTask;
    private CreateSaleItem createSaleItem;
    private GetCategoryTask getCategoryTask;

    private OnFragmentInteractionListener mListener;

    public Fragment_Items() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Fragment_Items newInstance() {
        Fragment_Items fragment = new Fragment_Items();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        listView=(ListView)view.findViewById(R.id.list);

        itemModels= new ArrayList<>();
        itemsAdded = new ArrayList<>();
        adapter= new ItemAdapter(itemModels,getActivity());

        listView.setAdapter(adapter);
        TextView empty = (TextView)view.findViewById(android.R.id.empty);
        empty.setText("No Items Added..");
        listView.setEmptyView(empty);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
   /* public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if(getItemTask != null)
            getItemTask.cancel(true);
        if(deleteItemTask != null)
            deleteItemTask.cancel(true);
        if(createSaleItem != null)
            createSaleItem.cancel(true);
        if(updatequantityTask != null)
            updatequantityTask.cancel(true);
        if(getCategoryTask != null)
            getCategoryTask.cancel(true);
    }

    /*@Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Items item, int action);
        void onCategoryUpdated(ArrayList<CategoryCount> count);
    }

    public void addItem(final String serialNumber){
        if(itemsAdded.contains(serialNumber)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    int index = itemsAdded.indexOf(serialNumber);
                    Items item = itemModels.get(index);
                    item.setItem_qty(item.getItem_qty() + 1);
                    itemModels.set(index, item);
                    adapter.notifyDataSetChanged();
                    mListener.onFragmentInteraction(item, 1);
                    updatequantityTask = new UpdatequantityTask();
                    updatequantityTask.execute(item);
                }

                ;
            });
          }
        else {
            getItemTask = new GetItem();
            getItemTask.execute(serialNumber);
            line_no++;
        }
    }

    public void updateQuantity(Items item,int action ){
        mListener.onFragmentInteraction(item,action);
        updatequantityTask = new UpdatequantityTask();
        updatequantityTask.execute(item);
    }

    public void deleteItem(Items item,int action){
        itemsAdded.remove(item.getItem_number());
        mListener.onFragmentInteraction(item,action);
        deleteItemTask = new DeleteItemTask();
        deleteItemTask.execute(item);
    }

    public void getSaleId(String id){
        sales_id = id;
    }

    private class GetItem extends AsyncTask<String, Void, Items> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Items doInBackground(String... arg) {
            BufferedReader reader = null;
            InputStream content = null;
            Items item = null;
            StringBuilder builder = new StringBuilder();
            try {
                URL url = new URL("http://192.168.1.3:9000/pos/get_item/"+arg[0]);
                //URL url = new URL("http://192.168.1.3:9000/pos");
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                connection.setRequestProperty("Accept","*/*");
                connection.setRequestProperty("content-type","application/json");
                //connection.setDoOutput(true);
                content = new BufferedInputStream(connection.getInputStream());
                reader = new BufferedReader(new InputStreamReader(content,"UTF-8"));
                String line;
                while((line = reader.readLine()) != null){
                    builder.append(line);
                }
                item = new Items();
                JSONObject jsonObject = new JSONObject(builder.toString());
                item.setItem_name(jsonObject.getString("item_name"));
                item.setItem_unit_price(jsonObject.getString("item_unit_price"));
                item.setItem_category(jsonObject.getString("item_category"));
                item.setItem_number(jsonObject.getString("item_number"));
                item.setItem_description(jsonObject.getString("item_description"));
                item.setItem_price(jsonObject.getString("item_price"));
                item.setItem_id(jsonObject.getInt("item_id"));
                item.setItem_qty(1);
                item.setItem_total(jsonObject.getString("item_unit_price"));
                item.setItem_percent(jsonObject.getString("item_percent"));
                itemsAdded.add(jsonObject.getString("item_number"));
            }catch (MalformedURLException e){
                Log.d(TAG, "URL exception");

            }catch (IOException ex){
                Log.d(TAG, "IOException");

            }catch (JSONException ex){
                Log.d(TAG, "JSONexception");

            }
            return item;
        }
        @Override
        protected void onPostExecute(Items result) {
            super.onPostExecute(result);
            if(result != null){
                itemModels.add(result);
                adapter.notifyDataSetChanged();
                mListener.onFragmentInteraction(result,1);
                createSaleItem = new CreateSaleItem();
                createSaleItem.execute(result);
            }
        }
    }

    private class CreateSaleItem extends AsyncTask<Items, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Items... arg) {
            String sale_id = "-1";
            BufferedReader reader = null;
            InputStream content = null;
            Items item = null;
            StringBuilder builder = new StringBuilder();
            try {
                String urlParameters  = "saleId="+sales_id+"&itemId="+arg[0].getItem_id()+"&serialNumber="+arg[0].getItem_number()+"&quantity=1&cost_price="+arg[0].getItem_price()+"&unit_price="+arg[0].getItem_unit_price()+"&line="+line_no;

                URL url = new URL("http://192.168.1.3:9000/pos/create_sale_item");
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod( "POST" );
                connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                connection.setRequestProperty("Accept","*/*");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "utf-8");
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(urlParameters);
                writer.flush();
                writer.close();
                os.close();

                content = new BufferedInputStream(connection.getInputStream());
                reader = new BufferedReader(new InputStreamReader(content,"UTF-8"));
                String line;
                while((line = reader.readLine()) != null){
                    builder.append(line);
                }
                JSONObject jsonObject = new JSONObject(builder.toString());
                sale_id =jsonObject.getString("sale_id");

            }catch (MalformedURLException e){
                Log.d(TAG, "URL exception");

            }catch (IOException ex){
                Log.d(TAG, "IOException");

            }catch (JSONException ex){
                Log.d(TAG, "JSONexception");

            }
            return sale_id;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result != "-1"){
                getCategoryTask = new GetCategoryTask();
                getCategoryTask.execute();
            }
        }
    }

    private class UpdatequantityTask extends AsyncTask<Items, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Items... arg) {
            String sale_id = "-1";
            BufferedReader reader = null;
            InputStream content = null;
            Items item = null;
            StringBuilder builder = new StringBuilder();
            try {
                URL url = new URL("http://192.168.1.3:9000/pos/update_quantity/"+sales_id+"/"+arg[0].getItem_id()+"/"+arg[0].getItem_qty());
                HttpURLConnection  connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod( "POST" );
                connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                connection.setRequestProperty("Accept","*/*");
                content = new BufferedInputStream(connection.getInputStream());
                reader = new BufferedReader(new InputStreamReader(content,"UTF-8"));
                String line;
                while((line = reader.readLine()) != null){
                    builder.append(line);
                }
                JSONObject jsonObject = new JSONObject(builder.toString());
                sale_id =jsonObject.getString("sale_id");

            }catch (MalformedURLException e){
                Log.d(TAG, "URL exception");

            }catch (IOException ex){
                Log.d(TAG, "IOException");

            }catch (JSONException ex){
                Log.d(TAG, "JSONexception");

            }
            return sale_id;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result != "-1") {
                getCategoryTask = new GetCategoryTask();
                getCategoryTask.execute();
            }
        }
    }

    private class DeleteItemTask extends AsyncTask<Items, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Items... arg) {
            String sale_id = "-1";
            BufferedReader reader = null;
            InputStream content = null;
            Items item = null;
            StringBuilder builder = new StringBuilder();
            try {
                URL url = new URL("http://192.168.1.3:9000/pos/delete_item/"+sales_id+"/"+arg[0].getItem_id());
                HttpURLConnection  connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod( "POST" );
                connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                connection.setRequestProperty("Accept","*/*");
                content = new BufferedInputStream(connection.getInputStream());
                reader = new BufferedReader(new InputStreamReader(content,"UTF-8"));
                String line;
                while((line = reader.readLine()) != null){
                    builder.append(line);
                }
                JSONObject jsonObject = new JSONObject(builder.toString());
                sale_id =jsonObject.getString("sale_id");

            }catch (MalformedURLException e){
                Log.d(TAG, "URL exception");

            }catch (IOException ex){
                Log.d(TAG, "IOException");

            }catch (JSONException ex){
                Log.d(TAG, "JSONexception");

            }
            return sale_id;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result != "-1") {
                getCategoryTask = new GetCategoryTask();
                getCategoryTask.execute();
            }
        }
    }

    private class GetCategoryTask extends AsyncTask<Void, Void, ArrayList<CategoryCount>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<CategoryCount> doInBackground(Void... arg) {
            BufferedReader reader = null;
            InputStream content = null;
            CategoryCount item = null;
            ArrayList<CategoryCount> cat = null;
            StringBuilder builder = new StringBuilder();
            try {
                URL url = new URL("http://192.168.1.3:9000/pos/get_category_stat/"+sales_id);
                //URL url = new URL("http://192.168.1.3:9000/pos");
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                connection.setRequestProperty("Accept","*/*");
                connection.setRequestProperty("content-type","application/json");
                //connection.setDoOutput(true);
                content = new BufferedInputStream(connection.getInputStream());
                reader = new BufferedReader(new InputStreamReader(content,"UTF-8"));
                String line;
                while((line = reader.readLine()) != null){
                    builder.append(line);
                }
                cat = new ArrayList<>();
                JSONObject jsonObject = new JSONObject(builder.toString());
                JSONArray jsonArray = jsonObject.getJSONArray("arrayStats");
                int size = jsonArray.length();
                int total =0;
                JSONObject obj;
                for (int i=0;i<size;i++){
                    obj = jsonArray.getJSONObject(i);
                    item = new CategoryCount();
                    item.setCategory(obj.getString("category"));
                    item.setCount(obj.getString("count"));
                    total += (int)(Double.parseDouble(obj.getString("count")));
                    item.setTotal(item.getTotal() + (int)(Double.parseDouble(obj.getString("count"))));
                    cat.add(item);
                }
                for (int i=0;i<size;i++){
                    CategoryCount cnt = cat.get(i);
                    cnt.setTotal(total);
                    cat.set(i,cnt);
                }
            }catch (MalformedURLException e){
                Log.d(TAG, "URL exception");

            }catch (IOException ex){
                Log.d(TAG, "IOException");

            }catch (JSONException ex){
                Log.d(TAG, "JSONexception");

            }
            return cat;
        }
        @Override
        protected void onPostExecute(ArrayList<CategoryCount> result) {
            super.onPostExecute(result);
            if(result != null){
                mListener.onCategoryUpdated(result);
            }
        }
    }

    public String getSales_id(){
        return sales_id;
    }


}

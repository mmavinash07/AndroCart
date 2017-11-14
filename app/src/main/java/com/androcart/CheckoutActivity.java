package com.androcart;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Avinash on 10/30/2017.
 */

public class CheckoutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_checkout);
        Bundle b = getIntent().getExtras();
        TextView txtamount = (TextView)findViewById(R.id.txtamount);
        txtamount.setText(b.get("total").toString());
        TextView txtsale = (TextView)findViewById(R.id.txtsale);
        txtsale.setText(b.get("id").toString());
    }
}

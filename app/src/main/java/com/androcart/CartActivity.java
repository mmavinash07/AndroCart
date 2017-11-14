package com.androcart;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class CartActivity extends Activity implements Fragment_Items.OnFragmentInteractionListener {

    private Handler mCameraHandler;
    private HandlerThread mCameraThread;
    private InitializeCamera mCamera;
    private BarcodeDetector detector;
    private static final String TRIGGER_PIN_NAME = "BCM22";
    private static final String ECHO_PIN_NAME = "BCM27";
    private static final String TAG = "EchoActivity";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1234;
    private static final int INTERVAL_BETWEEN_TRIGGERS_MS = 1000;
    private Handler mTriggerHandler = new Handler();
    private Gpio mTriggerGpio;
    private Gpio mEchoGpio;
    private long pulse_start;
    private long pulse_end;
    private long pulse_duration;
    private  boolean check_trigger = false;
    private  boolean in_range = false;
    private int echo_settle;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        getActionBar().setLogo(R.drawable.logo);
        getActionBar().setDisplayUseLogoEnabled(true);

        detector =
                new BarcodeDetector.Builder(getApplicationContext())
                        //.setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                        .build();
        if(!detector.isOperational()){
            //txtView.setText("Could not set up the detector!");
            return;
        }

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions( new String[] {  android.Manifest.permission.CAMERA },MY_PERMISSIONS_REQUEST_CAMERA);
        }
        else {

            mCameraThread = new HandlerThread("CameraBackground");
            mCameraThread.start();
            mCameraHandler = new Handler(mCameraThread.getLooper());
            mCamera = InitializeCamera.getInstance();
            mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);
        }

        PeripheralManagerService service = new PeripheralManagerService();
        try {
            mTriggerGpio = service.openGpio(TRIGGER_PIN_NAME);
            // Configure as an output.
            mTriggerGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            mEchoGpio = service.openGpio(ECHO_PIN_NAME);
            // Configure as an input.
            mEchoGpio.setDirection(Gpio.DIRECTION_IN);
            mEchoGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mEchoGpio.setActiveType(Gpio.ACTIVE_HIGH);

            mEchoGpio.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    try {
                        if(check_trigger) {
                            echo_settle++;
                            if (gpio.getValue()) {
                                pulse_start = System.nanoTime();
                            } else {
                                //if(echo_settle > 3) {
                                 //   echo_settle = 0;
                                    pulse_end = System.nanoTime();
                                    pulse_duration = pulse_end - pulse_start;
                                    if(pulse_duration <= 1500000 && !in_range) {
                                        mCamera.takePicture();
                                        in_range = true;
                                    }
                                    else
                                        in_range = false;
                                //}

                            }
                        }
                    } catch (IOException e){

                    }
                    return super.onGpioEdge(gpio);
                }

                @Override
                public void onGpioError(Gpio gpio, int error) {
                    super.onGpioError(gpio, error);
                }
            });
            mTriggerHandler.post(mTriggerRunnable);
            fragmentManager = getFragmentManager();
            new CreateSaleTask().execute();

        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_analytics);
        String tot="";
        if(fragment != null){
           tot = ((Fragment_Analytics)fragment).getTotal();
        }
        Fragment fragment_cart = fragmentManager.findFragmentById(R.id.fragment_cart);
        String id="";
        if(fragment_cart != null){
            id = ((Fragment_Items)fragment_cart).getSales_id();
        }
        Bundle b = new Bundle();
        b.putString("total",tot);
        b.putString("id",id);
        Intent intent = new Intent(this,CheckoutActivity.class);
        intent.putExtras(b);
        startActivity(intent);
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onFragmentInteraction(Items item,int action) {

        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_analytics);
        if(fragment != null){
            ((Fragment_Analytics)fragment).SetTotal(item,action);
        }

    }

    @Override
    public void onCategoryUpdated(ArrayList<CategoryCount> cat) {
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_analytics);
        if(fragment != null){
            ((Fragment_Analytics)fragment).UpdateCategory(cat);
        }
    }

    private Runnable mTriggerRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit if the GPIO is already closed
            if (mTriggerGpio == null) {
                return;
            }

            try {
                mTriggerGpio.setValue(false);
                Thread.sleep(0,2000);
                mTriggerGpio.setValue(true);
                Thread.sleep(0,10000); //10 microsec
                mTriggerGpio.setValue(false);
                check_trigger = true;
                mTriggerHandler.postDelayed(mTriggerRunnable,INTERVAL_BETWEEN_TRIGGERS_MS);


            } catch (IOException e) {
                check_trigger = false;
                Log.e(TAG, "Error on PeripheralIO API", e);
            } catch (InterruptedException ex){
                check_trigger = false;
            }
        }
    };

    // Callback to receive captured camera image data
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    // Get the raw image bytes
                    Image image = reader.acquireLatestImage();
                    final ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
                    final byte[] imageBytes = new byte[imageBuf.remaining()];
                    imageBuf.get(imageBytes);
                    image.close();
                    onPictureTaken(imageBytes);
                }
            };

    private void onPictureTaken(final byte[] imageBytes) {
        if (imageBytes != null) {
                new Runnable(){
                    @Override
                    public void run() {
                        Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        Frame frame = new Frame.Builder().setBitmap(bmp).build();
                        SparseArray<Barcode> barcodes = detector.detect(frame);
                        if(barcodes.size() > 0) {
                            Barcode code = barcodes.valueAt(0);
                            Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_cart);
                            if (fragment != null && fragment instanceof Fragment_Items) {
                                ((Fragment_Items) fragment).addItem(code.rawValue);
                            }
                        }
                    }
                }.run();
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    try {
                        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            mCameraThread = new HandlerThread("CameraBackground");
                            mCameraThread.start();
                            mCameraHandler = new Handler(mCameraThread.getLooper());
                            mCamera = InitializeCamera.getInstance();
                            mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);

                        }
                    } catch (Exception ie) {
                        Log.e("CAMERA SOURCE", ie.getMessage());
                    }


                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private class CreateSaleTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... arg) {
            String sale_id = "-1";
            BufferedReader reader = null;
            InputStream content = null;
            Items item = null;
            StringBuilder builder = new StringBuilder();
            try {
                URL url = new URL("http://192.168.1.3:9000/pos/create_sale_id");
                HttpURLConnection  connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod( "POST" );
                connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                connection.setRequestProperty("Accept","*/*");
                connection.setRequestProperty("content-type","application/json");
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
                Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_cart);
                if(fragment != null){
                    ((Fragment_Items)fragment).getSaleId(result);
                }
            }
        }
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        mCameraThread.quitSafely();
        mTriggerHandler.removeCallbacks(mTriggerRunnable);

        // Step 5. Close the resource.
        if (mTriggerGpio != null) {
            try {
                mTriggerGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }

        if (mEchoGpio != null) {
            try {
                mEchoGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    }

}

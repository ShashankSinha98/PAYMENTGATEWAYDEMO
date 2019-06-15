package com.example.paymentgatewaydemo;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.JsonParser;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Checksum extends AppCompatActivity implements PaytmPaymentTransactionCallback {

    private String custId = "", orderId = "", merchantId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checksum);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Intent intent = getIntent();

        orderId = intent.getExtras().getString("orderid");
        custId = intent.getExtras().getString("custid");

        merchantId = "YOUR_MERCHANT_ID";

        sendUserDetailToServer d1 = new sendUserDetailToServer();
        d1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);



    }


    @Override
    public void onTransactionResponse(Bundle bundle) {
        Log.e("checksum ", " respon true " + bundle.toString());
    }
    @Override
    public void networkNotAvailable() {
    }
    @Override
    public void clientAuthenticationFailed(String s) {
    }
    @Override
    public void someUIErrorOccurred(String s) {
        Log.e("checksum ", " ui fail respon  "+ s );
    }
    @Override
    public void onErrorLoadingWebPage(int i, String s, String s1) {
        Log.e("checksum ", " error loading pagerespon true "+ s + "  s1 " + s1);
    }
    @Override
    public void onBackPressedCancelTransaction() {
        Log.e("checksum ", " cancel call back respon  " );
    }
    @Override
    public void onTransactionCancel(String s, Bundle bundle) {
        Log.e("checksum ", "  transaction cancel " );
    }



    public class sendUserDetailToServer extends AsyncTask<ArrayList<String>, Void, String> {


        //http://babayaga98.epizy.com/generateChecksum.php
        String url = "YOUR_SERVER_URL_GENERATE_CHECKSUM";
        String verifyUrl = "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";

        String CHECKSUMHASH = "";


        @Override
        protected void onPreExecute() {
            Toast.makeText(Checksum.this, "Please wait", Toast.LENGTH_SHORT).show();
            Log.d("xlr8","onPreExecute");

        }


        @Override
        protected String doInBackground(ArrayList<String>... arrayLists) {

            JSONParser jsonParser = new JSONParser(Checksum.this);

            String param =
                    "MID=" + merchantId +
                            "&ORDER_ID=" + orderId +
                            "&CUST_ID=" + custId +
                            "&CHANNEL_ID=WAP&TXN_AMOUNT=100&WEBSITE=WEBSTAGING" +
                            "&CALLBACK_URL=" + verifyUrl + "&INDUSTRY_TYPE_ID=Retail";

            Log.e("xlr8",param);

            JSONObject jsonObject = jsonParser.makeHttpRequest(url, "POST", param);

            // yaha per checksum ke saht order id or status receive hoga..
//            Log.e("CheckSum result >>", jsonObject.toString());

            if (jsonObject != null) {
                Log.e("CheckSum result >>", jsonObject.toString());
                try {
                    CHECKSUMHASH = jsonObject.has("CHECKSUMHASH") ? jsonObject.getString("CHECKSUMHASH") : "";
                    Log.e("xlr8","Checksum: " + CHECKSUMHASH);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("xlr8","Exception 2: "+e.getMessage());

                }
            } else {
                // Toast.makeText(getApplicationContext(), "json Obj is null", Toast.LENGTH_SHORT).show();
                Log.d("xlr8","Json Obj is null!!");
            }

            return CHECKSUMHASH;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("xlr8","onPostExecute");
            Log.e(" setup acc ", "  signup result  " + result);
            Toast.makeText(Checksum.this, "Signup result : " + result, Toast.LENGTH_SHORT).show();

            PaytmPGService Service = PaytmPGService.getStagingService();

            HashMap<String, String> paramMap = new HashMap<String, String>();

            paramMap.put("MID", merchantId); //MID provided by paytm
            paramMap.put("ORDER_ID", orderId);
            paramMap.put("CUST_ID", custId);
            paramMap.put("CHANNEL_ID", "WAP");
            paramMap.put("TXN_AMOUNT", "100");
            paramMap.put("WEBSITE", "WEBSTAGING");
            paramMap.put("CALLBACK_URL" ,verifyUrl);
            paramMap.put("CHECKSUMHASH" ,CHECKSUMHASH);
            paramMap.put("INDUSTRY_TYPE_ID", "Retail");


            PaytmOrder Order = new PaytmOrder(paramMap);
            Log.e("checksum ", "param "+ paramMap.toString());


            Service.initialize(Order,null);

            // start payment service call here
            Service.startPaymentTransaction(Checksum.this, true, true,
                    Checksum.this  );
        }



    }
}


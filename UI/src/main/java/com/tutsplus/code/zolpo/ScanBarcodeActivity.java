package com.tutsplus.code.zolpo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tutsplus.code.zolpo.Models.RequestToServer;

public class ScanBarcodeActivity extends AppCompatActivity {

    private static final String TAG = "ScanBarcodeActivity";

    //vars
    private String mScanContent;
    private Intent mIntent;
    private RequestToServer mRequestToServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);
        //getting RequestToServer object from previous activity
        Bundle infoFromPreviousActivity = getIntent().getExtras();
        mRequestToServer = (RequestToServer) infoFromPreviousActivity.get("RequestToServer");
        initCameraAndBarcodeSettings();
    }

    private void initCameraAndBarcodeSettings() {
        Log.d(TAG, "initCameraAndBarcodeSettings : initializing camera and barcode settings");
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.PRODUCT_CODE_TYPES);
        integrator.setPrompt("סרוק ברקוד");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: getting scanning result");
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result!= null) {
            if (result.getContents() == null) {
                Log.d(TAG, "onActivityResult: results' content is null");
                mIntent = new Intent(ScanBarcodeActivity.this, MainActivity.class);
                startActivity(mIntent);
            } else {
                Log.d(TAG, "onActivityResult: results' content is ok");
                mScanContent = result.getContents();
                mRequestToServer.setBarcode(mScanContent);
                mIntent = new Intent(ScanBarcodeActivity.this, ShowStoresActivity.class);
                mIntent.putExtra("RequestToServer", mRequestToServer);
                startActivity(mIntent);
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

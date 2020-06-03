package com.tutsplus.code.zolpo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tutsplus.code.zolpo.Models.RequestToServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class PostImageActivity extends AppCompatActivity {

    private static final String TAG = "PostImageActivity";
    private static final int PICK_IMAGE_REQUEST = 111;
    private static final String PostImageUrl = "http://vmedu153.mtacloud.co.il:8082/api/ProductDetails";

    private ImageView mTakenPicImageView;
    private ImageButton mBackImageBtn;
    private Button mTakePicBtn, mUploadPicBtn;
    private Bitmap mBitmap;
    private boolean mImageUploadedSuccessfully = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_image);

        mBackImageBtn = findViewById(R.id.backImageBtn);
        mTakenPicImageView = findViewById(R.id.image);
        mTakePicBtn = findViewById(R.id.takePic);
        mUploadPicBtn = findViewById(R.id.upload);

        //back button click setting
        mBackImageBtn.setOnClickListener(v -> onBackPressed());

        //takePic button click setting - upload picture to server
        mTakePicBtn.setOnClickListener(view -> takePicClickSettings());

        //uploadPic button click setting - opening image chooser option
        mUploadPicBtn.setOnClickListener(view -> uploadPicToServerSettings());
    }

    private void takePicClickSettings()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, PICK_IMAGE_REQUEST);
            mUploadPicBtn.setVisibility(View.VISIBLE);
        }
    }

    private void uploadPicToServerSettings()
    {
        ProgressDialog uploadingProgressDialog;
        uploadingProgressDialog = new ProgressDialog(PostImageActivity.this);
        uploadingProgressDialog.setMessage("מעלה תמונה לשרת, אנא המתן...");
        uploadingProgressDialog.show();

        //converting image to base64 string
        Log.d(TAG, "Converting image to base64 string...");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        final String imageInBase64StringFormat = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        try {
            JSONObject parametersToSendInJsonObj = new JSONObject();
            parametersToSendInJsonObj.put("image", imageInBase64StringFormat);
            //getting the product's barcode from the last activity
            parametersToSendInJsonObj.put("barcode", getIntent().getExtras().get("Barcode"));

            Log.d(TAG, "Making post request...");
            JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, PostImageUrl, parametersToSendInJsonObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    uploadingProgressDialog.dismiss();
                    Log.d(TAG, "Uploaded Successful");
                    mImageUploadedSuccessfully = true;
                    onBackPressed();
                }
            }, error -> {
                uploadingProgressDialog.dismiss();
                setErrorAtUploadMsg();
                onBackPressed();
            });

            VolleyRequestQueueSingleton.getInstance(PostImageActivity.this).addToRequestQueue(postRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setErrorAtUploadMsg() {
        Log.d(TAG, "Some error occurred!");
        Toast.makeText(getApplicationContext(),"התרחשה בעיה בהעלאת התמונה...", Toast.LENGTH_SHORT ).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            mBitmap = (Bitmap) extras.get("data");
            mTakenPicImageView.setImageBitmap(mBitmap);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "on Back Pressed");
        if(!mImageUploadedSuccessfully)
        {
            super.onBackPressed();
        }
        else {
            Intent showStoresIntent = new Intent();
            showStoresIntent.putExtra("NewProductImage", mBitmap);
            setResult(RESULT_OK, showStoresIntent);
            finish();
        }
    }

}
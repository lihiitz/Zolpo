package com.tutsplus.code.zolpo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alexzaitsev.meternumberpicker.MeterView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tutsplus.code.zolpo.Adapters.StoresListToDataAdapter;
import com.tutsplus.code.zolpo.Models.ImageLoader;
import com.tutsplus.code.zolpo.Models.RequestToServer;
import com.tutsplus.code.zolpo.Models.StoreListItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShowStoresActivity extends AppCompatActivity{

    private static final String TAG = "ShowStoresActivity";
    private static final int MAX_DISTANCE = 30;
    private static final int MIN_DISTANCE = 1;
    private static final int WAIT_FOR_RESPONSE_INTERVAL_MS = 90000;
    private static final int CHANGE_PRODUCT_PIC_REQUEST_CODE = 12;
    private final int SORT_BY_PRICE_LOW_TO_HIGH = 0;
    private final int SORT_BY_DISTANCE_SHORT_TO_LONG = 1;

    //members
    private Context mContext;
    private int mDistance, mLastSortChoice;
    private ArrayAdapter<String> mSortSpinnerAdapter;
    private StoreListItemHolder mStoreListItemHolder;
    private RequestToServer mRequestToServer;
    private final String[] mSortByOptions = new String[]{
            "מחיר - זול ליקר  ",
            "מרחק - קרוב לרחוק"
    };
    //widgets
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerViewAdapter;
    private List<StoreListItem> mListItems;
    private Spinner mSortSpinner;
    private TextView mProductName;
    private MeterView mMeterNumberPicker;
    private ImageButton mReScanImageBtn, mSearchKmImageBtn, mProductImageBtn;
    private TextView mNumberOfResultsTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_stores);

        //getting RequestToServer object from previous activity
        Bundle b = getIntent().getExtras();
        mRequestToServer = (RequestToServer) b.get("RequestToServer");

        mContext = getApplicationContext();
        mLastSortChoice = SORT_BY_PRICE_LOW_TO_HIGH;
        mStoreListItemHolder =  new StoreListItemHolder();
        initializeViews();
        loadRecyclerViewData();
    }

    private void initializeViews() {
        Log.d(TAG,"initializeViews: initializing...");
        mProductName = findViewById(R.id.productNameTextView);
        //Recycler View - list of items
        initializeRecycleView();
        //Product Image
        mProductImageBtn = findViewById(R.id.productImageImageBtn);
        mProductImageBtn.setOnClickListener(v -> handleChangeProductPicture());
        //Radius
        mDistance = mRequestToServer.GetDefaultDistance();
        mMeterNumberPicker =  findViewById(R.id.meterView);
        mSearchKmImageBtn = findViewById(R.id.searchKmImageBtn);
        setAndDisplayDistance();
        //Sort spinner
        initializeSortSpinner();
        //number of results textView
        mNumberOfResultsTextView = findViewById(R.id.numberOfResultsTextView);
        //Scan another product button
        mReScanImageBtn = findViewById(R.id.reScanImageBtn);
        mReScanImageBtn.setOnClickListener(v -> goToScanActivity());
    }

    private void handleChangeProductPicture() {
        Log.d(TAG, "handleChangeProductPicture");
        if(mRequestToServer != null)
        {// There was a request
            buildAlertDialogChangeProductPic();
        }
    }

    private void buildAlertDialogChangeProductPic() {
        Log.d(TAG, "buildAlertDialogChangeProductPic: build Alert Message Change product picture");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("האם תרצה לשנות תמונת מוצר?")
                .setCancelable(false)
                .setPositiveButton("כן", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent postPicIntent = new Intent(ShowStoresActivity.this, PostImageActivity.class);
                        postPicIntent.putExtra("Barcode", mRequestToServer.getBarcode());
                        startActivityForResult(postPicIntent,CHANGE_PRODUCT_PIC_REQUEST_CODE);
                    }
                })
                .setNegativeButton("לא", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        Log.d(TAG, "onActivityResult");
        if (requestCode == CHANGE_PRODUCT_PIC_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.d(TAG, String.format("onActivityResult : requestCode %d was successful", CHANGE_PRODUCT_PIC_REQUEST_CODE));
                mProductImageBtn.setImageBitmap((Bitmap)resultIntent.getExtras().get("NewProductImage"));
            }
        }
    }
    private void setAndDisplayDistance() {
        mMeterNumberPicker.setValue(mRequestToServer.GetDefaultDistance());
        mSearchKmImageBtn.setOnClickListener(v -> {
            displayDistance(mMeterNumberPicker.getValue());
        });
        displayDistance(mMeterNumberPicker.getValue());
    }

    private void goToScanActivity() {
        mRequestToServer.setBarcode(null);
        mRequestToServer.setDistance(mRequestToServer.GetDefaultDistance());
        Intent intent = new Intent(ShowStoresActivity.this, ScanBarcodeActivity.class);
        intent.putExtra("RequestToServer", mRequestToServer);
        startActivity(intent);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        goToScanActivity();
    }

    private void initializeRecycleView() {
        Log.d(TAG,"initializeRecycleView: initializing recycleView...");
        mRecyclerView = findViewById(R.id.storesRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
    }

    private void initializeSortSpinner() {
        Log.d(TAG,"initializeSortSpinner: initializing sort spinner...");

        mSortSpinner =  findViewById(R.id.sortSpinner);
        mSortSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, mSortByOptions);
        mSortSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        mSortSpinner.setAdapter(mSortSpinnerAdapter);
    }


    private void loadRecyclerViewData() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        setWaitForResultProgressDialog(progressDialog);

        String GetRequestUrl = mRequestToServer.GetFormattedUrl();
        Log.d(TAG,"loadRecyclerViewData: The Get Url is:" + GetRequestUrl);

        Log.d(TAG, "loadRecyclerViewData: Making get request...");
        final StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                GetRequestUrl,
                response -> {
                    Log.d(TAG, "onResponse: Handling response...");
                    progressDialog.dismiss();

                    try {
                        Log.d(TAG, "onResponse: try: Extracting from JSON");
                        Type collectionType = new TypeToken<ArrayList<StoreListItem>>() {}.getType();
                        mListItems = new Gson().fromJson(response, collectionType);
                        Log.d(TAG, "onResponse: Extracting from JSON succeeded");
                        setStoresListView();
                    }
                    catch(Exception e)
                    {
                        Log.d(TAG, "onResponse: catch: Extracting from JSON failed");
                        Log.e(TAG,e.getMessage());
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    String errorMsg = tryToExtractStringMsgFromJsonObj(error);
                    TextView titleTextView = new TextView(ShowStoresActivity.this);
                    titleTextView.setText(R.string.error_msg);
                    titleTextView.setPadding(10, 10, 10, 10);
                    titleTextView.setGravity(Gravity.CENTER);
                    titleTextView.setTextSize(20);
                    titleTextView.setTextColor(Color.WHITE);

                    setErrorMsg(titleTextView, errorMsg, R.string.try_again);
                });

        RetryPolicy policy = new DefaultRetryPolicy(WAIT_FOR_RESPONSE_INTERVAL_MS,
                                 DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                 DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        VolleyRequestQueueSingleton.getInstance(ShowStoresActivity.this).addToRequestQueue(stringRequest);
    }

    private void setWaitForResultProgressDialog(ProgressDialog iProgressDialog) {
        Log.d(TAG,"setWaitForResultProgressDialog: setting progress dialog...");
        iProgressDialog.setMessage("ממתין לתוצאות...");
        iProgressDialog.show();
        iProgressDialog.setCanceledOnTouchOutside(false);
        iProgressDialog.setOnCancelListener(v -> onBackPressed());
    }

    /**************************** Handle Response From The server *********************************/

    private String tryToExtractStringMsgFromJsonObj(VolleyError iError)
    {
        Log.d(TAG,"tryToExtractStringMsgFromJsonObj");
        String jsonFormatMsg, stringFormatMsg;
        stringFormatMsg = "אירעה בעיה כללית";
        if (iError.networkResponse != null && iError.networkResponse.data != null)
        {
            jsonFormatMsg = new String(iError.networkResponse.data);
            Log.e(TAG, "tryToExtractStringMsgFromJsonObj" + jsonFormatMsg);
            try {
                Log.e(TAG, "trying to extract  message from json object...");
                JSONObject errorJsonObj = new JSONObject(jsonFormatMsg);
                stringFormatMsg = errorJsonObj.getString("Message");
            } catch (JSONException e) {
                Log.e(TAG, "extracting error message from json object failed.");
            }
        }
        else if(iError instanceof NetworkError)
        {
            stringFormatMsg = "אנא בדוק את החיבור לאינטרנט";
        }
        return stringFormatMsg;
    }

    private void setStoresListView() {

        StoreListItem firstListItem = mListItems.get(0);
        mStoreListItemHolder.setStoreListItems(mListItems);
        mStoreListItemHolder.setMaxDistance(mDistance);
        mProductName.setText(firstListItem.getProductName());
        //setting the recycle view with all the data from the server
        mRecyclerViewAdapter = new StoresListToDataAdapter(mListItems, mContext);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        ImageLoader.StaticImageLoader.LoadImageFromUrl(mContext,firstListItem.getProductImageURL(),mProductImageBtn,R.drawable.ic_question_mark);
        //update on item click listener AFTER finish setting the recycle view
        setupSort();
        sortMenuByChoice();
        setupNumberOfResults();
    }


    private void setErrorMsg(TextView iTitleTextView, String iResponseError, int iPositiveBtnMsg) {
        Log.d(TAG, "setErrorMsg: creating error message...");
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ShowStoresActivity.this, android.R.style.Theme_Holo_Dialog));
        builder.setCustomTitle(iTitleTextView);
        builder.setMessage(iResponseError)
                .setPositiveButton(iPositiveBtnMsg, (dialog, id) -> goToScanActivity());
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
    /*************************************** Number of results ************************************/

    private void setupNumberOfResults() {
        Log.d(TAG,"setupNumberOfResults: setup number of results ");
        int numberOfResults = mRecyclerViewAdapter.getItemCount();
        if(numberOfResults != 0)
        {
            mNumberOfResultsTextView.setText(String.format("%d תוצאות",numberOfResults));
        }
        else
        {
            mNumberOfResultsTextView.setText(R.string.no_results_msg);
        }
    }

    /******************************* Handle change of distance option *****************************/

    private void displayDistance(int iDesiredDistance) {
        if(iDesiredDistance > MAX_DISTANCE)
        {
            Toast.makeText(mContext,String.format("%d ק\"מ הינו הטווח המקסימלי ביותר לחיפוש", MAX_DISTANCE),Toast.LENGTH_LONG).show();
        }
        else if(iDesiredDistance < MIN_DISTANCE)
        {
            Toast.makeText(mContext,String.format("%d ק\"מ הינו הטווח המינימלי ביותר לחיפוש", MIN_DISTANCE),Toast.LENGTH_LONG).show();
        }
        else {
            mDistance = iDesiredDistance;
            if (iDesiredDistance > mStoreListItemHolder.getMaxDistance())//!!new
            {
                mRequestToServer.setDistance(mDistance);
                loadRecyclerViewData();
            }
            else if (mStoreListItemHolder.getmStoreListItems() != null)
            {
                mStoreListItemHolder.GetFilteredListByUpToDistance(mDistance);
                mListItems = mStoreListItemHolder.getmStoreListItems();
                mRecyclerViewAdapter = new StoresListToDataAdapter(mListItems, mContext);
                mRecyclerView.setAdapter(mRecyclerViewAdapter);
                setupNumberOfResults();
                sortMenuByChoice();
            }
        }
    }

    /********************** Handle selected item in the SortBy-Spinner ****************************/

    private void setupSort() {
        mSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"OnItemSelected: sort option was selected in dropdown menu");
                mLastSortChoice = position;
                sortMenuByChoice();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void sortMenuByChoice() {
        Log.d(TAG,"sortMenuByPosition: sorting by sort choice");
        switch(mLastSortChoice) {
            case SORT_BY_PRICE_LOW_TO_HIGH:
                sortMenuByPrice();
                break;
            case SORT_BY_DISTANCE_SHORT_TO_LONG:
                sortMenuByDistance();
                break;
        }
        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void sortMenuByPrice() {
        Log.d(TAG,"sortMenuByPrice: sorting by price");
        Collections.sort(mListItems, (l1, l2) -> {
            int returnVal;
            if (l1.getProductPrice() > l2.getProductPrice()) {
                returnVal = 1;
            } else if (l1.getProductPrice() < l2.getProductPrice()) {
                returnVal = -1;
            }else{
                returnVal = 0;
            }
            return returnVal;
        });
    }

    private void sortMenuByDistance() {
        Log.d(TAG,"sortMenuByRadius: sorting by distance");
        Collections.sort(mListItems, (l1, l2) -> {
            if (l1.getDistance() > l2.getDistance()) {
                return 1;
            } else if (l1.getDistance() < l2.getDistance()) {
                return -1;
            }else{
                return 0;
            }
        });
    }

}

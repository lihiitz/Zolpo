package com.tutsplus.code.zolpo;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyRequestQueueSingleton {

    private static  VolleyRequestQueueSingleton mInstance;
    private RequestQueue requestQueue;
    private static Context mCtx;

    public VolleyRequestQueueSingleton(Context context) {
        mCtx =context;
        requestQueue=getRequestQueue();
    }

    public RequestQueue getRequestQueue() {

        if(requestQueue == null)
        {
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized VolleyRequestQueueSingleton getInstance(Context context)
    {
        if(mInstance == null)
        {
            mInstance =new VolleyRequestQueueSingleton(context);
        }
        return mInstance;
    }

    public <T> void addToRequestQueue(Request<T> request)
    {
        requestQueue.add(request);
    }
}

package com.mia.smartlight.service;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyRequestQueueController {

    private static VolleyRequestQueueController instance;
    private RequestQueue requestQueue;
    private static Context context;

    private VolleyRequestQueueController(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized VolleyRequestQueueController getInstance(Context context) {
        if (instance == null) {
            instance = new VolleyRequestQueueController(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}

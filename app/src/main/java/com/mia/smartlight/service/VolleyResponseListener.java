package com.mia.smartlight.service;

public interface VolleyResponseListener {
    void onError(String message);

    void onResponse(Object response);
}

package com.mia.smartlight.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mia.smartlight.model.Attribute;
import com.mia.smartlight.model.NexaUnit;
import com.mia.smartlight.model.UserConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NexaService {

    private static NexaService instance;
    public static final String FLAG = "no_url";
    private UserConfig userConfig;

    private NexaService(Context context) {
        userConfig = UserConfig.getInstance(context);
    }

    public static synchronized NexaService getInstance(Context context) {
        if (instance == null) {
            instance = new NexaService(context);
        }
        return instance;
    }

    public void getUnit(final int id, Context context, final VolleyResponseListener listener) {

        Log.d("Class", "NexaService");

        if (userConfig.getServerURL() == null || userConfig.getServerURL().isEmpty()) {
            listener.onError(FLAG);
        } else {
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, userConfig.getServerURL() + "/" + id,
                            null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                Log.d("Request", "Fetched item " + String.valueOf(id));

                                List<String> actions = new ArrayList<>();
                                List<Attribute> attributes = new ArrayList<>();

                                JSONArray array = response.getJSONArray("actions");
                                for (int i = 0; i < array.length(); i++) {
                                    actions.add(array.getString(i));
                                }
                                array = response.getJSONArray("attributes");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject o = array.getJSONObject(i);
                                    Attribute a = new Attribute(o.getString("name"), o.getString("value"));
                                    attributes.add(a);
                                }

                                NexaUnit unit = new NexaUnit(response.optString("name"), Integer.parseInt(response.optString("id")),
                                        response.optString("className"), response.optString("category"), actions, attributes);

                                listener.onResponse(unit);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            listener.onError(error.toString());
                        }
                    });

            VolleyRequestQueueController.getInstance(context.getApplicationContext()).addToRequestQueue(jsObjRequest);
        }
    }

    public void getUnits(Context context, final VolleyResponseListener listener) {

        Log.d("Class", "NexaService");

        if (userConfig.getServerURL() == null || userConfig.getServerURL().isEmpty()) {
            listener.onError(FLAG);
        } else {
            JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                    (Request.Method.GET, userConfig.getServerURL(),
                            null, new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {

                            try {
                                Log.d("Request", "Fetched all items");

                                List<NexaUnit> units = new ArrayList<>();

                                for (int i = 0; i < response.length(); i++) {

                                    JSONObject j = response.getJSONObject(i);

                                    if (j.getString("category").equals("Lamps")) {

                                        NexaUnit unit = new NexaUnit(j.optString("name"), Integer.parseInt(j.optString("id")), j.optString("category"));

                                        units.add(unit);
                                    }
                                }

                                listener.onResponse(units);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            listener.onError(error.toString());
                        }
                    });

            VolleyRequestQueueController.getInstance(context.getApplicationContext()).addToRequestQueue(jsArrayRequest);
        }
    }

    public void invokeAction(final int id, final String action, Context context, final VolleyResponseListener listener) {

        Log.d("Class", "NexaService");

        if (userConfig.getServerURL() == null || userConfig.getServerURL().isEmpty()) {
            listener.onError(FLAG);
        } else {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, userConfig.getServerURL() + "/" + id
                    + "/actions/" + action + "/invoke", null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {

                                Log.d("Request", "Invoked action '" + action + "' on server");

                                List<Attribute> attributes = new ArrayList<>();

                                JSONArray array = response.getJSONArray("attributes");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject o = array.getJSONObject(i);
                                    Attribute a = new Attribute(o.getString("name"), o.getString("value"));
                                    attributes.add(a);
                                }

                                listener.onResponse(attributes);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    listener.onError(error.toString());
                }
            }) {
                @Override
                public byte[] getBody() {
                    String httpPostBody = action;
                    return httpPostBody.getBytes();
                }
            };

            VolleyRequestQueueController.getInstance(context.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
        }
    }
}

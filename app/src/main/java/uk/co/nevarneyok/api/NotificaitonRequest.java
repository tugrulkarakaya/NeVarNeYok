package uk.co.nevarneyok.api;

import android.app.DownloadManager;
import android.support.v4.app.FragmentManager;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import uk.co.nevarneyok.MyApplication;
import uk.co.nevarneyok.entities.FIRNotificaiton;
import uk.co.nevarneyok.utils.JsonUtils;

/**
 * Created by tugrulkarakaya on 03/02/2017.
 */

public class NotificaitonRequest extends JsonRequest {
    private final String accessToken;

    public NotificaitonRequest(JSONObject jsonRequest , Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener, FragmentManager fragmentManager, String accessToken) throws JSONException {
        super(Method.POST, EndPoints.SEND_NOTIFICATION, jsonRequest, successListener, errorListener, fragmentManager, accessToken);
        this.accessToken = accessToken;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Client-Version", MyApplication.APP_VERSION);
        headers.put("Device-Token", MyApplication.ANDROID_ID);
        headers.put("Content-Type", "application/json");

        // Determine if request should be authorized.
        if ( this.accessToken!= null && !this.accessToken.isEmpty()) {
            headers.put("Authorization", "key=" + this.accessToken);
        }
        return headers;
    }
}

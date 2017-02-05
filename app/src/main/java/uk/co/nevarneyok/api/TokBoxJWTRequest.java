package uk.co.nevarneyok.api;

import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import timber.log.Timber;
import uk.co.nevarneyok.BuildConfig;
import uk.co.nevarneyok.MyApplication;
import uk.co.nevarneyok.controllers.AppSettingController;
import uk.co.nevarneyok.utils.Utils;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by mcagri on 27/01/2017.
 */

public class TokBoxJWTRequest extends GsonRequest<JSONObject> {
    static String tokBoxApiKey = AppSettingController.getSetting("tokboxKey");
    static String tokBoxSecret = AppSettingController.getSetting("tokboxSecret");
    static String tokBoxUrl = AppSettingController.getSetting("tokboxUrl");
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private final String accessToken;


    /**
     * Create a new authorized request.
     *
     * @param successListener Listener to retrieve successful response
     * @param errorListener   Error listener, or null to ignore errors
     * @param fragmentManager Manager to create re-login dialog on HTTP status 403. Null is allowed.
     */
    public TokBoxJWTRequest( Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener, FragmentManager fragmentManager) {
        super(Method.POST, tokBoxUrl, " ", JSONObject.class, successListener, errorListener, fragmentManager, getJWT());
        this.accessToken = getJWT();
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            setStatusCode(response.statusCode);
            if (BuildConfig.DEBUG) {
                Timber.d("%s URL: %s. ResponseCode: %d", this.getClass().getSimpleName(), tokBoxUrl, response.statusCode);
                JSONObject testSession = new JSONObject("{\"session_id\":\"1_MX40NTc1NzMzMn5-MTQ4NjI5MTE3MTY0OX5WU0xINUFQaXpCcFh4ZTRlbFUzOEs1Zzd-fg\",\"project_id\":\"45757332\",\"partner_id\":\"45757332\",\"create_dt\":\"Sun Feb 05 02:34:13 PST 2017\",\"media_server_url\":\"\"}");
                return Response.success(testSession,HttpHeaderParser.parseCacheHeaders(response));
            }
            // Parse response and return obtained object
            String json = new String(response.data, "utf-8").replace('[',' ').replace(']',' ').trim();
            JSONObject result = new JSONObject(json);
            //JSONObject result = Utils.getGsonParser().fromJson(json, JSONObject.class);
            if (result == null) return Response.error(new ParseError(new NullPointerException()));
            else return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Client-Version", MyApplication.APP_VERSION);
        headers.put("Device-Token", MyApplication.ANDROID_ID);
        headers.put("Accept", "application/json");
        //connection.setRequestProperty("X-TB-PARTNER-AUTH", "KEYID:SECRETKEY"); //This is old method. after july will be depreciated by opentok.



        // Determine if request should be authorized.
        if (accessToken != null && !accessToken.isEmpty()) {
            headers.put("X-OPENTOK-AUTH", accessToken);
        }
        return headers;
    }
    protected static String getJWT() {
        final long ONE_MINUTE_IN_MILLIS=1000;
        long t= Calendar.getInstance().getTimeInMillis();
        Date currentTime=new Date(t);
        Date fiveMinutesLater=new Date(t + (5 * 60 * ONE_MINUTE_IN_MILLIS));

        String payload =  Jwts.builder()
                .setIssuedAt(currentTime)
                .setIssuer(tokBoxApiKey)
                .setExpiration(fiveMinutesLater)
                .claim("ist", "project")
                .setHeaderParam("typ","JWT")
                .signWith(SignatureAlgorithm.HS256, Base64.encodeToString(tokBoxSecret.getBytes(),0))
                .compact();
        return payload;
    }

    public static String CreateToken(String sessionId, String role, String data, long expireInMinutes) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        String token="";
        double timeNow = Math.floor(Calendar.getInstance().getTimeInMillis()/1000);
        double expire= timeNow + (expireInMinutes * 60);
        double rand =  Math.random() * 999999;
        String dataString;
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(0);
        dataString = "session_id=" + sessionId + "&create_time=" + df.format(timeNow) + "&expire_time=" + df.format(expire) + "&role=" + role + "&connection_data=" + data + "&nonce=" + rand;
        String hash = calculateRFC2104HMAC(dataString, tokBoxSecret);
        String preCoded = "partner_id="+tokBoxApiKey+"&sig="+hash+":"+dataString;
        token = "T1=="+ Base64.encodeToString(preCoded.getBytes(),0);
        if(BuildConfig.DEBUG){
            return "T1==cGFydG5lcl9pZD00NTc1NzMzMiZzaWc9MWNiMDljYzdiYmYxNWRiZThjZmVlNzExY2IxODc5MjNkZWE3ZTY0MTpzZXNzaW9uX2lkPTFfTVg0ME5UYzFOek16TW41LU1UUTROakk1TVRFM01UWTBPWDVXVTB4SU5VRlFhWHBDY0ZoNFpUUmxiRlV6T0VzMVp6ZC1mZyZjcmVhdGVfdGltZT0xNDg2MjkxMzc3Jm5vbmNlPTAuMzQyNDIwNDIxODk2NjQ3NTYmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTQ4ODg4MzM3Ng==";
        }
        return token;
    }


    private static String calculateRFC2104HMAC(String data, String secret)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
    {
        SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);

        Formatter formatter = new Formatter();
        for (byte b : mac.doFinal(data.getBytes())) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }
}

/*
final TokBoxJWTRequest tokBoxJWTRequest = new TokBoxJWTRequest(new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String sessionId = response.getString("session_id");
                                    String token =  TokBoxJWTRequest.CreateToken(sessionId,"Publisher","",60);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                } catch (SignatureException e) {
                                    e.printStackTrace();
                                } catch (InvalidKeyException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }, getFragmentManager());

                        tokBoxJWTRequest.setRetryPolicy(MyApplication.getDefaultRetryPolice());
                        tokBoxJWTRequest.setShouldCache(false);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MyApplication.getInstance().addToRequestQueue(tokBoxJWTRequest, CONST.NOTIFICATION_SEND_TAG);
                            }
                        }, 75);
 */
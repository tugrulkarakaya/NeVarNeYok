package uk.co.nevarneyok.api;

import android.support.v4.app.FragmentManager;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONObject;

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
import uk.co.nevarneyok.MyApplication;
import uk.co.nevarneyok.controllers.AppSettingController;

/**
 * Created by mcagri on 27/01/2017.
 */

public class TokBoxJWTRequest extends JsonRequest {
    static String tokBoxApiKey = AppSettingController.getSetting("tokboxKey");
    static String tokBoxSecret = AppSettingController.getSetting("tokboxSecret");
    static String tokBoxUrl = AppSettingController.getSetting("tokboxUrl");
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private final String accessToken;


    /**
     * Create a new authorized request.
     *
     * @param jsonRequest     A {@link JSONObject} to post with the request. Null is allowed and
     *                        indicates no parameters will be posted along with request.
     * @param successListener Listener to retrieve successful response
     * @param errorListener   Error listener, or null to ignore errors
     * @param fragmentManager Manager to create re-login dialog on HTTP status 403. Null is allowed.
     * @param accessToken     Token identifying user used for user specific requests.
     */
    public TokBoxJWTRequest( JSONObject jsonRequest, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener, FragmentManager fragmentManager, String accessToken) {
        super(Method.POST, tokBoxUrl, jsonRequest, successListener, errorListener, fragmentManager, accessToken);
        this.accessToken = accessToken;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Client-Version", MyApplication.APP_VERSION);
        //headers.put("Device-Token", MyApplication.ANDROID_ID);
        //headers.put("X-OPENTOK-AUTH", payload); //This is old method. after july will be depreciated by opentok.
        headers.put("Accept", "application/json");

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

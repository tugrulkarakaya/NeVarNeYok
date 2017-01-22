package uk.co.nevarneyok.controllers;


import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


public class OpenTokRestApiController extends AsyncTask<String, Void, String> {

    public interface AsyncResponse {
        void processFinish(String sessionId);
    }

    AsyncResponse delegate;
    static String tokBoxApiKey = AppSettingController.getSetting("tokboxKey");
    static String tokBoxSecret = AppSettingController.getSetting("tokboxSecret");

    public OpenTokRestApiController(AsyncResponse delegate){
        this.delegate = delegate;
    }

    public String getJWT() {
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

    @Override
    protected String doInBackground(String... params) {

        String payload = getJWT();

        try {
            String OPEN_TOK_API = "https://api.opentok.com/session/create";
            URL url = new URL(OPEN_TOK_API);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("X-OPENTOK-AUTH", payload);
            connection.setRequestProperty("Accept", "application/json");
            //connection.setRequestProperty("X-TB-PARTNER-AUTH", "45737392:db4fde14b51a4032a838a1ba64c865d19a14be01");
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());

            out.write("location:10.1.200.30");
            out.close();

            int responseCode = connection.getResponseCode();
            if(responseCode>399){
                //error
                return "";
            }
            StringBuffer response = new StringBuffer();
            String inputLine;

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();

        } catch (Exception e) {
            System.out.println("\nError while calling OpenTok REST Service");
            System.out.println(e);
        }
        return "";
    }
    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject json = new JSONObject(result.replace('[',' ').replace(']',' ').trim());
            String sessionId = json.getString("session_id");
            delegate.processFinish(sessionId);

        } catch (JSONException e) {
            //data is not json type probably
            e.printStackTrace();
        }
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
        //http://tokbox.com/blog/generating-tokens-without-server-side-sdk/
    }


    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }

    public static String calculateRFC2104HMAC(String data, String secret)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
    {
        SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return toHexString(mac.doFinal(data.getBytes()));
    }

}


/*
Call Method
               final String[] executionResult = {""};
               final OpenTokRestApiController openTokController = new OpenTokRestApiController(new OpenTokRestApiController.AsyncResponse() {

                   @Override
                   public void processFinish(String sessionId) {
                       executionResult[0] = sessionId;
                   }
               });

               openTokController.execute();
                   }

 */
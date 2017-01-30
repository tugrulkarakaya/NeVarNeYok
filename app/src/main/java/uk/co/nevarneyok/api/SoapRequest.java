package uk.co.nevarneyok.api;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import java.net.Proxy;

/**
 * Created by tugrulk on 29/01/2017.
 */

public class SoapRequest extends AsyncTask<Void, Void, String> {

    private final Response.Listener<String> successListener;
    private String soapAction;      //nameSpace + methodName
    private String methodName;      //gfGet_AuthorizationTicket
    private String convertParams;
    private String nameSpace;       //http://karincasoft.com/KRNC_WS/Login09
    private String requestUrl;      //http://acente.turaturizm.com.tr/KRNC_WS/WS/Login09.asmx

    private final static String TAG = SoapRequest.class.getSimpleName();

    public SoapRequest(String soapAction, String requestUrl, String nameSpace, String methodName,  String convertParams, Response.Listener<String> successListener) {
        this.successListener = successListener;
        this.methodName = methodName;
        this.soapAction = soapAction;
        this.nameSpace = nameSpace;
        this.requestUrl = requestUrl;
        this.convertParams = convertParams;
    }

    @Override
    protected String doInBackground(Void... params) {
        //create a new soap request object
        SoapObject soapRequests = new SoapObject(nameSpace, methodName);
        //add properties for soap object
        //soapRequests.addProperty(convertParams, params[0]);
        soapRequests.addProperty("sTurop", "MRK");
        soapRequests.addProperty("sUser", "MOBILE");
        soapRequests.addProperty("sPass", "MOBILE");

        //request to server and get Soap Primitive response
        try {
            StringBuffer result;
            SoapSerializationEnvelope envelope = getSoapSerializationEnvelope(soapRequests);

            HttpTransportSE ht = getHttpTransportSE();
            ht.call(soapAction, envelope);
            SoapObject response =  (SoapObject) envelope.getResponse();

            result = new StringBuffer(response.toString());
            Log.i(TAG, "result: " + result.toString());
            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result == null) {
            Log.i(TAG, "cannot get result");
        } else {
            //invoke call back method of Activity
            //activity.callBackDataFromAsyncTask(result);
            if (successListener != null)
                successListener.onResponse(result);
        }
    }

    private final SoapSerializationEnvelope getSoapSerializationEnvelope(SoapObject request) {
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.dotNet = true;
        envelope.implicitTypes = true;
        envelope.setAddAdornments(false);
        envelope.setOutputSoapObject(request);

        return envelope;
    }

    private final HttpTransportSE getHttpTransportSE() {
        HttpTransportSE ht = new HttpTransportSE(Proxy.NO_PROXY, requestUrl, 60000);
        ht.debug = true;
        ht.setXmlVersionTag("<!--?xml version=\"1.0\" encoding= \"UTF-8\" ?-->");
        return ht;
    }
}

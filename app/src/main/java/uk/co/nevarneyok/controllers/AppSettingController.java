package uk.co.nevarneyok.controllers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import uk.co.nevarneyok.R;
import uk.co.nevarneyok.api.FIRDataServices;
import uk.co.nevarneyok.utils.MsgUtils;
/*
Firebase'e izin ekledim. biraz tehlikeli gibi. düşünelim üzerinde.
"{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null",
    "Application": {
    	  "Parameters":{
          "public":{
        			".read":true
        }
      }
    }
  }
}
 */

public class AppSettingController {
    private DatabaseReference publicSettingsRef;
    private DatabaseReference privateSettingsRef;
    private  Boolean isFetched = false;
    public final Map<String, String> parameters = new HashMap<String, String>();
    public static AppSettingController appSettingController;

    private  AppSettingController(){}


    public static AppSettingController getAppSettingController(){
        if(appSettingController==null){
            appSettingController = new AppSettingController();
        }
        return appSettingController;
    }


    public void loadSettings(final AsyncResponse delegate) {
        if (isFetched) {
            delegate.processFinish(isFetched);
            return;
        }
        publicSettingsRef = FIRDataServices.PublicParametersRef;
        privateSettingsRef = FIRDataServices.PrivateParametersRef;

        try {
            publicSettingsRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ConSnapshot : dataSnapshot.getChildren()) {
                        String value = ConSnapshot.getValue().toString();
                        String key = ConSnapshot.getKey();
                        parameters.put(key, value);
                    }

                    try {
                        privateSettingsRef.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ConSnapshot : dataSnapshot.getChildren()) {
                                    String value = ConSnapshot.getValue().toString();
                                    String key = ConSnapshot.getKey();
                                    parameters.put(key, value);
                                }
                                isFetched = true;
                                delegate.processFinish(isFetched);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                delegate.processFinish(isFetched);
                            }

                        });
                    } catch (Exception ex) {
                        delegate.processFinish(isFetched);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    delegate.processFinish(isFetched);
                }

            });
        } catch (Exception ex) {
            MsgUtils.showToast(R.string.data_retrieve_error, MsgUtils.TOAST_TYPE_MESSAGE, MsgUtils.ToastLength.LONG);
        }
    }

    public static void refreshSettings(AsyncResponse delegate){
        appSettingController.isFetched = false;
        appSettingController.loadSettings(delegate);
    }

    public final static String getSetting(String key){
        if(appSettingController.parameters.containsKey(key)) {
            return appSettingController.parameters.get(key);
        } else {
            return null;
        }
    }

    public interface AsyncResponse {
        void processFinish(Boolean isFetched);
    }

}

package uk.co.nevarneyok.controllers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class AppSettingController {
    private FirebaseDatabase myFirebaseDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference refAppSettings;
    private Boolean isFetched = false;
    public static final Map<String, String> parameters = new HashMap<String, String>();


    public void loadSettings(final AsyncResponse delegate) {
        if (isFetched){
            delegate.processFinish(isFetched);
            return;
        }
        refAppSettings = myFirebaseDatabase.getReference().child("Application").child("parameters");
        refAppSettings.addListenerForSingleValueEvent(new ValueEventListener() {

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
    }

    public void refreshSettings(AsyncResponse delegate){
        isFetched = false;
        loadSettings(delegate);
    }

    public final static String getSetting(String key){
        if(parameters.containsKey(key)) {
            return parameters.get(key);
        } else {
            return null;
        }
    }

    public interface AsyncResponse {
        void processFinish(Boolean isFetched);
    }

}

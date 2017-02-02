package uk.co.nevarneyok.services;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.entities.User;

/**
 * Created by mcagrikarakaya on 29.01.2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";
    User activeUser = SettingsMy.getActiveUser();
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }
    private void sendRegistrationToServer(String token) {
        DatabaseReference IIDToken = FirebaseDatabase.getInstance().getReference();
        if (activeUser != null) {
            IIDToken.child("users").child(activeUser.getUid()).child("IIDToken").setValue(token);
        }
        // TODO: Implement this method to send token to your app server.
    }
}

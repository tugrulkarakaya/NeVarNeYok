package uk.co.nevarneyok.controllers;

import android.content.Context;
import android.net.Uri;

import uk.co.nevarneyok.MyApplication;
import uk.co.nevarneyok.R;
import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.api.FIRDataServices;
import uk.co.nevarneyok.entities.User;
import uk.co.nevarneyok.utils.MsgUtils;
import uk.co.nevarneyok.ux.MainActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;


public class UserController  {
    public interface FirebaseCallResult{
        void onComplete(boolean result);
    }
    public interface completion{
        void setResult(boolean result, User user);
    }

    private User user;
    public User getUser(){return user;}

    public void setUser(User user){this.user = user;}

    private DatabaseReference getFirReference() {
        try {
            if (user.getUid() == null || user.getUid().trim().length() == 0) {
                String newKey = FIRDataServices.DBUserRef.push().getKey();
                user.setUid(newKey);
            }
            return FIRDataServices.DBUserRef.child(user.getUid());
        }
        catch(Exception ex){
            MsgUtils.showToast("", MsgUtils.TOAST_TYPE_INTERNAL_ERROR, MsgUtils.ToastLength.LONG);
        }
        return null;
    }

    public UserController(User user){
        super();
        this.user = user;

    }

    public void save(final FirebaseCallResult callResult) {
        try {
            getFirReference().setValue(this.user, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                if (user.getCreateDate() == 0) {
                                    getFirReference().child("createDate").setValue(ServerValue.TIMESTAMP);
                                }
                                callResult.onComplete(true);
                            } else {
                                callResult.onComplete(false);
                            }
                        }
                    }
            );
        } catch(Exception e){
            MsgUtils.showToast(R.string.save_error, MsgUtils.TOAST_TYPE_MESSAGE, MsgUtils.ToastLength.LONG);
        }
    }

    public void getAuthInfo(){
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {

                String displayName = user.getDisplayName();
                if (displayName != null && displayName.trim().length() > 0) {
                    this.user.setName(user.getDisplayName());
                }

                String email = user.getEmail();
                if (email != null && email.trim().length() > 0) {
                    this.user.setEmail(user.getEmail());
                }
                Uri photoUrl = user.getPhotoUrl();

                // The user's ID, unique to the Firebase project. Do NOT use this value to
                // authenticate with your backend server, if you have one. Use
                // FirebaseUser.getToken() instead.
                String uid = user.getUid();
            }
        }
        catch(Exception ex){
            MsgUtils.showToast("", MsgUtils.TOAST_TYPE_INTERNAL_ERROR, MsgUtils.ToastLength.LONG);
        }
    }

    public void saveAndRetrieveData(final completion callResult) {
        retrieveData(new completion() {
            @Override
            public void setResult(boolean result, User user) {
                if(result)
                {
                   callResult.setResult(true,user);
                }else{
                    save(new FirebaseCallResult() {
                        @Override
                        public void onComplete(boolean result) {
                            if(result) {
                                retrieveData(callResult);
                            }else{
                                callResult.setResult(false,null);
                            }
                        }
                    });
                }
            }
        });
    }

    public void retrieveData(final completion callResult) {
        try{
            getFirReference().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        user = dataSnapshot.getValue(User.class);
                        callResult.setResult(true, user);
                    } else {
                        callResult.setResult(false, user);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callResult.setResult(false, user);
                }
            });
        }
        catch(Exception e){
            MsgUtils.showToast(R.string.data_retrieve_error, MsgUtils.TOAST_TYPE_MESSAGE, MsgUtils.ToastLength.LONG);
        }
    }

    public static void signOut(){
        try {
            FirebaseAuth.getInstance().signOut();
            SettingsMy.setActiveUser(null);
            MainActivity.invalidateDrawerMenuHeader();
        } catch(Exception ex){
            MsgUtils.showToast(R.string.Sign_out_error, MsgUtils.TOAST_TYPE_MESSAGE, MsgUtils.ToastLength.LONG);
        }

    }
}

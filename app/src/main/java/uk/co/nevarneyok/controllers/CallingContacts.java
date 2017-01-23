package uk.co.nevarneyok.controllers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.entities.Contact;
import uk.co.nevarneyok.entities.User;
import uk.co.nevarneyok.utils.MsgUtils;

/**
 * Created by mcagrikarakaya on 21.01.2017.
 */

public class CallingContacts {
    static User activeUser = SettingsMy.getActiveUser();
    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("contacts").child(activeUser.getUid()).child("callinggroups");
    DatabaseReference getFirReference = FirebaseDatabase.getInstance().getReference("contacts").child(activeUser.getUid());

    public interface completion{
        void setResult(boolean result);
    }

    public void addCallingGroup(String groupName, String contactsKey, Contact contact){
        try {
            myRef.child(groupName).child(contactsKey).setValue(contact);
        }catch(Exception ex){
            MsgUtils.showToast("", MsgUtils.TOAST_TYPE_INTERNAL_ERROR, MsgUtils.ToastLength.LONG);
        }

    }

    public void removeCallingGroup(String groupName, String contactsKey){
        try{
            myRef.child(groupName).child(contactsKey).removeValue();
        }catch(Exception ex){
            MsgUtils.showToast("", MsgUtils.TOAST_TYPE_INTERNAL_ERROR, MsgUtils.ToastLength.LONG);
        }
    }

    public void existsData(final completion callResult) {
        try{
            getFirReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        callResult.setResult(true);
                    } else {
                        callResult.setResult(false);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callResult.setResult(false);
                }
            });
        } catch(Exception ex){
            MsgUtils.showToast("", MsgUtils.TOAST_TYPE_INTERNAL_ERROR, MsgUtils.ToastLength.LONG);
        }
    }

}
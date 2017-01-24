package uk.co.nevarneyok.controllers;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.entities.Contact;
import uk.co.nevarneyok.entities.User;
import uk.co.nevarneyok.utils.MsgUtils;

/**
 * Created by mcagrikarakaya on 21.01.2017.
 */

public class CallingContacts {
    User activeUser = SettingsMy.getActiveUser();
    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("contacts").child(activeUser.getUid()).child("callinggroups");
    DatabaseReference getFirReference = FirebaseDatabase.getInstance().getReference("contacts").child(activeUser.getUid());
    DatabaseReference pushRef;
    Query myQueryRef;

    HashMap<String,Contact> Contacts = new HashMap<String, Contact>();
    ArrayList<Contact> addContactList=new ArrayList<Contact>();
    ArrayList<String> removeContactList = new ArrayList<String>();
    public interface completion{
        void setResult(boolean result);
    }
    public interface getContactsCompletion{
        void setResult(HashMap<String,Contact> ContactHashMap);
    }
    public interface getUserCompletion{
        void setResult(String key, User user);
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

    public void refreshContacts(final ArrayList<Contact> contactlist){
        getContacts(new getContactsCompletion() {
            @Override
            public void setResult(HashMap<String, Contact> ContactHashMap) {
                boolean remove=true;
                boolean add = true;
                for (Map.Entry<String, Contact> entry:
                ContactHashMap.entrySet()){
                    String key=entry.getKey();
                    Contact c=entry.getValue();
                    for (int i = 0; i < contactlist.size(); i++) {
                        if (c.getPhone().equals(contactlist.get(i).getPhone()) && c.getName().equals(contactlist.get(i).getName())) {
                            remove=false;
                        }
                    }
                    if (remove){
                        removeContactList.add(key);
                    }
                    remove=true;
                }
                for (int i = 0; i < contactlist.size(); i++) {
                    for (Map.Entry<String, Contact> entry:
                            ContactHashMap.entrySet()) {
                        Contact c=entry.getValue();
                        if (contactlist.get(i).getName().equals(c.getName()) && contactlist.get(i).getPhone().equals(c.getPhone())) {
                            add=false;
                        }
                    }
                    if (add) {
                        addContactList.add(contactlist.get(i));
                    }
                    add=true;
                }
                for(Contact contact : addContactList){
                    pushRef = getFirReference.child("contacts").push();
                    pushRef.setValue(contact);
                }
                for (String key :
                        removeContactList) {
                    getFirReference.child("contacts").child(key).removeValue();
                }
            }
        });
    }
    public void getContacts(final getContactsCompletion getContactsCompletion){
        getFirReference.child("contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot contactSnapshot : dataSnapshot.getChildren()) {
                    Contacts.put(contactSnapshot.getKey(),contactSnapshot.getValue(Contact.class));
                }
                getContactsCompletion.setResult(Contacts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getUserExist(final getUserCompletion getUserCompletion){


        myQueryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot :
                        dataSnapshot.getChildren()) {
                    if (userSnapshot.exists()) {
                        getUserCompletion.setResult(userSnapshot.getKey(),userSnapshot.getValue(User.class));
                        break;
                    }
                    else getUserCompletion.setResult(null,null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkFriendsGroup(String phone, final String contactkey){
        myQueryRef = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("phone").equalTo(phone);
        getUserExist(new getUserCompletion() {
            @Override
            public void setResult(String key, User user) {
                myRef.child("friends").child(contactkey).child("uid").setValue(key);
                if (user.getProfileImageUrl() != null) {
                    myRef.child("friends").child(contactkey).child("photoUrl").setValue(user.getProfileImageUrl());
                }


            }
        });
    }
}
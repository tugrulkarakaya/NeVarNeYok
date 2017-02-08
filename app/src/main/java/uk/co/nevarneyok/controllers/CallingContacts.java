package uk.co.nevarneyok.controllers;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.api.EndPoints;
import uk.co.nevarneyok.entities.Contact;
import uk.co.nevarneyok.entities.User;
import uk.co.nevarneyok.utils.MsgUtils;

/**
 * Created by mcagrikarakaya on 21.01.2017.
 * Kişi Listesi ile ilgili işlemler bu sayfa ile yapılıyor.
 */

public class CallingContacts {
    private User activeUser = SettingsMy.getActiveUser();
    private DatabaseReference myRef =  FirebaseDatabase.getInstance().getReference(EndPoints.FIREBASE_CONTACTS).child(activeUser.getUid()).child(EndPoints.FIREBASE_CALLING_GROUPS);
    private DatabaseReference getFirReference = FirebaseDatabase.getInstance().getReference(EndPoints.FIREBASE_CONTACTS).child(activeUser.getUid());
    private DatabaseReference pushRef;
    private Query myQueryRef;

    private HashMap<String,Contact> Contacts = new HashMap<>();
    private ArrayList<Contact> addContactList= new ArrayList<>();
    private ArrayList<String> removeContactList = new ArrayList<>();
    public interface completion{
        void setResult(boolean result);
    }
    interface getContactsCompletion{
        void setResult(HashMap<String,Contact> ContactHashMap);
    }
    interface getUserCompletion{
        void setResult(String key, User user);
    }
    public interface getFriendsCountsCompletion{
        void setResult(boolean counts, Contact contact);
    }

    public void addCallingGroup(String groupName, String contactsKey, Contact contact){
        try {
            myRef.child(groupName).child(contactsKey).setValue(contact);
            getFirReference.child(EndPoints.FIREBASE_CONTACTS).child(contactsKey).child(EndPoints.FIREBASE_ADDED).setValue(true);
        }catch(Exception ex){
            MsgUtils.showToast("", MsgUtils.TOAST_TYPE_INTERNAL_ERROR, MsgUtils.ToastLength.LONG);
        }

    }

    public void removeCallingGroup(final String groupName, final String contactsKey){
        try{
            myRef.child(groupName).child(contactsKey).removeValue();
            getFirReference.child(EndPoints.FIREBASE_CONTACTS).child(contactsKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        getFirReference.child(EndPoints.FIREBASE_CONTACTS).child(contactsKey).child(EndPoints.FIREBASE_ADDED).setValue(false);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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
                    pushRef = getFirReference.child(EndPoints.FIREBASE_CONTACTS).push();
                    pushRef.setValue(contact);
                }
                for (String key :
                        removeContactList) {
                    getFirReference.child(EndPoints.FIREBASE_CONTACTS).child(key).removeValue();
                }
            }
        });
    }
    private void getContacts(final getContactsCompletion getContactsCompletion){
        getFirReference.child(EndPoints.FIREBASE_CONTACTS).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void getUserExist(final getUserCompletion getUserCompletion){


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
        myQueryRef = FirebaseDatabase.getInstance().getReference().child(EndPoints.FIREBASE_USERS).orderByChild(EndPoints.FIREBASE_PHONE).equalTo(phone);
        getUserExist(new getUserCompletion() {
            @Override
            public void setResult(String key, User user) {
                myRef.child(EndPoints.FIREBASE_FRIENDS).child(contactkey).child(EndPoints.FIREBASE_UID).setValue(key);
                if (user.getProfileImageUrl() != null) {
                    myRef.child(EndPoints.FIREBASE_FRIENDS).child(contactkey).child(EndPoints.FIREBASE_PHOTO_URL).setValue(user.getProfileImageUrl());
                }


            }
        });
    }
    public void getFriendsCount(final getFriendsCountsCompletion getFriendsCountsCompletion){
        FirebaseDatabase.getInstance().getReference().child(EndPoints.FIREBASE_CONTACTS)
                .child(activeUser.getUid()).child(EndPoints.FIREBASE_CALLING_GROUPS)
                .child(EndPoints.FIREBASE_FRIENDS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Random rand = new Random();

                        Contact contact;
                        ArrayList<Contact> contactList= new ArrayList<>();
                        for (DataSnapshot contactsnapshot :
                                dataSnapshot.getChildren()) {
                            if(contactsnapshot.getValue(Contact.class).getUid()!=null){
                                contactList.add(contactsnapshot.getValue(Contact.class));
                            }
                        }

                        if(contactList.size()>=2){
                            int n = rand.nextInt(contactList.size());
                            contact=contactList.get(n);
                            getFriendsCountsCompletion.setResult(true, contact);
                        }else{
                            getFriendsCountsCompletion.setResult(false, null);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
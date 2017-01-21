package uk.co.nevarneyok.controllers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.entities.Contact;
import uk.co.nevarneyok.entities.User;

/**
 * Created by mcagrikarakaya on 21.01.2017.
 */

public class CallingContacts {
    static User activeUser = SettingsMy.getActiveUser();
    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("contacts").child(activeUser.getUid()).child("callinggroups");
    public void addCallingGroup(String groupName, String contactsKey, Contact contact){
        myRef.child(groupName).child(contactsKey).setValue(contact);
    }

    public void removeCallingGroup(String groupName, String contactsKey){
        myRef.child(groupName).child(contactsKey).removeValue();
    }

}
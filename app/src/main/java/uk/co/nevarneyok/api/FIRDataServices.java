package uk.co.nevarneyok.api;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class FIRDataServices {
    public static FirebaseDatabase DBBase =FirebaseDatabase.getInstance();
    public static DatabaseReference DBUserRef = DBBase.getReference().child("users");
}

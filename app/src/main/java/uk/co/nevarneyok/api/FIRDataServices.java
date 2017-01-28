package uk.co.nevarneyok.api;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class FIRDataServices {
    public final static FirebaseDatabase DBBase =FirebaseDatabase.getInstance();
    public final static DatabaseReference DBUserRef = DBBase.getReference().child("users");
    public final static StorageReference StorageBase = FirebaseStorage.getInstance().getReference();
    public final static StorageReference StorageUser = StorageBase.child("users");

}

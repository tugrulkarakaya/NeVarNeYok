package uk.co.nevarneyok.api;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class FIRDataServices {
    public final static FirebaseDatabase DBBase =FirebaseDatabase.getInstance();
    public final static DatabaseReference DBUserRef = DBBase.getReference().child("users");
    public final static DatabaseReference ApplicationRef = DBBase.getReference().child("Application");
    public final static DatabaseReference PublicParametersRef = ApplicationRef.child("Parameters").child("public");
    public final static DatabaseReference PrivateParametersRef = ApplicationRef.child("Parameters").child("private");

    public final static StorageReference StorageBase = FirebaseStorage.getInstance().getReference();
    public final static StorageReference StorageUser = StorageBase.child("users");

}

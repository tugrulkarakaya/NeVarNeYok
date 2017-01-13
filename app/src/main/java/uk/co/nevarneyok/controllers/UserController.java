package uk.co.nevarneyok.controllers;

import com.google.firebase.auth.FirebaseAuth;

import uk.co.nevarneyok.api.FIRDataServices;
import uk.co.nevarneyok.entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class UserController {
    private User user;

    public User getUser(){return user;}

    public void setUser(User user){this.user = user;}

    private DatabaseReference getFirReference() {
        return FIRDataServices.DBUserRef.child(user.getUid());
    }

    public UserController(){

    }
    public UserController(User user){
        this.user = user;
    }
    public interface completion{
        void setResult(boolean result);
    }

    public void isUserRecorded(final completion callResult ){
        getFirReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    callResult.setResult(true);
                } else{
                    callResult.setResult(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void createUserRecord(){
        getFirReference().child("created").setValue(ServerValue.TIMESTAMP);
        getFirReference().child("Uid").setValue(getFirReference().getKey());
    }

    public void retrieveData(final completion callResult){
        getFirReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    user = dataSnapshot.getValue(User.class);
                    callResult.setResult(true);
                }else{
                    callResult.setResult(false);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                callResult.setResult(false);
            }
        });
    }

    public void signOut(){
        FirebaseAuth.getInstance().signOut();
    }
}

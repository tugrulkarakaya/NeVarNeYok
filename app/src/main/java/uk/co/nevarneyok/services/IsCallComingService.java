package uk.co.nevarneyok.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.api.TokBoxJWTRequest;
import uk.co.nevarneyok.entities.CallConnection;
import uk.co.nevarneyok.entities.User;
import uk.co.nevarneyok.ux.IncomingCallActivity;
import uk.co.nevarneyok.ux.OpenTokVideoActivity;

public class IsCallComingService extends Service {
    DatabaseReference myFirebaseRef ;
    private User activeUser = SettingsMy.getActiveUser();
    private String OpponentConnectionKey;
    private String SessionId;
    private String TOKEN;

    public IsCallComingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SessionId=intent.getExtras().getString("Sessionid");
        try {
            TOKEN =  TokBoxJWTRequest.CreateToken(SessionId,"Publisher","",60);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if(activeUser!=null){
            myFirebaseRef = FirebaseDatabase.getInstance().getReference();
            myFirebaseRef.child("connections").child(activeUser.getUid()).limitToLast(1).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                    CallConnection con=dataSnapshot.getValue(CallConnection.class);
                    if (con.getDirection()==0 && (con.getCallanswer()==2 ) || con.getCallanswer()==1){//yeni gelen çağrı varsa altta incomingcall sayfasına yolluyorum.

                        final Intent intent;
                        if (  con.getCallanswer()==1){
                            intent=new Intent(getApplicationContext(),OpenTokVideoActivity.class);
                        }
                        else{
                            intent=new Intent(getApplicationContext(),IncomingCallActivity.class);//Gideceğim sayfayı tanımlıyorum
                        }
                        intent.putExtra("OpponentId",con.getCallerid());//arayan kişinin Id'sini ekstra bilgi olarak ekliyorum
                        intent.putExtra("MyConnectionKey",dataSnapshot.getKey());
                        intent.putExtra("SessionId",SessionId);
                        intent.putExtra("TOKEN",TOKEN);
                        myFirebaseRef.child("connections").child(con.getCallerid()).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot conSnap:dataSnapshot.getChildren()){
                                    OpponentConnectionKey=conSnap.getKey();
                                    intent.putExtra("OpponentConnectionKey",OpponentConnectionKey);
                                }
                                startActivity(intent);//Sayfayı açıyorum.
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }


                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}

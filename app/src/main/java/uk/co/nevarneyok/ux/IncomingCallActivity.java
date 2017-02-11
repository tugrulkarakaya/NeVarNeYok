package uk.co.nevarneyok.ux;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.co.nevarneyok.BuildConfig;
import uk.co.nevarneyok.R;
import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.controllers.BeepRunnable;
import uk.co.nevarneyok.entities.User;

public class IncomingCallActivity extends AppCompatActivity {
    ImageView photo;
    ImageButton accept;
    ImageButton rejected;
    private MediaPlayer ses;
    private BeepRunnable beepRunnable;

    private User activeUser = SettingsMy.getActiveUser();

    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference myFirebaseRef;
    String TOKEN;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        photo=(ImageView)findViewById(R.id.opponent_picture);
        accept=(ImageButton)findViewById(R.id.accept);
        rejected=(ImageButton)findViewById(R.id.rejected);
        myFirebaseRef=firebaseDatabase.getReference();
        ses = MediaPlayer.create(this,R.raw.music);
        View view =findViewById(android.R.id.content);
        beepRunnable = new BeepRunnable(view, 30, 1500);
        view.post(beepRunnable);

        final String OpponentConnectionKey=getIntent().getExtras().getString("OpponentConnectionKey");
        final String MyConnectionKey=getIntent().getExtras().getString("MyConnectionKey");
        final String OpponentId=getIntent().getExtras().getString("OpponentId");
        final String SessionId=getIntent().getExtras().getString("Sessionid");
        TOKEN=getIntent().getExtras().getString("TOKEN");
        myFirebaseRef.child("connections").child(activeUser.getUid()).child(MyConnectionKey).child("callanswer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() instanceof Long) {
                    if ((long) dataSnapshot.getValue() > 100) {
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myFirebaseRef.child("connections").child(activeUser.getUid()).child(MyConnectionKey).child("callanswer").setValue(1);
                myFirebaseRef.child("connections").child(OpponentId).child(OpponentConnectionKey).child("callanswer").setValue(1);
                Intent intent=new Intent(getApplicationContext(),OpenTokVideoActivity.class);
                intent.putExtra("user_id",activeUser.getUid());
                intent.putExtra("OpponentId",OpponentId);
                intent.putExtra("MyConnectionKey",MyConnectionKey);
                intent.putExtra("OpponentConnectionKey",OpponentConnectionKey);
                intent.putExtra("SessionId",SessionId);
                if(BuildConfig.DEBUG){
                    TOKEN="T1==cGFydG5lcl9pZD00NTc1NzMzMiZzaWc9NDYzMDY0NTdhM2ZkOWM0Yjk1OGViOWU3MmI5NTI0Mz" +
                            "BiYTE4OGJmMjpzZXNzaW9uX2lkPTFfTVg0ME5UYzFOek16TW41LU1UUTROakk1TVRFM01UWTBPWD" +
                            "VXVTB4SU5VRlFhWHBDY0ZoNFpUUmxiRlV6T0VzMVp6ZC1mZyZjcmVhdGVfdGltZT0xNDg2NzM2Nz" +
                            "kzJm5vbmNlPTAuMTYzMzgxNjExMTgyODM5Njcmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTQ4OTMyODgxOQ==";
                }
                intent.putExtra("TOKEN",TOKEN);
                startActivity(intent);
                finish();
            }
        });

        rejected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myFirebaseRef.child("connections").child(activeUser.getUid()).child(MyConnectionKey).child("callanswer").setValue(101);
                myFirebaseRef.child("connections").child(OpponentId).child(OpponentConnectionKey).child("callanswer").setValue(101);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}

package uk.co.nevarneyok.ux.fragments;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Collections;

import timber.log.Timber;
import uk.co.nevarneyok.CONST;
import uk.co.nevarneyok.MyApplication;
import uk.co.nevarneyok.R;
import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.api.NotificaitonRequest;
import uk.co.nevarneyok.api.TokBoxJWTRequest;
import uk.co.nevarneyok.controllers.CallingContacts;
import uk.co.nevarneyok.entities.CallConnection;
import uk.co.nevarneyok.entities.Contact;
import uk.co.nevarneyok.entities.FIRNotificaiton;
import uk.co.nevarneyok.entities.User;
import uk.co.nevarneyok.utils.MsgUtils;
import uk.co.nevarneyok.ux.OpenTokVideoActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class CallBuddyFragment extends Fragment {
    Button callBuddyButton;
    CallingContacts callingContacts;
    private static FrameLayout framelayout;

    private String connectionKey;
    private String MyConnectionKey;

    private DatabaseReference refMyUser;
    private DatabaseReference refOpponentUserCon;
    private DatabaseReference refCallAnswer;
    private DatabaseReference refGetOpponentIID;

    private FirebaseDatabase myFirebaseDatabase=FirebaseDatabase.getInstance();
    private FirebaseDatabase myOpFirebaseDatabase=FirebaseDatabase.getInstance();

    User activeUser = SettingsMy.getActiveUser();
    Contact Callcontact;


    String accessToken = "AAAABmit7S4:APA91bEQvRmLoo_q_yvvyqdsn3nbSwUTpwEk0f8h-PSH3hLLDP5ZOFw5prZ1QLlGSXjHLxGDMkhl0KDT4Tcp6XXXMdof9h8B0R55ATSiWADEjASetuQjn_a4RuKlrY8pCLAUj19wrRxB";


    final TokBoxJWTRequest tokBoxJWTRequest = new TokBoxJWTRequest(new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {

                final String sessionId = response.getString("session_id");
                String token =  TokBoxJWTRequest.CreateToken(sessionId,"Publisher","",60);

                refMyUser=myFirebaseDatabase.getReference().child("connections").child(activeUser.getUid());
                refMyUser.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        CallConnection con = null;
                        for (DataSnapshot ConSnapshot: dataSnapshot.getChildren()){
                            con= ConSnapshot.getValue(CallConnection.class);
                            MyConnectionKey=ConSnapshot.getKey();
                        }
                        if(dataSnapshot.getChildrenCount()==0){
                            userSetConnection(Callcontact.getUid());
                            opponentStatusAndMakeACall(Callcontact.getUid());
                        }
                        else{
                            if (con.getCallanswer() > 100){
                                userSetConnection(Callcontact.getUid());
                                opponentStatusAndMakeACall(Callcontact.getUid());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                final FIRNotificaiton firNotificaiton = new FIRNotificaiton();
                FirebaseDatabase.getInstance().getReference().child("userIIDs")
                        .child(Callcontact.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                firNotificaiton.setRegistrationIds(Collections.singletonList(dataSnapshot.getValue().toString()));
                                firNotificaiton.setRegistrationIds(Collections.singletonList("dwcUDLQ-beM:APA91bHzbUwMSmnwfZi-NGIz13nz1alor-" +
                                        "FlGkr0IBrUD6DFgfy26hdZ-XoOMDU-tXC81_bgHlfMqieBCGnoPI6KdTmcxFSp5sAQIgFXIlu8SLWrdQDF4c-f3GrQhSi8p_tNhdnmZ4V1"));
                                FIRNotificaiton.Data data = firNotificaiton.getData();
                                data.setSessionId(sessionId);
                                String accessToken = "AAAAI19nIgY:APA91bG80k7rLZ2s-u3rdmmFLGg6nJg0NTlPfAN_" +
                                        "OzjCGDG1BP4LpslryQSaOb_zmdkH5iWr13zHpvUGRVwMLBYX4LIVTsngZvmtiAqtVueQK_lTBnWOq8BYBL5gasGrJK59i7ynl32W";
                                try {
                                    final NotificaitonRequest notificaitonRequest  = new NotificaitonRequest(firNotificaiton.jsonObject(), new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Timber.d("Update item in cart: %s", response.toString());
                                            MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_MESSAGE, getString(R.string.Ok), MsgUtils.ToastLength.SHORT);

                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {

                                        }
                                    }, getFragmentManager(), accessToken);
                                    notificaitonRequest.setRetryPolicy(MyApplication.getDefaultRetryPolice());
                                    notificaitonRequest.setShouldCache(false);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            MyApplication.getInstance().addToRequestQueue(notificaitonRequest, CONST.NOTIFICATION_SEND_TAG);
                                        }
                                    }, 150);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            }

        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    }, getFragmentManager());

    public CallBuddyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_buddy,container,false);
        callBuddyButton = (Button) view.findViewById(R.id.callBuddyButton);
        callingContacts = new CallingContacts();
        framelayout = (FrameLayout) view.findViewById(R.id.callBuddyFramlayout);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        callBuddyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callingContacts.getFriendsCount(new CallingContacts.getFriendsCountsCompletion() {
                    @Override
                    public void setResult(boolean counts, Contact contact) {
                        Callcontact=contact;
                        if (counts){
                            tokBoxJWTRequest.setRetryPolicy(MyApplication.getDefaultRetryPolice());
                            tokBoxJWTRequest.setShouldCache(false);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MyApplication.getInstance().addToRequestQueue(tokBoxJWTRequest, CONST.TOKBOX_SESSION_GENERATION);
                                }
                            }, 75);
                        }
                        else{
                            Snackbar snackbar = Snackbar
                                    .make(framelayout, R.string.need_more_friends, Snackbar.LENGTH_LONG);
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
                        }
                    }
                });
            }
        });
    }

    public void opponentStatusAndMakeACall(final String oppenent_id){
        refOpponentUserCon =myOpFirebaseDatabase.getReference();

        refOpponentUserCon.child("connections").child(oppenent_id).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CallConnection con=null;
                for (DataSnapshot ConSnapshot:dataSnapshot.getChildren()){
                    con=ConSnapshot.getValue(CallConnection.class);
                }

                if(dataSnapshot.getChildrenCount() == 0)
                {
                    opponentUserSetConnection(oppenent_id);
                }
                else {
                    if (con.getCallanswer() > 100) {
                        opponentUserSetConnection(oppenent_id);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public void userSetConnection(String DoctorId){
        //I am on call now. set the proper values
        CallConnection con=new CallConnection(2,DoctorId,1);

        DatabaseReference newCallRef= refMyUser.push();
        newCallRef.setValue(con);
        MyConnectionKey=newCallRef.getKey();

        if(refCallAnswer !=null){
            refCallAnswer.onDisconnect().cancel();
        }
        refCallAnswer = newCallRef.child("callanswer");
        refCallAnswer.onDisconnect().setValue(102);
    }

    public void opponentUserSetConnection(String oppenent_id){
        CallConnection con=new CallConnection(2,activeUser.getUid(),0);
        DatabaseReference newCallRef = refOpponentUserCon.child("connections").child(oppenent_id).push();
        connectionKey=newCallRef.getKey();
        newCallRef.setValue(con);

        refCallAnswer = newCallRef.child("callanswer");
        refCallAnswer.onDisconnect().setValue(201);
        startIntent(oppenent_id);
    }
    public void startIntent(String userId){
        Intent intent=new Intent(getContext(),OpenTokVideoActivity.class);
        intent.putExtra("user_id", activeUser.getUid());
        intent.putExtra("oppenent_id",userId);
        intent.putExtra("connectionKey",connectionKey);
        intent.putExtra("MyConnectionKey",MyConnectionKey);
        startActivity(intent);

    }
}

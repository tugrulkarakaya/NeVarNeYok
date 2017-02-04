package uk.co.nevarneyok.ux.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import uk.co.nevarneyok.R;
import uk.co.nevarneyok.controllers.CallingContacts;
import uk.co.nevarneyok.entities.Contact;

/**
 * A simple {@link Fragment} subclass.
 */
public class CallBuddyFragment extends Fragment {
    Button callBuddyButton;
    CallingContacts callingContacts;
    private static FrameLayout framelayout;
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
                        if (counts){

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
}

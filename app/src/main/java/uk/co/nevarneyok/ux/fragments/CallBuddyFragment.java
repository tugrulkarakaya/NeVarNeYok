package uk.co.nevarneyok.ux.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import uk.co.nevarneyok.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CallBuddyFragment extends Fragment {
    Button callBuddyButton;

    public CallBuddyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_buddy,container,false);
        callBuddyButton = (Button) view.findViewById(R.id.callBuddyButton);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        callBuddyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Arama algoritması
            }
        });
    }
}

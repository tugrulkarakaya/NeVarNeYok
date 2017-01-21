package uk.co.nevarneyok.ux.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import uk.co.nevarneyok.R;
import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.controllers.CallingContacts;
import uk.co.nevarneyok.entities.Contact;
import uk.co.nevarneyok.entities.User;
import uk.co.nevarneyok.ux.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsGroupFragment extends Fragment {


    DatabaseReference myRef=FirebaseDatabase.getInstance().getReference();
    DatabaseReference myFirebaseRef;

    static CallingContacts callingContacts;

    private RecyclerView contactsListView;

    User activeUser = SettingsMy.getActiveUser();

    public FriendsGroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity.setActionBarTitle(getString(R.string.friends_group));
        View view = inflater.inflate(R.layout.fragment_friends_group, container, false);
        contactsListView = (RecyclerView) view.findViewById(R.id.friendgrouplist);
        contactsListView.setHasFixedSize(true);
        contactsListView.setLayoutManager(new LinearLayoutManager(getContext()));

        if(activeUser!=null){
            callingContacts = new CallingContacts();
            myRef = FirebaseDatabase.getInstance().getReference("contacts").child(activeUser.getUid());
        }
        myFirebaseRef=myRef.child("callinggroups").child("friends");

        return view;
    }

    public static class FriendsGroupListHolder extends RecyclerView.ViewHolder{
        View mView;
        public FriendsGroupListHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name){
            TextView contact_name = (TextView) mView.findViewById(R.id.contact_name);
            contact_name.setText(name);
        }
        public void setPhone(String phone){
            TextView contact_phone = (TextView) mView.findViewById(R.id.contact_phone);
            contact_phone.setText(phone);
        }
        public void setPhoto(String photo){
            ImageView contact_photo = (ImageView) mView.findViewById(R.id.contact_photo);
            if(photo!=null) {
                Picasso.with(mView.getContext()).load(photo).into(contact_photo);
            }else{
                contact_photo.setBackgroundResource(R.drawable.user_black);
            }
        }
        public void setRemove(final String key){
            ImageView contact_add_remove = (ImageView) mView.findViewById(R.id.contact_add_remove);
            contact_add_remove.setBackgroundResource(R.drawable.ic_remove_circle_outline_black_24dp);
            contact_add_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callingContacts.removeCallingGroup("friends",key);
                    Toast.makeText(mView.getContext(), "Aranacaklar Listesinden Çıkartıldı.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Contact, FriendsGroupListHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contact, FriendsGroupListHolder>(
                Contact.class,
                R.layout.contacts_list_row,
                FriendsGroupListHolder.class,
                myFirebaseRef

        ) {
            @Override
            protected void populateViewHolder(FriendsGroupListHolder viewHolder, final Contact model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setPhone(model.getPhone());
                viewHolder.setPhoto(model.getPhotoUrl());
                viewHolder.setRemove(getRef(position).getKey());

            }
        };
        contactsListView.setAdapter(firebaseRecyclerAdapter);
    }
}

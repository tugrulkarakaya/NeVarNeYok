package uk.co.nevarneyok.ux.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import uk.co.nevarneyok.R;
import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.controllers.CallingContacts;
import uk.co.nevarneyok.entities.Contact;
import uk.co.nevarneyok.entities.User;
import uk.co.nevarneyok.ux.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    Cursor cursor;
    ArrayList<Contact> contactlist = new ArrayList<Contact>();
    int counter;
    private ProgressDialog pDialog;
    private Handler updateBarHandler;


    DatabaseReference myRef=FirebaseDatabase.getInstance().getReference();
    DatabaseReference pushRef;

    DatabaseReference myFirebaseRef;
    Query myQueryRef;
    private RecyclerView contactsListView;
    User activeUser = SettingsMy.getActiveUser();

    String[] permission = {
            android.Manifest.permission.READ_CONTACTS
    };
    int CONTACTS_READ_CODE = 67;
    private static FrameLayout framelayout;
    private static final String TAG = "MyFirebaseIIDService";
    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        MainActivity.setActionBarTitle(getString(R.string.Contact_List));
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        contactsListView = (RecyclerView) view.findViewById(R.id.contactlist);
        contactsListView.setLayoutManager(new LinearLayoutManager(getContext()));

        updateBarHandler =new Handler();
        pDialog = new ProgressDialog(getContext());

        if(activeUser!=null){
            myRef = FirebaseDatabase.getInstance().getReference("contacts").child(activeUser.getUid());
        }
        myFirebaseRef=myRef.child("contacts");
        myQueryRef = myFirebaseRef.orderByChild("name");
        myQueryRef.keepSynced(true);
        framelayout = (FrameLayout) view.findViewById(R.id.contactsframelayout);

        // Inflate the layout for this fragment
        return view;
    }

    public static class ContactListHolder extends RecyclerView.ViewHolder{
        View mView;
        CallingContacts callingContacts=new CallingContacts();
        public ContactListHolder(View itemView) {
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
        public void setAdd(final String key, final Contact contact){
            ImageView contact_add_remove = (ImageView) mView.findViewById(R.id.contact_add_remove);
            contact_add_remove.setBackgroundResource(R.drawable.ic_add_circle_outline_black_24dp);
            if(contact.isAdded()){
                mView.setBackgroundColor(Color.LTGRAY);
                contact_add_remove.setEnabled(false);
            }
            contact_add_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callingContacts.addCallingGroup("friends",key, contact);
                    callingContacts.checkFriendsGroup(contact.getPhone(),key);
                    Snackbar snackbar = Snackbar
                            .make(framelayout, R.string.added_to_list, Snackbar.LENGTH_LONG);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    snackbar.show();
                }
            });
        }
    }



    @Override
    public void onStart() {
        super.onStart();
        if(activeUser!=null) {
            DatabaseReference IIDToken = FirebaseDatabase.getInstance().getReference();
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            IIDToken.child("IIDToken").child(activeUser.getUid()).setValue(refreshedToken);
            FirebaseRecyclerAdapter<Contact, ContactListHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contact, ContactListHolder>(
                    Contact.class,
                    R.layout.contacts_list_row,
                    ContactListHolder.class,
                    myQueryRef

            ) {
                @Override
                protected void populateViewHolder(ContactListHolder viewHolder, final Contact model, int position) {

                    viewHolder.setName(model.getName());
                    viewHolder.setPhone(model.getPhone());
                    viewHolder.setAdd(getRef(position).getKey(), model);
                }
            };
            contactsListView.setAdapter(firebaseRecyclerAdapter);
        }

        if(activeUser != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    //-- Eğer almak istediğimiz izinler daha önceden kullanıcı tarafından onaylanmış ise bu kısımda istediğimiz işlemleri yapabiliriz..
                    //-- Mesela uygulama açılışında SD Kart üzerindeki herhangi bir dosyaya bu kısımda erişebiliriz.
                    final CallingContacts callingContacts=new CallingContacts();
                    callingContacts.existsData(new CallingContacts.completion() {
                        @Override
                        public void setResult(boolean result) {
                            if(!result){
                                pDialog.setMessage(R.string.reading_contacts+"...");
                                pDialog.setCancelable(false);
                                pDialog.show();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getContacts(false);
                                    }
                                }).start();
                            }
                            else{
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getContacts(true);
                                        callingContacts.refreshContacts(contactlist);
                                    }
                                }).start();
                            }
                        }
                    });

                } else {
                    //-- Almak istediğimiz izinler daha öncesinde kullanıcı tarafından onaylanmamış ise bu kod bloğu harekete geçecektir.
                    //-- Burada requestPermissions() metodu ile kullanıcıdan ilgili Manifest izinlerini onaylamasını istiyoruz.

                    requestPermissions(permission, CONTACTS_READ_CODE);

                }

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 67: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    final CallingContacts callingContacts=new CallingContacts();
                    callingContacts.existsData(new CallingContacts.completion() {
                        @Override
                        public void setResult(boolean result) {
                            if(!result){
                                pDialog.setMessage(R.string.reading_contacts+"...");
                                pDialog.setCancelable(false);
                                pDialog.show();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getContacts(false);
                                    }
                                }).start();
                            }
                            else{
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getContacts(true);
                                        callingContacts.refreshContacts(contactlist);
                                    }
                                }).start();
                            }
                        }
                    });

                } else {

                    Snackbar snackbar = Snackbar
                            .make(framelayout, R.string.permissio_required, Snackbar.LENGTH_LONG);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();

                }
                return;
            }
        }
    }

    public void getContacts(final boolean refresh) {

        String phoneNumber = null;
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Contact contact;
        ContentResolver contentResolver = getContext().getContentResolver();
        cursor = contentResolver.query(CONTENT_URI, null,null, null, null);
        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {
            counter = 0;
            while (cursor.moveToNext()) {
                // Update the progress message
                updateBarHandler.post(new Runnable() {
                    public void run() {
                        pDialog.setMessage(R.string.reading_contacts+" : "+ counter++ +"/"+cursor.getCount());
                    }
                });
                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
                if (hasPhoneNumber > 0) {

                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);

                    while (phoneCursor.moveToNext()) {
                        contact = new Contact();
                        contact.setName(name);

                        int phoneType = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        phoneNumber = phoneNumber.replace(" ","");
                        if(phoneType== ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE){
                            contact.setPhone(phoneNumber);
                        }
                        if (!(contact.getName() ==null)&& !(contact.getPhone()==null)){
                            contactlist.add(contact);
                        }
                    }
                    phoneCursor.close();
                }
                // Add the contact to the ArrayList



            }
            // ListView has to be updated using a ui thread
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i=0; i<contactlist.size();i++){
                        for (int j=i+1; j<contactlist.size();j++){
                            if(contactlist.get(i).getName().equals(contactlist.get(j).getName()) &&
                                    contactlist.get(i).getPhone().equals(contactlist.get(j).getPhone())){
                                contactlist.remove(j);
                                j--;
                            }
                        }
                    }
                    if (!refresh) {
                        for(Contact contact : contactlist){
                            pushRef = myRef.child("contacts").push();
                            pushRef.setValue(contact);
                        }
                    }
                }
            });
            // Dismiss the progressbar after 500 millisecondds
            updateBarHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pDialog.cancel();
                }
            }, 500);
        }
    }

}

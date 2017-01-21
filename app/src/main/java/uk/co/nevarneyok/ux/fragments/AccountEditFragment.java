package uk.co.nevarneyok.ux.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import uk.co.nevarneyok.CONST;
import uk.co.nevarneyok.MyApplication;
import uk.co.nevarneyok.R;
import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.api.EndPoints;
import uk.co.nevarneyok.api.FIRDataServices;
import uk.co.nevarneyok.api.JsonRequest;
import uk.co.nevarneyok.controllers.UserController;
import uk.co.nevarneyok.entities.User;
import uk.co.nevarneyok.listeners.OnSingleClickListener;
import uk.co.nevarneyok.utils.JsonUtils;
import uk.co.nevarneyok.utils.MsgUtils;
import uk.co.nevarneyok.utils.Utils;
import uk.co.nevarneyok.ux.MainActivity;
import uk.co.nevarneyok.ux.dialogs.LoginExpiredDialogFragment;
import timber.log.Timber;


import static com.facebook.login.widget.ProfilePictureView.TAG;

/**
 * Fragment provides options to editing user information and password change.
 */
public class AccountEditFragment extends Fragment {

    private ProgressDialog progressDialog;

    /**
     * Indicate which fort is active.
     */
    private boolean isPasswordForm = false;

    // Account editing form
    private LinearLayout accountForm;
    private TextInputLayout nameInputWrapper;
    private TextInputLayout phoneInputWrapper;
    private TextInputLayout emailInputWrapper;
    private static TextInputLayout birthDateInputWrapper;
    private EditText edBirtDate;
    private ImageView profilePicture;

    // Password change form
    private LinearLayout passwordForm;
    private TextInputLayout currentPasswordWrapper;
    private TextInputLayout newPasswordWrapper;
    private TextInputLayout newPasswordAgainWrapper;

    private ProgressDialog mProgress;

    private static final int GALLERY_REQUEST=1;
    private Uri mImageUri =null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("%s - OnCreateView", this.getClass().getSimpleName());
        MainActivity.setActionBarTitle(getString(R.string.Account));

        View view = inflater.inflate(R.layout.fragment_account_edit, container, false);

        progressDialog = Utils.generateProgressDialog(getActivity(), false);

        // Account details form
        accountForm = (LinearLayout) view.findViewById(R.id.account_edit_form);

        nameInputWrapper = (TextInputLayout) view.findViewById(R.id.account_edit_name_wrapper);
        phoneInputWrapper = (TextInputLayout) view.findViewById(R.id.account_edit_phone_wrapper);
        birthDateInputWrapper = (TextInputLayout) view.findViewById(R.id.account_edit_birth_date_wrapper);
        edBirtDate = (EditText) view.findViewById(R.id.account_edit_birth_date_et);
        emailInputWrapper = (TextInputLayout) view.findViewById(R.id.account_edit_email_wrapper);
        profilePicture = (ImageView) view.findViewById(R.id.account_photo);

        // Password form
        passwordForm = (LinearLayout) view.findViewById(R.id.account_edit_password_form);
        currentPasswordWrapper = (TextInputLayout) view.findViewById(R.id.account_edit_password_current_wrapper);
        newPasswordWrapper = (TextInputLayout) view.findViewById(R.id.account_edit_password_new_wrapper);
        newPasswordAgainWrapper = (TextInputLayout) view.findViewById(R.id.account_edit_password_new_again_wrapper);

        mProgress = new ProgressDialog(view.getContext());

        final Button btnChangePassword = (Button) view.findViewById(R.id.account_edit_change_form_btn);
        User activeUser = SettingsMy.getActiveUser();
        String provider = getString(R.string.providers_facebook);
        if(activeUser.getProvider().trim().equals(provider)){
            btnChangePassword.setVisibility(View.GONE);
        }

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordForm) {
                    isPasswordForm = false;
                    passwordForm.setVisibility(View.GONE);
                    accountForm.setVisibility(View.VISIBLE);
                    btnChangePassword.setText(getString(R.string.Change_password));
                } else {
                    isPasswordForm = true;
                    passwordForm.setVisibility(View.VISIBLE);
                    accountForm.setVisibility(View.GONE);
                    btnChangePassword.setText(R.string.Cancel);
                }
            }
        });

        // Fill user informations
        if (activeUser != null) {
            refreshScreen(activeUser);
            Timber.d("user: %s", activeUser.toString());
        } else {
            Timber.e(new RuntimeException(), "No active user. Shouldn't happen.");
        }

        Button confirmButton = (Button) view.findViewById(R.id.account_edit_confirm_button);
        confirmButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!isPasswordForm) {
                    try {
                        final User user = getUserFromView();
                        mProgress.setMessage("Bilgileriniz Kaydediliyor...");
                        mProgress.setCancelable(false);
                        mProgress.show();
                        if(mImageUri!=null){
                            StorageReference filepath = FIRDataServices.StorageUser.child(mImageUri.getLastPathSegment());
                            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    user.setProfileImageUrl(downloadUrl.toString());
                                    putUser(user);
                                }
                            });
                        }
                        else{
                            putUser(user);
                        }
                        mProgress.dismiss();
                    } catch (Exception e) {
                        Timber.e(e, "Update user information exception.");
                        MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_INTERNAL_ERROR, null, MsgUtils.ToastLength.SHORT);
                    }
                } else {
                    changePassword();
                }
                // Remove soft keyboard
                if (getActivity().getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                }
            }
        });
        //Doğum Tarihi
        edBirtDate.setKeyListener(null);
        birthDateInputWrapper.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View dateview, boolean hasfocus) {
                if(hasfocus){
                    DialogFragment newFragment = new SelectDateFragment();
                    newFragment.show(getFragmentManager(), "DatePicker");
                }
            }
        });
        edBirtDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View dateview, boolean hasfocus) {
                if(hasfocus){
                    DialogFragment newFragment = new SelectDateFragment();
                    newFragment.show(getFragmentManager(), "DatePicker");
                }
            }
        });
        birthDateInputWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View dateview) {
                DialogFragment newFragment = new SelectDateFragment();
                newFragment.show(getFragmentManager(), "DatePicker");
            }
        });
        edBirtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View dateview) {
                DialogFragment newFragment = new SelectDateFragment();
                newFragment.show(getFragmentManager(), "DatePicker");
            }
        });

        return view;
    }

    public static class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, yy, mm, dd);
        }

        public void onDateSet(DatePicker view, int yy, int mm, int dd) {
            populateSetDate(yy, mm+1, dd);
        }
        public void populateSetDate(int year, int month, int day) {
            Utils.setTextToInputLayout(birthDateInputWrapper, day+"/"+month+"/"+year);
        }

        //Doğum Tarihi

    }

    @Override
    public void onPause() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
        super.onPause();
    }

    private User getUserFromView() {
        User user = SettingsMy.getActiveUser();
        if(user == null) return null;
//Doğum Tarihini miliseconda çeviriyorum
        final String dTarih = Utils.getTextFromInputLayout(birthDateInputWrapper);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date birtdate = null;
        try {
            birtdate = simpleDateFormat.parse(dTarih);
        } catch (ParseException e) {
            e.printStackTrace();
        }
//Doğum Tarihini miliseconda çeviriyorum
        user.setName(Utils.getTextFromInputLayout(nameInputWrapper));
        user.setPhone(Utils.getTextFromInputLayout(phoneInputWrapper));
        user.setBirthDate(birtdate.getTime());
        return user;
    }

    private void refreshScreen(User user) {
        Utils.setTextToInputLayout(nameInputWrapper, user.getName());
        Utils.setTextToInputLayout(emailInputWrapper, user.getEmail());
        Utils.setTextToInputLayout(phoneInputWrapper, user.getPhone());
        //TODO Çağrı Cagri CAGRI aşağıda string long tarih seçeneği vs hikayeleri var. sen edit ekranında takvim ile sçeilmesini sağla.
        //
        SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
        Date birtdate = new Date();
        birtdate.setTime(user.getBirthDate());
        //
        Utils.setTextToInputLayout(birthDateInputWrapper, dateformat.format(birtdate));
        String imageUrl=null;
        if (user.getProfileImageUrl()!=null) {
            imageUrl =user.getProfileImageUrl().toString();

            if (imageUrl!=null){
                Picasso.with(this.getContext()).load(imageUrl).into(profilePicture);
            }

        }

    }

    /**
     * Check if all input fields are filled.
     * Method highlights all unfilled input fields.
     *
     * @return true if everything is Ok.
     */
    private boolean isRequiredFields() {
        // Check and show all missing values
        String fieldRequired = getString(R.string.Required_field);
        boolean nameCheck = Utils.checkTextInputLayoutValueRequirement(nameInputWrapper, fieldRequired);
        boolean phoneCheck = Utils.checkTextInputLayoutValueRequirement(phoneInputWrapper, fieldRequired);
        boolean birthDateCheck = Utils.checkTextInputLayoutValueRequirement(birthDateInputWrapper, fieldRequired);
        boolean emailCheck = Utils.checkTextInputLayoutValueRequirement(emailInputWrapper, fieldRequired);

        return nameCheck && birthDateCheck && phoneCheck && emailCheck;
    }

    /**
     * Check if all input password fields are filled and entries for new password matches.
     *
     * @return true if everything is Ok.
     */
    private boolean isRequiredPasswordFields() {
        String fieldRequired = getString(R.string.Required_field);
        boolean currentCheck = Utils.checkTextInputLayoutValueRequirement(currentPasswordWrapper, fieldRequired);
        boolean newCheck = Utils.checkTextInputLayoutValueRequirement(newPasswordWrapper, fieldRequired);
        boolean newAgainCheck = Utils.checkTextInputLayoutValueRequirement(newPasswordAgainWrapper, fieldRequired);

        if (newCheck && newAgainCheck) {
            if (!Utils.getTextFromInputLayout(newPasswordWrapper).equals(Utils.getTextFromInputLayout(newPasswordAgainWrapper))) {
                Timber.d("The entries for the new password must match");
                newPasswordWrapper.setErrorEnabled(true);
                newPasswordAgainWrapper.setErrorEnabled(true);
                newPasswordWrapper.setError(getString(R.string.The_entries_must_match));
                newPasswordAgainWrapper.setError(getString(R.string.The_entries_must_match));
                return false;
            } else {
                newPasswordWrapper.setErrorEnabled(false);
                newPasswordAgainWrapper.setErrorEnabled(false);
            }
        }
        return currentCheck && newCheck && newAgainCheck;
    }

    /**
     * Volley request for update user details.
     *
     * @param user new user data.
     */
    private void putUser(final User user) {
        if (isRequiredFields()) {
            User activeUser = SettingsMy.getActiveUser();
            if (activeUser != null) {
                progressDialog.show();
                UserController userController = new UserController(user);
                userController.save(new UserController.FirebaseCallResult(){
                    @Override
                    public void onComplete(boolean result) {
                        if(result){
                            SettingsMy.setActiveUser(user);
                            refreshScreen(user);
                            progressDialog.cancel();
                            MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_MESSAGE, getString(R.string.Ok), MsgUtils.ToastLength.SHORT);
                            getFragmentManager().popBackStackImmediate();
                        } else{
                            if (progressDialog != null) progressDialog.cancel();
                            JSONObject json = new JSONObject();
                            try {
                                json = new JSONObject(getString(R.string.Your_session_has_expired_Please_log_in_again));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            MsgUtils.showMessage(getActivity(), json);
                        }
                    }
                });
            } else {
                LoginExpiredDialogFragment loginExpiredDialogFragment = new LoginExpiredDialogFragment();
                loginExpiredDialogFragment.show(getFragmentManager(), "loginExpiredDialogFragment");
            }
        } else {
            Timber.d("Missing required fields.");
        }
    }

    /**
     * Updates the user's password. Before the request is sent, the input fields are checked for valid values.
     */
    private void changePassword() {
        if (isRequiredPasswordFields()) {
            progressDialog.show();

            User user = SettingsMy.getActiveUser();
            if (user != null) {
                final FirebaseUser firUser = FirebaseAuth.getInstance().getCurrentUser();

                AuthCredential credential = EmailAuthProvider
                        .getCredential(firUser.getEmail(), Utils.getTextFromInputLayout(currentPasswordWrapper).trim());

                // Prompt the user to re-provide their sign-in credentials
                firUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User re-authenticated.");
                                    String newPassword = Utils.getTextFromInputLayout(newPasswordWrapper).trim();

                                    firUser.updatePassword(newPassword)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Timber.d("Change password successful: %s", firUser);
                                                        MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_MESSAGE, getString(R.string.Ok), MsgUtils.ToastLength.SHORT);
                                                        if (progressDialog != null) progressDialog.cancel();
                                                        getFragmentManager().popBackStackImmediate();
                                                    } else {
                                                        Timber.d("Change password unsuccessful: %s", firUser);
                                                        MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_INTERNAL_ERROR, getString(R.string.Password_change_error), MsgUtils.ToastLength.LONG);
                                                        if (progressDialog != null) progressDialog.cancel();
                                                    }
                                                }
                                            });
                                } else { //reauthantication is failed
                                    if (progressDialog != null) progressDialog.cancel();
                                    MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_MESSAGE, getString(R.string.Wrong_password), MsgUtils.ToastLength.LONG);
                                }
                            }
                        });
            } else {
                LoginExpiredDialogFragment loginExpiredDialogFragment = new LoginExpiredDialogFragment();
                loginExpiredDialogFragment.show(getFragmentManager(), "loginExpiredDialogFragment");
            }
        }
    }

    @Override
    public void onStop() {
        if (progressDialog != null) progressDialog.cancel();
        MyApplication.getInstance().cancelPendingRequests(CONST.ACCOUNT_EDIT_REQUESTS_TAG);
        super.onStop();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK){
            mImageUri = data.getData();

            profilePicture.setImageURI(mImageUri);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galeryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galeryIntent.setType("image/*");
                startActivityForResult(galeryIntent,GALLERY_REQUEST);
            }
        });
    }
}

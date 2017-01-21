package uk.co.nevarneyok.ux.dialogs;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import uk.co.nevarneyok.BuildConfig;
import uk.co.nevarneyok.CONST;
import uk.co.nevarneyok.MyApplication;
import uk.co.nevarneyok.R;
import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.api.EndPoints;
import uk.co.nevarneyok.api.GsonRequest;
import uk.co.nevarneyok.api.JsonRequest;
import uk.co.nevarneyok.controllers.UserController;
import uk.co.nevarneyok.entities.User;
import uk.co.nevarneyok.interfaces.LoginDialogInterface;
import uk.co.nevarneyok.listeners.OnSingleClickListener;
import uk.co.nevarneyok.listeners.OnTouchPasswordListener;
import uk.co.nevarneyok.utils.JsonUtils;
import uk.co.nevarneyok.utils.MsgUtils;
import uk.co.nevarneyok.utils.Utils;
import uk.co.nevarneyok.ux.MainActivity;
import timber.log.Timber;
import com.facebook.FacebookSdk;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.gson.JsonNull;

import static com.android.volley.VolleyLog.TAG;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Dialog handles user login, registration and forgotten password function.
 */
public class LoginDialogFragment extends DialogFragment implements FacebookCallback<LoginResult> {

    public static final String MSG_RESPONSE = "response: %s";
    private CallbackManager callbackManager;
    private LoginDialogInterface loginDialogInterface;
    private ProgressDialog progressDialog;
    private FormState actualFormState = FormState.BASE;
    private LinearLayout loginBaseForm;
    private LinearLayout loginRegistrationForm;
    private LinearLayout loginEmailForm;
    private LinearLayout loginEmailForgottenForm;

    private TextInputLayout loginRegistrationEmailWrapper;
    private TextInputLayout loginRegistrationPasswordWrapper;
    private RadioButton loginRegistrationGenderWoman;
    private TextInputLayout loginEmailEmailWrapper;
    private TextInputLayout loginEmailPasswordWrapper;
    private TextInputLayout loginEmailForgottenEmailWrapper;
    private FirebaseAuth mAuth;


    /**
     * Creates dialog which handles user login, registration and forgotten password function.
     *
     * @param loginDialogInterface listener receiving login/registration results.
     * @return new instance of dialog.
     */
    public static LoginDialogFragment newInstance(LoginDialogInterface loginDialogInterface) {
        LoginDialogFragment frag = new LoginDialogFragment();
        frag.loginDialogInterface = loginDialogInterface;
        return frag;
    }

    public static void logoutUser() {
        //TODO TUGRUL tarafından düzenlendi.
        UserController.signOut();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.dialogFullscreen);
        progressDialog = Utils.generateProgressDialog(getActivity(), false);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Window window = d.getWindow();
            window.setLayout(width, height);
            window.setWindowAnimations(R.style.dialogFragmentAnimation);
            d.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (BuildConfig.DEBUG)
                        Timber.d("onKey: %d (Back=%d). Event:%d (Down:%d, Up:%d)", keyCode, KeyEvent.KEYCODE_BACK, event.getAction(),
                                KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP);
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                        switch (actualFormState) {
                            case REGISTRATION:
                                if (event.getAction() == KeyEvent.ACTION_UP) {
                                    setVisibilityOfRegistrationForm(false);
                                }
                                return true;
                            case FORGOTTEN_PASSWORD:
                                if (event.getAction() == KeyEvent.ACTION_UP) {
                                    setVisibilityOfEmailForgottenForm(false);
                                }
                                return true;
                            case EMAIL:
                                if (event.getAction() == KeyEvent.ACTION_UP) {
                                    setVisibilityOfEmailForm(false);
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("%s - OnCreateView", this.getClass().getSimpleName());
        View view = inflater.inflate(R.layout.dialog_login, container, false);
        callbackManager = CallbackManager.Factory.create();

        loginBaseForm = (LinearLayout) view.findViewById(R.id.login_base_form);
        loginRegistrationForm = (LinearLayout) view.findViewById(R.id.login_registration_form);
        loginEmailForm = (LinearLayout) view.findViewById(R.id.login_email_form);
        loginEmailForgottenForm = (LinearLayout) view.findViewById(R.id.login_email_forgotten_form);

        prepareLoginFormNavigation(view);
        prepareInputBoxes(view);
        prepareActionButtons(view);
        return view;
    }

    private void prepareInputBoxes(View view) {
        // Registration form
        loginRegistrationEmailWrapper = (TextInputLayout) view.findViewById(R.id.login_registration_email_wrapper);
        loginRegistrationPasswordWrapper = (TextInputLayout) view.findViewById(R.id.login_registration_password_wrapper);
        loginRegistrationGenderWoman = (RadioButton) view.findViewById(R.id.login_registration_sex_woman);
        EditText registrationPassword = loginRegistrationPasswordWrapper.getEditText();
        if (registrationPassword != null) {
            registrationPassword.setOnTouchListener(new OnTouchPasswordListener(registrationPassword));
        }


        // Login email form
        loginEmailEmailWrapper = (TextInputLayout) view.findViewById(R.id.login_email_email_wrapper);
        EditText loginEmail = loginEmailEmailWrapper.getEditText();
        if (loginEmail != null) loginEmail.setText(SettingsMy.getUserEmailHint());
        loginEmailPasswordWrapper = (TextInputLayout) view.findViewById(R.id.login_email_password_wrapper);
        EditText emailPassword = loginEmailPasswordWrapper.getEditText();
        if (emailPassword != null) {
            emailPassword.setOnTouchListener(new OnTouchPasswordListener(emailPassword));
            emailPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEND || actionId == 124) {
                        invokeLoginWithEmail();
                        return true;
                    }
                    return false;
                }
            });
        }

        loginEmailForgottenEmailWrapper = (TextInputLayout) view.findViewById(R.id.login_email_forgotten_email_wrapper);
        EditText emailForgottenPassword = loginEmailForgottenEmailWrapper.getEditText();
        if (emailForgottenPassword != null)
            emailForgottenPassword.setText(SettingsMy.getUserEmailHint());

        // Simple accounts whisperer.
        Account[] accounts = AccountManager.get(getActivity()).getAccountsByType("com.google");
        String[] addresses = new String[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            addresses[i] = accounts[i].name;
            Timber.e("Sets autocompleteEmails: %s", accounts[i].name);
        }

        ArrayAdapter<String> emails = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, addresses);
        AutoCompleteTextView textView = (AutoCompleteTextView) view.findViewById(R.id.login_registration_email_text_auto);
        textView.setAdapter(emails);
    }

    private void prepareLoginFormNavigation(View view) {
        // Login email
        Button loginFormEmailButton = (Button) view.findViewById(R.id.login_form_email_btn);
        loginFormEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibilityOfEmailForm(true);
            }
        });
        ImageButton closeEmailBtn = (ImageButton) view.findViewById(R.id.login_email_close_button);
        closeEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Slow to display ripple effect
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setVisibilityOfEmailForm(false);
                    }
                }, 200);
            }
        });

        // Registration
        TextView loginFormRegistrationButton = (TextView) view.findViewById(R.id.login_form_registration_btn);
        loginFormRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibilityOfRegistrationForm(true);
            }
        });
        ImageButton closeRegistrationBtn = (ImageButton) view.findViewById(R.id.login_registration_close_button);
        closeRegistrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Slow to display ripple effect
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setVisibilityOfRegistrationForm(false);
                    }
                }, 200);
            }
        });

        // Email forgotten password
        TextView loginEmailFormForgottenButton = (TextView) view.findViewById(R.id.login_email_forgotten_password);
        loginEmailFormForgottenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibilityOfEmailForgottenForm(true);
            }
        });
        ImageButton closeEmailForgottenFormBtn = (ImageButton) view.findViewById(R.id.login_email_forgotten_back_button);
        closeEmailForgottenFormBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Slow to display ripple effect
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setVisibilityOfEmailForgottenForm(false);
                    }
                }, 200);
            }
        });
    }

    private void prepareActionButtons(View view) {
        TextView loginBaseSkip = (TextView) view.findViewById(R.id.login_form_skip);
        loginBaseSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (loginDialogInterface != null) loginDialogInterface.skipLogin();
                dismiss();
            }
        });

        // FB login

        Button fbLogin = (Button) view.findViewById(R.id.login_form_facebook);
        fbLogin.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                invokeFacebookLogin();
            }
        });

        Button emailLogin = (Button) view.findViewById(R.id.login_email_confirm);
        emailLogin.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                invokeLoginWithEmail();
            }
        });

        Button registerBtn = (Button) view.findViewById(R.id.login_registration_confirm);
        registerBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                invokeRegisterNewUser();
            }
        });

        Button resetPassword = (Button) view.findViewById(R.id.login_email_forgotten_confirm);
        resetPassword.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                invokeResetPassword();
            }
        });
    }

    private void invokeFacebookLogin() {
        LoginManager.getInstance().registerCallback(callbackManager, this);
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
    }
    private void handleFacebookAccessToken(final AccessToken token) {

        GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject fbUser, GraphResponse graphResponse) {
                firebaseAccessCheck(token, fbUser);
            }
        });

        Bundle bundle=new Bundle();
        bundle.putString("fields","id,email,name");
        request.setParameters(bundle);
        request.executeAsync();

    }

    private void firebaseAccessCheck(AccessToken token, final JSONObject fbUser){
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Timber.i("signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Timber.e("Error on receiving user profile information.");
                            Timber.e(task.getException(), "Error: %s", task.getException()==null?"":task.getException().toString());
                            handleNonFatalError(getString(R.string.Receiving_facebook_profile_failed), true);
                        }else {
                            User user = new User(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            user.setEmail(fbUser.optString("email"));
                            user.setName(fbUser.optString("name"));
                            user.setFbId(fbUser.optString("id"));
                            user.setProviderId(fbUser.optString("id"));
                            user.setProvider(getString(R.string.providers_facebook));
                            user.setProfileImageUrl("https://graph.facebook.com/"+user.getProviderId()+"/picture?type=large");
                            final UserController userController = new UserController(user);
                            userController.saveAndRetrieveData(new UserController.completion() {
                                @Override
                                public void setResult(boolean result, User user) {
                                    if(result){
                                        Timber.d(MSG_RESPONSE, user.toString());
                                        handleUserLogin(user);
                                    } else{
                                        userController.signOut();
                                        if (progressDialog != null) progressDialog.cancel();
                                        MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_MESSAGE, null, MsgUtils.ToastLength.LONG);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void invokeRegisterNewUser() {
        hideSoftKeyboard();
        if (isRequiredFields(loginRegistrationEmailWrapper, loginRegistrationPasswordWrapper)) {
//            SettingsMy.setUserEmailHint(etRegistrationEmail.getText().toString());
            registerNewUser(loginRegistrationEmailWrapper.getEditText(), loginRegistrationPasswordWrapper.getEditText());
        }
    }

    private void registerNewUser(EditText editTextEmail, EditText editTextPassword) {
        SettingsMy.setUserEmailHint(editTextEmail.getText().toString());
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString().trim(), editTextPassword.getText().toString().trim())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Timber.d(MSG_RESPONSE, "Parse new user registration failed");
                            MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_INTERNAL_ERROR, null, MsgUtils.ToastLength.SHORT);
                            if (progressDialog != null) progressDialog.cancel();
                            return;
                        }
                        final User user = new User(task.getResult().getUser().getUid());
                        user.setGender(loginRegistrationGenderWoman.isChecked() ? "female" : "male");
                        user.setProvider(getString(R.string.providers_email));
                        UserController userController= new UserController(user);
                        userController.getAuthInfo();
                        userController.save(new UserController.FirebaseCallResult()
                        {
                            @Override
                            public void onComplete(boolean result) {
                                Timber.d(MSG_RESPONSE, user.toString());
                                handleUserLogin(user);
                            }
                        });
                    }
                });
    }

    private void invokeLoginWithEmail() {
        hideSoftKeyboard();
        if (isRequiredFields(loginEmailEmailWrapper, loginEmailPasswordWrapper)) {
            logInWithEmail(loginEmailEmailWrapper.getEditText(), loginEmailPasswordWrapper.getEditText());
        }
    }

    private void logInWithEmail(EditText editTextEmail, final EditText editTextPassword) {
        SettingsMy.setUserEmailHint(editTextEmail.getText().toString());
        String url = String.format(EndPoints.USER_LOGIN_EMAIL, SettingsMy.getActualNonNullShop(getActivity()).getId());
        progressDialog.show();

        if (BuildConfig.DEBUG) Timber.d("Login user: %s", editTextEmail.getText().toString().trim());

        mAuth.signInWithEmailAndPassword(editTextEmail.getText().toString().trim(), editTextPassword.getText().toString().trim())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        editTextPassword.setText("");
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            if (progressDialog != null) progressDialog.cancel();
                            MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_MESSAGE, null, MsgUtils.ToastLength.LONG);
                        }
                        else{
                            //get user details here
                            User user = new User(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            final UserController userController = new UserController(user);
                            userController.retrieveData(new UserController.completion() {
                                @Override
                                public void setResult(boolean result, User user) {
                                    if(result){
                                        Timber.d(MSG_RESPONSE, user.toString());
                                        handleUserLogin(user);
                                    } else{
                                        userController.signOut();
                                        if (progressDialog != null) progressDialog.cancel();
                                        MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_MESSAGE, null, MsgUtils.ToastLength.LONG);
                                    }
                                }
                            });

                        }
                    }
                });
    }

    private void handleUserLogin(User user) {
        if (progressDialog != null) progressDialog.cancel();
        SettingsMy.setActiveUser(user);

        // Invalidate GCM token for new registration with authorized user.
        SettingsMy.setTokenSentToServer(false);

        if (getActivity() instanceof MainActivity)
            ((MainActivity) getActivity()).registerGcmOnServer();

        MainActivity.invalidateDrawerMenuHeader();

        if (loginDialogInterface != null) {
            loginDialogInterface.successfulLoginOrRegistration(user);
        } else {
            Timber.e("Interface is null");
            MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_INTERNAL_ERROR, null, MsgUtils.ToastLength.SHORT);
        }
        dismiss();
    }

    private void invokeResetPassword() {
        EditText emailForgottenPasswordEmail = loginEmailForgottenEmailWrapper.getEditText();
        if (emailForgottenPasswordEmail == null || emailForgottenPasswordEmail.getText().toString().equalsIgnoreCase("")) {
            loginEmailForgottenEmailWrapper.setErrorEnabled(true);
            loginEmailForgottenEmailWrapper.setError(getString(R.string.Required_field));
        } else {
            loginEmailForgottenEmailWrapper.setErrorEnabled(false);
            resetPassword(emailForgottenPasswordEmail);
        }
    }

    private void resetPassword(EditText emailOfForgottenPassword) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String emailAddress = emailOfForgottenPassword.getText().toString().trim();
        progressDialog.show();
        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Timber.d("Reset password on url success. Response: %s", task.toString());
                            progressDialog.cancel();
                            MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_MESSAGE, getString(R.string.Check_your_email_we_sent_you_an_confirmation_email), MsgUtils.ToastLength.LONG);
                            setVisibilityOfEmailForgottenForm(false);
                        }else{
                            if (progressDialog != null) progressDialog.cancel();
                            JSONObject json = new JSONObject();
                            try {
                                json = new JSONObject(getString(R.string.Send_password_reset_error));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            MsgUtils.showMessage(getActivity(), json);
                        }
                    }
                });
    }

    private void hideSoftKeyboard() {
        if (getActivity() != null && getView() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    private void setVisibilityOfRegistrationForm(boolean setVisible) {
        if (setVisible) {
            actualFormState = FormState.REGISTRATION;
            loginBaseForm.setVisibility(View.INVISIBLE);
            loginRegistrationForm.setVisibility(View.VISIBLE);
        } else {
            actualFormState = FormState.BASE;
            loginBaseForm.setVisibility(View.VISIBLE);
            loginRegistrationForm.setVisibility(View.INVISIBLE);
            hideSoftKeyboard();
        }
    }

    private void setVisibilityOfEmailForm(boolean setVisible) {
        if (setVisible) {
            actualFormState = FormState.EMAIL;
            loginBaseForm.setVisibility(View.INVISIBLE);
            loginEmailForm.setVisibility(View.VISIBLE);
        } else {
            actualFormState = FormState.BASE;
            loginBaseForm.setVisibility(View.VISIBLE);
            loginEmailForm.setVisibility(View.INVISIBLE);
            hideSoftKeyboard();
        }
    }

    private void setVisibilityOfEmailForgottenForm(boolean setVisible) {
        if (setVisible) {
            actualFormState = FormState.FORGOTTEN_PASSWORD;
            loginEmailForm.setVisibility(View.INVISIBLE);
            loginEmailForgottenForm.setVisibility(View.VISIBLE);
        } else {
            actualFormState = FormState.EMAIL;
            loginEmailForm.setVisibility(View.VISIBLE);
            loginEmailForgottenForm.setVisibility(View.INVISIBLE);
        }
        hideSoftKeyboard();
    }

    /**
     * Check if editTexts are valid view and if user set all required fields.
     *
     * @return true if ok.
     */
    private boolean isRequiredFields(TextInputLayout emailWrapper, TextInputLayout passwordWrapper) {
        if (emailWrapper == null || passwordWrapper == null) {
            Timber.e(new RuntimeException(), "Called isRequiredFields with null parameters.");
            MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_INTERNAL_ERROR, null, MsgUtils.ToastLength.LONG);
            return false;
        } else {
            EditText email = emailWrapper.getEditText();
            EditText password = passwordWrapper.getEditText();
            if (email == null || password == null) {
                Timber.e(new RuntimeException(), "Called isRequiredFields with null editTexts in wrappers.");
                MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_INTERNAL_ERROR, null, MsgUtils.ToastLength.LONG);
                return false;
            } else {
                boolean isEmail = false;
                boolean isPassword = false;

                if (email.getText().toString().equalsIgnoreCase("")) {
                    emailWrapper.setErrorEnabled(true);
                    emailWrapper.setError(getString(R.string.Required_field));
                } else {
                    emailWrapper.setErrorEnabled(false);
                    isEmail = true;
                }

                if (password.getText().toString().equalsIgnoreCase("")) {
                    passwordWrapper.setErrorEnabled(true);
                    passwordWrapper.setError(getString(R.string.Required_field));
                } else {
                    passwordWrapper.setErrorEnabled(false);
                    isPassword = true;
                }

                if (isEmail && isPassword) {
                    return true;
                } else {
                    Timber.e("Some fields are required.");
                    return false;
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        MyApplication.getInstance().getRequestQueue().cancelAll(CONST.LOGIN_DIALOG_REQUESTS_TAG);

    }

    @Override
    public void onDetach() {
        loginDialogInterface = null;
        super.onDetach();
    }

    @Override
    public void onSuccess(final LoginResult loginResult) {
        Timber.d("FB login success");
        if (loginResult == null) {
            Timber.e("Fb login succeed with null loginResult.");
            handleNonFatalError(getString(R.string.Facebook_login_failed), true);
        } else {
            Timber.d("Result: %s", loginResult.toString());
            handleFacebookAccessToken(loginResult.getAccessToken());
        }
    }

    @Override
    public void onCancel() {
        Timber.d("Fb login canceled");
    }

    @Override
    public void onError(FacebookException e) {
        Timber.e(e, "Fb login error");
        handleNonFatalError(getString(R.string.Facebook_login_failed), false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager != null)
            callbackManager.onActivityResult(requestCode, resultCode, data);
        else {
            Timber.d("OnActivityResult, null callbackManager object.");
        }
    }

    /**
     * Volley request that sends FB_ID and FB_ACCESS_TOKEN to API
     */
    private void verifyUserOnApi(JSONObject userProfileObject, AccessToken fbAccessToken) {
        String url = String.format(EndPoints.USER_LOGIN_FACEBOOK, SettingsMy.getActualNonNullShop(getActivity()).getId());
        JSONObject jo = new JSONObject();
        try {
            jo.put(JsonUtils.TAG_FB_ID, userProfileObject.getString("id"));
            jo.put(JsonUtils.TAG_FB_ACCESS_TOKEN, fbAccessToken.getToken());
        } catch (JSONException e) {
            Timber.e(e, "Exception while parsing fb user.");
            MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_INTERNAL_ERROR, null, MsgUtils.ToastLength.LONG);
            return;
        }

        progressDialog.show();
        GsonRequest<User> verifyFbUser = new GsonRequest<>(Request.Method.POST, url, jo.toString(), User.class,
                new Response.Listener<User>() {
                    @Override
                    public void onResponse(@NonNull User response) {
                        Timber.d(MSG_RESPONSE, response.toString());
                        handleUserLogin(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressDialog != null) progressDialog.cancel();
                MsgUtils.logAndShowErrorMessage(getActivity(), error);
                LoginDialogFragment.logoutUser();
            }
        }, getFragmentManager(), null);
        verifyFbUser.setRetryPolicy(MyApplication.getDefaultRetryPolice());
        verifyFbUser.setShouldCache(false);
        MyApplication.getInstance().addToRequestQueue(verifyFbUser, CONST.LOGIN_DIALOG_REQUESTS_TAG);
    }

    /**
     * Handle errors, when user have identity at least.
     * Show error message to user.
     */
    private void handleNonFatalError(String message, boolean logoutFromFb) {
        if (logoutFromFb) {
            LoginDialogFragment.logoutUser();
        }
        if (getActivity() != null)
            MsgUtils.showToast(getActivity(), MsgUtils.TOAST_TYPE_MESSAGE, message, MsgUtils.ToastLength.LONG);
    }

    private enum FormState {
        BASE, REGISTRATION, EMAIL, FORGOTTEN_PASSWORD
    }
}

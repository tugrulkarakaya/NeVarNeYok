package uk.co.nevarneyok.ux.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.nevarneyok.CONST;
import uk.co.nevarneyok.MyApplication;
import uk.co.nevarneyok.R;
import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.api.EndPoints;
import uk.co.nevarneyok.api.GsonRequest;
import uk.co.nevarneyok.controllers.UserController;
import uk.co.nevarneyok.entities.User;
import uk.co.nevarneyok.entities.delivery.Shipping;
import uk.co.nevarneyok.interfaces.LoginDialogInterface;
import uk.co.nevarneyok.interfaces.ShippingDialogInterface;
import uk.co.nevarneyok.listeners.OnSingleClickListener;
import uk.co.nevarneyok.utils.MsgUtils;
import uk.co.nevarneyok.utils.Utils;
import uk.co.nevarneyok.ux.MainActivity;
import uk.co.nevarneyok.ux.dialogs.LoginDialogFragment;
import uk.co.nevarneyok.ux.dialogs.ShippingDialogFragment;
import timber.log.Timber;

/**
 * Fragment provides the account screen with options such as logging, editing and more.
 */
public class AccountFragment extends Fragment {

    private ProgressDialog pDialog;

    /**
     * Indicates if user data should be loaded from server or from memory.
     */
    private boolean mAlreadyLoaded = false;

    // User information
    private LinearLayout userInfoLayout;
    private TextView tvUserName;
    private TextView tvPhone;
    private TextView tvEmail;
    private TextView tvBirthDate;

    // Actions
    private Button loginLogoutBtn;
    private Button updateUserBtn;
    private Button myOrdersBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("%s - OnCreateView", this.getClass().getSimpleName());
        MainActivity.setActionBarTitle(getString(R.string.Profile));

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        pDialog = Utils.generateProgressDialog(getActivity(), false);

        userInfoLayout = (LinearLayout) view.findViewById(R.id.account_user_info);
        tvUserName = (TextView) view.findViewById(R.id.account_name);
        tvEmail = (TextView) view.findViewById(R.id.account_email);
        tvPhone = (TextView) view.findViewById(R.id.account_phone);
        tvBirthDate = (TextView) view.findViewById(R.id.birth_date);

        updateUserBtn = (Button) view.findViewById(R.id.account_update);
        updateUserBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).onAccountEditSelected();
            }
        });
        myOrdersBtn = (Button) view.findViewById(R.id.account_my_orders);
        myOrdersBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).onOrdersHistory();
            }
        });


        Button settingsBtn = (Button) view.findViewById(R.id.account_settings);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null && activity instanceof MainActivity)
                    ((MainActivity) getActivity()).startSettingsFragment();
            }
        });
        Button dispensingPlaces = (Button) view.findViewById(R.id.account_dispensing_places);
        dispensingPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShippingDialogFragment shippingDialogFragment = ShippingDialogFragment.newInstance(new ShippingDialogInterface() {
                    @Override
                    public void onShippingSelected(Shipping shipping) {

                    }
                });
                shippingDialogFragment.show(getFragmentManager(), "shippingDialogFragment");
            }
        });

        loginLogoutBtn = (Button) view.findViewById(R.id.account_login_logout_btn);
        loginLogoutBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (SettingsMy.getActiveUser() != null) {
                    LoginDialogFragment.logoutUser();
                    refreshScreen(null);
                } else {
                    LoginDialogFragment loginDialogFragment = LoginDialogFragment.newInstance(new LoginDialogInterface() {
                        @Override
                        public void successfulLoginOrRegistration(User user) {
                            refreshScreen(user);
                            //TODO TUGRUL Cart sayısı bilgisi bu uygulamada bize lazım değil.
                            //MainActivity.updateCartCountNotification();
                        }
                    });
                    loginDialogFragment.show(getFragmentManager(), LoginDialogFragment.class.getSimpleName());
                }
            }
        });


        User user = SettingsMy.getActiveUser();
        if (user != null) {
            Timber.d("user: %s", user.toString());
            // Sync user data if fragment created (not reuse from backstack)
            if (savedInstanceState == null && !mAlreadyLoaded) {
                mAlreadyLoaded = true;
                syncUserData(user);
            } else {
                refreshScreen(user);
            }
        } else {
            refreshScreen(null);
        }
        return view;
    }

    private void syncUserData(@NonNull User user) {
        // Tugrul tarafından düzenlendi
        UserController userController;
        userController = new UserController(user);
        pDialog.show();
        userController.retrieveData( new UserController.completion(){
            @Override
            public void setResult(boolean result, User user) {
                if(result){
                    Timber.d("response: %s", user.toString());
                    SettingsMy.setActiveUser(user);
                    refreshScreen(user);
                    if (pDialog != null) pDialog.cancel();
                }
                else{
                    if (pDialog != null) pDialog.cancel();
                    JSONObject jsonMessage = new JSONObject();
                    try {
                        jsonMessage = new JSONObject(String.valueOf(R.string.Your_session_has_expired_Please_log_in_again));
                    } catch (JSONException e) {}
                    MsgUtils.showMessage(getActivity(), jsonMessage);
                    MsgUtils.logAndShowErrorMessage(getActivity(), null);
                }
            }
        });

        //TODO TUGRUL aşağıdaki kodu sadece en sonraki fragmentin gittiği yerde login sayfasına yönlendirdiği için tutuyorum inceleyeceğim.
/*
        String url = String.format(EndPoints.USER_SINGLE, SettingsMy.getActualNonNullShop(getActivity()).getId(), user.getId());
        pDialog.show();

        GsonRequest<User> getUser = new GsonRequest<>(Request.Method.GET, url, null, User.class,
                new Response.Listener<User>() {
                    @Override
                    public void onResponse(@NonNull User response) {
                        Timber.d("response: %s", response.toString());
                        SettingsMy.setActiveUser(response);
                        refreshScreen(SettingsMy.getActiveUser());
                        if (pDialog != null) pDialog.cancel();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (pDialog != null) pDialog.cancel();
                MsgUtils.logAndShowErrorMessage(getActivity(), error);
            }
        }, getFragmentManager(), user.getAccessToken());
        getUser.setRetryPolicy(MyApplication.getDefaultRetryPolice());
        getUser.setShouldCache(false);
        MyApplication.getInstance().addToRequestQueue(getUser, CONST.ACCOUNT_REQUESTS_TAG);
*/
    }

    private void refreshScreen(User user) {
        if (user == null) {
            loginLogoutBtn.setText(getString(R.string.Log_in));
            userInfoLayout.setVisibility(View.GONE);
            updateUserBtn.setVisibility(View.GONE);
            myOrdersBtn.setVisibility(View.GONE);
        } else {
            loginLogoutBtn.setText(getString(R.string.Log_out));
            userInfoLayout.setVisibility(View.VISIBLE);
            updateUserBtn.setVisibility(View.VISIBLE);
            myOrdersBtn.setVisibility(View.VISIBLE);

            tvUserName.setText(user.getName());
            if(user.getBirthDate()>0) {
                SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
                Date birtdate = new Date();
                birtdate.setTime(user.getBirthDate());
                tvBirthDate.setText(dateformat.format(birtdate));
            }
            tvEmail.setText(user.getEmail());
            tvPhone.setText(user.getPhone());
        }
    }

    /**
     * The method combines two strings. As the string separator is used space or comma.
     *
     * @param result   first part of final string.
     * @param append   second part of final string.
     * @param addComma true if comma with space should be used as separator. Otherwise is used space.
     * @return concatenated string.
     */
    private String appendCommaText(String result, String append, boolean addComma) {
        /* a sample usage

        String address = user.getStreet();
        address = appendCommaText(address, user.getHouseNumber(), false);
        address = appendCommaText(address, user.getCity(), true);
        address = appendCommaText(address, user.getZip(), true);
        */

        if (result != null && !result.isEmpty()) {
            if (append != null && !append.isEmpty()) {
                if (addComma)
                    result += getString(R.string.format_comma_prefix, append);
                else
                    result += getString(R.string.format_space_prefix, append);
            }
            return result;
        } else {
            return append;
        }
    }

    @Override
    public void onStop() {
        MyApplication.getInstance().getRequestQueue().cancelAll(CONST.ACCOUNT_REQUESTS_TAG);
        super.onStop();
    }
}

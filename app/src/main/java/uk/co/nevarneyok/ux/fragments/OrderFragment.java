package uk.co.nevarneyok.ux.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import uk.co.nevarneyok.CONST;
import uk.co.nevarneyok.MyApplication;
import uk.co.nevarneyok.R;
import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.api.EndPoints;
import uk.co.nevarneyok.api.GsonRequest;
import uk.co.nevarneyok.entities.User;
import uk.co.nevarneyok.entities.order.Order;
import uk.co.nevarneyok.utils.MsgUtils;
import uk.co.nevarneyok.utils.RecyclerMarginDecorator;
import uk.co.nevarneyok.utils.Utils;
import uk.co.nevarneyok.ux.adapters.OrderRecyclerAdapter;
import uk.co.nevarneyok.ux.dialogs.LoginExpiredDialogFragment;
import timber.log.Timber;

/**
 * Fragment shows a detail of an order from the order history.
 */
public class OrderFragment extends Fragment {

    private static final String ORDER_ID = "order_id";

    private ProgressDialog progressDialog;

    private OrderRecyclerAdapter orderRecyclerAdapter;

    /**
     * Create a new fragment instance, which displays the order history
     *
     * @param orderId id of the order to load from server.
     * @return new fragment instance.
     */
    public static OrderFragment newInstance(long orderId) {
        Bundle args = new Bundle();
        args.putLong(ORDER_ID, orderId);
        OrderFragment fragment = new OrderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("%s - onCreateView", this.getClass().getSimpleName());
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        progressDialog = Utils.generateProgressDialog(getActivity(), false);
        prepareOrder(view);
        if (getArguments() != null) {
            long orderId = getArguments().getLong(ORDER_ID);
            loadOrderDetail(orderId);
        } else {
            Timber.e("Missing arguments with orderId");
        }

        return view;
    }

    /**
     * Prepare content recycler. Create custom adapter and endless scroll.
     *
     * @param view root fragment view.
     */
    private void prepareOrder(View view) {
        RecyclerView orderRecycler = (RecyclerView) view.findViewById(R.id.order_recycler);
        orderRecyclerAdapter = new OrderRecyclerAdapter(getActivity());
        orderRecycler.setAdapter(orderRecyclerAdapter);
        orderRecycler.setLayoutManager(new LinearLayoutManager(orderRecycler.getContext()));
        orderRecycler.setItemAnimator(new DefaultItemAnimator());
        orderRecycler.setHasFixedSize(true);
        orderRecycler.addItemDecoration(new RecyclerMarginDecorator(getResources().getDimensionPixelSize(R.dimen.base_margin)));
    }

    private void loadOrderDetail(long orderId) {
        User user = SettingsMy.getActiveUser();
        if (user != null) {
            String url = String.format(EndPoints.ORDERS_SINGLE, SettingsMy.getActualNonNullShop(getActivity()).getId(), orderId);

            progressDialog.show();

            GsonRequest<Order> req = new GsonRequest<>(Request.Method.GET, url, null, Order.class, new Response.Listener<Order>() {
                @Override
                public void onResponse(Order response) {
                    orderRecyclerAdapter.addOrder(response);
                    if (progressDialog != null) progressDialog.cancel();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (progressDialog != null) progressDialog.cancel();
                    MsgUtils.logAndShowErrorMessage(getActivity(), error);
                }
            }, getFragmentManager(), user.getAccessToken());

            req.setRetryPolicy(MyApplication.getDefaultRetryPolice());
            req.setShouldCache(false);
            MyApplication.getInstance().addToRequestQueue(req, CONST.ORDERS_DETAIL_REQUESTS_TAG);
        } else {
            LoginExpiredDialogFragment loginExpiredDialogFragment = new LoginExpiredDialogFragment();
            loginExpiredDialogFragment.show(getFragmentManager(), "loginExpiredDialogFragment");
        }
    }

    @Override
    public void onStop() {
        if (progressDialog != null) progressDialog.cancel();
        MyApplication.getInstance().cancelPendingRequests(CONST.ORDERS_HISTORY_REQUESTS_TAG);
        super.onStop();
    }

}

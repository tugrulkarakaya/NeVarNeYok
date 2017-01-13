package uk.co.nevarneyok.ux.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import uk.co.nevarneyok.entities.Metadata;
import uk.co.nevarneyok.entities.User;
import uk.co.nevarneyok.entities.order.Order;
import uk.co.nevarneyok.entities.order.OrderResponse;
import uk.co.nevarneyok.interfaces.OrdersRecyclerInterface;
import uk.co.nevarneyok.utils.EndlessRecyclerScrollListener;
import uk.co.nevarneyok.utils.MsgUtils;
import uk.co.nevarneyok.utils.RecyclerMarginDecorator;
import uk.co.nevarneyok.utils.Utils;
import uk.co.nevarneyok.ux.MainActivity;
import uk.co.nevarneyok.ux.adapters.OrdersHistoryRecyclerAdapter;
import uk.co.nevarneyok.ux.dialogs.LoginExpiredDialogFragment;
import timber.log.Timber;

/**
 * Fragment shows the user's order history.
 */
public class OrdersHistoryFragment extends Fragment {

    private ProgressDialog progressDialog;

    // Fields referencing complex screen layouts.
    private View empty;
    private View content;

    /**
     * Request metadata containing urls for endlessScroll.
     */
    private Metadata ordersMetadata;

    private OrdersHistoryRecyclerAdapter ordersHistoryRecyclerAdapter;
    private EndlessRecyclerScrollListener endlessRecyclerScrollListener;

    /**
     * Field for recovering scroll position.
     */
    private RecyclerView ordersRecycler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("%s - onCreateView", this.getClass().getSimpleName());
        MainActivity.setActionBarTitle(getString(R.string.Order_history));

        View view = inflater.inflate(R.layout.fragment_orders_history, container, false);

        progressDialog = Utils.generateProgressDialog(getActivity(), false);

        empty = view.findViewById(R.id.order_history_empty);
        content = view.findViewById(R.id.order_history_content);

        prepareOrdersHistoryRecycler(view);

        loadOrders(null);
        return view;
    }

    /**
     * Prepare content recycler. Create custom adapter and endless scroll.
     *
     * @param view root fragment view.
     */
    private void prepareOrdersHistoryRecycler(View view) {
        ordersRecycler = (RecyclerView) view.findViewById(R.id.orders_history_recycler);
        ordersHistoryRecyclerAdapter = new OrdersHistoryRecyclerAdapter(new OrdersRecyclerInterface() {
            @Override
            public void onOrderSelected(View v, Order order) {
                Activity activity = getActivity();
                if (activity instanceof MainActivity) ((MainActivity) activity).onOrderSelected(order);
            }
        });
        ordersRecycler.setAdapter(ordersHistoryRecyclerAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ordersRecycler.getContext());
        ordersRecycler.setLayoutManager(layoutManager);
        ordersRecycler.setItemAnimator(new DefaultItemAnimator());
        ordersRecycler.setHasFixedSize(true);
        ordersRecycler.addItemDecoration(new RecyclerMarginDecorator(getResources().getDimensionPixelSize(R.dimen.base_margin)));

        endlessRecyclerScrollListener = new EndlessRecyclerScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                if (ordersMetadata != null && ordersMetadata.getLinks() != null && ordersMetadata.getLinks().getNext() != null) {
                    loadOrders(ordersMetadata.getLinks().getNext());
                } else {
                    Timber.d("CustomLoadMoreDataFromApi NO MORE DATA");
                }
            }
        };
        ordersRecycler.addOnScrollListener(endlessRecyclerScrollListener);
    }

    /**
     * Endless content loader. Should be used after views inflated.
     *
     * @param url null for fresh load. Otherwise use URLs from response metadata.
     */
    private void loadOrders(String url) {
        User user = SettingsMy.getActiveUser();
        if (user != null) {
            progressDialog.show();
            if (url == null) {
                ordersHistoryRecyclerAdapter.clear();
                url = String.format(EndPoints.ORDERS, SettingsMy.getActualNonNullShop(getActivity()).getId());
            }
            GsonRequest<OrderResponse> req = new GsonRequest<>(Request.Method.GET, url, null, OrderResponse.class, new Response.Listener<OrderResponse>() {
                @Override
                public void onResponse(OrderResponse response) {
                    ordersMetadata = response.getMetadata();
                    ordersHistoryRecyclerAdapter.addOrders(response.getOrders());

                    if (ordersHistoryRecyclerAdapter.getItemCount() > 0) {
                        empty.setVisibility(View.GONE);
                        content.setVisibility(View.VISIBLE);
                    } else {
                        empty.setVisibility(View.VISIBLE);
                        content.setVisibility(View.GONE);
                    }
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
            MyApplication.getInstance().addToRequestQueue(req, CONST.ORDERS_HISTORY_REQUESTS_TAG);
        } else {
            LoginExpiredDialogFragment loginExpiredDialogFragment = new LoginExpiredDialogFragment();
            loginExpiredDialogFragment.show(getFragmentManager(), "loginExpiredDialogFragment");
        }
    }

    @Override
    public void onStop() {
        if (progressDialog != null) {
            // Hide progress dialog if exist.
            if (progressDialog.isShowing() && endlessRecyclerScrollListener != null) {
                // Fragment stopped during loading data. Allow new loading on return.
                endlessRecyclerScrollListener.resetLoading();
            }
            progressDialog.cancel();
        }
        MyApplication.getInstance().cancelPendingRequests(CONST.ORDERS_HISTORY_REQUESTS_TAG);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (ordersRecycler != null) ordersRecycler.clearOnScrollListeners();
        super.onDestroyView();
    }
}

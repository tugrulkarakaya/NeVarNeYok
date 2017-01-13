package uk.co.nevarneyok.interfaces;

import android.view.View;

import uk.co.nevarneyok.entities.order.Order;

public interface OrdersRecyclerInterface {

    void onOrderSelected(View v, Order order);

}

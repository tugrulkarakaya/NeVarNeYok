package uk.co.nevarneyok.interfaces;

import uk.co.nevarneyok.entities.cart.CartDiscountItem;
import uk.co.nevarneyok.entities.cart.CartProductItem;

public interface CartRecyclerInterface {

    void onProductUpdate(CartProductItem cartProductItem);

    void onProductDelete(CartProductItem cartProductItem);

    void onDiscountDelete(CartDiscountItem cartDiscountItem);

    void onProductSelect(long productId);

}

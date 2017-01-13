package uk.co.nevarneyok.interfaces;

public interface UpdateCartItemListener {

    void updateProductInCart(long productCartId, long newVariantId, int newQuantity);

}


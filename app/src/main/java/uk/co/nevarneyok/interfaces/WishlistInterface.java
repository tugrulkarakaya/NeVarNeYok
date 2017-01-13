package uk.co.nevarneyok.interfaces;

import android.view.View;

import uk.co.nevarneyok.entities.wishlist.WishlistItem;

public interface WishlistInterface {

    void onWishlistItemSelected(View view, WishlistItem wishlistItem);

    void onRemoveItemFromWishList(View caller, WishlistItem wishlistItem, int adapterPosition);
}

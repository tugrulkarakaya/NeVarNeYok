package uk.co.nevarneyok.interfaces;

import android.view.View;

import uk.co.nevarneyok.entities.product.Product;

public interface RelatedProductsRecyclerInterface {

    void onRelatedProductSelected(View v, int position, Product product);
}

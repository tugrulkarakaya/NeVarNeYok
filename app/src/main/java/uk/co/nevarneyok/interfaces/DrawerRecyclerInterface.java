package uk.co.nevarneyok.interfaces;

import android.view.View;

import uk.co.nevarneyok.entities.drawerMenu.DrawerItemCategory;
import uk.co.nevarneyok.entities.drawerMenu.DrawerItemPage;

public interface DrawerRecyclerInterface {

    void onCategorySelected(View v, DrawerItemCategory drawerItemCategory);

    void onPageSelected(View v, DrawerItemPage drawerItemPage);

    void onHeaderSelected();
}

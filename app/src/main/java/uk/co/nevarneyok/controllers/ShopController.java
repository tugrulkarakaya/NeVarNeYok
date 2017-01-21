package uk.co.nevarneyok.controllers;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import uk.co.nevarneyok.R;
import uk.co.nevarneyok.SettingsMy;
import uk.co.nevarneyok.api.FIRDataServices;
import uk.co.nevarneyok.entities.Shop;
import uk.co.nevarneyok.entities.User;
import uk.co.nevarneyok.ux.MainActivity;


public class ShopController {
    public interface FirebaseCallResult{
        void onComplete(boolean result);
    }
    public interface completion{
        void setResult(boolean result, User user);
    }

    public static ArrayList<Shop> getShopList(){

        ArrayList<Shop> shopList = new ArrayList<Shop>();
        Shop ShopEn = new Shop();
        ShopEn.setId(18);
        ShopEn.setCurrency("USD");

        ShopEn.setName("English");
        ShopEn.setDescription("English");
        ShopEn.setFlagIcon(R.drawable.flag_en);
        ShopEn.setLanguage("en");

        Shop ShopTr = new Shop();
        ShopTr.setName("Türkçe");
        ShopTr.setId(21);
        ShopTr.setCurrency("TRY");
        ShopTr.setDescription("Uygulamayı Türkçe Kullan");
        ShopTr.setFlagIcon(R.drawable.flag_tr);
        ShopTr.setLanguage("tr");

        shopList.add(ShopEn);
        shopList.add(ShopTr);

        return shopList;
    }
}

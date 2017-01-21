package uk.co.nevarneyok.entities;

/**
 * Created by mcagrikarakaya on 18.01.2017.
 */

public class Contact {
    private String name, phone, photoUrl;

    public Contact(){}

    public Contact(String name, String phone, String photoUrl) {
        this.name = name;
        this.phone = phone;
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}

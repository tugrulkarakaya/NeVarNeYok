package uk.co.nevarneyok.entities;

/**
 * Created by mcagrikarakaya on 18.01.2017.
 */

public class Contact {
    private String name, phone, photoUrl, uid;
    private boolean added=false;

    public Contact(){}

    public Contact(String name, String phone, String photoUrl, String uid, boolean added) {
        this.name = name;
        this.phone = phone;
        this.photoUrl = photoUrl;
        this.uid = uid;
        this.added=added;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isAdded() {
        return added;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }
}

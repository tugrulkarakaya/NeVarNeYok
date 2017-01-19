package uk.co.nevarneyok.entities;


import android.os.Build;
import android.provider.Contacts;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Objects;

public class User {

    private long id;
    private String fbId;
    private String accessToken;
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private long birthDate;
    private long createDate;
    private String street;
    private String city;
    private String houseNumber;
    private String zip;
    private String country;
    private String profileImageUrl;
    private String Uid;
    private String providerId;
    private String provider;

    public User() {

    }

    public User(String Uid) {
        this.Uid = Uid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName){this.lastName = lastName; }

    public String getProfileImageUrl(){return this.profileImageUrl;}

    public void setProfileImageUrl(String profileImageUrl){this.profileImageUrl = profileImageUrl;}

    public long getCreateDate(){return createDate;}

    public void setCreateDate(long createDate){ this.createDate = createDate;}

    public long getBirthDate(){return this.birthDate;}

    public void setBirthDate(long birthDate){this.birthDate = birthDate;}

    public String getUid(){return this.Uid;}

    public void setUid(String Uid){this.Uid = Uid;}

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProviderId(){return providerId;}

    public void setProviderId(String providerId){ this.providerId = providerId;}

    public String getProvider(){return provider;}

    public void setProvider(String provider){this.provider = provider;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (fbId != null ? !fbId.equals(user.fbId) : user.fbId != null) return false;
        if (accessToken != null ? !accessToken.equals(user.accessToken) : user.accessToken != null)
            return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        if (lastName != null ? !lastName.equals(user.lastName) : user.lastName != null) return false;
        if (street != null ? !street.equals(user.street) : user.street != null) return false;
        if (city != null ? !city.equals(user.city) : user.city != null) return false;
        if (houseNumber != null ? !houseNumber.equals(user.houseNumber) : user.houseNumber != null)
            return false;
        if (zip != null ? !zip.equals(user.zip) : user.zip != null) return false;
        if (profileImageUrl != null ? !profileImageUrl.equals(user.profileImageUrl) : user.profileImageUrl != null) return false;
        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        if (phone != null ? !phone.equals(user.phone) : user.phone != null) return false;
        if (gender != null ? !gender.equals(user.gender) : user.gender != null) return false;
        if (birthDate != user.birthDate) return false;
        if (gender != null ? !gender.equals(user.gender) : user.gender != null) return false;
        if (Uid != null ? !Uid.equals(user.Uid) : user.Uid != null) return false;
        if (providerId != null ? !providerId.equals(user.providerId) : user.providerId != null) return false;
        if (provider != null ? !provider.equals(user.provider) : user.provider != null) return false;
        if (createDate != user.createDate) return false;
        return !(country != null ? !country.equals(user.country) : user.country != null);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (fbId != null ? fbId.hashCode() : 0);
        result = 31 * result + (accessToken != null ? accessToken.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (street != null ? street.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (houseNumber != null ? houseNumber.hashCode() : 0);
        result = 31 * result + (zip != null ? zip.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (Objects.toString(birthDate, null).hashCode());
        result = 31 * result + (profileImageUrl != null ? profileImageUrl.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (Uid != null ? Uid.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (provider != null ? provider.hashCode() : 0);
        result = 31 * result + (providerId != null ? providerId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", Uid='" + Uid + '\'' +
                ", fbId='" + fbId + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", houseNumber='" + houseNumber + '\'' +
                ", zip='" + zip + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                ", country='" + country + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", providerId='" + providerId + '\'' +
                ", provider='" + provider + '\'' +
                '}';
    }

    public void setUser(User user){
        this.setId(user.id);
        this.setFbId(user.fbId);
        this.setAccessToken(user.accessToken);
        this.setName(user.name);
        this.setLastName(user.lastName);
        this.setStreet(user.street);
        this.setCity(user.city);
        this.setHouseNumber(user.houseNumber);
        this.setZip(user.zip);
        this.setEmail(user.email);
        this.setPhone(user.phone);
        this.setGender(user.gender);
        this.setCountry(user.country);
        this.setProfileImageUrl(user.profileImageUrl);
        this.setBirthDate(user.birthDate);
        this.setUid(user.Uid);
        this.setProvider(user.provider);
        this.setProviderId(user.providerId);
    }
}

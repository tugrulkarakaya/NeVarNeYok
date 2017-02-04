/*
http://www.jsonschema2pojo.org/
    {"data": {"link": "http://www.hurriyet.com.tr/",
            "message": "İndirimli turlarımız güncellendi. Hemen bakın",
            "title": "Çok şanslısınız!",
            "image_url": "https://image.freepik.com/free-icon/twitter-logo_318-40459.jpg",
            "htmlId":"5463"},
            "registration_ids" : ["c0BzVw3seFM:APA91bEXaZhc-oofjjU-RccKSg4aAXL45kGaEblVp2KwPG0kyKq_jF3UMYkpeQZ60I_XQc7bV6jZxxY_yItEJzZgcyRGzpVkRAnqFATGA65EUxoJ5EJJzrt-reEJildRbEQg2syEYlyF",
            "clfzJR5Yt_g:APA91bErZeBtmfV9Avmi8xv_RTIDyrVbeN24xAIiEi-fzYSFAYg7GTFZv-l-the0RVGkSwpr511U1BTMr2vs_iHNmyElu8BYhZrSRr_JVunOvuRtQ6eFGXUl5IHLLWZmLdpDXyNfVMwt"]
     }
 */
package uk.co.nevarneyok.entities;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.Gson;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "data",
        "registration_ids"
})
public class FIRNotificaiton {

    @JsonProperty("data")
    private Data data;
    @JsonProperty("registration_ids")
    private List<String> registration_ids = null;

    /**
     * No args constructor for use in serialization
     */
    public FIRNotificaiton() {
    }

    /**
     * @param data
     * @param registrationIds
     */
    public FIRNotificaiton(Data data, List<String> registrationIds) {
        super();
        this.data = data;
        this.registration_ids = registrationIds;
    }

    @JsonProperty("data")
    public Data getData() {
        if(data==null){
            data = new Data();
        }
        return data;
    }

    @JsonProperty("data")
    public void setData(Data data) {
        this.data = data;
    }

    public FIRNotificaiton withData(Data data) {
        this.data = data;
        return this;
    }

    @JsonProperty("registration_ids")
    public List<String> getRegistrationIds() {
        return registration_ids;
    }

    @JsonProperty("registration_ids")
    public void setRegistrationIds(List<String> registrationIds) {
        this.registration_ids = registrationIds;
    }

    public FIRNotificaiton withRegistrationIds(List<String> registrationIds) {
        this.registration_ids = registrationIds;
        return this;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        String jsonString = gson.toJson(this);
        return jsonString;
    }

    public JSONObject jsonObject() throws JSONException {
        return new JSONObject(toString());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "link",
            "message",
            "title",
            "image_url",
            "htmlId"
    })
    public class Data {

        @JsonProperty("link")
        private String link;
        @JsonProperty("message")
        private String message;
        @JsonProperty("title")
        private String title;
        @JsonProperty("image_url")
        private String imageUrl;
        @JsonProperty("htmlId")
        private String htmlId;

        /**
         * No args constructor for use in serialization
         */
        public Data() {
        }

        /**
         * @param message
         * @param title
         * @param imageUrl
         * @param link
         * @param htmlId
         */
        public Data(String link, String message, String title, String imageUrl, String htmlId) {
            super();
            this.link = link;
            this.message = message;
            this.title = title;
            this.imageUrl = imageUrl;
            this.htmlId = htmlId;
        }

        @JsonProperty("link")
        public String getLink() {
            return link;
        }

        @JsonProperty("link")
        public void setLink(String link) {
            this.link = link;
        }

        public Data withLink(String link) {
            this.link = link;
            return this;
        }

        @JsonProperty("message")
        public String getMessage() {
            return message;
        }

        @JsonProperty("message")
        public void setMessage(String message) {
            this.message = message;
        }

        public Data withMessage(String message) {
            this.message = message;
            return this;
        }

        @JsonProperty("title")
        public String getTitle() {
            return title;
        }

        @JsonProperty("title")
        public void setTitle(String title) {
            this.title = title;
        }

        public Data withTitle(String title) {
            this.title = title;
            return this;
        }

        @JsonProperty("image_url")
        public String getImageUrl() {
            return imageUrl;
        }

        @JsonProperty("image_url")
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public Data withImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        @JsonProperty("htmlId")
        public String getHtmlId() {
            return htmlId;
        }

        @JsonProperty("htmlId")
        public void setHtmlId(String htmlId) {
            this.htmlId = htmlId;
        }

        public Data withHtmlId(String htmlId) {
            this.htmlId = htmlId;
            return this;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

    }
}






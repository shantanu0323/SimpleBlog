package shantanu.simpleblog;

/**
 * Created by SHAAN on 03-06-17.
 */

public class Blog {
    private String title;
    private String desc;
    private String image;
    private String username;
    private String time;
    private String uid;

    public Blog() {

    }

    public Blog(String title, String desc, String image, String username, String time, String uid) {
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.username = username;
        this.time = time;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

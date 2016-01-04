package boy.dekatku.com.kpmg.Models;

/**
 * Created by Boy on 9/21/15.
 */
public class SEModel {

    private String catID;
    private String catName;
    private String catIcon;
    private String status;
    private String message;
    private String description;
    private String catPic;
    private String catDate;
    private String catDes;



    public String getCatID() {
        return catID;
    }

    public String getCatPic() {
        return catPic;
    }

    public void setCatPic(String catPic) {
        this.catPic = catPic;
    }

    public String getCatDate() {
        return catDate;
    }

    public void setCatDate(String catDate) {
        this.catDate = catDate;
    }

    public String getCatDes() {
        return catDes;
    }

    public void setCatDes(String catDes) {
        this.catDes = catDes;
    }

    public void setCatID(String catID) {
        this.catID = catID;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public String getCatIcon() {
        return catIcon;
    }

    public void setCatIcon(String catIcon) {
        this.catIcon = catIcon;
    }



    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SEModel(){

    }

    public SEModel(String catDes, String catDate, String catPic, String description, String message, String catName, String catIcon, String status, String catID) {
        this.catDes = catDes;
        this.catDate = catDate;
        this.catPic = catPic;
        this.description = description;
        this.message = message;
        this.catName = catName;
        this.catIcon = catIcon;
        this.status = status;
        this.catID = catID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

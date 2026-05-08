package model;
import java.time.LocalDateTime;
import model.UserModel;
/**
 * Reflects de Registros table in the database.
 * This class is used to represent register data in the application.
 */
public class RegisterModel {
    private LocalDateTime date_hour;
    private int MID;
    private int UID;
    private String detail;


    // Getters and Setters
    public LocalDateTime getDate_hour() {
        return this.date_hour;
    }

    public void setDate_hour(LocalDateTime date_hour) {
        this.date_hour = date_hour;
    }

    public int getMID() {
        return this.MID;
    }

    public void setMID(int MID) {
        this.MID = MID;
    }

    public int getUID() {
        return this.UID;
    }

    public void setUID(int UID) {
        this.UID = UID;
    }

    public String getDetail() {
        return this.detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}

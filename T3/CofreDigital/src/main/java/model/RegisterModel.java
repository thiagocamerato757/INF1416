package model;
import java.time.LocalDateTime;

public class RegisterModel {
    private LocalDateTime date_hour;
    private int MID;
    private int UID;
    private String fname;

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

    public String getFname() {
        return this.fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }
}

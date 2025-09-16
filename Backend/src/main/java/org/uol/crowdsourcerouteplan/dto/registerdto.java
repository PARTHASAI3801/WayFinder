package org.uol.crowdsourcerouteplan.dto;



import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class registerdto {

    private String fullName;
    private String uname;
    private String password;
    private String confirmPassword;

    public registerdto(String fullName, String uname, String password, String confirmPassword) {
        this.fullName = fullName;
        this.uname = uname;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public registerdto() {
    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}

package com.gamevault.univibe;

public class User {
    private String fullName;
    private String institutionalEmail;
    private String university;
    private String password;

    public User() {
    }

    public User(String fullName, String institutionalEmail, String university, String password) {
        this.fullName = fullName;
        this.institutionalEmail = institutionalEmail;
        this.university = university;
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getInstitutionalEmail() {
        return institutionalEmail;
    }

    public void setInstitutionalEmail(String institutionalEmail) {
        this.institutionalEmail = institutionalEmail;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

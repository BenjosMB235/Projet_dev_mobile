package com.example.mit_plateform.Models;

public class User {

    private String id;
    private String name;
    private String email;
    private String role;
    private String profileImage;
    private int availability = 0; // 0 = hors ligne, 1 = en ligne

    public User() {}

    // ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Nom
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    // Email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Rôle (admin/client)
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Image de profil
    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    // Disponibilité
    public int getAvailability() {
        return availability;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }
}

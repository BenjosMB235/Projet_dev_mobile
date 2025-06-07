package com.example.mit_plateform.Models;

import java.util.Date;

public class PreInscription {
    private String dossierNumber;
    private String userId;
    private String nom;
    private String prenom;
    private Date submissionDate;
    private Date validationDate;
    private boolean preInscriptionComplete;
    private boolean postSoumissionComplete;
    private boolean paiementComplete;
    private String status; // pending, approved, rejected

    public PreInscription() {}

    // Getters and Setters
    public String getDossierNumber() {
        return dossierNumber;
    }

    public void setDossierNumber(String dossierNumber) {
        this.dossierNumber = dossierNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNomComplet() {
        return nom + " " + prenom;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }

    public Date getValidationDate() {
        return validationDate;
    }

    public void setValidationDate(Date validationDate) {
        this.validationDate = validationDate;
    }

    public boolean isPreInscriptionComplete() {
        return preInscriptionComplete;
    }

    public void setPreInscriptionComplete(boolean preInscriptionComplete) {
        this.preInscriptionComplete = preInscriptionComplete;
    }

    public boolean isPostSoumissionComplete() {
        return postSoumissionComplete;
    }

    public void setPostSoumissionComplete(boolean postSoumissionComplete) {
        this.postSoumissionComplete = postSoumissionComplete;
    }

    public boolean isPaiementComplete() {
        return paiementComplete;
    }

    public void setPaiementComplete(boolean paiementComplete) {
        this.paiementComplete = paiementComplete;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
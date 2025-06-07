package com.example.mit_plateform.Models;

import java.util.Date;

public class ChatMessage {

    public String senderId;
    public String recipientId; // ⬅️ renommé pour cohérence avec le reste du code
    public String message;

    public String dateTime;    // formaté pour affichage
    public Date dateObject;    // utilisé pour le tri chronologique

    public String file;        // contenu encodé (Base64)
    public String fileType;    // MIME type (image/png, application/pdf…)

    // Optionnel — utilisé si tu affiches des aperçus de conversation
    public String conversionId;
    public String conversionName;
    public String conversionImage;

    // Constructeur vide requis par Firebase
    public ChatMessage() {}

    // Constructeur utile pour créer un message rapidement
    public ChatMessage(String senderId, String recipientId, String message, Date dateObject) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.message = message;
        this.dateObject = dateObject;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Date getDateObject() {
        return dateObject;
    }

    public void setDateObject(Date dateObject) {
        this.dateObject = dateObject;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getConversionId() {
        return conversionId;
    }

    public void setConversionId(String conversionId) {
        this.conversionId = conversionId;
    }

    public String getConversionName() {
        return conversionName;
    }

    public void setConversionName(String conversionName) {
        this.conversionName = conversionName;
    }

    public String getConversionImage() {
        return conversionImage;
    }

    public void setConversionImage(String conversionImage) {
        this.conversionImage = conversionImage;
    }
}

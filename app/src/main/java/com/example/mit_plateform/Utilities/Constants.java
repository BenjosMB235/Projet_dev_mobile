package com.example.mit_plateform.Utilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe de constantes pour l'application.
 * Contient les clés utilisées pour Firebase, SharedPreferences et les en-têtes de messages.
 */
public class Constants {

    // ====================== Collections Firestore ======================
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";

    // ====================== Champs Utilisateur ======================
    public static final String KEY_PASSWORD = "password"; // À éviter en clair - préférer Firebase Auth
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String KEY_USER = "user";


    // ====================== Messagerie ======================
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";


    // ====================== Préférences Partagées ======================
    public static final String KEY_PREFERENCE_NAME = "mitPlatformPrefs";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ROLE = "role";
    public static final String KEY_IS_SIGNED_IN = "is_signed_in";
    // ✅ ID fixe de l'administrateur pour les requêtes côté admin
    public static final String ADMIN_ID = "C6ZCABiiPvcWqstgi60KvtJIOVI2";

    // ====================== Fichiers ======================
    public static final String KEY_FILE = "file";
    public static final String KEY_FILE_TYPE = "fileType";

    // ====================== Messages Distants (FCM) ======================
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type"; // Correction de la casse
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    // ====================== Améliorations ======================
    private static final String FCM_SERVER_KEY = "BAKvUVkzBl8mOOdBnEuFXfGTzQXdPc7wXSLyuKIqbN_Kg9milnjeQ7ctAjmiERSSHLfifzrGE0htd_3MdxR9XrM";

    /**
     * Retourne les en-têtes pour les requêtes FCM.
     * Version thread-safe avec initialisation tardive.
     */
    public static Map<String, String> getRemoteMsgHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(REMOTE_MSG_AUTHORIZATION, "key=" + FCM_SERVER_KEY);
        headers.put(REMOTE_MSG_CONTENT_TYPE, "application/json");
        return headers;
    }

    // Alternative plus sécurisée (à implémenter) :
    // public static String getFcmServerKey() {
    //     return BuildConfig.FCM_SERVER_KEY; // Stockée dans gradle.properties
    // }


}

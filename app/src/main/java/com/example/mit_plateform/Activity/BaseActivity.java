package com.example.mit_plateform.Activity;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mit_plateform.Utilities.Constants;
import com.example.mit_plateform.Utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

/**
 * Classe de base pour les activités de l'application.
 * Gère la disponibilité de l'utilisateur automatiquement lors des changements de lifecycle.
 */
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    private DocumentReference userDocumentRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeUserAvailabilityTracker();
    }

    /**
     * Initialise le suivi de disponibilité de l'utilisateur
     */
    private void initializeUserAvailabilityTracker() {
        try {
            PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
            String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

            if (currentUserId != null && !currentUserId.isEmpty()) {
                userDocumentRef = FirebaseFirestore.getInstance()
                        .collection(Constants.KEY_COLLECTION_USERS)
                        .document(currentUserId);
            } else {
                Log.w(TAG, "User ID not found in preferences");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing availability tracker", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserAvailability(0); // Marque comme indisponible
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserAvailability(1); // Marque comme disponible
    }

    /**
     * Met à jour le statut de disponibilité de l'utilisateur
     * @param availability 1 pour disponible, 0 pour indisponible
     */
    private void updateUserAvailability(int availability) {
        if (userDocumentRef != null) {
            userDocumentRef.update(Constants.KEY_AVAILABILITY, availability)
                    .addOnFailureListener(e -> {
                        if (e instanceof FirebaseFirestoreException) {
                            FirebaseFirestoreException firestoreEx = (FirebaseFirestoreException) e;
                            Log.e(TAG, "Error updating availability. Code: " + firestoreEx.getCode(), e);
                        } else {
                            Log.e(TAG, "Error updating availability", e);
                        }
                    });
        }
    }

    /**
     * Fournit une référence au document utilisateur pour les classes enfants
     */
    protected DocumentReference getUserDocumentReference() {
        return userDocumentRef;
    }
}
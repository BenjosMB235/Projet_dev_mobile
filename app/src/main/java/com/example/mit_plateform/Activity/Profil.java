package com.example.mit_plateform.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mit_plateform.R;
import com.example.mit_plateform.Utilities.NavigationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Profil extends AppCompatActivity {

    private TextView tvNom, tvPrenom, tvEmail, tvTelephone;
    private Button btnModifierProfil;
    private ImageView btnAccueil, btnNotes, btnMessages, btnProfil, btnNotifications;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    // Déclaration du ActivityResultLauncher
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        // Initialisation du launcher
        setupActivityLauncher();

        // Initialisation des vues
        initViews();

        // Initialisation Firebase
        initFirebase();

        // Configuration des listeners
        setupListeners();

        NavigationUtils.updateActiveIcon(this, btnProfil, true);
    }

    private void setupActivityLauncher() {
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Recharger les données après modification
                        loadUserData();
                        Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initViews() {
        tvNom = findViewById(R.id.tvNom);
        tvPrenom = findViewById(R.id.tvPrenom);
        tvEmail = findViewById(R.id.tvEmail);
        tvTelephone = findViewById(R.id.tvTelephone);
        btnModifierProfil = findViewById(R.id.btnModifierProfil);

        btnAccueil = findViewById(R.id.btnAccueil);
        btnNotes = findViewById(R.id.btnNotes);
        btnMessages = findViewById(R.id.btnMessages);
        btnProfil = findViewById(R.id.btnProfil);
        btnNotifications = findViewById(R.id.btnNotifications);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            loadUserData();
        } else {
            navigateTo(Connexion.class);
        }
    }

    private void setupListeners() {
        btnModifierProfil.setOnClickListener(v -> openEditProfile());

        btnAccueil.setOnClickListener(v -> navigateTo(Home.class));
        btnNotifications.setOnClickListener(v -> navigateTo(Notification.class));
        btnMessages.setOnClickListener(v -> navigateTo(Chat.class));
        btnNotes.setOnClickListener(v -> navigateTo(Pre_inscription.class));
    }

    private void navigateTo(Class<?> destination) {
        if (!this.getClass().equals(destination)) {
            startActivity(new Intent(this, destination));
            finish();
        }
    }

    private void loadUserData() {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    updateUI(document);
                } else {
                    handleError("Aucune donnée trouvée", null);
                    // Option: créer un document vide si c'est la première connexion
                    createInitialUserDocument();
                }
            } else {
                handleError("Erreur de connexion", task.getException());
            }
        });
    }

    private void updateUI(DocumentSnapshot document) {
        try {
            String nom = document.getString("nom");
            String prenom = document.getString("prenom");
            String email = document.getString("email");
            String telephone = document.getString("telephone");

            tvNom.setText(nom != null ? nom : "Non renseigné");
            tvPrenom.setText(prenom != null ? prenom : "Non renseigné");
            tvEmail.setText(email != null ? email : "Non renseigné");
            tvTelephone.setText(telephone != null ? telephone : "Non renseigné");
        } catch (Exception e) {
            handleError("Format de données invalide", e);
        }
    }

    private void createInitialUserDocument() {
        Map<String, Object> user = new HashMap<>();
        user.put("nom", "Non renseigné");
        user.put("prenom", "Non renseigné");
        user.put("email", mAuth.getCurrentUser().getEmail());
        user.put("telephone", "Non renseigné");

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> loadUserData())
                .addOnFailureListener(e -> handleError("Échec création profil", e));
    }

    private void handleError(String message, Exception e) {
        String errorMsg = message;
        if (e != null && e.getMessage() != null) {
            errorMsg += ": " + e.getMessage();
        }
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        Log.e("ProfilActivity", errorMsg, e);
    }


    private void openEditProfile() {
        try {
            Log.d("Profil", "Début de openEditProfile");
            Intent intent = new Intent(this, EditProfil.class);
            intent.putExtra("nom", tvNom.getText().toString());
            intent.putExtra("prenom", tvPrenom.getText().toString());
            intent.putExtra("email", tvEmail.getText().toString());
            intent.putExtra("telephone", tvTelephone.getText().toString());

            Log.d("Profil", "Avant lancement de EditProfil");
            editProfileLauncher.launch(intent);
            Log.d("Profil", "Après lancement de EditProfil");
        } catch (Exception e) {
            Log.e("Profil", "Erreur dans openEditProfile", e);
            Toast.makeText(this, "Erreur lors de l'ouverture de l'éditeur", Toast.LENGTH_SHORT).show();
        }
    }
}
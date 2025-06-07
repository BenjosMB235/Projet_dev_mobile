package com.example.mit_plateform.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.mit_plateform.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditProfil extends AppCompatActivity {

    private EditText etNom, etPrenom, etEmail, etTelephone;
    private Button btnSave;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profil);

        // Initialisation des vues
        initViews();

        // Initialisation Firebase
        initFirebase();

        // Remplir les champs avec les données existantes
        populateFields();

        // Gestion du clic sur le bouton Enregistrer
        btnSave.setOnClickListener(v -> updateProfile());
    }

    // Ajoutez cette vérification dans initViews()
    private void initViews() {
        etNom = findViewById(R.id.etNom);
        etPrenom = findViewById(R.id.etPrenom);
        etEmail = findViewById(R.id.etEmail);
        etTelephone = findViewById(R.id.etTelephone);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        if (etNom == null || etPrenom == null || etEmail == null
                || etTelephone == null || btnSave == null || progressBar == null) {
            Toast.makeText(this, "Erreur d'initialisation des vues", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initFirebase() {
        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser == null) {
                Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            userId = currentUser.getUid();
        } catch (Exception e) {
            Log.e("EditProfil", "Erreur d'initialisation Firebase", e);
            Toast.makeText(this, "Erreur d'initialisation", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void populateFields() {
        Intent intent = getIntent();
        if (intent != null) {
            etNom.setText(intent.getStringExtra("nom") != null ? intent.getStringExtra("nom") : "");
            etPrenom.setText(intent.getStringExtra("prenom") != null ? intent.getStringExtra("prenom") : "");
            etEmail.setText(intent.getStringExtra("email") != null ? intent.getStringExtra("email") : "");
            etTelephone.setText(intent.getStringExtra("telephone") != null ? intent.getStringExtra("telephone") : "");
        }
    }

    private void updateProfile() {
        // Valider les champs
        if (!validateFields()) {
            return;
        }

        // Afficher le loader et désactiver le bouton
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // Récupérer les nouvelles valeurs
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("nom", etNom.getText().toString().trim());
        userUpdates.put("prenom", etPrenom.getText().toString().trim());
        userUpdates.put("email", etEmail.getText().toString().trim());
        userUpdates.put("telephone", etTelephone.getText().toString().trim());

        // Mettre à jour dans Firestore
        db.collection("users").document(userId)
                .update(userUpdates)
                .addOnCompleteListener(task -> {
                    // Cacher le loader et réactiver le bouton
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);

                    if (task.isSuccessful()) {
                        handleUpdateSuccess();
                    } else {
                        handleUpdateFailure(task.getException());
                    }
                });
    }

    private boolean validateFields() {
        if (etNom.getText().toString().trim().isEmpty()) {
            etNom.setError("Le nom est requis");
            return false;
        }
        if (etPrenom.getText().toString().trim().isEmpty()) {
            etPrenom.setError("Le prénom est requis");
            return false;
        }
        if (etEmail.getText().toString().trim().isEmpty()) {
            etEmail.setError("L'email est requis");
            return false;
        }
        return true;
    }

    private void handleUpdateSuccess() {
        Toast.makeText(this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();

        // Retourner avec résultat OK
        setResult(RESULT_OK);
        finish();
    }

    private void handleUpdateFailure(Exception exception) {
        String errorMessage = "Échec de la mise à jour";
        if (exception != null && exception.getMessage() != null) {
            errorMessage += ": " + exception.getMessage();
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
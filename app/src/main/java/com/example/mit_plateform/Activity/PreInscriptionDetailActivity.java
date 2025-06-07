package com.example.mit_plateform.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mit_plateform.Models.PreInscription;
import com.example.mit_plateform.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import androidx.core.content.ContextCompat;


public class PreInscriptionDetailActivity extends AppCompatActivity {

    private TextView tvDossierNumber, tvStudentName, tvSubmissionDate, tvValidationDate, tvStatus;
    private TextView tvFiliere, tvEmail, tvTelephone, tvAdresse;
    private Button btnValidate;
    private FirebaseFirestore db;
    private String dossierId;
    private String dossierNumber; // Ajout de cette variable pour stocker le numéro de dossier

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_inscription_detail);

        db = FirebaseFirestore.getInstance();
        dossierId = getIntent().getStringExtra("DOSSIER_ID");

        initViews();
        loadDossierDetails();
    }

    private void initViews() {
        tvDossierNumber = findViewById(R.id.tvDossierNumber);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvSubmissionDate = findViewById(R.id.tvSubmissionDate);
        tvValidationDate = findViewById(R.id.tvValidationDate);
        tvStatus = findViewById(R.id.tvStatus);
        tvFiliere = findViewById(R.id.tvFiliere);
        tvEmail = findViewById(R.id.tvEmail);
        tvTelephone = findViewById(R.id.tvTelephone);
        tvAdresse = findViewById(R.id.tvAdresse);
        btnValidate = findViewById(R.id.btnValidate);

        btnValidate.setOnClickListener(v -> validateDossier());
    }

    private void loadDossierDetails() {
        db.collection("preInscriptions").document(dossierId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Stocker le numéro de dossier pour l'utiliser plus tard
                        dossierNumber = documentSnapshot.getString("dossierNumber");
                        displayDossierDetails(documentSnapshot);
                    } else {
                        Toast.makeText(this, "Dossier introuvable", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayDossierDetails(DocumentSnapshot document) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        tvDossierNumber.setText(document.getString("dossierNumber"));
        tvStudentName.setText(document.getString("nom") + " " + document.getString("prenom"));
        tvSubmissionDate.setText(sdf.format(document.getDate("submissionDate")));
        tvFiliere.setText(document.getString("filiere"));
        tvEmail.setText(document.getString("email"));
        tvTelephone.setText(document.getString("telephone"));
        tvAdresse.setText(document.getString("adresse"));

        if (document.getBoolean("postSoumissionComplete") != null &&
                document.getBoolean("postSoumissionComplete")) {
            tvStatus.setText("Statut : Validé");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.vert));
            btnValidate.setEnabled(false);
            btnValidate.setVisibility(View.GONE);

            if (document.getDate("validationDate") != null) {
                tvValidationDate.setText("Validé le : " + sdf.format(document.getDate("validationDate")));
                tvValidationDate.setVisibility(View.VISIBLE);
            }
        } else {
            tvStatus.setText("Statut : En attente de validation");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.bleu_royal));
            btnValidate.setEnabled(true);
            tvValidationDate.setVisibility(View.GONE);
        }
    }

    private void validateDossier() {
        db.collection("preInscriptions").document(dossierId)
                .update(
                        "postSoumissionComplete", true,
                        "validationDate", new java.util.Date(),
                        "status", "approved"
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Dossier validé avec succès", Toast.LENGTH_SHORT).show();
                    sendNotificationToUser(
                            "Votre dossier " + dossierNumber + " a été approuvé"
                    );
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de la validation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendNotificationToUser(String message) {
        // Récupérer l'userId depuis Firestore avant d'envoyer la notification
        db.collection("preInscriptions").document(dossierId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userId = documentSnapshot.getString("userId");
                    if (userId != null) {
                        // Implémentation de l'envoi de notification
                        Map<String, Object> notification = new HashMap<>();
                        notification.put("userId", userId);
                        notification.put("title", "Dossier validé");
                        notification.put("message", message);
                        notification.put("timestamp", new java.util.Date());
                        notification.put("read", false);

                        db.collection("notifications").add(notification);
                    }
                });
    }
}
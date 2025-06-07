package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.mit_plateform.R;
import com.example.mit_plateform.Utilities.NavigationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Post_Soumission extends AppCompatActivity {
    private ImageView btnAccueil, btnNotifications, btnNotes, btnMessages, btnProfil, back_button;
    private TextView tvDossierNumber, tvStatus;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_soumission);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        initViews();
        setupNavigation();
        setupButtons();
        loadDossierData();
    }

    private void initViews() {
        btnAccueil = findViewById(R.id.btnAccueil);
        btnNotes = findViewById(R.id.btnNotes);
        btnMessages = findViewById(R.id.btnMessages);
        btnProfil = findViewById(R.id.btnProfil);
        btnNotifications = findViewById(R.id.btnNotifications);
        back_button = findViewById(R.id.back_button);

        tvDossierNumber = findViewById(R.id.ref_num);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void setupNavigation() {
        NavigationUtils.updateActiveIcon(this, btnNotes, true);

        btnAccueil.setOnClickListener(view -> navigateTo(Home.class));
        btnNotifications.setOnClickListener(view -> navigateTo(Notification.class));
        btnMessages.setOnClickListener(view -> navigateTo(Chat.class));
        btnProfil.setOnClickListener(view -> navigateTo(Profil.class));
        back_button.setOnClickListener(view -> navigateTo(Pre_inscription.class));
    }

    private void setupButtons() {
        Button viewCopyButton = findViewById(R.id.view_copy_button);
        viewCopyButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewPreInscriptionCopy.class);
            startActivity(intent);
        });

        Button contactButton = findViewById(R.id.contact_button);
        contactButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, Chat.class);
            intent.putExtra("recipient", "admin");
            startActivity(intent);
        });
    }

    private void loadDossierData() {
        if (userId == null) return;

        db.collection("preInscriptions").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            updateUI(document);
                        } else {
                            Toast.makeText(this, "Aucun dossier trouvé", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(DocumentSnapshot document) {
        // Numéro de dossier
        String dossierNumber = document.getString("dossierNumber");
        tvDossierNumber.setText(dossierNumber != null ? dossierNumber : "N/A");

        // Dates
        Date submissionDate = document.getDate("submissionDate");
        TextView tvSubmissionDate = findViewById(R.id.tvSubmissionDate);
        tvSubmissionDate.setText("Soumis le : " + getFormattedDate(submissionDate));

        // Statut
        boolean isApproved = document.getBoolean("postSoumissionComplete") != null
                && document.getBoolean("postSoumissionComplete");

        TextView tvValidationDate = findViewById(R.id.tvValidationDate);

        if (isApproved) {
            tvStatus.setText("Dossier validé");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.vert));

            Date validationDate = document.getDate("validationDate");
            tvValidationDate.setText("Validé le : " + getFormattedDate(validationDate));
            tvValidationDate.setVisibility(View.VISIBLE);

            findViewById(R.id.tvValidationTime).setVisibility(View.GONE);
        } else {
            tvStatus.setText("En attente de validation");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.bleu_royal));

            if (submissionDate != null) {
                long estimatedTime = submissionDate.getTime() + TimeUnit.HOURS.toMillis(72);
                TextView tvValidationTime = findViewById(R.id.tvValidationTime);
                tvValidationTime.setText("Délai estimé : " + getFormattedDate(new Date(estimatedTime)));
            }

            tvValidationDate.setVisibility(View.GONE);
        }
    }

    private String getFormattedDate(Date date) {
        if (date == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    private void navigateTo(Class<?> destination) {
        if (!this.getClass().equals(destination)) {
            startActivity(new Intent(this, destination));
            finish();
        }
    }
}
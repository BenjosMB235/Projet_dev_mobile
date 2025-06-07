package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mit_plateform.R;
import com.example.mit_plateform.Utilities.NavigationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Pre_inscription extends AppCompatActivity {

    private LinearLayout etapePreInscription, etapePostSoumission, etapePaiement;
    private ImageView preCheck, preHourglass, preBlock;
    private ImageView postCheck, postHourglass, postBlock;
    private ImageView paiementCheck, paiementHourglass, paiementBlock;
    private ImageView btnAccueil, btnNotes, btnMessages, btnProfil, btnNotifications, btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_inscription);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        initViews();
        setupClickListeners();
        loadProcessStatus();
        NavigationUtils.updateActiveIcon(this, btnNotes, true);
    }

    private void initViews() {
        etapePreInscription = findViewById(R.id.etape_pre_inscription);
        etapePostSoumission = findViewById(R.id.etape_post_soumission);
        etapePaiement = findViewById(R.id.etape_paiement);

        preCheck = findViewById(R.id.pre_check);
        preHourglass = findViewById(R.id.pre_hourglass);
        preBlock = findViewById(R.id.pre_block);

        postCheck = findViewById(R.id.post_check);
        postHourglass = findViewById(R.id.post_hourglass);
        postBlock = findViewById(R.id.post_block);

        paiementCheck = findViewById(R.id.paiement_check);
        paiementHourglass = findViewById(R.id.paiement_hourglass);
        paiementBlock = findViewById(R.id.paiement_block);

        btnAccueil = findViewById(R.id.btnAccueil);
        btnNotes = findViewById(R.id.btnNotes);
        btnMessages = findViewById(R.id.btnMessages);
        btnProfil = findViewById(R.id.btnProfil);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnBack = findViewById(R.id.back_button);
    }

    private void setupClickListeners() {
        btnAccueil.setOnClickListener(v -> navigateTo(Home.class));
        btnNotifications.setOnClickListener(v -> navigateTo(Notification.class));
        btnMessages.setOnClickListener(v -> navigateTo(Chat.class));
        btnProfil.setOnClickListener(v -> navigateTo(Profil.class));
        btnBack.setOnClickListener(v -> navigateTo(Home.class));

        etapePreInscription.setOnClickListener(v -> {
            db.collection("preInscriptions").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists() &&
                                    document.getBoolean("preInscriptionComplete") != null &&
                                    document.getBoolean("preInscriptionComplete")) {
                                Toast.makeText(this,
                                        "Vous avez déjà complété la pré-inscription",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                startActivity(new Intent(this, Formulaire_Pre_Inscription.class));
                            }
                        }
                    });
        });

        etapePostSoumission.setOnClickListener(v -> {
            db.collection("preInscriptions").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot doc = task.getResult();
                            boolean preComplete = doc.getBoolean("preInscriptionComplete") != null &&
                                    doc.getBoolean("preInscriptionComplete");
                            boolean postComplete = doc.getBoolean("postSoumissionComplete") != null &&
                                    doc.getBoolean("postSoumissionComplete");

                            if (!preComplete) {
                                Toast.makeText(this,
                                        "Veuvez compléter la pré-inscription d'abord",
                                        Toast.LENGTH_SHORT).show();
                            } else if (postComplete) {
                                showDossierDetails();
                            } else {
                                Toast.makeText(this,
                                        "Votre dossier est en cours de validation",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        etapePaiement.setOnClickListener(v -> {
            db.collection("preInscriptions").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot doc = task.getResult();
                            boolean postComplete = doc.getBoolean("postSoumissionComplete") != null &&
                                    doc.getBoolean("postSoumissionComplete");
                            boolean paiementComplete = doc.getBoolean("paiementComplete") != null &&
                                    doc.getBoolean("paiementComplete");

                            if (!postComplete) {
                                Toast.makeText(this,
                                        "Attendez la validation de votre dossier",
                                        Toast.LENGTH_SHORT).show();
                            } else if (paiementComplete) {
                                Toast.makeText(this,
                                        "Paiement déjà effectué",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                startPaymentProcess();
                            }
                        }
                    });
        });
    }

    private void loadProcessStatus() {
        if (userId == null) return;

        db.collection("preInscriptions").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            updateUI(document);
                        } else {
                            resetAllSteps();
                        }
                    }
                });
    }

    private void updateUI(DocumentSnapshot document) {
        boolean preComplete = document.getBoolean("preInscriptionComplete") != null
                && document.getBoolean("preInscriptionComplete");
        updateStepUI(preCheck, preHourglass, preBlock, preComplete);

        boolean postComplete = document.getBoolean("postSoumissionComplete") != null
                && document.getBoolean("postSoumissionComplete");
        updateStepUI(postCheck, postHourglass, postBlock, postComplete);

        boolean paiementComplete = document.getBoolean("paiementComplete") != null
                && document.getBoolean("paiementComplete");
        updateStepUI(paiementCheck, paiementHourglass, paiementBlock, paiementComplete);
    }

    private void resetAllSteps() {
        preCheck.setVisibility(View.GONE);
        preHourglass.setVisibility(View.GONE);
        preBlock.setVisibility(View.VISIBLE);

        postCheck.setVisibility(View.GONE);
        postHourglass.setVisibility(View.GONE);
        postBlock.setVisibility(View.VISIBLE);

        paiementCheck.setVisibility(View.GONE);
        paiementHourglass.setVisibility(View.GONE);
        paiementBlock.setVisibility(View.VISIBLE);
    }

    private void updateStepUI(ImageView check, ImageView hourglass, ImageView block, boolean isComplete) {
        if (isComplete) {
            check.setVisibility(View.VISIBLE);
            hourglass.setVisibility(View.GONE);
            block.setVisibility(View.GONE);
        } else {
            check.setVisibility(View.GONE);
            hourglass.setVisibility(View.VISIBLE);
            block.setVisibility(View.GONE);
        }
    }

    private void showDossierDetails() {
        startActivity(new Intent(this, Post_Soumission.class));
    }

    private void startPaymentProcess() {
        startActivity(new Intent(this, Paiement.class));
    }

    private void navigateTo(Class<?> destination) {
        if (this.getClass() != destination) {
            startActivity(new Intent(this, destination));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProcessStatus();
    }
}
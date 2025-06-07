package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mit_plateform.R;
import com.example.mit_plateform.Utilities.NavigationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Paiement extends AppCompatActivity {

    private ImageView btnAccueil, btnNotifications, btnNotes, btnMessages, btnProfil, back_button;
    private Button uploadBordereauButton, onlinePaymentButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadBordereau(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paiement);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        initViews();
        setupNavigation();
        setupPaymentButtons();
        checkPaymentStatus();
    }

    private void initViews() {
        btnAccueil = findViewById(R.id.btnAccueil);
        btnNotes = findViewById(R.id.btnNotes);
        btnMessages = findViewById(R.id.btnMessages);
        btnProfil = findViewById(R.id.btnProfil);
        btnNotifications = findViewById(R.id.btnNotifications);
        back_button = findViewById(R.id.back_button);

        uploadBordereauButton = findViewById(R.id.upload_bordereau);
        onlinePaymentButton = findViewById(R.id.paiement_en_ligne);
    }

    private void setupNavigation() {
        NavigationUtils.updateActiveIcon(this, btnNotes, true);

        btnAccueil.setOnClickListener(view -> navigateTo(Home.class));
        btnNotifications.setOnClickListener(view -> navigateTo(Notification.class));
        btnMessages.setOnClickListener(view -> navigateTo(Chat.class));
        btnProfil.setOnClickListener(view -> navigateTo(Profil.class));
        back_button.setOnClickListener(view -> navigateTo(Pre_inscription.class));
    }

    private void setupPaymentButtons() {
        uploadBordereauButton.setOnClickListener(v -> {
            filePickerLauncher.launch("application/pdf");
        });

        onlinePaymentButton.setOnClickListener(v -> {
            // Intégration avec un service de paiement
            startOnlinePayment();
        });
    }

    private void checkPaymentStatus() {
        if (userId == null) return;

        db.collection("preInscriptions").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() &&
                            documentSnapshot.getBoolean("paiementComplete") != null &&
                            documentSnapshot.getBoolean("paiementComplete")) {
                        Toast.makeText(this,
                                "Paiement déjà effectué",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void uploadBordereau(Uri fileUri) {
        // Implémenter l'upload du bordereau
        Toast.makeText(this, "Bordereau téléversé avec succès", Toast.LENGTH_SHORT).show();

        // Marquer le paiement comme complet
        db.collection("preInscriptions").document(userId)
                .update("paiementComplete", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Paiement confirmé",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void startOnlinePayment() {
        // Implémenter l'intégration avec le service de paiement
        Toast.makeText(this, "Redirection vers le paiement en ligne", Toast.LENGTH_SHORT).show();

        // Pour l'exemple, on simule un paiement réussi
        db.collection("preInscriptions").document(userId)
                .update("paiementComplete", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Paiement effectué avec succès",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void navigateTo(Class<?> destination) {
        if (!this.getClass().equals(destination)) {
            startActivity(new Intent(this, destination));
            finish();
        }
    }
}
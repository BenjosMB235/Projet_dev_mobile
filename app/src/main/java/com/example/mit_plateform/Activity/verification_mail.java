package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mit_plateform.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class verification_mail extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button verifyEmailButton;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_mail);

        auth = FirebaseAuth.getInstance();

        backButton = findViewById(R.id.back_button);
        verifyEmailButton = findViewById(R.id.verify_email_button);

        // Bouton de retour
        backButton.setOnClickListener(view -> {
            Intent intent=new Intent(verification_mail.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Bouton de vérification de l'email
        verifyEmailButton.setOnClickListener(view -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                // 1. Ouvrir la boîte mail de l'utilisateur
                openEmailClient();

                // 2. Renvoyer l'email de vérification (au cas où)
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Email de vérification envoyé", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Vérifier périodiquement si l'email a été vérifié
        checkEmailVerification();
    }

    private void openEmailClient() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            // Fallback si l'app mail n'est pas installée
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://mail.google.com"));
            startActivity(intent);
        }
    }

    private void checkEmailVerification() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    // Rediriger vers Home si email vérifié
                    startActivity(new Intent(this, Home.class));
                    finish();
                } else {
                    // Vérifier à nouveau après un délai
                    verifyEmailButton.postDelayed(this::checkEmailVerification, 5000); // Toutes les 5 secondes
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Vérifier à nouveau quand l'utilisateur revient à l'app
        checkEmailVerification();
    }
}
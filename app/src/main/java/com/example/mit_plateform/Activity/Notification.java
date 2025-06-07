package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mit_plateform.R;
import com.example.mit_plateform.Utilities.NavigationUtils;

public class Notification extends AppCompatActivity {
    private ImageView btnNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        btnNotifications = findViewById(R.id.btnNotifications);

        // Mettre en surbrillance l'icône Notifications (page actuelle)
        NavigationUtils.updateActiveIcon(this, btnNotifications, true);

        // Gestion du clic sur le bouton retour
        findViewById(R.id.back_arrow).setOnClickListener(view -> {
            Intent intent = new Intent(Notification.this, Home.class);
            startActivity(intent);
            finish();
        });

        // Initialiser les autres icônes de navigation
        ImageView btnAccueil = findViewById(R.id.btnAccueil);
        ImageView btnNotes = findViewById(R.id.btnNotes);
        ImageView btnMessages = findViewById(R.id.btnMessages);
        ImageView btnProfil = findViewById(R.id.btnProfil);

        // Gestion des clics sur la barre de navigation
        btnAccueil.setOnClickListener(view -> {
            Intent intent = new Intent(Notification.this, Home.class);
            startActivity(intent);
            finish();
        });

        btnNotes.setOnClickListener(view -> {
            Intent intent = new Intent(Notification.this, Pre_inscription.class);
            startActivity(intent);
            finish();
        });

        btnMessages.setOnClickListener(view -> {
            Intent intent = new Intent(Notification.this, Chat.class);
            startActivity(intent);
            finish();
        });

        btnProfil.setOnClickListener(view -> {
            Intent intent = new Intent(Notification.this, Profil.class);
            startActivity(intent);
            finish();
        });
    }
}
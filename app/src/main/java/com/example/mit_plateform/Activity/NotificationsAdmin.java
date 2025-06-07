package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mit_plateform.R;
import com.example.mit_plateform.Utilities.NavigationUtils;

public class NotificationsAdmin extends AppCompatActivity {
    private ImageView btnNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_admin);

        btnNotifications = findViewById(R.id.btnNotifications);

        // Mettre en surbrillance l'icône Notifications (page actuelle)
        NavigationUtils.updateActiveIcon(this, btnNotifications, true);

        // Gestion du clic sur le bouton retour
        findViewById(R.id.back_arrow).setOnClickListener(view -> {
            Intent intent = new Intent(NotificationsAdmin.this, DashboardMessages.class);
            startActivity(intent);
            finish();
        });

        // Initialiser les autres icônes de navigation
        ImageView btnNotes = findViewById(R.id.btnNotes);
        ImageView btnMessages = findViewById(R.id.btnMessages);
        ImageView btnProfil = findViewById(R.id.btnProfil);

        // Gestion des clics sur la barre de navigation

        btnNotes.setOnClickListener(view -> {
            Intent intent = new Intent(NotificationsAdmin.this, DashboardInscriptions.class);
            startActivity(intent);
            finish();
        });

        btnMessages.setOnClickListener(view -> {
            Intent intent = new Intent(NotificationsAdmin.this, DashboardMessages.class);
            startActivity(intent);
            finish();
        });

        btnProfil.setOnClickListener(view -> {
            Intent intent = new Intent(NotificationsAdmin.this, ProfilAdmin.class);
            startActivity(intent);
            finish();
        });
    }
}
package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mit_plateform.R;
import com.example.mit_plateform.Utilities.NavigationUtils;
import com.google.firebase.firestore.FirebaseFirestore;

public class Home extends AppCompatActivity {
    private ImageView btnAccueil, btnNotifications, btnNotes, btnMessages, btnProfil;
    private ViewFlipper imageFlipper;
    private TextView descriptionMIT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialisation navigation
        btnAccueil = findViewById(R.id.btnAccueil);
        btnNotes = findViewById(R.id.btnNotes);
        btnMessages = findViewById(R.id.btnMessages);
        btnProfil = findViewById(R.id.btnProfil);
        btnNotifications = findViewById(R.id.btnNotifications);

        NavigationUtils.updateActiveIcon(this, btnAccueil, true);

        btnAccueil.setOnClickListener(view -> {
            // Ne rien faire (page actuelle)
        });

        btnNotes.setOnClickListener(view -> {
            startActivity(new Intent(Home.this, Pre_inscription.class));
            finish();
        });

        btnNotifications.setOnClickListener(view -> {
            startActivity(new Intent(Home.this, Notification.class));
            finish();
        });

        btnMessages.setOnClickListener(view -> {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("role", "admin")
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String adminId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            Intent intent = new Intent(Home.this, Chat.class);
                            intent.putExtra("recipient_id", adminId);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Home.this, "Aucun administrateur disponible", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FIRESTORE_ADMIN_ERROR", "Erreur recherche admin : " + e.getMessage(), e);
                        Toast.makeText(Home.this, "Erreur lors de la recherche de l'admin", Toast.LENGTH_SHORT).show();
                    });

        });


        btnProfil.setOnClickListener(view -> {
            startActivity(new Intent(Home.this, Profil.class));
            finish();
        });

        // ----------- Slideshow d'images MIT ----------------
        imageFlipper = findViewById(R.id.image_flipper);
        int[] images = {
                R.drawable.mitlogo,
                R.drawable.mit1,
                R.drawable.mit2,
                R.drawable.mit3,
                R.drawable.mit4,
                R.drawable.mit5,
                R.drawable.mit6,
                R.drawable.mit7
        };

        for (int image : images) {
            ImageView img = new ImageView(this);
            img.setBackgroundResource(image);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageFlipper.addView(img);
        }

        imageFlipper.setAutoStart(true);
        imageFlipper.setFlipInterval(2000); // 3 secondes
        imageFlipper.setInAnimation(this, android.R.anim.slide_in_left);
        imageFlipper.setOutAnimation(this, android.R.anim.slide_out_right);

        // ------------- Texte descriptif MIT ----------------
        descriptionMIT = findViewById(R.id.mit_description);

        String htmlText = "<h2 style='text-align:center;'>Massachusetts Institute of Technology (MIT)</h2>" +
                "<p><b>Découvrez l’excellence.</b> Depuis 1861, le MIT est bien plus qu’une université. C’est une légende vivante de l’innovation, " +
                "un véritable moteur de progrès scientifique et technologique qui façonne le monde de demain.</p>" +

                "<p>Implanté au cœur de Cambridge (Massachusetts), le MIT attire les plus grands esprits de la planète et transforme leurs idées en révolutions technologiques. " +
                "Plus de 90 prix Nobel, des milliers de brevets, et des entreprises de renommée mondiale sont nées dans ses laboratoires.</p>" +

                "<h3>🌐 Domaines d'expertise :</h3>" +
                "<ul>" +
                "<li><b>Ingénierie :</b> Robotique, Aérospatiale, Génie électrique, Génie mécanique</li>" +
                "<li><b>Informatique :</b> Intelligence Artificielle, Cybersécurité, Big Data, Technologies du futur</li>" +
                "<li><b>Sciences fondamentales :</b> Physique quantique, Mathématiques avancées, Biotechnologie</li>" +
                "<li><b>Économie & Management :</b> Sloan School of Management, Entrepreneuriat, Leadership mondial</li>" +
                "<li><b>Climat & Environnement :</b> Énergies renouvelables, Urbanisme durable, Recherche climatique</li>" +
                "</ul>" +

                "<p><b>✨ Pourquoi choisir MIT ?</b><br>Parce que c’est ici que le futur se construit.</p>" +

                "<h3 style='color:#007bff;'>🚀 Rejoignez-nous !</h3>" +
                "<p>MIT n’est pas seulement la meilleure université du monde, c’est <i>votre tremplin vers l’excellence</i>. " +
                "Inscrivez-vous dès maintenant et devenez l’innovateur, le chercheur ou l’entrepreneur que vous rêvez d’être. " +
                "<br><b>Le futur vous attend. Et il commence ici.</b></p>";


        descriptionMIT.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY));
    }
}

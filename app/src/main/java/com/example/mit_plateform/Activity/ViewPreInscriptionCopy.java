package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.mit_plateform.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewPreInscriptionCopy extends AppCompatActivity {

    private TextView tvNom, tvPrenom, tvDateNaissance, tvSexe, tvNationalite;
    private TextView tvAdresse, tvTelephone, tvEmail, tvFiliere, tvDossierNumber;
    private TextView tvSubmissionDate;
    private ImageView ivPhoto, btnBack;
    private Button btnViewDocument;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pre_inscription_copy);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        initViews();
        setupButtons();
        loadPreInscriptionData();
    }

    private void initViews() {
        tvNom = findViewById(R.id.tv_nom);
        tvPrenom = findViewById(R.id.tv_prenom);
        tvDateNaissance = findViewById(R.id.tv_date_naissance);
        tvSexe = findViewById(R.id.tv_sexe);
        tvNationalite = findViewById(R.id.tv_nationalite);
        tvAdresse = findViewById(R.id.tv_adresse);
        tvTelephone = findViewById(R.id.tv_telephone);
        tvEmail = findViewById(R.id.tv_email);
        tvFiliere = findViewById(R.id.tv_filiere);
        tvDossierNumber = findViewById(R.id.tv_dossier_number);
        tvSubmissionDate = findViewById(R.id.tv_submission_date);
        ivPhoto = findViewById(R.id.iv_photo);
        btnViewDocument = findViewById(R.id.btn_view_document);
        btnBack = findViewById(R.id.back_button);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());

        btnViewDocument.setOnClickListener(v -> {
            db.collection("preInscriptions").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String documentBase64 = documentSnapshot.getString("documentBase64");
                        if (documentBase64 != null && !documentBase64.isEmpty()) {
                            Intent intent = new Intent(this, ViewPdfActivity.class);
                            intent.putExtra("pdfBase64", documentBase64);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this,
                                    "Aucun document joint",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void loadPreInscriptionData() {
        db.collection("preInscriptions").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            displayPreInscriptionData(document);
                        }
                    }
                });
    }

    private void displayPreInscriptionData(DocumentSnapshot document) {
        tvNom.setText(String.format("Nom: %s", document.getString("nom")));
        tvPrenom.setText(String.format("Prénom: %s", document.getString("prenom")));
        tvDateNaissance.setText(String.format("Date de naissance: %s",
                document.getString("dateNaissance")));
        tvSexe.setText(String.format("Sexe: %s", document.getString("sexe")));
        tvNationalite.setText(String.format("Nationalité: %s",
                document.getString("nationalite")));
        tvAdresse.setText(String.format("Adresse: %s", document.getString("adresse")));
        tvTelephone.setText(String.format("Téléphone: %s", document.getString("telephone")));
        tvEmail.setText(String.format("Email: %s", document.getString("email")));
        tvFiliere.setText(String.format("Filière choisie: %s", document.getString("filiere")));
        tvDossierNumber.setText(document.getString("dossierNumber"));

        Date submissionDate = document.getDate("submissionDate");
        if (submissionDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvSubmissionDate.setText(String.format("Soumis le: %s", sdf.format(submissionDate)));
        }

        String photoBase64 = document.getString("photoBase64");
        if (photoBase64 != null && !photoBase64.isEmpty()) {
            byte[] decodedString = Base64.decode(photoBase64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ivPhoto.setImageBitmap(decodedByte);
        }
    }
}
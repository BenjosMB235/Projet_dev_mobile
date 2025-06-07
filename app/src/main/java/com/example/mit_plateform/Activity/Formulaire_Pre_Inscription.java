package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mit_plateform.R;
import com.example.mit_plateform.Utilities.NavigationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Formulaire_Pre_Inscription extends AppCompatActivity {

    private EditText nomEditText, prenomEditText, dateNaissanceEditText, nationaliteEditText;
    private EditText adresseEditText, telephoneEditText, emailEditText;
    private RadioGroup sexeRadioGroup;
    private Button uploadPhotoButton, selectFileButton, validerButton;
    private CheckBox certificationCheckBox;
    private Spinner filiereSpinner;
    private ImageView btnAccueil, btnNotes, btnMessages, btnProfil, btnNotifications, back_button;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Uri photoUri, documentUri;

    private final ActivityResultLauncher<String> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    photoUri = uri;
                    uploadPhotoButton.setText("Photo sélectionnée");
                    uploadPhotoButton.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_check, 0, 0, 0);
                }
            });

    private final ActivityResultLauncher<String> documentPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    documentUri = uri;
                    selectFileButton.setText("Document sélectionné");
                    selectFileButton.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_check, 0, 0, 0);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("preInscriptions")
                    .document(user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() &&
                                task.getResult().exists() &&
                                task.getResult().getBoolean("preInscriptionComplete")) {
                            Toast.makeText(this,
                                    "Pré-inscription déjà complétée",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            setContentView(R.layout.activity_formulaire_pre_inscription);
                            initViews();
                            setupFiliereSpinner();
                            setupClickListeners();
                            NavigationUtils.updateActiveIcon(this, btnNotes, true);
                        }
                    });
        }
    }

    private void initViews() {
        nomEditText = findViewById(R.id.nom_edit_text);
        prenomEditText = findViewById(R.id.prenom_edit_text);
        dateNaissanceEditText = findViewById(R.id.date_naissance_edit_text);
        nationaliteEditText = findViewById(R.id.nationalite_edit_text);
        adresseEditText = findViewById(R.id.adresse_edit_text);
        telephoneEditText = findViewById(R.id.telephone_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        sexeRadioGroup = findViewById(R.id.sexe_radio_group);
        uploadPhotoButton = findViewById(R.id.upload_photo);
        selectFileButton = findViewById(R.id.select_file);
        certificationCheckBox = findViewById(R.id.certification_check_box);
        validerButton = findViewById(R.id.valider_button);
        filiereSpinner = findViewById(R.id.filiere_spinner);
        btnAccueil = findViewById(R.id.btnAccueil);
        btnNotes = findViewById(R.id.btnNotes);
        btnMessages = findViewById(R.id.btnMessages);
        btnProfil = findViewById(R.id.btnProfil);
        btnNotifications = findViewById(R.id.btnNotifications);
        back_button = findViewById(R.id.back_button);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupFiliereSpinner() {
        String[] filieres = new String[]{
                "Sélectionner une filière",
                "Informatique",
                "Gestion",
                "Comptabilité",
                "Marketing",
                "Ressources Humaines"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filieres
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filiereSpinner.setAdapter(adapter);
    }

    private void setupClickListeners() {
        uploadPhotoButton.setOnClickListener(v ->
                photoPickerLauncher.launch("image/*"));

        selectFileButton.setOnClickListener(v ->
                documentPickerLauncher.launch("application/pdf"));

        validerButton.setOnClickListener(v -> validateAndSubmitForm());

        btnAccueil.setOnClickListener(view -> navigateTo(Home.class));
        btnNotifications.setOnClickListener(view -> navigateTo(Notification.class));
        btnMessages.setOnClickListener(view -> navigateTo(Chat.class));
        btnProfil.setOnClickListener(view -> navigateTo(Pre_inscription.class));
        back_button.setOnClickListener(view -> navigateTo(Pre_inscription.class));
    }

    private void navigateTo(Class<?> destination) {
        if (!this.getClass().equals(destination)) {
            startActivity(new Intent(this, destination));
            finish();
        }
    }

    private void validateAndSubmitForm() {
        if (!validateFields()) {
            showToast("Veuillez remplir tous les champs obligatoires");
            return;
        }

        if (!certificationCheckBox.isChecked()) {
            showToast("Veuillez certifier que les informations sont vraies");
            return;
        }

        savePreInscriptionData();
    }

    private boolean validateFields() {
        return !nomEditText.getText().toString().isEmpty() &&
                !prenomEditText.getText().toString().isEmpty() &&
                !dateNaissanceEditText.getText().toString().isEmpty() &&
                !nationaliteEditText.getText().toString().isEmpty() &&
                !adresseEditText.getText().toString().isEmpty() &&
                !telephoneEditText.getText().toString().isEmpty() &&
                !emailEditText.getText().toString().isEmpty() &&
                sexeRadioGroup.getCheckedRadioButtonId() != -1 &&
                filiereSpinner.getSelectedItemPosition() > 0;
    }

    private void savePreInscriptionData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showToast("Erreur d'authentification - Veuillez vous reconnecter");
            return;
        }
        String userId = user.getUid();

        progressBar.setVisibility(View.VISIBLE);
        validerButton.setEnabled(false);

        db.collection("preInscriptions").document(userId)
                .get()
                .addOnCompleteListener(checkTask -> {
                    if (checkTask.isSuccessful()) {
                        DocumentSnapshot document = checkTask.getResult();

                        if (document.exists() &&
                                document.getBoolean("preInscriptionComplete") != null &&
                                document.getBoolean("preInscriptionComplete")) {

                            progressBar.setVisibility(View.GONE);
                            validerButton.setEnabled(true);
                            showToast("Vous avez déjà complété la pré-inscription");
                            finish();
                            return;
                        }

                        Map<String, Object> preInscriptionData = createPreInscriptionData();

                        db.collection("preInscriptions").document(userId)
                                .set(preInscriptionData, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    validerButton.setEnabled(true);
                                    showToast("Pré-inscription enregistrée avec succès");
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    validerButton.setEnabled(true);
                                    showToast("Erreur technique: " + e.getMessage());
                                    Log.e("SAVE_PRE_INSCRIPTION", "Erreur Firestore", e);
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        validerButton.setEnabled(true);
                        showToast("Erreur de vérification");
                        Log.e("CHECK_PRE_INSCRIPTION", "Erreur Firestore", checkTask.getException());
                    }
                });
    }

    private Map<String, Object> createPreInscriptionData() {
        Map<String, Object> data = new HashMap<>();
        data.put("nom", nomEditText.getText().toString());
        data.put("prenom", prenomEditText.getText().toString());
        data.put("dateNaissance", dateNaissanceEditText.getText().toString());
        data.put("sexe", getSelectedSexe());
        data.put("nationalite", nationaliteEditText.getText().toString());
        data.put("adresse", adresseEditText.getText().toString());
        data.put("telephone", telephoneEditText.getText().toString());
        data.put("email", emailEditText.getText().toString());
        data.put("filiere", filiereSpinner.getSelectedItem().toString());
        data.put("preInscriptionComplete", true);
        data.put("submissionDate", new java.util.Date());
        data.put("dossierNumber", generateDossierNumber());
        data.put("status", "pending");

        if (photoUri != null) {
            String photoBase64 = encodeFileToBase64(photoUri);
            if (photoBase64 != null) data.put("photoBase64", photoBase64);
        }

        if (documentUri != null) {
            String documentBase64 = encodeFileToBase64(documentUri);
            if (documentBase64 != null) data.put("documentBase64", documentBase64);
        }

        return data;
    }

    private String generateDossierNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String datePart = sdf.format(new Date());
        int randomNum = (int) (Math.random() * 9000) + 1000;
        return "MIT-" + datePart + "-" + randomNum;
    }

    private String encodeFileToBase64(Uri fileUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getSelectedSexe() {
        int selectedId = sexeRadioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        return radioButton.getText().toString();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
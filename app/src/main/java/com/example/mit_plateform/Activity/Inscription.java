package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mit_plateform.R;
import com.example.mit_plateform.Utilities.Constants;
import com.example.mit_plateform.Utilities.PreferenceManager;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.*;

public class Inscription extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister, btnGoogle, btnFacebook;
    private ImageView backButton;
    private CheckBox cbConditions;
    private TextView tvOu;
    private ProgressBar progressBar;

    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription);

        auth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(this);

        // Init vues
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbConditions = findViewById(R.id.cbConditions);
        btnRegister = findViewById(R.id.btnRegister);
        backButton = findViewById(R.id.back_button);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        tvOu = findViewById(R.id.tvOu);
        progressBar = findViewById(R.id.progressBar);

        // Config retour
        backButton.setOnClickListener(view -> {
            startActivity(new Intent(Inscription.this, MainActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(view -> registerWithEmail());
        btnGoogle.setOnClickListener(view -> signInWithGoogle());
        btnFacebook.setOnClickListener(view -> signInWithFacebook());

        // Config Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        try {
                            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                    .getResult(ApiException.class);
                            if (account != null && account.getIdToken() != null) {
                                firebaseAuthWithGoogle(account.getIdToken());
                            }
                        } catch (ApiException e) {
                            Toast.makeText(this, "Erreur Google: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        // Facebook
        callbackManager = CallbackManager.Factory.create();
    }

    private void registerWithEmail() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbConditions.isChecked()) {
            Toast.makeText(this, "Veuillez accepter les conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.updateProfile(new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build())
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            sendEmailVerification(user);
                                            createUserDocument(user);
                                            saveToPreferences(user);
                                        } else {
                                            hideLoading();
                                            Toast.makeText(this, "Erreur profil", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        hideLoading();
                        Toast.makeText(this, "Échec: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    hideLoading();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Email de vérification envoyé", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, verification_mail.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        showLoading();

        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            createUserDocument(user);
                            saveToPreferences(user);
                            redirectToHome();
                        }
                    } else {
                        hideLoading();
                        Toast.makeText(this, "Échec authentification Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(Inscription.this, "Connexion annulée", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(Inscription.this, "Erreur Facebook: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        showLoading();

        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            createUserDocument(user);
                            saveToPreferences(user);
                            redirectToHome();
                        }
                    } else {
                        hideLoading();
                        Toast.makeText(this, "Échec authentification Facebook", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirectToHome() {
        startActivity(new Intent(this, Home.class));
        finish();
    }

    private void createUserDocument(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Map<String, Object> data = new HashMap<>();
                data.put("email", user.getEmail());
                data.put("name", user.getDisplayName() != null ? user.getDisplayName() : "");
                data.put("prenom", "");
                data.put("telephone", "");
                data.put("role", "client");
                data.put("createdAt", FieldValue.serverTimestamp());
                data.put("provider", user.getProviderId());

                db.collection("users").document(user.getUid()).set(data)
                        .addOnSuccessListener(unused -> Log.d("Inscription", "Utilisateur Firestore enregistré"))
                        .addOnFailureListener(e -> Log.e("Inscription", "Erreur Firestore", e));
            }
        });
    }

    private void saveToPreferences(FirebaseUser user) {
        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
        preferenceManager.putString(Constants.KEY_USER_ID, user.getUid());
        preferenceManager.putString(Constants.KEY_EMAIL, user.getEmail());
        preferenceManager.putString(Constants.KEY_ROLE, "client");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void showLoading() {
        tvOu.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);
        btnGoogle.setEnabled(false);
        btnFacebook.setEnabled(false);
    }

    private void hideLoading() {
        tvOu.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        btnRegister.setEnabled(true);
        btnGoogle.setEnabled(true);
        btnFacebook.setEnabled(true);
    }
}

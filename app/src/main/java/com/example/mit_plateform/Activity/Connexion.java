package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mit_plateform.R;
import com.example.mit_plateform.Utilities.Constants;
import com.example.mit_plateform.Utilities.PreferenceManager;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Connexion extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogle, btnFacebook;
    private ImageView backButton;
    private TextView tvForgotPassword;
    private TextView tvOu;
    private ProgressBar progressBar;

    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private PreferenceManager preferenceManager;

    private static final String[] ADMIN_EMAILS = {
            "admin@domain.com",
            "support@domain.com"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        preferenceManager = new PreferenceManager(this);
        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        backButton = findViewById(R.id.back_button);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        tvOu = findViewById(R.id.tvOu);
        progressBar = findViewById(R.id.progressBar);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(Connexion.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnLogin.setOnClickListener(v -> signInWithEmail());
        tvForgotPassword.setOnClickListener(v -> resetPassword());

        setupGoogleSignIn();
        setupFacebookLogin();
    }

    private void createUserDocument(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userEmail = user.getEmail();

        db.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", userEmail);
                            userData.put("createdAt", FieldValue.serverTimestamp());
                            userData.put("provider", user.getProviderId());
                            userData.put("name", "");
                            userData.put("prenom", "");
                            userData.put("telephone", "");
                            userData.put("role", isAdminEmail(userEmail) ? "admin" : "client");

                            db.collection("users").document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> Log.d("Auth", "User document created"))
                                    .addOnFailureListener(e -> Log.w("Auth", "Error creating user document", e));
                        } else {
                            Log.d("Auth", "User document already exists");
                        }
                    } else {
                        Log.w("Auth", "Error checking user document", task.getException());
                    }
                });
    }

    private boolean isAdminEmail(String email) {
        if (email == null) return false;
        for (String adminEmail : ADMIN_EMAILS) {
            if (adminEmail.equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    private void signInWithEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkUserRoleAndRedirect();
                    } else {
                        hideLoading();
                        Toast.makeText(this, "Échec de la connexion", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRoleAndRedirect() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String role = document.getString("role");
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID, user.getUid());
                            preferenceManager.putString(Constants.KEY_EMAIL, user.getEmail());
                            preferenceManager.putString(Constants.KEY_ROLE, role);
                            redirectBasedOnRole(role);
                        } else {
                            createUserDocument(user);
                            String inferredRole = isAdminEmail(user.getEmail()) ? "admin" : "client";
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID, user.getUid());
                            preferenceManager.putString(Constants.KEY_EMAIL, user.getEmail());
                            preferenceManager.putString(Constants.KEY_ROLE, inferredRole);
                            redirectBasedOnRole(inferredRole);
                        }
                    } else {
                        hideLoading();
                        Toast.makeText(this, "Erreur de vérification du rôle", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirectBasedOnRole(String role) {
        Intent intent;
        if ("admin".equals(role)) {
            intent = new Intent(this, DashboardMessages.class);
        } else {
            intent = new Intent(this, Home.class);
        }
        startActivity(intent);
        finish();
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer votre email", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Email de réinitialisation envoyé", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Échec : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupGoogleSignIn() {
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
                            Toast.makeText(this, "Échec Google Sign-In", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void setupFacebookLogin() {
        callbackManager = CallbackManager.Factory.create();

        btnFacebook.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(
                    this, Arrays.asList("email", "public_profile")
            );

            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Toast.makeText(Connexion.this, "Connexion annulée", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(Connexion.this, "Erreur Facebook: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        showLoading();
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            createUserDocument(user);
                            checkUserRoleAndRedirect();
                        }
                    } else {
                        hideLoading();
                        Toast.makeText(this, "Échec de l'authentification Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        showLoading();
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            createUserDocument(user);
                            checkUserRoleAndRedirect();
                        }
                    } else {
                        hideLoading();
                        Toast.makeText(this, "Échec de l'authentification Facebook", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading() {
        tvOu.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        btnLogin.setEnabled(false);
        btnGoogle.setEnabled(false);
        btnFacebook.setEnabled(false);
    }

    private void hideLoading() {
        tvOu.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        btnLogin.setEnabled(true);
        btnGoogle.setEnabled(true);
        btnFacebook.setEnabled(true);
    }
}

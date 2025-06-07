package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mit_plateform.Adapters.PreInscriptionAdapter;
import com.example.mit_plateform.Models.PreInscription;
import com.example.mit_plateform.R;
import com.example.mit_plateform.Utilities.NavigationUtils;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class DashboardInscriptions extends AppCompatActivity implements PreInscriptionAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private PreInscriptionAdapter adapter;
    private FirebaseFirestore db;
    private SearchView searchView;
    private ImageView btnNotifications, btnNotes, btnMessages, btnProfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_inscriptions);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupNavigation();
        setupRecyclerView();
        setupSearchView();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        btnNotes = findViewById(R.id.btnNotes);
        btnMessages = findViewById(R.id.btnMessages);
        btnProfil = findViewById(R.id.btnProfil);
        btnNotifications = findViewById(R.id.btnNotifications);
    }

    private void setupNavigation() {
        NavigationUtils.updateActiveIcon(this, btnNotes, true);

        btnNotes.setOnClickListener(v -> {
            // Already in this activity
        });

        btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsAdmin.class));
            finish();
        });

        btnMessages.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardMessages.class));
            finish();
        });

        btnProfil.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfilAdmin.class));
            finish();
        });
    }

    private void setupRecyclerView() {
        Query query = db.collection("preInscriptions")
                .whereEqualTo("preInscriptionComplete", true)
                .orderBy("submissionDate", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<PreInscription> options = new FirestoreRecyclerOptions.Builder<PreInscription>()
                .setQuery(query, PreInscription.class)
                .build();

        adapter = new PreInscriptionAdapter(options, true);
        adapter.setOnItemClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchPreInscriptions(newText);
                return false;
            }
        });
    }

    private void searchPreInscriptions(String searchText) {
        Query query;
        if (searchText.isEmpty()) {
            query = db.collection("preInscriptions")
                    .whereEqualTo("preInscriptionComplete", true)
                    .orderBy("submissionDate", Query.Direction.DESCENDING);
        } else {
            query = db.collection("preInscriptions")
                    .whereEqualTo("dossierNumber", searchText.toUpperCase())
                    .limit(10);
        }

        FirestoreRecyclerOptions<PreInscription> newOptions = new FirestoreRecyclerOptions.Builder<PreInscription>()
                .setQuery(query, PreInscription.class)
                .build();

        adapter.updateOptions(newOptions);
    }

    @Override
    public void onDetailsClick(DocumentSnapshot documentSnapshot, int position) {
        PreInscription preInscription = documentSnapshot.toObject(PreInscription.class);
        Intent intent = new Intent(this, PreInscriptionDetailActivity.class);
        intent.putExtra("DOSSIER_ID", documentSnapshot.getId());
        intent.putExtra("DOSSIER_NUMBER", preInscription.getDossierNumber());
        startActivity(intent);
    }

    @Override
    public void onValidateClick(DocumentSnapshot documentSnapshot, int position) {
        db.collection("preInscriptions").document(documentSnapshot.getId())
                .update(
                        "postSoumissionComplete", true,
                        "validationDate", new java.util.Date()
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Dossier validé avec succès", Toast.LENGTH_SHORT).show();
                    sendNotificationToUser(documentSnapshot.getString("userId"),
                            "Votre pré-inscription " + documentSnapshot.getString("dossierNumber") +
                                    " a été approuvée");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de la validation", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendNotificationToUser(String userId, String message) {
        // Implémentation de l'envoi de notification
        // Vous pouvez utiliser FCM ou stocker dans Firestore
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", "Mise à jour de votre dossier");
        notification.put("message", message);
        notification.put("timestamp", new java.util.Date());
        notification.put("read", false);

        db.collection("notifications").add(notification);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
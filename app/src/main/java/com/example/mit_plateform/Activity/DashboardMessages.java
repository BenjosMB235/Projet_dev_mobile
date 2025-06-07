package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mit_plateform.R;
import com.example.mit_plateform.Adapters.UserAdapter;
import com.example.mit_plateform.Models.User;
import com.example.mit_plateform.Utilities.NavigationUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardMessages extends AppCompatActivity implements UserAdapter.UserListener{

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private ImageView btnNotifications, btnNotes, btnMessages, btnProfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_messages);

        recyclerView = findViewById(R.id.usersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialisation navigation
        btnNotes = findViewById(R.id.btnNotes);
        btnMessages = findViewById(R.id.btnMessages);
        btnProfil = findViewById(R.id.btnProfil);
        btnNotifications = findViewById(R.id.btnNotifications);

        NavigationUtils.updateActiveIcon(this, btnMessages, true);

        btnNotes.setOnClickListener(view -> {
            startActivity(new Intent(DashboardMessages.this, DashboardInscriptions.class));
            finish();
        });

        btnNotifications.setOnClickListener(view -> {
            startActivity(new Intent(DashboardMessages.this, NotificationsAdmin.class));
            finish();
        });

        btnMessages.setOnClickListener(view -> {

        });

        btnProfil.setOnClickListener(view -> {
            startActivity(new Intent(DashboardMessages.this, ProfilAdmin.class));
            finish();
        });

        userList = new ArrayList<>();
        // Passez 'this' comme UserListener puisque l'activité implémente maintenant l'interface
        userAdapter = new UserAdapter(userList, this, true);
        recyclerView.setAdapter(userAdapter);

        loadUsers();
    }

    private void loadUsers() {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("role", "client")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            user.setId(document.getId());
                            userList.add(user);
                        }
                        userAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Erreur de chargement: " + task.getException(), Toast.LENGTH_SHORT).show();
                        Log.e("AdminDashboard", "Erreur de chargement", task.getException());
                    }
                });
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(this, Chat.class);
        intent.putExtra("recipient_id", user.getId());
        intent.putExtra("recipient_name", user.getName()); // optionnel pour affichage dans la topbar

        startActivity(intent);
    }
}
package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;

import com.example.mit_plateform.R;

public class MainActivity extends AppCompatActivity {
    private Button btnConnexion;
    private Button btnInscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnConnexion=findViewById(R.id.btnConnexion);
        btnInscription=findViewById(R.id.btnInscription);

        btnConnexion.setOnClickListener(view -> {
                Intent intent=new Intent(MainActivity.this, Connexion.class);
                startActivity(intent);
                finish();
        });

        btnInscription.setOnClickListener(view -> {
            Intent intent=new Intent(MainActivity.this, Inscription.class);
            startActivity(intent);
            finish();
        });
    }
}
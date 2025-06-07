package com.example.mit_plateform.Activity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.mit_plateform.R;
import java.util.Base64;

public class ViewPdfActivity extends AppCompatActivity {

    private WebView webView;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pdf);

        webView = findViewById(R.id.webView);
        btnBack = findViewById(R.id.back_button);

        btnBack.setOnClickListener(v -> finish());

        String pdfBase64 = getIntent().getStringExtra("pdfBase64");
        displayPdf(pdfBase64);
    }

    private void displayPdf(String pdfBase64) {
        try {
            // Configurer le WebView
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setAllowFileAccess(true);
            webView.setWebViewClient(new WebViewClient());

            // Afficher le PDF
            String pdfData = "data:application/pdf;base64," + pdfBase64;
            webView.loadUrl(pdfData);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Erreur lors de l'affichage du document",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
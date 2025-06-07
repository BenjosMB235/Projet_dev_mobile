package com.example.mit_plateform.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Base64;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mit_plateform.Adapters.ChatAdapter;
import com.example.mit_plateform.Models.ChatMessage;
import com.example.mit_plateform.R;
import com.example.mit_plateform.Utilities.PreferenceManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class Chat extends BaseActivity {

    private String senderId;
    private String recipientId;

    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;

    private EditText messageInput;
    private ImageView sendButton;
    private RecyclerView recyclerView;

    private ListenerRegistration messageListener;

    private ActivityResultLauncher<Intent> filePickerLauncher;
    private String selectedFileBase64;
    private String selectedFileMimeType;

    private LinearLayout filePreviewContainer;
    private ImageView previewImage, btnClearFile;
    private TextView previewFileLabel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        preferenceManager = new PreferenceManager(this);
        db = FirebaseFirestore.getInstance();

        senderId = preferenceManager.getString("user_id");
        recipientId = getIntent().getStringExtra("recipient_id");

        // Initialisez le filePickerLauncher ici dans onCreate()
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            try {
                                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                                byte[] bytes = new byte[inputStream.available()];
                                inputStream.read(bytes);
                                selectedFileBase64 = Base64.encodeToString(bytes, Base64.DEFAULT);
                                selectedFileMimeType = getContentResolver().getType(fileUri);

                                showFilePreview(fileUri, selectedFileMimeType);

                            } catch (Exception e) {
                                Toast.makeText(this, "Erreur de lecture du fichier", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        if (recipientId == null) {
            Toast.makeText(this, "Aucun destinataire", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkRolesAndInitChat();
    }

    private void checkRolesAndInitChat() {
        db.collection("users").document(recipientId).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Toast.makeText(this, "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String recipientRole = documentSnapshot.getString("role");
            String currentUserRole = preferenceManager.getString("role");

            if ("client".equals(currentUserRole) && !"admin".equals(recipientRole)) {
                Toast.makeText(this, "Vous ne pouvez discuter qu'avec un administrateur", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            initViews();
            listenToMessages();

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Erreur lors de la vérification", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void initViews() {
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.btn_send);
        recyclerView = findViewById(R.id.message_list);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, senderId);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sendButton.setOnClickListener(v -> sendMessage());

        // Ne plus initialiser le filePickerLauncher ici, il est déjà initialisé dans onCreate()

        //ImageView attachButton = findViewById(R.id.btn_attach);
       // attachButton.setOnClickListener(v -> openFilePicker());

        ImageView attachButton = findViewById(R.id.btn_attach);
        attachButton.setOnClickListener(v -> {
            if (selectedFileBase64 != null) {
                // Si un fichier est déjà sélectionné, l'envoyer directement
                sendFileMessage();
            } else {
                // Sinon ouvrir le sélecteur de fichiers
                openFilePicker();
            }
        });

        filePreviewContainer = findViewById(R.id.file_preview_container);
        previewImage = findViewById(R.id.preview_image);
        previewFileLabel = findViewById(R.id.preview_file_label);
        btnClearFile = findViewById(R.id.btn_clear_file);

        btnClearFile.setOnClickListener(v -> clearFilePreview());
    }

    private void showFilePreview(Uri fileUri, String mimeType) {
        filePreviewContainer.setVisibility(View.VISIBLE);
        btnClearFile.setVisibility(View.VISIBLE);

        if (mimeType != null && mimeType.startsWith("image/")) {
            previewImage.setVisibility(View.VISIBLE);
            previewImage.setImageURI(fileUri);
            previewFileLabel.setVisibility(View.GONE);
        } else {
            previewImage.setVisibility(View.GONE);
            previewFileLabel.setVisibility(View.VISIBLE);
            String label = detectFileLabel(mimeType);
            previewFileLabel.setText(label);
        }
    }

    private String detectFileLabel(String mimeType) {
        switch (mimeType) {
            case "application/pdf":
                return "📄 PDF sélectionné";
            case "application/msword":
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return "📝 Document Word";
            case "application/vnd.ms-excel":
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return "📊 Fichier Excel";
            case "application/vnd.ms-powerpoint":
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return "📽️ Fichier PowerPoint";
            default:
                return "📁 Fichier sélectionné";
        }
    }



    private void clearFilePreview() {
        selectedFileBase64 = null;
        selectedFileMimeType = null;

        previewImage.setVisibility(View.GONE);
        previewFileLabel.setVisibility(View.GONE);
        btnClearFile.setVisibility(View.GONE);
        filePreviewContainer.setVisibility(View.GONE);
    }


    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "image/*",
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        });
        filePickerLauncher.launch(Intent.createChooser(intent, "Choisir un fichier"));
    }

    private void sendFileMessage() {
        if (selectedFileBase64 == null || selectedFileMimeType == null) {
            Toast.makeText(this, "Aucun fichier sélectionné", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("senderId", senderId);
        message.put("recipientId", recipientId);
        message.put("timestamp", FieldValue.serverTimestamp());
        message.put("file", selectedFileBase64);
        message.put("fileType", selectedFileMimeType);

        String textContent = messageInput.getText().toString().trim();
        if (!textContent.isEmpty()) {
            message.put("message", textContent);
        }

        db.collection("messages").add(message)
                .addOnSuccessListener(docRef -> {
                    messageInput.setText("");
                    selectedFileBase64 = null;
                    selectedFileMimeType = null;
                    clearFilePreview();
                    // Ajoutez cette vérification
                    if (chatAdapter.getItemCount() > 0) {
                        recyclerView.post(() -> {
                            recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Échec d'envoi", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }



    private void sendMessage() {
        String content = messageInput.getText().toString().trim();

        if (selectedFileBase64 != null && selectedFileMimeType != null) {
            sendFileMessage();
            return;
        }

        if (content.isEmpty()) return;

        Map<String, Object> message = new HashMap<>();
        message.put("senderId", senderId);
        message.put("recipientId", recipientId);
        message.put("message", content);
        message.put("timestamp", FieldValue.serverTimestamp());

        db.collection("messages").add(message)
                .addOnSuccessListener(documentReference -> {
                    messageInput.setText("");
                    // Ajoutez cette vérification
                    if (chatAdapter.getItemCount() > 0) {
                        recyclerView.post(() -> {
                            recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                        });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur d'envoi", Toast.LENGTH_SHORT).show());
    }

    private void listenToMessages() {
        messageListener = db.collection("messages")
                .whereIn("senderId", Arrays.asList(senderId, recipientId))
                .whereIn("recipientId", Arrays.asList(senderId, recipientId))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Erreur d'écoute", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots == null) return;

                    List<ChatMessage> newMessages = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        ChatMessage message = new ChatMessage();
                        message.setSenderId(doc.getString("senderId"));
                        message.setRecipientId(doc.getString("recipientId"));
                        message.setMessage(doc.getString("message"));

                        if (doc.contains("file")) {
                            message.setFile(doc.getString("file"));
                            message.setFileType(doc.getString("fileType"));
                        }

                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        if (timestamp != null) {
                            message.dateTime = new SimpleDateFormat("HH:mm", Locale.getDefault())
                                    .format(timestamp.toDate());
                            message.dateObject = timestamp.toDate();
                        }

                        newMessages.add(message);
                    }

                    chatAdapter.updateMessages(newMessages);

                    // Ajoutez cette vérification avant de faire défiler
                    if (!newMessages.isEmpty()) {
                        recyclerView.post(() -> {
                            recyclerView.smoothScrollToPosition(newMessages.size() - 1);
                        });
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) {
            messageListener.remove();
        }
    }
}

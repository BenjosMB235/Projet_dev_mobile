package com.example.mit_plateform.Adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mit_plateform.databinding.ItemContainerReceivedMessageBinding;
import com.example.mit_plateform.databinding.ItemContainerSentMessageBinding;
import com.example.mit_plateform.Models.ChatMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> chatMessages;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    private static final String TAG = "ChatAdapter";

    public ChatAdapter(List<ChatMessage> chatMessages, String senderId) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
    }

    // Nouvelle méthode pour mettre à jour les messages efficacement
    public void updateMessages(List<ChatMessage> newMessages) {
        this.chatMessages = newMessages;
        notifyDataSetChanged();

        // Alternative optimisée (à implémenter si les performances sont critiques):
        // DiffUtil.DiffResult result = DiffUtil.calculateDiff(new MessageDiffCallback(chatMessages, newMessages));
        // this.chatMessages = newMessages;
        // result.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).senderId.equals(senderId)
                ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);

        // Animation pour le nouveau message
        if (position == chatMessages.size() - 1) {
            holder.itemView.setAlpha(0f);
            holder.itemView.animate().alpha(1f).setDuration(300).start();
        }
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(message);
        } else {
            ((ReceivedMessageViewHolder) holder).setData(message); // ← plus d’image
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage message) {
            hideAll();

            if (message.file != null && message.fileType != null) {
                if (message.fileType.startsWith("image/")) {
                    binding.imageFile.setVisibility(View.VISIBLE);
                    displayImage(message.file, binding.imageFile);
                } else if (message.fileType.equals("application/pdf")) {
                    binding.pdfFile.setVisibility(View.VISIBLE);
                    binding.pdfFile.setText(Html.fromHtml("<u>PDF File</u>"));
                    binding.pdfFile.setOnClickListener(v -> openFile(message.file, message.fileType));
                } else {
                    binding.otherFile.setVisibility(View.VISIBLE);
                    binding.otherFile.setText(Html.fromHtml("<u>" + detectFileLabel(message.fileType) + "</u>"));
                    binding.otherFile.setOnClickListener(v -> openFile(message.file, message.fileType));
                }
            } else {
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.textMessage.setText(message.message);
            }

            binding.textDateTime.setText(message.dateTime);
            adjustDateConstraint();
        }

        private void hideAll() {
            binding.textMessage.setVisibility(View.GONE);
            binding.imageFile.setVisibility(View.GONE);
            binding.pdfFile.setVisibility(View.GONE);
            binding.otherFile.setVisibility(View.GONE);
        }

        private void adjustDateConstraint() {
            ConstraintLayout.LayoutParams params =
                    (ConstraintLayout.LayoutParams) binding.textDateTime.getLayoutParams();

            if (binding.textMessage.getVisibility() == View.VISIBLE)
                params.topToBottom = binding.textMessage.getId();
            else if (binding.imageFile.getVisibility() == View.VISIBLE)
                params.topToBottom = binding.imageFile.getId();
            else if (binding.pdfFile.getVisibility() == View.VISIBLE)
                params.topToBottom = binding.pdfFile.getId();
            else if (binding.otherFile.getVisibility() == View.VISIBLE)
                params.topToBottom = binding.otherFile.getId();

            binding.textDateTime.setLayoutParams(params);
        }

        private void displayImage(String encoded, ImageView view) {
            byte[] data = Base64.decode(encoded, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            view.setImageBitmap(bmp);
        }

        private void openFile(String encodedFile, String fileType) {
            try {
                byte[] bytes = Base64.decode(encodedFile, Base64.DEFAULT);
                String name = "file_" + System.currentTimeMillis() + getExtension(fileType);
                File file = new File(itemView.getContext().getCacheDir(), name);

                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write(bytes);
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(
                        FileProvider.getUriForFile(itemView.getContext(),
                                itemView.getContext().getPackageName() + ".provider", file),
                        fileType);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (intent.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                    itemView.getContext().startActivity(intent);
                } else {
                    Toast.makeText(itemView.getContext(), "Aucune application disponible pour ce fichier", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String getExtension(String type) {
            switch (type) {
                case "application/pdf": return ".pdf";
                case "application/msword": return ".doc";
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": return ".docx";
                case "application/vnd.ms-excel": return ".xls";
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": return ".xlsx";
                case "application/vnd.ms-powerpoint": return ".ppt";
                case "application/vnd.openxmlformats-officedocument.presentationml.presentation": return ".pptx";
                default: return "";
            }
        }

        private String detectFileLabel(String type) {
            switch (type) {
                case "application/msword":
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": return "Word Document";
                case "application/vnd.ms-excel":
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": return "Excel File";
                case "application/vnd.ms-powerpoint":
                case "application/vnd.openxmlformats-officedocument.presentationml.presentation": return "PowerPoint File";
                default: return "File";
            }
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage message) {
            hideAll();

            if (message.file != null && message.fileType != null) {
                if (message.fileType.startsWith("image/")) {
                    binding.imageFile.setVisibility(View.VISIBLE);
                    displayImage(message.file, binding.imageFile);
                } else if (message.fileType.equals("application/pdf")) {
                    binding.pdfFile.setVisibility(View.VISIBLE);
                    binding.pdfFile.setText(Html.fromHtml("<u>PDF File</u>"));
                    binding.pdfFile.setOnClickListener(v -> openFile(message.file, message.fileType));
                } else {
                    binding.otherFile.setVisibility(View.VISIBLE);
                    binding.otherFile.setText(Html.fromHtml("<u>" + detectFileLabel(message.fileType) + "</u>"));
                    binding.otherFile.setOnClickListener(v -> openFile(message.file, message.fileType));
                }
            } else {
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.textMessage.setText(message.message);
            }

            binding.textDateTime.setText(message.dateTime);
            adjustDateConstraint();
        }

        private void hideAll() {
            binding.textMessage.setVisibility(View.GONE);
            binding.imageFile.setVisibility(View.GONE);
            binding.pdfFile.setVisibility(View.GONE);
            binding.otherFile.setVisibility(View.GONE);
        }

        private void adjustDateConstraint() {
            ConstraintLayout.LayoutParams params =
                    (ConstraintLayout.LayoutParams) binding.textDateTime.getLayoutParams();

            if (binding.textMessage.getVisibility() == View.VISIBLE)
                params.topToBottom = binding.textMessage.getId();
            else if (binding.imageFile.getVisibility() == View.VISIBLE)
                params.topToBottom = binding.imageFile.getId();
            else if (binding.pdfFile.getVisibility() == View.VISIBLE)
                params.topToBottom = binding.pdfFile.getId();
            else if (binding.otherFile.getVisibility() == View.VISIBLE)
                params.topToBottom = binding.otherFile.getId();

            binding.textDateTime.setLayoutParams(params);
        }

        private void displayImage(String encoded, ImageView view) {
            byte[] data = Base64.decode(encoded, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            view.setImageBitmap(bmp);
        }

        private void openFile(String encodedFile, String fileType) {
            try {
                byte[] bytes = Base64.decode(encodedFile, Base64.DEFAULT);
                String name = "file_" + System.currentTimeMillis() + getExtension(fileType);
                File file = new File(itemView.getContext().getCacheDir(), name);

                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write(bytes);
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(
                        FileProvider.getUriForFile(itemView.getContext(),
                                itemView.getContext().getPackageName() + ".provider", file),
                        fileType);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (intent.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                    itemView.getContext().startActivity(intent);
                } else {
                    Toast.makeText(itemView.getContext(), "Aucune application disponible pour ce fichier", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String getExtension(String type) {
            switch (type) {
                case "application/pdf": return ".pdf";
                case "application/msword": return ".doc";
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": return ".docx";
                case "application/vnd.ms-excel": return ".xls";
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": return ".xlsx";
                case "application/vnd.ms-powerpoint": return ".ppt";
                case "application/vnd.openxmlformats-officedocument.presentationml.presentation": return ".pptx";
                default: return "";
            }
        }

        private String detectFileLabel(String type) {
            switch (type) {
                case "application/msword":
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": return "Word Document";
                case "application/vnd.ms-excel":
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": return "Excel File";
                case "application/vnd.ms-powerpoint":
                case "application/vnd.openxmlformats-officedocument.presentationml.presentation": return "PowerPoint File";
                default: return "File";
            }
        }
    }


}

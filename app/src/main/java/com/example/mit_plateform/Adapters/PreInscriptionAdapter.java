package com.example.mit_plateform.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mit_plateform.Models.PreInscription;
import com.example.mit_plateform.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PreInscriptionAdapter extends FirestoreRecyclerAdapter<PreInscription, PreInscriptionAdapter.PreInscriptionHolder> {

    public interface OnItemClickListener {
        void onDetailsClick(DocumentSnapshot documentSnapshot, int position);
        void onValidateClick(DocumentSnapshot documentSnapshot, int position);
    }

    private OnItemClickListener listener;
    private final boolean isAdminView;
    private final SimpleDateFormat dateFormat;

    public PreInscriptionAdapter(@NonNull FirestoreRecyclerOptions<PreInscription> options,
                                 boolean isAdminView) {
        super(options);
        this.isAdminView = isAdminView;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @Override
    protected void onBindViewHolder(@NonNull PreInscriptionHolder holder, int position, @NonNull PreInscription model) {
        holder.tvDossierNumber.setText(model.getDossierNumber());
        holder.tvStudentName.setText(model.getNom() + " " + model.getPrenom());
        holder.tvSubmissionDate.setText(dateFormat.format(model.getSubmissionDate()));

        if (model.isPostSoumissionComplete()) {
            holder.tvStatus.setText("Validé");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.vert));
            holder.btnValidate.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setText("En attente");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.bleu_royal));
            holder.btnValidate.setVisibility(isAdminView ? View.VISIBLE : View.GONE);
        }

        // Masquer le bouton de validation si ce n'est pas la vue admin
        if (!isAdminView) {
            holder.btnValidate.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public PreInscriptionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pre_inscription, parent, false);
        return new PreInscriptionHolder(view);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    class PreInscriptionHolder extends RecyclerView.ViewHolder {
        TextView tvDossierNumber, tvStudentName, tvSubmissionDate, tvStatus;
        Button btnValidate, btnDetails;

        public PreInscriptionHolder(@NonNull View itemView) {
            super(itemView);
            tvDossierNumber = itemView.findViewById(R.id.tvDossierNumber);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvSubmissionDate = itemView.findViewById(R.id.tvSubmissionDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnValidate = itemView.findViewById(R.id.btnValidate);
            btnDetails = itemView.findViewById(R.id.btnDetails);

            btnDetails.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDetailsClick(getSnapshots().getSnapshot(position), position);
                }
            });

            btnValidate.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onValidateClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }
}
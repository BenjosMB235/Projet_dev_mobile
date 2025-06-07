package com.example.mit_plateform.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mit_plateform.R;
import com.example.mit_plateform.Models.User;
import com.example.mit_plateform.Utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Collections;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> users;
    private final UserListener userListener;
    private final boolean isAdminView;
    private final FirebaseFirestore database;

    public UserAdapter(List<User> users, UserListener userListener, boolean isAdminView) {
        this.users = users;
        this.userListener = userListener;
        this.isAdminView = isAdminView;
        this.database = FirebaseFirestore.getInstance();

        if (isAdminView) {
            Collections.sort(this.users, (u1, u2) -> {
                int a1 = u1.getAvailability();
                int a2 = u2.getAvailability();
                return Integer.compare(a2, a1);
            });
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_container_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textEmail, textAvailability, textUnreadCount;
        ImageView imageProfile;

        UserViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textEmail = itemView.findViewById(R.id.textEmail);
            textAvailability = itemView.findViewById(R.id.textAvailability);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            textUnreadCount = itemView.findViewById(R.id.textUnreadCount);
        }

        void setUserData(User user) {
            if (user == null) return;

            textName.setText(user.getName() != null ? user.getName() : "");
            textEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            imageProfile.setImageResource(R.drawable.ic_profile);

            textAvailability.setVisibility(isAdminView ? View.VISIBLE : View.GONE);

            if (isAdminView) {
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(user.getId())
                        .addSnapshotListener((value, error) -> {
                            if (value != null && value.exists()) {
                                Integer availability = value.getLong(Constants.KEY_AVAILABILITY) != null ?
                                        value.getLong(Constants.KEY_AVAILABILITY).intValue() : 0;
                                textAvailability.setText(availability == 1 ? "En ligne" : "Hors ligne");
                                textAvailability.setTextColor(availability == 1 ?
                                        itemView.getContext().getColor(R.color.vert) :
                                        itemView.getContext().getColor(R.color.gray));
                                itemView.setBackgroundResource(
                                        availability == 1 ? R.drawable.bg_user_online : R.drawable.bg_user_offline
                                );
                                user.setAvailability(availability);
                            }
                        });

                // 🔔 Vérifier les messages non lus
                database.collection("messages")
                        .whereEqualTo("senderId", user.getId())
                        .whereEqualTo("recipientId", Constants.ADMIN_ID)
                        .whereEqualTo("read", false)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .addSnapshotListener((snapshots, error) -> {
                            if (snapshots != null && !snapshots.isEmpty()) {
                                textUnreadCount.setVisibility(View.VISIBLE);
                                textUnreadCount.setText(String.valueOf(snapshots.size()));
                            } else {
                                textUnreadCount.setVisibility(View.GONE);
                            }
                        });
            } else {
                textUnreadCount.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (userListener != null) {
                    if (isAdminView) {
                        userListener.onUserClicked(user);
                    } else {
                        if (user.getRole() != null && user.getRole().equals("admin")) {
                            userListener.onUserClicked(user);
                        }
                    }
                }
            });
        }
    }

    public interface UserListener {
        void onUserClicked(User user);
    }
}

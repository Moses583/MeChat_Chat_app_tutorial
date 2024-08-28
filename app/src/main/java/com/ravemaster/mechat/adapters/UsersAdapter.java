package com.ravemaster.mechat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ravemaster.mechat.R;
import com.ravemaster.mechat.models.User;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersViewHolder> {
    private Context context;
    private ArrayList<User> usersList = new ArrayList<>();

    public UsersAdapter(Context context) {
        this.context = context;
    }

    public void setUsersList(ArrayList<User> usersList) {
        this.usersList = usersList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UsersViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_users,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        holder.nameOfUser.setText(usersList.get(position).name);
        holder.emailOfUser.setText(usersList.get(position).email);
        holder.userProfile.setImageBitmap(getUserImage(usersList.get(position).image));
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
class UsersViewHolder extends RecyclerView.ViewHolder {
    ImageView userProfile;
    TextView nameOfUser, emailOfUser;
    public UsersViewHolder(@NonNull View itemView) {
        super(itemView);
        userProfile = itemView.findViewById(R.id.imgUserProfileImage);
        nameOfUser = itemView.findViewById(R.id.txtNameOfUser);
        emailOfUser = itemView.findViewById(R.id.txtEmailOfUser);
    }
}

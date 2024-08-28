package com.ravemaster.mechat.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ravemaster.mechat.R;
import com.ravemaster.mechat.adapters.UsersAdapter;
import com.ravemaster.mechat.models.User;
import com.ravemaster.mechat.utilities.Constants;
import com.ravemaster.mechat.utilities.PreferenceManager;

import java.util.ArrayList;

public class UsersActivity extends AppCompatActivity {
    TextView errorMessage;
    ProgressBar progressBar;
    ImageView back;
    RecyclerView recyclerView;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_users);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        preferenceManager = new PreferenceManager(getApplicationContext());
        getUsers();
        back.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null){
                        ArrayList<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot snapshot:
                             task.getResult()) {
                            if (currentUserId.equalsIgnoreCase(snapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = snapshot.getString(Constants.KEY_NAME);
                            user.email = snapshot.getString(Constants.KEY_EMAIL);
                            user.image = snapshot.getString(Constants.KEY_IMAGE);
                            user.token = snapshot.getString(Constants.KEY_FCM_TOKEN);
                            users.add(user);
                        }
                        if (!users.isEmpty()){
                            UsersAdapter adapter = new UsersAdapter(getApplicationContext());
                            adapter.setUsersList(users);
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            recyclerView.setHasFixedSize(true);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage(){
        errorMessage.setText(String.format("%s","No user available"));
        errorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(boolean isLoading){
        if (isLoading){
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void initViews() {
        back = findViewById(R.id.imgBackButton);
        progressBar = findViewById(R.id.usersProgressBar);
        recyclerView = findViewById(R.id.usersRecycler);
        errorMessage = findViewById(R.id.userErrorMessage);
    }
}
package com.ravemaster.mechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ravemaster.mechat.R;
import com.ravemaster.mechat.utilities.Constants;
import com.ravemaster.mechat.utilities.PreferenceManager;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    TextView txtCreateAccount;
    Button button;
    TextInputLayout enterEmail,enterPassword;
    EditText one,two;
    ProgressBar progressBar;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        initEditTexts();

        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        txtCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        button.setOnClickListener(view ->{
            if (complete()){
                signIn();
            }
        });

    }

    private void signIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,one.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,two.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().getDocuments().isEmpty()){
                        DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);

                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID,snapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME,snapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE,snapshot.getString(Constants.KEY_IMAGE));

                        Intent intent = new Intent(SignInActivity.this, MainActivity.class );
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else{
                        loading(false);
                        showToasts("Unable to log in, check your credentials");
                    }
                });
    }

    private Boolean complete(){
        if (one.getText().toString().trim().isEmpty()){
            showToasts("Please enter your email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(one.getText().toString()).matches()) {
            showToasts("Enter a valid email address");
            return  false;
        } else if (two.getText().toString().trim().isEmpty()) {
            showToasts("Please enter password");
            return false;
        }  else {
            return true;
        }
    }

    private void loading(boolean isLoading){
        if (isLoading){
            button.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void initEditTexts() {
        one = enterEmail.getEditText();
        two = enterPassword.getEditText();
    }

    private void showToasts(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void initViews() {
        txtCreateAccount = findViewById(R.id.txtCreateAccount);
        button = findViewById(R.id.btnSignIn);
        enterEmail = findViewById(R.id.enterEmail);
        enterPassword = findViewById(R.id.enterPassword);
        progressBar = findViewById(R.id.myProgressBar2);
    }
}
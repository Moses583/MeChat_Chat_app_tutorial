package com.ravemaster.mechat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ravemaster.mechat.R;
import com.ravemaster.mechat.utilities.Constants;
import com.ravemaster.mechat.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    TextView txtGoToLogIn;
    String encodedImage;
    TextInputLayout username,email,password,confirmPassword;
    EditText one,two,three,four;
    Button button;
    ProgressBar progressBar;
    ImageView profile;
    CardView cardView;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        initEditTexts();

        preferenceManager = new PreferenceManager(this);

        txtGoToLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (complete()){
                    signUp();
                }
            }
        });
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
        });
    }

    private void initEditTexts() {
        one = username.getEditText();
        two = email.getEditText();
        three = password.getEditText();
        four = confirmPassword.getEditText();
    }

    private void showToasts(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void signUp(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, one.getText().toString());
        user.put(Constants.KEY_EMAIL, two.getText().toString());
        user.put(Constants.KEY_PASSWORD, four.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        loading(false);

                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                        preferenceManager.putString(Constants.KEY_NAME,one.getText().toString());
                        preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);

                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class );
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loading(false);
                        showToasts(e.getMessage());
                    }
                });
    }

    private Boolean complete(){
        if (encodedImage.isEmpty()){
            showToasts("Please add a profile photo");
            return false;
        } else if (one.getText().toString().isEmpty()) {
            showToasts("Please enter username");
            return false;
        } else if (two.getText().toString().isEmpty()) {
            showToasts("Please enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(two.getText().toString()).matches()) {
            showToasts("Enter a valid email address");
            return  false;
        } else if (three.getText().toString().trim().isEmpty()) {
            showToasts("Please enter password");
            return false;
        } else if (four.getText().toString().trim().isEmpty()) {
            showToasts("Please enter password");
            return false;
        } else if (!four.getText().toString().equalsIgnoreCase(three.getText().toString())) {
           showToasts("Passwords do not match");
           return false;
        } else {
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

    private ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
           if (o.getResultCode() == RESULT_OK){
               if (o.getData() != null){
                   Uri imageUri = o.getData().getData();
                   try {
                       InputStream inputStream = getContentResolver().openInputStream(imageUri);
                       Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                       profile.setImageBitmap(bitmap);
                       encodedImage = getEncodedImage(bitmap);
                   } catch (FileNotFoundException e){
                       e.printStackTrace();
                   }
               }
           }
        }
    });

    private String getEncodedImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth/bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,outputStream);
        byte[] bytes = outputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    private void initViews() {
        txtGoToLogIn = findViewById(R.id.txtGoToLogIn);
        username = findViewById(R.id.createUsername);
        email = findViewById(R.id.createEmail);
        password = findViewById(R.id.createPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        button = findViewById(R.id.btnCreateAccount);
        progressBar = findViewById(R.id.myProgressBar);
        profile = findViewById(R.id.imgProfilePhoto);
        cardView = findViewById(R.id.selectImage);
    }
}
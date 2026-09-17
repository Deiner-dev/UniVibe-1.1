package com.gamevault.univibe;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PefilUser extends AppCompatActivity {

    private ShapeableImageView ivProfilePic;
    private TextView tvName, tvEmail, tvCareer, tvUniversity, tvAbout;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    private String currentUserId; 

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    pickImage();
                } else {
                    Toast.makeText(this, "Permiso denegado para acceder a la galería", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadImage(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pefil_user);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        } else {
            irALogin();
            return;
        }

        initViews();
        initFirebase();
        setupNavigation();
        loadUserProfile();
    }

    private void initViews() {
        ivProfilePic = findViewById(R.id.iv_profile_pic);
        tvName = findViewById(R.id.tv_user_name_profile);
        tvEmail = findViewById(R.id.tv_user_handle_profile);
        tvCareer = findViewById(R.id.tv_career_profile);
        tvUniversity = findViewById(R.id.tv_university_profile);
        tvAbout = findViewById(R.id.tv_about_content);

        ivProfilePic.setOnClickListener(v -> checkPermissionAndPickImage());
        
        ImageButton ibLogout = findViewById(R.id.ib_logout);
        if (ibLogout != null) {
            ibLogout.setOnClickListener(v -> cerrarSesion());
        }
    }

    private void cerrarSesion() {
        // 1. Firebase Logout
        mAuth.signOut();

        // 2. Clear Local Session (if any)
        SharedPreferences pref = getSharedPreferences("UniVibePrefs", MODE_PRIVATE);
        pref.edit().clear().apply();

        // 3. Show Success Message
        Toast.makeText(this, R.string.msg_logout_success, Toast.LENGTH_SHORT).show();

        // 4. Redirect to Login and Clear Stack
        irALogin();
    }

    private void irALogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initFirebase() {
        userRef = FirebaseDatabase.getInstance().getReference("registroUsuario").child(currentUserId);
        storageRef = FirebaseStorage.getInstance().getReference("imgPerfil").child(currentUserId).child("perfil.jpg");
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void pickImage() {
        pickImageLauncher.launch("image/*");
    }

    private void uploadImage(Uri imageUri) {
        if (imageUri == null) return;

        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show();
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                
                // Save URL in registroUsuario field imgPerfil
                userRef.child("imgPerfil").setValue(downloadUrl).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Glide.with(PefilUser.this)
                                .load(downloadUrl)
                                .circleCrop()
                                .into(ivProfilePic);
                        Toast.makeText(PefilUser.this, "Foto actualizada correctamente", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(PefilUser.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void loadUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String photoUrl = snapshot.child("imgPerfil").getValue(String.class);
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Glide.with(PefilUser.this)
                                .load(photoUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .circleCrop()
                                .into(ivProfilePic);
                    }
                    
                    String name = snapshot.child("fullName").getValue(String.class);
                    String email = snapshot.child("institutionalEmail").getValue(String.class);
                    String university = snapshot.child("university").getValue(String.class);
                    
                    if (name != null) tvName.setText(name);
                    if (email != null) tvEmail.setText(email);
                    if (university != null) tvUniversity.setText(university);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PefilUser.this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigation() {
        View navHome = findViewById(R.id.ll_nav_inicio);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, panelPrincipal.class);
                startActivity(intent);
                finish();
            });
        }
        
        View navSearch = findViewById(R.id.ll_nav_buscar);
        if (navSearch != null) {
            navSearch.setOnClickListener(v -> Toast.makeText(this, R.string.nav_search, Toast.LENGTH_SHORT).show());
        }
        
        View navAdd = findViewById(R.id.fl_nav_center);
        if (navAdd != null) {
            navAdd.setOnClickListener(v -> Toast.makeText(this, R.string.desc_add, Toast.LENGTH_SHORT).show());
        }
        
        View navAlerts = findViewById(R.id.ll_nav_alertas);
        if (navAlerts != null) {
            navAlerts.setOnClickListener(v -> Toast.makeText(this, R.string.nav_alerts, Toast.LENGTH_SHORT).show());
        }
    }
}

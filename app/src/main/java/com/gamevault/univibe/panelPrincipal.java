package com.gamevault.univibe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class panelPrincipal extends AppCompatActivity {

    private LinearLayout llTabLocal, llTabColombia;
    private TextView tvGreeting;
    private ShapeableImageView ivAvatarDashboard;
    private String currentUserId;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_panel_principal);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            irALogin();
            return;
        }

        initViews();
        setupTabs();
        setupNavigation();
        initFirebase();
        loadUserInfo();
    }

    private void initViews() {
        llTabLocal = findViewById(R.id.ll_tab_local);
        llTabColombia = findViewById(R.id.ll_tab_colombia);
        tvGreeting = findViewById(R.id.tv_greeting);
        ivAvatarDashboard = findViewById(R.id.iv_avatar_dashboard);
    }

    private void initFirebase() {
        userRef = FirebaseDatabase.getInstance().getReference("registroUsuario").child(currentUserId);
    }

    private void loadUserInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    if (name != null && tvGreeting != null) {
                        tvGreeting.setText(getString(R.string.greeting_format, name));
                    }

                    String photoUrl = snapshot.child("imgPerfil").getValue(String.class);
                    if (photoUrl != null && !photoUrl.isEmpty() && ivAvatarDashboard != null) {
                        Glide.with(panelPrincipal.this)
                                .load(photoUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .circleCrop()
                                .into(ivAvatarDashboard);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void irALogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupTabs() {
        if (llTabLocal != null) {
            llTabLocal.setOnClickListener(v -> {
                llTabLocal.setBackgroundResource(R.drawable.bg_tab_selected);
                llTabColombia.setBackgroundResource(0);
                Toast.makeText(this, R.string.label_local, Toast.LENGTH_SHORT).show();
            });
        }
        
        if (llTabColombia != null) {
            llTabColombia.setOnClickListener(v -> {
                llTabColombia.setBackgroundResource(R.drawable.bg_tab_selected);
                llTabLocal.setBackgroundResource(0);
                Toast.makeText(this, R.string.label_colombia, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupNavigation() {
        // Simple toasts for navigation items
        View navInicio = findViewById(R.id.ll_nav_inicio);
        if (navInicio != null) navInicio.setOnClickListener(v -> Toast.makeText(this, R.string.nav_home, Toast.LENGTH_SHORT).show());
        
        View navBuscar = findViewById(R.id.ll_nav_buscar);
        if (navBuscar != null) navBuscar.setOnClickListener(v -> Toast.makeText(this, R.string.nav_search, Toast.LENGTH_SHORT).show());
        
        View navAdd = findViewById(R.id.fl_nav_center);
        if (navAdd != null) navAdd.setOnClickListener(v -> Toast.makeText(this, R.string.desc_add, Toast.LENGTH_SHORT).show());
        
        View navAlertas = findViewById(R.id.ll_nav_alertas);
        if (navAlertas != null) navAlertas.setOnClickListener(v -> Toast.makeText(this, R.string.nav_alerts, Toast.LENGTH_SHORT).show());
        
        View navPerfil = findViewById(R.id.ll_nav_perfil);
        if (navPerfil != null) {
            navPerfil.setOnClickListener(v -> {
                Intent intent = new Intent(this, PefilUser.class);
                startActivity(intent);
            });
        }
        
        // Header buttons
        View btnNotifications = findViewById(R.id.ib_notifications);
        if (btnNotifications != null) btnNotifications.setOnClickListener(v -> Toast.makeText(this, R.string.desc_notifications, Toast.LENGTH_SHORT).show());
        
        View btnChat = findViewById(R.id.ib_chat);
        if (btnChat != null) btnChat.setOnClickListener(v -> Toast.makeText(this, R.string.desc_messages, Toast.LENGTH_SHORT).show());
    }
}

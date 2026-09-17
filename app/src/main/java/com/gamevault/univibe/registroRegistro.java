package com.gamevault.univibe;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class registroRegistro extends AppCompatActivity {

    private EditText etFullName, etInstitutionalEmail, etPassword;
    private Spinner spUniversity;
    private ImageView ivShowPassword;
    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_registro);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupNavigation();
        setupSpinner();

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference("registroUsuario");
    }

    private void initViews() {
        etFullName = findViewById(R.id.et_full_name);
        etInstitutionalEmail = findViewById(R.id.et_institutional_email);
        etPassword = findViewById(R.id.et_password_reg);
        spUniversity = findViewById(R.id.sp_university);
        ivShowPassword = findViewById(R.id.iv_show_password_reg);

        if (ivShowPassword != null) {
            ivShowPassword.setOnClickListener(v -> togglePasswordVisibility());
        }

        Button btnRegister = findViewById(R.id.btn_register_me);
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> validarRegistro());
        }
    }

    private void setupNavigation() {
        TextView tvLoginLink = findViewById(R.id.tv_login_link);
        if (tvLoginLink != null) {
            tvLoginLink.setOnClickListener(v -> {
                Intent intent = new Intent(registroRegistro.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupSpinner() {
        if (spUniversity != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.universities_array, R.layout.spinner_selected_item);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            spUniversity.setAdapter(adapter);

            spUniversity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Item selected logic if needed
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        View ivArrow = findViewById(R.id.iv_university_arrow);
        LinearLayout llUniversity = findViewById(R.id.ll_university);
        
        if (ivArrow != null && spUniversity != null) {
            ivArrow.setOnClickListener(v -> spUniversity.performClick());
        }
        
        if (llUniversity != null && spUniversity != null) {
            llUniversity.setOnClickListener(v -> spUniversity.performClick());
        }
    }

    private void validarRegistro() {
        if (etFullName == null || etInstitutionalEmail == null || etPassword == null || spUniversity == null) return;

        String name = etFullName.getText().toString().trim();
        String email = etInstitutionalEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String university = spUniversity.getSelectedItem().toString();
        int universityPos = spUniversity.getSelectedItemPosition();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || universityPos <= 0) {
            Toast.makeText(this, R.string.msg_fill_fields, Toast.LENGTH_SHORT).show();
        } else {
            // Create Firebase User
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = mAuth.getCurrentUser().getUid();
                    User newUser = new User(name, email, university, password);
                    
                    // Save to Realtime Database using UID
                    myRef.child(uid).setValue(newUser).addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful()) {
                            Toast.makeText(registroRegistro.this, R.string.msg_registered_success, Toast.LENGTH_SHORT).show();
                            
                            // Return to Login
                            Intent intent = new Intent(registroRegistro.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(registroRegistro.this, "Error al guardar datos", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(registroRegistro.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivShowPassword.setImageResource(android.R.drawable.ic_lock_idle_lock); 
        } else {
            // Show password
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivShowPassword.setImageResource(android.R.drawable.ic_menu_view); 
        }
        isPasswordVisible = !isPasswordVisible;
        // Move cursor to the end
        etPassword.setSelection(etPassword.getText().length());
    }
}

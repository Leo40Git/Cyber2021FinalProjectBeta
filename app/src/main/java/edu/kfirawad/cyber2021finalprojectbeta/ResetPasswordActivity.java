package edu.kfirawad.cyber2021finalprojectbeta;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private static final String TAG = "C2021FPB:ResetPassword";

    private FirebaseAuth fbAuth;

    private EditText etEmail;
    private Button btnSendEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        fbAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        btnSendEmail = findViewById(R.id.btnSendEmail);
    }

    public void onClick_btnSendEmail(View view) {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "E-mail cannot be empty!", Toast.LENGTH_LONG).show();
            return;
        }

        etEmail.setText("");
        btnSendEmail.setEnabled(false);

        fbAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "E-mail has been sent! Check your inbox.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (task.isCanceled())
                            Log.e(TAG, "onClick_sendBtnEmail:sendPasswordResetEmail [canceled]");
                        else
                            Log.e(TAG, "onClick_sendBtnEmail:sendPasswordResetEmail", task.getException());
                        Toast.makeText(this, "Failed to send reset password email!", Toast.LENGTH_SHORT).show();
                    }
                    btnSendEmail.setEnabled(true);
                });
    }
}
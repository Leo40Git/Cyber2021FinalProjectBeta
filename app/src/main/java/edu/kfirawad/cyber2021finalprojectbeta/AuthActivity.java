package edu.kfirawad.cyber2021finalprojectbeta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = "C2021FPB:Main";

    private boolean isRegistering;
    private FirebaseAuth fbAuth;

    private TextView tvTitle;
    private LinearLayout layName;
    private EditText etName, etEmail, etPassword;
    private Button btnAction;
    private TextView tvSwitchBtnDesc;
    private Button btnSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        fbAuth = FirebaseAuth.getInstance();

        tvTitle = findViewById(R.id.tvTitle);
        layName = findViewById(R.id.layName);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnAction = findViewById(R.id.btnAction);
        tvSwitchBtnDesc = findViewById(R.id.tvSwitchBtnDesc);
        btnSwitch = findViewById(R.id.btnSwitch);

        setMode(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fbAuth.getAccessToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                toRideSelect();
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRegistering", isRegistering);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        setMode(savedInstanceState.getBoolean("isRegistering", false));
    }

    private void setMode(boolean isRegistering) {
        this.isRegistering = isRegistering;
        tvTitle.setText(isRegistering ? "Register" : "Log In");
        layName.setVisibility(isRegistering ? View.VISIBLE : View.GONE);
        etName.setText("");
        btnAction.setText(isRegistering ? "Register" : "Log In");
        tvSwitchBtnDesc.setText(isRegistering ? "Already have an account?" : "Don't have an account?");
        btnSwitch.setText(isRegistering ? "Log In" : "Register");
    }

    public void onClick_btnAction(View view) {
        String name = null;
        if (isRegistering) {
            name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_LONG).show();
                return;
            }
        }
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "E-mail cannot be empty!", Toast.LENGTH_LONG).show();
            return;
        }
        String password = etPassword.getText().toString().trim();
        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty!", Toast.LENGTH_LONG).show();
            return;
        }
        if (isRegistering)
            createAccount(name, email, password);
        else
            signIn(email, password);
    }

    private void createAccount(String name, String email, String password) {
        fbAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        Log.d(TAG, "createAccount:success");
                        task.getResult().getUser().updateProfile(new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build());
                        toRideSelect();
                    } else {
                        Log.w(TAG, "createAccount:failure", task.getException());
                        // TODO better error display
                        Toast.makeText(this, "Failed to create account!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signIn(String email, String password) {
        fbAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signIn:success");
                        toRideSelect();
                    } else {
                        Log.w(TAG, "signIn:failure", task.getException());
                        // TODO better error display
                        Toast.makeText(this, "Failed to sign in!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toRideSelect() {
        Intent intent = new Intent(this, RideSelectActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void onClick_btnSwitch(View view) {
        setMode(!isRegistering);
    }
}
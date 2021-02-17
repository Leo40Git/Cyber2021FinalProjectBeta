package edu.kfirawad.cyber2021finalprojectbeta;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private boolean isRegistering;

    private TextView tvTitle;
    private EditText etEmail, etPassword;
    private Button btnAction;
    private TextView tvSwitchBtnDesc;
    private Button btnSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTitle = findViewById(R.id.tvTitle);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnAction = findViewById(R.id.btnAction);
        tvSwitchBtnDesc = findViewById(R.id.tvSwitchBtnDesc);
        btnSwitch = findViewById(R.id.btnSwitch);
    }

    private void setMode(boolean isRegistering) {
        this.isRegistering = isRegistering;
        tvTitle.setText(isRegistering ? "Register" : "Log In");
        btnAction.setText(isRegistering ? "Register" : "Log In");
        tvSwitchBtnDesc.setText(isRegistering ? "Already have an account?" : "Don't have an account?");
        btnSwitch.setText(isRegistering ? "Log In" : "Register");
    }

    public void onClick_btnAction(View view) {
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
        if (isRegistering) {
            // TODO actually register
        } else {
            // TODO actually log in
        }
        etEmail.setText("");
        etPassword.setText("");
    }

    public void onClick_btnSwitch(View view) {
        setMode(!isRegistering);
    }
}
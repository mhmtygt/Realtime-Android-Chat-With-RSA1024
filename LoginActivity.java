package com.yigit.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mLogtoolbar;
    private TextInputEditText mLogEmail;
    private TextInputEditText mlogPass;
    private Button mLogButton;
    private TextView mLogtxt;
    private ProgressDialog mlogProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLogEmail=findViewById(R.id.log_email);
        mlogPass=findViewById(R.id.log_sifre);
        mLogButton=findViewById(R.id.log_log_btn);

        mlogProgress=new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("Users");

        mLogtoolbar=findViewById(R.id.login_toolbar);
        setSupportActionBar(mLogtoolbar);
        getSupportActionBar().setTitle("Giriş");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=mLogEmail.getText().toString();
                String password=mlogPass.getText().toString();

                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){

                    mlogProgress.setTitle("Giriş Yapılıyor");
                    mlogProgress.setMessage("Bilgileriniz kontrol edilirken lütfen bekleyin.");
                    mlogProgress.setCanceledOnTouchOutside(false);
                    mlogProgress.show();
                    LoginUser(email,password);
                }
            }
        });
    }

    private void LoginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mlogProgress.dismiss();

                    String current_user=mAuth.getCurrentUser().getUid();
                    String deviceToken= FirebaseInstanceId.getInstance().getToken();
                    mUserDatabase.child(current_user).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent mainIntent= new Intent(LoginActivity.this,MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    });


                }
                else{
                    mlogProgress.hide();
                    Toast.makeText(LoginActivity.this,"Hesaba giriş yapılamadı.Lütfen bağlantınızı kontrol edin.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

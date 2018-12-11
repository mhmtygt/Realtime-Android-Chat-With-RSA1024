package com.yigit.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

import static com.yigit.chat.RSAGenerator.buildKeyPair;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText mName;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private Button mBtnKayit;

    private ProgressDialog mRegProgress;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private Toolbar mRegisterToolBar;

    private DatabaseHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName=findViewById(R.id.reg_Name);
        mEmail=findViewById(R.id.reg_email);
        mPassword=findViewById(R.id.reg_password);
        mBtnKayit=findViewById(R.id.reg_btnHesapOlustur);
        mAuth = FirebaseAuth.getInstance();
        myDb=new DatabaseHelper(this);

        mRegProgress=new ProgressDialog(this);

        mRegisterToolBar=findViewById(R.id.register_toolbar);
        setSupportActionBar(mRegisterToolBar);
        getSupportActionBar().setTitle("Hesap Oluştur");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBtnKayit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name=mName.getText().toString();
                String email=mEmail.getText().toString();
                String password=mPassword.getText().toString();
                if(!TextUtils.isEmpty(name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    mRegProgress.setTitle("Kullanıcı Kaydediliyor");
                    mRegProgress.setMessage("Lütfen hesap oluşturulurken bekleyin.");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    registerUser(name,email,password);
                }

            }
        });

    }

    private void registerUser(final String name, final String email, String password) {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                    String uid=current_user.getUid();
                    mDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                  //  RSAGenerator rsaGenerator=new RSAGenerator();
                    String device_token = FirebaseInstanceId.getInstance().getToken();
                    HashMap<String,String> userMap=new HashMap<>();
                    userMap.put("name",name);
                    userMap.put("status","Merhaba, ben chat uygulaması kullanıyorum.");
                    userMap.put("image","default");
                    userMap.put("thumb_image","default");
                    userMap.put("device_token", device_token);


                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mRegProgress.dismiss();

                                Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });

                }
                else{
                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this,"Hesap Oluşturulamadı.Lütfen formu kontrol edip tekrar deneyin.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

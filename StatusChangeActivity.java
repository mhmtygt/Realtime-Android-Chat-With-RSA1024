package com.yigit.chat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StatusChangeActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mSavebtn;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_change);

        mToolbar=findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Durum Ayarı");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus=findViewById(R.id.status_change);
        mSavebtn=findViewById(R.id.status_change_btn);

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String currentUid=mCurrentUser.getUid();
        mStatusDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);

        String status_value=getIntent().getStringExtra("status_value");
        mStatus.getEditText().setText(status_value);


        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress=new ProgressDialog(StatusChangeActivity.this);
                mProgress.setTitle("Kaydediliyor.");
                mProgress.setMessage("Değişiklikler kaydedilirken lütfen bekleyin.");
                mProgress.show();

                String status=mStatus.getEditText().getText().toString();

                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgress.dismiss();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Değişiklikler kaydedilirken bir hata oluştu.",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

}

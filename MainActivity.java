package com.yigit.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolBar;
    private DatabaseReference mUserRef;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private DatabaseReference pubKeyref;
    private TabLayout mTabLayout;

    public String privateKeyString;
    private String publicKeyString;

    KeyPairGenerator kpg;
    KeyPair kp;
    PublicKey publicKey;
    PrivateKey privateKey;

    public String MY_PRESS_NAME="socio_prefs";

    public void genKeys(){
        SharedPreferences prefs=getSharedPreferences(MY_PRESS_NAME,MODE_PRIVATE);
        privateKeyString=prefs.getString("private_key"+mAuth.getCurrentUser().getUid(),null);
        publicKeyString=prefs.getString("public_key"+mAuth.getCurrentUser().getUid(),null);

        if(privateKeyString==null && publicKeyString==null){
            try {
                kpg=KeyPairGenerator.getInstance("RSA");
                kpg.initialize(1024);
                kp=kpg.genKeyPair();
                publicKey=kp.getPublic();
                privateKey=kp.getPrivate();

                if(privateKey!=null){
                    privateKeyString= Base64.encodeToString(privateKey.getEncoded(),Base64.DEFAULT);
                }
                if(publicKey!=null){
                    publicKeyString=Base64.encodeToString(publicKey.getEncoded(),Base64.DEFAULT);
                }

                pubKeyref.child(mAuth.getCurrentUser().getUid()).child("pub").setValue(publicKeyString).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        SharedPreferences.Editor editor=getSharedPreferences(MY_PRESS_NAME,MODE_PRIVATE).edit();
                        editor.putString("private_key"+mAuth.getCurrentUser().getUid(),privateKeyString);
                        editor.putString("public_key"+mAuth.getCurrentUser().getUid(),publicKeyString);
                        editor.apply();
                    }
                });


            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        pubKeyref=FirebaseDatabase.getInstance().getReference().child("PubKey");
        mToolBar =findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Chat");

        if(mAuth.getCurrentUser()!=null){
            genKeys();
            mUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }
        mViewPager=findViewById(R.id.mainTabPager);
        mSectionsPagerAdapter=new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout=findViewById(R.id.mainTab);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser==null){
            toStartActivity();
        }
        else{
            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }

    }

    private void toStartActivity() {
        Intent startIntent= new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main_menu,menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId()==R.id.main_logout_btn){
             FirebaseAuth.getInstance().signOut();
             toStartActivity();
         }
         if(item.getItemId()==R.id.main_settings_btn){
             Intent settingsIntent=new Intent(MainActivity.this,SettingsActivity.class);
             startActivity(settingsIntent);
         }
        if (item.getItemId()==R.id.main_allUser_btn) {
            Intent users=new Intent(MainActivity.this,UsersActivity.class);
            startActivity(users);
        }
        
         return true;
    }
}

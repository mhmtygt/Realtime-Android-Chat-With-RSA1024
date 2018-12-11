package com.yigit.chat;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView mProfileImage;
    private TextView mProfileName,mProfileStatus,mProfileFriendsCount;
    private Button mProfileSendReq,mDeclineBtn;

    private DatabaseReference mDatabaseUsers;
    private ProgressDialog mProgressDialog;

    private DatabaseReference mFriendsReqDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotificationsDatabase;
    private FirebaseUser mCurrentUser;

    private DatabaseReference mRootRef;

    private String mCurrent_state;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id=getIntent().getStringExtra("user_id");

        mRootRef=FirebaseDatabase.getInstance().getReference();

        mDatabaseUsers= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendsReqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendsDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationsDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage=findViewById(R.id.profile_imageView);
        mProfileName=findViewById(R.id.profile_DisplayName);
        mProfileStatus=findViewById(R.id.profile_status);
        mProfileFriendsCount=findViewById(R.id.profile_friends);
        mProfileSendReq=findViewById(R.id.profile_request_btn);
        mDeclineBtn=findViewById(R.id.profile_decline_btn);

        mCurrent_state="not_friends";
        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setTitle("Lütfen Bekleyin...");
        mProgressDialog.setMessage("Kullanıcı verileri yükleniyor.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String displayName=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(displayName);
                mProfileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.profil).into(mProfileImage);

                // Arkadaş Listesi ve İstek Özellikleri
                mFriendsReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)){
                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received")){
                                mCurrent_state="req_received";
                                mProfileSendReq.setText("Arkadaşlık İsteğini Kabul Et");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);
                            }
                            else if(req_type.equals("sent")){
                                mCurrent_state="req_sent";
                                mProfileSendReq.setText("Arkadaşlık İsteğini Geri Al");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        }
                        else{
                            mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        mCurrent_state="friends";
                                        mProfileSendReq.setText("Arkadaşlıktan Çık");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               mProfileSendReq.setEnabled(false);

               //Arkadaş Değillerse

                if(mCurrent_state.equals("not_friends")){

                   DatabaseReference newNotificationref=mRootRef.child("notifications").child(user_id).push();
                   String newNotificationId=newNotificationref.getKey();

                    HashMap<String,String> notificationData=new HashMap<>();
                    notificationData.put("from",mCurrentUser.getUid());
                    notificationData.put("type","request");

                   Map requestMap=new HashMap();
                   requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id + "/request_type","sent");
                   requestMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type","received");
                   requestMap.put("notifications/" + user_id + "/" + newNotificationId,notificationData);

                   mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                       @Override
                       public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                           if(databaseError != null){
                                Toast.makeText(ProfileActivity.this,"İstek gönderilirken bir hata oluştu.",Toast.LENGTH_LONG).show();
                           }

                            mProfileSendReq.setEnabled(true);
                           mCurrent_state="req_sent";
                           mProfileSendReq.setText("Arkadaşlık İsteğini Geri Al");
                       }
                   });
                }

                //req_sent ise

                if(mCurrent_state.equals("req_sent")){
                    mFriendsReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendsReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReq.setEnabled(true);
                                    mCurrent_state="not_friends";
                                    mProfileSendReq.setText("Arkadaşlık İsteği Gönder");

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }

                // req_received ise
                if(mCurrent_state.equals("req_received")){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/"  + mCurrentUser.getUid() + "/date", currentDate);


                    friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid(), null);


                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null){

                                mProfileSendReq.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileSendReq.setText("Arkadaşlıktan Çık");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                        }
                    });
                }

                if(mCurrent_state.equals("friends")){
                    Map unfriendMap=new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id ,null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() ,null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null){


                                mCurrent_state = "not_friends";
                                mProfileSendReq.setText("İstek Gönder");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }
                            mProfileSendReq.setEnabled(true);

                        }
                    });

                }


            }
        });
    }
}


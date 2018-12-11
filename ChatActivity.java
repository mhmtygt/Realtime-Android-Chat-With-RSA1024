package com.yigit.chat;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import de.hdodenhof.circleimageview.CircleImageView;
import static com.yigit.chat.DatabaseHelper.TABLE_NAME;


public class ChatActivity extends AppCompatActivity {
    private String mChatUser;
    private String mChatUserOld;
    private Toolbar mChatToolbar;
    private DatabaseReference mRootRef;

    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private String mCurrentUserIdOld;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEM_TO_LOAD=10;
    private int mCurrentPage=1;
    private SwipeRefreshLayout mRefreshLayout;

    private int itemPos=0;
    private String mLastKey="";
    private String mPrevKey="";

    private int refreshed=0;

    private DatabaseHelper myDb;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    private BigInteger pubK;
    private BigInteger priK;
    public String[] projection=new String[]{
      DatabaseHelper.COL1,
            DatabaseHelper.COL3,
            DatabaseHelper.COL5
    };

    private String publicKeyString;
    private String publicKeyStringOwn;
    private String privateKeyString;
    private int crypted = 0;
    private DatabaseReference pubkeyref;
    private String Uid;
    public String MY_PREFS_NAME="socio_prefs";
    public String messageGlobal;
    public int decrypted=0;
    String decryptedMessage=" ";
    public String from=" ";
    public String received=" ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRootRef= FirebaseDatabase.getInstance().getReference();
        pubkeyref=FirebaseDatabase.getInstance().getReference().child("PubKey");
        mAuth=FirebaseAuth.getInstance();
        myDb=new DatabaseHelper(this);

        mCurrentUserId=mCurrentUserIdOld=mAuth.getCurrentUser().getUid();

        mChatToolbar=findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mChatUser=mChatUserOld=getIntent().getStringExtra("user_id");
        String userName=getIntent().getStringExtra("user_name");

        getSupportActionBar().setTitle(userName);
        LayoutInflater inflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        mAdapter=new MessageAdapter(messagesList);

        mTitleView=findViewById(R.id.custom_bar_title);
        mLastSeenView=findViewById(R.id.custom_bar_seen);
        mProfileImage=findViewById(R.id.custom_bar_image);

        mChatAddBtn=findViewById(R.id.chat_add_btn);
        mChatMessageView=findViewById(R.id.chat_message_view); //edittext hatası verebilir.Textview yap.
        mChatSendBtn=findViewById(R.id.chat_send_btn);

        mMessagesList=findViewById(R.id.messages_list);
        mLinearLayout=new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);

        mRefreshLayout=findViewById(R.id.swipe_message_layout);


        
        
        mTitleView.setText(userName);

        pubkeyref.child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("pub")){
                    publicKeyString=dataSnapshot.child("pub").getValue().toString();
                    crypted=1;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        pubkeyref.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("pub")){
                    publicKeyStringOwn=dataSnapshot.child("pub").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        pubkeyref.keepSynced(true);

        loadMessages();

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online=dataSnapshot.child("online").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();



                if(online.equals("true")){
                    mLastSeenView.setText("Çevrimiçi");
                }
                else{
                    GetTimeAgo getTimeAgo=new GetTimeAgo();
                    long lastTime=Long.parseLong(online);
                    String lastSeenTime=getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mLastSeenView.setText(lastSeenTime); //parametre lastSeenTime olacak "false" olayını çöz.
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap=new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap=new HashMap();
                    chatUserMap.put("Chat/"+ mCurrentUserId+"/"+mChatUser,chatAddMap);
                    chatUserMap.put("Chat/"+mChatUser+"/"+mCurrentUserId,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Log.d("Chat_Log",databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendMessage();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            }
        });




    }


    private void loadMessages() {

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME,MODE_PRIVATE);
        privateKeyString=prefs.getString("private_key"+mCurrentUserId,null);
        String publicc=prefs.getString("public_key"+mAuth.getCurrentUser().getUid(),null);
        Log.d("privateKey",privateKeyString);
        Log.d("publicKey",publicc);
        String mPrevKey,mLastKey;

        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
     //   Query messageQuery=messageRef.limitToLast(mCurrentPage*TOTAL_ITEM_TO_LOAD);

        messageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message=dataSnapshot.getValue(Messages.class);
                String messageKey=dataSnapshot.getKey();

                RSAAlgo rsaAlgo=new RSAAlgo();

                StringTokenizer tkn=new StringTokenizer(message.getMessage(),",");

                while (tkn.hasMoreTokens()){
                       try {
                     decryptedMessage=rsaAlgo.Decrypt(tkn.nextToken(),privateKeyString);
                    message.setMessage(decryptedMessage);

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                }

             Log.d("message",message.getMessage());

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size()-1);
              //  mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
       // refreshed=0;
        String message=mChatMessageView.getText().toString();
        messageGlobal=message;
        from=mCurrentUserId;
        received=mChatUser;

        RSAAlgo rsaAlgo=new RSAAlgo();
        String encryptedMessage=rsaAlgo.Encrypt(message,publicKeyString)+",";
        String encryptedMessageOwn=rsaAlgo.Encrypt(message,publicKeyStringOwn)+",";

        if(!TextUtils.isEmpty(message)){
            String current_user_ref="messages/"+mCurrentUserId+"/"+mChatUser;
            String chat_user_ref="messages/"+mChatUser+"/"+mCurrentUserId;

            DatabaseReference user_message_push=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();
            String push_id=user_message_push.getKey();

            Map messagesMap=new HashMap();
            messagesMap.put("message",encryptedMessage);
            messagesMap.put("seen",false);
            messagesMap.put("type","text");
            messagesMap.put("time",ServerValue.TIMESTAMP);
            messagesMap.put("from",mCurrentUserId);

            Map messagemapOwn = new HashMap();
            messagemapOwn.put("message",encryptedMessageOwn);
            messagemapOwn.put("seen",false);
            messagemapOwn.put("type","text");
            messagemapOwn.put("time",ServerValue.TIMESTAMP);
            messagemapOwn.put("from",mCurrentUserId);

            Map messageUserMap=new HashMap();
            messageUserMap.put(current_user_ref+"/"+push_id,messagemapOwn);
            messageUserMap.put(chat_user_ref+"/"+push_id,messagesMap);

            mChatMessageView.setText("");
           // MessageAdapter.MessageViewHolder.messageText.setText(message2);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d("Chat_Log",databaseError.getMessage().toString());
                    }
                }
            });
        }
    }
}

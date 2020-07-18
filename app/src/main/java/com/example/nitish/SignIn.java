package com.example.nitish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.WebDialog;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.facebook.login.LoginBehavior.NATIVE_WITH_FALLBACK;

public class SignIn extends AppCompatActivity {

    EditText email,password;
    FirebaseAuth mAuth;
    String mail,pass;
    private final static int RC_SIGN_IN = 123;
    GoogleSignInClient mGoogleSignInClient;
    CallbackManager callbackManager;
    LoginManager loginManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        email = (EditText)findViewById(R.id.login_user_name);
        password = (EditText)findViewById(R.id.login_password);
        mAuth = FirebaseAuth.getInstance();
        createRequest();
        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();
        LoginBehavior loginBehavior = NATIVE_WITH_FALLBACK;
        loginManager.setLoginBehavior(loginBehavior);
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException exception){
            }
        });
    }

    public void signUp(View view) {
        startActivity(new Intent(SignIn.this,SignUp.class));
        finish();
    }

    public void Login(View view) {
        mail = email.getText().toString().trim();
        pass = password.getText().toString().trim();
        if(checkInput()) {
            mAuth.signInWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignIn.this,"Signed In",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignIn.this,MainActivity.class));
                                finish();

                            } else {
                                Toast.makeText(SignIn.this, "User not exist",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }

    private boolean checkInput() {
        boolean b = true;
        if(mail.isEmpty()){
            email.setError("Username is required");
            b = false;
        }
        if(pass.isEmpty()){
            password.setError("Password is required");
            b = false;
        }
        return b;
    }

    private void createRequest() {


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    public void GoogleSignIn(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(SignIn.this,"Error occurred: Retry",Toast.LENGTH_SHORT).show();
            }
        }
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final DatabaseReference rootref;
                            rootref=FirebaseDatabase.getInstance().getReference();
                            final String uid = mAuth.getUid();
                            rootref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!(dataSnapshot.child("Users").child(uid).exists())){
                                        Intent i = new Intent(SignIn.this,Register.class);
                                        i.putExtra("type of reg", "google");
                                        startActivity(i);
                                        finish();
                                    }
                                    else{
                                        startActivity(new Intent(SignIn.this,MainActivity.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else {
                            Toast.makeText(SignIn.this, "Sign in Failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final DatabaseReference rootref;
                            rootref= FirebaseDatabase.getInstance().getReference();
                            final String uid = mAuth.getUid();
                            rootref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!(dataSnapshot.child("Users").child(uid).exists())){
                                        Intent i = new Intent(SignIn.this,Register.class);
                                        i.putExtra("type of reg", "facebook");
                                        startActivity(i);
                                        finish();

                                    }
                                    else{
                                        startActivity(new Intent(SignIn.this,MainActivity.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else {
                            Toast.makeText(SignIn.this, "Sign in Failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    public void FacebookSignIn(View view) {
        List<String> anotherList = new ArrayList<>();
        anotherList.add("email");
        anotherList.add("public_profile");
        loginManager.logIn(SignIn.this, (Collection<String>) anotherList);
    }
}
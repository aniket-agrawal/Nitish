package com.example.nitish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class Register extends AppCompatActivity {

    private EditText fullName;
    private EditText phoneNum;
    private TextView dob;
    private String fullNameString="",typeOfReg="",currentUserId="",dobString="",phoneNumberString="";
    private Button submitButton;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    GoogleSignInClient googleSignInClient;
    CallbackManager callbackManager;
    LoginManager loginManager;
    private boolean b = true;
    private FirebaseUser user;
    private ImageButton pickDob;
    DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Intent i = getIntent();
        typeOfReg = i.getStringExtra("type of reg");
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference().child("Users");
        currentUserId = mAuth.getCurrentUser().getUid();
        phoneNum = findViewById(R.id.signup_phone_number);

        fullName = findViewById(R.id.full_name);

        dob = findViewById(R.id.dob_info_signup);
        submitButton = findViewById(R.id.submit_details_button);
        pickDob = findViewById(R.id.dob_user_signup);


        pickDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(Register.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,
                        mDateSetListener,
                        year,month,day
                );

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                String dateOfBirth = dayOfMonth  + "/ " + month  + "/ " + year;
                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dobString = DateFormat.getDateInstance(DateFormat.SHORT).format(c.getTime());
//                "onDateSet: dd/mm/yyyy";
                dob.setText(dobString);
            }
        };



        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullNameString = fullName.getText().toString();
                phoneNumberString = phoneNum.getText().toString();

                if(fullNameString.equals("") || dobString.equals("") || phoneNumberString.equals("")){
                    Toast.makeText(Register.this, "Please input all values", Toast.LENGTH_SHORT).show();
                }
                else{
                    HashMap<String, Object> detailsMap = new HashMap<>();
                    detailsMap.put("Full Name", fullNameString);
                    detailsMap.put("Phone Number", phoneNumberString);
                    detailsMap.put("dob", dobString);
                    detailsMap.put("Type of Registration", typeOfReg);

                    usersRef.child(currentUserId).updateChildren(detailsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                b = false;
                                Toast.makeText(Register.this, "Registered", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Register.this,MainActivity.class));
                                finish();
                            }
                            else{
                                Toast.makeText(Register.this, "Please retry.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        if(b){
            if(typeOfReg.equals("google")) {
                googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        user.delete();
                    }
                });
            }
            else {
                loginManager.logOut();
                user.delete();
            }
        }
        super.onDestroy();
    }
}
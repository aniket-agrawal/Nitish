package com.example.nitish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    EditText email,password,confirmPassword,Name,Phone;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    String mail="",pass="",confirmPass="",name="",phone="",dob="";
    ImageButton userDob;
    boolean b = false;
    FirebaseUser user;
    DatePickerDialog.OnDateSetListener mDateSetListener;
    TextView dobInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        email = (EditText)findViewById(R.id.signup_user_name);
        password = (EditText)findViewById(R.id.signup_password);
        Name = (EditText)findViewById(R.id.user_name);
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference().child("Users");
        Phone = (EditText)findViewById(R.id.user_phone_number);
        confirmPassword = (EditText)findViewById(R.id.confirm_password_signup);
        userDob = findViewById(R.id.dob_user);
        dobInfo = findViewById(R.id.dob_info);

        userDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(SignUp.this,
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
                String dateOfBirth = DateFormat.getDateInstance(DateFormat.SHORT).format(c.getTime());
//                "onDateSet: dd/mm/yyyy";
                dob = dateOfBirth;
                dobInfo.setText(dateOfBirth);
            }
        };
    }

    public void signIn(View view) {
        startActivity(new Intent(SignUp.this,SignIn.class));
        finish();
    }

    public void Logup(View view) {
        mail = email.getText().toString().trim();
        pass = password.getText().toString().trim();
        name = Name.getText().toString();
        phone = Phone.getText().toString().trim();
        confirmPass = confirmPassword.getText().toString().trim();
        if(checkInput()) {
            mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                b = true;
                                HashMap<String, Object> detailsMap = new HashMap<>();
                                detailsMap.put("Full Name", name);
                                detailsMap.put("Phone Number", phone);
                                detailsMap.put("dob", dob);
                                detailsMap.put("Type of Registration", "normal");
                                detailsMap.put("Username",mail);
                                detailsMap.put("Password",pass);
                                String currentUserId = mAuth.getUid();
                                user = mAuth.getCurrentUser();

                                usersRef.child(currentUserId).updateChildren(detailsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            b = false;
                                            Toast.makeText(SignUp.this, "Signed Up", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignUp.this,MainActivity.class));
                                            finish();
                                        }
                                        else{
                                            Toast.makeText(SignUp.this, "Please retry.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            } else {
                                Toast.makeText(SignUp.this, "User already exist.",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }

    }

    private boolean checkInput() {
        boolean b = true;
        if(name.isEmpty()){
            Name.setError("Name is required");
        }
        if(phone.isEmpty()){
            Phone.setError("Phone is required");
        }
        if(mail.isEmpty()){
            email.setError("Email is required");
            b = false;
        }
        if(pass.isEmpty()){
            password.setError("Password is required");
            b = false;
        }
        if(confirmPass.isEmpty()){
            confirmPassword.setError("Confirm your password");
            b = false;
        }
        else if(!confirmPass.equals(pass)){
            confirmPassword.setError("Both password should match");
            b = false;
        }
        if (dob.isEmpty()){
            dobInfo.setError("Date of birth is required");
        }
        return b;
    }

    @Override
    protected void onDestroy() {
        if(b){
            user.delete();
        }
        super.onDestroy();
    }
}
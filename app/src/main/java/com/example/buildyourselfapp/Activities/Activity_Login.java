package com.example.buildyourselfapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.buildyourself.R;
import com.example.buildyourselfapp.DAL.ExerciseDAL;
import com.example.buildyourselfapp.DAL.MealDAL;
import com.example.buildyourselfapp.DAL.UsersDAL;
import com.google.firebase.auth.FirebaseAuth;

public class Activity_Login extends AppCompatActivity implements View.OnClickListener {

    private Button register, login;
    private TextView email, password;
    private UsersDAL userDAL;
    private MealDAL mealDAL;
    private ExerciseDAL exerciseDAL;
    FirebaseAuth fAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.userDAL = UsersDAL.getInstance();
        this.mealDAL = MealDAL.getInstance();
        this.exerciseDAL = ExerciseDAL.getInstance();
        fAuth = FirebaseAuth.getInstance();
        findViews();
        initViews();
    }


    private void findViews() {
        //Edit Texts
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        //Buttons
        login = findViewById(R.id.btn_login);
        register = findViewById(R.id.btn_register);
    }

    private void initViews() {
        register.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_register) {
            startActivity(new Intent(Activity_Login.this, Activity_Register.class));
        } else if (v.getId() == R.id.btn_login) {
            String emailTxt = email.getText().toString(),
                    passwordTxt = password.getText().toString();
            if ((emailTxt != null && passwordTxt != null)
                    && (!emailTxt.isEmpty() && !passwordTxt.isEmpty())) {
                Log.d("TAJ", "emailTxt: " + emailTxt + " passwordTxt: " + passwordTxt);

                if(userDAL.LogIn(emailTxt,passwordTxt)) {
                    fAuth.signInWithEmailAndPassword(emailTxt, passwordTxt).addOnSuccessListener(s->{
                        int planTemp = this.userDAL.getCurrentUser().getPlan();
                        if(planTemp==0) {
                            startActivity(new Intent(Activity_Login.this, Activity_Add_Image.class));
                        }else{
                            startActivity(new Intent(Activity_Login.this, Activity_Add_Image.class));
                        }
                    }).addOnFailureListener(f->{
                        Toast.makeText(Activity_Login.this, "email or password incorrect!",
                                Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                Toast.makeText(Activity_Login.this, "email and password must be provided!",
                        Toast.LENGTH_SHORT).show();
            }
        }

    }
}
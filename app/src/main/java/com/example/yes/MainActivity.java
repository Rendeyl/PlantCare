package com.example.yes;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    //Password
    String MainPassword = "Group1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);

        Intent Home = new Intent(this, com.example.yes.Home.class);

        //Password Manager
        EditText passwordIPT = findViewById(R.id.passwordIPT);
    Button submitBTN = findViewById(R.id.submitBTN);
    submitBTN.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (passwordIPT.getText().toString().equals(MainPassword)){
                startActivity(Home);
                passwordIPT.setText("");
            } else{
                passwordIPT.setText("");
                passwordIPT.setHint("Incorrect Password");
                passwordIPT.setHintTextColor(Color.parseColor("#ff0000"));

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        passwordIPT.setHint("Password");
                        passwordIPT.setHintTextColor(Color.parseColor("#808080"));
                    }
                }, 1500);
            }
        }
    });

        //CheckBox Manager
        CheckBox showpassCB = findViewById(R.id.showpassCB);
        showpassCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    passwordIPT.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else passwordIPT.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

    }
}
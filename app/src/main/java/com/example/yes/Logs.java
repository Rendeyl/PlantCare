package com.example.yes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import android.util.Log;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class Logs extends AppCompatActivity {

    private ListView listView;
    private LogAdapter adapter;
    private List<LogEntry> logList;  // Ensure this is properly defined.
    private Button btnAddLog;

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;

    Calendar calendar = Calendar.getInstance();
    int hour = calendar.get(Calendar.HOUR_OF_DAY); // 24-hour format
    int minute = calendar.get(Calendar.MINUTE);
    int second = calendar.get(Calendar.SECOND);


    //vars

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        //Intent vars
        Intent Home = new Intent(this, Home.class);
        Intent Credits = new Intent(this, Credits.class);
        Intent Info = new Intent(this, Info.class);
        Intent Logout = new Intent(this, MainActivity.class);

        //Logout Alert
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(Logs.this)
                        .setTitle("Log-out Confirmation")
                        .setMessage("Are you sure you want to Log-out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(Logout);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        //Nav Drawer Init
        toolbar = findViewById(R.id.toobar);
        drawerLayout = findViewById(R.id.Logs);
        navigationView = findViewById(R.id.nav);

        //Nav Drawer
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemid = item.getItemId();
                if (itemid == R.id.homee) {
                    startActivity(Home);
                } else if (itemid == R.id.credits) {
                    startActivity(Credits);
                } else if (itemid == R.id.info) {
                    startActivity(Info);
                } else if (itemid == R.id.logout) {
                    startActivity(Logout);
                } else {
                    Toast.makeText(Logs.this, "DEFAULT", Toast.LENGTH_SHORT).show();
                }
                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(Logs.this)
                        .setTitle("Log-out Confirmation")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Logs.this, MainActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        listView = findViewById(R.id.listView);
        btnAddLog = findViewById(R.id.btnAddLog);
        logList = new ArrayList<>();  // Initialize the list to avoid null issues.

        adapter = new LogAdapter(this, logList);
        listView.setAdapter(adapter);

        // Test entry
        receiveBluetoothMessage("Successfully watered the plants");

        btnAddLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLogEntry("Successfully watered the plants");
            }
        });

    }
    //Functions

    private void receiveBluetoothMessage(String message) {
        if (logList.size() >= 10) {
            logList.remove(logList.size() - 1);  // Remove oldest entry
        }

        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
        logList.add(0, new LogEntry(message, timestamp));  // Add newest at top
        adapter.notifyDataSetChanged();
    }

    private void addLogEntry(String message) {
        if (logList.size() >= 10) {
            logList.remove(logList.size() - 1);  // Remove the oldest entry
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        logList.add(0, new LogEntry(message, timestamp));  // Add new entry at the top
        adapter.notifyDataSetChanged();
    }

    //End of Functions
}
package com.example.yes;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import android.util.Log;


public class Home extends AppCompatActivity {

    //Bluetooth Connection Vars that idk
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private static final String DEVICE_NAME = "ESP32"; // ESP32 Bluetooth Name
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean isConnected = false;
    private Thread receiveThread;
    private boolean isReceiving = false;

    //Drawe Vars
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;

    //Resevior Vars
    public int waterlevelprog = 0;
    public TextView waterlevel, waterpercent;
    public int waterper = 0;

    //Moist
    public TextView plantbox, moistpercent;
    public int moistlevel = 0;

    //Other
    int Max = 200;
    int Max2 = 100;
    int Min = 0;

    public TextView schedule;

    public String selected_schedule = "Everyday";

    boolean active = true;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        schedule = findViewById(R.id.currday);

        //Bluetooth Connection
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Intent Vars
        Intent Logs = new Intent(this, Logs.class);
        Intent Credits = new Intent(this, Credits.class);
        Intent Info = new Intent(this, Info.class);
        Intent Logout = new Intent(this, MainActivity.class);

        //Logout Alert
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(Home.this)
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
        drawerLayout = findViewById(R.id.Home);
        navigationView = findViewById(R.id.nav);

        //Nav Drawer
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

        //Water Resevior Vars
        waterlevel = findViewById(R.id.waterlevel);
        waterpercent = findViewById(R.id.waterpercent);
        Button addwater = findViewById(R.id.addw);
        Button removewater = findViewById(R.id.remw);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemid = item.getItemId();
                if (itemid == R.id.logs) {
                    startActivity(Logs);
                } else if (itemid == R.id.credits) {
                    startActivity(Credits);
                } else if (itemid == R.id.info) {
                    startActivity(Info);
                } else if (itemid == R.id.logout) {
                    startActivity(Logout);
                } else{
                    Toast.makeText(Home.this, "DEFAULT", Toast.LENGTH_SHORT).show();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        Button connectButton = findViewById(R.id.Connect);
        //bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectToEsp32();
            }
        });

        //Water Resevior Cmnds
        updatereservior();
        addwater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waterlevelprog != Max) {
                    waterlevelprog = waterlevelprog + 2;
                    waterper = waterper + 1;
                    updatereservior();
                }
            }
        });

        removewater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waterlevelprog != Min){
                    waterlevelprog = waterlevelprog - 2;
                    waterper = waterper - 1;
                    updatereservior();
                }
            }
        });

        //Plant Box Moist Thingy
        plantbox = findViewById(R.id.plantdirt);
        Button moistadd = findViewById(R.id.moistadd);
        Button moistrem = findViewById(R.id.moistremo);
        moistpercent = findViewById(R.id.moistPercent);

        moistadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moistlevel != Max2) {
                    moistlevel = moistlevel + 1;
                    updatemoist();
                }
            }
        });

        moistrem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moistlevel != Min) {
                    moistlevel = moistlevel - 1;
                    updatemoist();
                }
            }
        });

        TextView timeTXT = findViewById(R.id.currtime);
        Button changetimeBTN = findViewById(R.id.change_time);
        changetimeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                int mins = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(Home.this, AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        c.set(Calendar.MINUTE,minute);
                        c.setTimeZone(TimeZone.getDefault());
                        SimpleDateFormat format = new SimpleDateFormat("h:mm a");
                        String time = format.format(c.getTime());
                        timeTXT.setText(time);
                    }
                },hours, mins, false);
                timePickerDialog.show();
            }
        });

        Button showbtn = findViewById(R.id.showBTN);
        showbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (active == true) {
                    addwater.setVisibility(View.INVISIBLE);
                    removewater.setVisibility(View.INVISIBLE);
                    moistadd.setVisibility(View.INVISIBLE);
                    moistrem.setVisibility(View.INVISIBLE);
                    connectButton.setVisibility(View.INVISIBLE);
                    active = false;
                } else if (active == false) {
                    addwater.setVisibility(View.VISIBLE);
                    removewater.setVisibility(View.VISIBLE);
                    moistadd.setVisibility(View.VISIBLE);
                    moistrem.setVisibility(View.VISIBLE);
                    connectButton.setVisibility(View.VISIBLE);
                    active = true;
                }

            }
        });

        Button scheduleBTN = findViewById(R.id.change_date);
        scheduleBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeday();
            }
        });

        ConnectToEsp32();

    }
    //Functions

    @SuppressLint("MissingPermission")
    public void ConnectToEsp32() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth Not Supported", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Turn On Bluetooth first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for paired devices and try to connect
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(DEVICE_NAME)) {
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(BT_UUID);
                    bluetoothSocket.connect(); // Attempt to connect
                    outputStream = bluetoothSocket.getOutputStream();
                    inputStream = bluetoothSocket.getInputStream();

                    outputStream.write("Hello ESP32".getBytes());
                    Toast.makeText(this, "Connected to ESP32!", Toast.LENGTH_SHORT).show();
                    isConnected = true;
                    startReceivingData();
                } catch (IOException e) {
                    Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
                    isConnected = false;
                    reconnectToEsp32();
                    e.printStackTrace();
                    stopReceivingData();
                }
                return;
            }
        }

        Toast.makeText(this, "ESP32 not found. Pair it first!", Toast.LENGTH_SHORT).show();
    }
    public int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public void checkifconnected(){
        if (isConnected == false){
            //Water
            waterpercent.setTextColor(Color.parseColor("#d10300"));
            waterpercent.setText("0%");
            //Moist
            moistpercent.setTextColor(Color.parseColor("#d10300"));
            moistpercent.setText("0%");
        }
    }

    public void updatereservior() {
        if (waterlevelprog == 0) {
            waterlevel.setVisibility(View.GONE);
            waterpercent.setText(waterper + "%");
        } else {
            waterlevel.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = waterlevel.getLayoutParams();
            params.height = dpToPx(waterlevelprog);
            waterlevel.setLayoutParams(params);
            waterlevel.requestLayout();
            waterpercent.setText(waterper + "%");
        }
    }

    public void updatemoist() {
            moistpercent.setText(moistlevel + "%");
            if (moistlevel < 0) plantbox.setBackgroundResource(R.drawable.pb0);
            else if (moistlevel < 20) plantbox.setBackgroundResource(R.drawable.pb1);
            else if (moistlevel < 30) plantbox.setBackgroundResource(R.drawable.pb2);
            else if (moistlevel < 40) plantbox.setBackgroundResource(R.drawable.pb3);
            else if (moistlevel < 50) plantbox.setBackgroundResource(R.drawable.pb4);
            else if (moistlevel < 60) plantbox.setBackgroundResource(R.drawable.pb5);
            else if (moistlevel < 70) plantbox.setBackgroundResource(R.drawable.pb6);
            else if (moistlevel < 80) plantbox.setBackgroundResource(R.drawable.pb7);
            else if (moistlevel < 90) plantbox.setBackgroundResource(R.drawable.pb8);
            else if (moistlevel < 95) plantbox.setBackgroundResource(R.drawable.pb9);
            else if (moistlevel < 98) plantbox.setBackgroundResource(R.drawable.pb10);
        }

    @SuppressLint("MissingPermission")
    private void reconnectToEsp32() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth Not Supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Turn On Bluetooth first", Toast.LENGTH_SHORT).show();
            reconnectToEsp32();
            return;
        }

        Toast.makeText(this, "Attempting to reconnect...", Toast.LENGTH_SHORT).show();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(DEVICE_NAME)) {
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(BT_UUID);
                    bluetoothSocket.connect();
                    outputStream = bluetoothSocket.getOutputStream();
                    inputStream = bluetoothSocket.getInputStream();

                    Toast.makeText(this, "Reconnected to ESP32!", Toast.LENGTH_SHORT).show();
                    isConnected = true;
                    startReceivingData();
                    return;
                } catch (IOException e) {
                    Toast.makeText(this, "Reconnection failed", Toast.LENGTH_SHORT).show();
                    isReceiving = false;
                    e.printStackTrace();
                }
            }
        }

        Toast.makeText(this, "ESP32 not found. Pair it first!", Toast.LENGTH_SHORT).show();
    }

    private void startReceivingData() {
        isReceiving = true;
        isConnected = true;
        runOnUiThread(() -> Toast.makeText(Home.this, "Log: Phase 1", Toast.LENGTH_SHORT).show());

        if (inputStream == null) {
            Toast.makeText(Home.this, "Bluetooth not connected. Trying to reconnect...", Toast.LENGTH_SHORT).show();
            reconnectToEsp32();  // Attempt to reconnect
            isConnected = false;
            if (inputStream == null) return;  // Exit if reconnection faiss
        }

        receiveThread = new Thread(() -> {
            try {
                byte[] buffer = new byte[256];
                int bytes;
                StringBuilder receivedBuffer = new StringBuilder();

                runOnUiThread(() -> Toast.makeText(Home.this, "Log: Phase 2", Toast.LENGTH_SHORT).show());

                while (isReceiving) {
                    try {
                        if (inputStream != null && inputStream.available() > 0) {

                            bytes = inputStream.read(buffer);
                            if (bytes > 0) {
                                String receivedChunk = new String(buffer, 0, bytes).trim();
                                receivedBuffer.append(receivedChunk);
                                Log.d("BT", "Received: " + receivedChunk);

                                if (receivedBuffer.toString().contains("\n")) {
                                    String fullData = receivedBuffer.toString().trim();
                                    receivedBuffer.setLength(0);

                                    Log.d("BT", "Received: " + fullData);
                                    runOnUiThread(() -> Toast.makeText(Home.this,"Received: " + fullData, Toast.LENGTH_SHORT).show());

                                    // Split into individual integer values
                                    String[] values = fullData.split(",");
                                    if (values.length >= 2) {
                                        try {
                                            int sensor1 = Integer.parseInt(values[0].trim());
                                            int sensor2 = Integer.parseInt(values[1].trim());

                                            runOnUiThread(() -> {
                                                //Water
                                                waterper = sensor1;
                                                waterlevelprog = sensor1 * 2;
                                                updatereservior();

                                                //Moist
                                                moistlevel = sensor2;
                                                updatemoist();

                                                Log.d("BT", "Sensor1: " + sensor1 + ", Sensor2: " + sensor2);
                                            });
                                        } catch (NumberFormatException e) {
                                            Log.e("BT", "Invalid number format in received data: " + fullData, e);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        Log.e("BT", "IOException while reading input stream: " + e.getMessage(), e);
                        runOnUiThread(() -> Toast.makeText(Home.this, "Connection lost. Reconnecting...", Toast.LENGTH_SHORT).show());
                        reconnectToEsp32();
                        if (inputStream == null) break;
                    }
                }
            } catch (Exception e) {
                Log.e("BT", "Unexpected error in thread: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(Home.this, "Log: ERROR - Unexpected exception", Toast.LENGTH_SHORT).show());
            }
        });

        receiveThread.start();
    }

    private void stopReceivingData() {
        isReceiving = false;
        isConnected = false;
        if (receiveThread != null) {
            receiveThread.interrupt();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            stopReceivingData();
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateschedule() {
        schedule.setText(selected_schedule);
    }

    public void changeday() {
        String[] dates = {"Everyday", "Every 2 Days", "Every 3 Days"};
        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        builder.setTitle("Select Schedule");
        builder.setSingleChoiceItems(dates, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selected_schedule = dates[which];
                updateschedule();
            }
        });
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    //End of Functions
}
package com.ta.stb_03;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    private SecurityPopup securityPopup;
    private static final String CHANNEL_ID = "IoTNotificationsChannel"; // Notification channel ID
    private int notificationId = 0; // Initialize a unique ID for each notification
    private TextView horg;
    private TextView forg;
    private TextView hang;
    private TextView fang;
    private ImageView imgOrg, imgArg, menu;
    private TextView headerName;
    private TextView headerEmail;

    private Boolean horgBoolean = false;
    private Boolean forgBoolean = false;
    private Boolean hangBoolean = false;
    private Boolean fangBoolean = false;


    private FirebaseDatabase database;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fang = findViewById(R.id.fang);
        forg = findViewById(R.id.forg);
        hang = findViewById(R.id.hang);
        horg = findViewById(R.id.horg);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menu = findViewById(R.id.Drawer);
        toolbar = findViewById(R.id.toolbar);
        securityPopup = new SecurityPopup(this);
        securityPopup.setOnPasswordEnteredListener(new SecurityPopup.OnPasswordEnteredListener() {
            @Override
            public void onPasswordEntered() {
                // Handle the case when the correct password is entered
                // You can put your logic here to navigate to another activity or perform other actions
                Intent intent = new Intent(MainActivity.this, Graph.class);
                startActivity(intent);
            }
        });


        setSupportActionBar(toolbar);

        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_rate).setVisible(false);
        menu.findItem(R.id.nav_logout).setVisible(false);
        menu.findItem(R.id.nav_trash).setVisible(false);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_home);

        imgOrg = findViewById(R.id.imgOrg);
        imgArg = findViewById(R.id.imgArg);

        database = FirebaseDatabase.getInstance();
        DatabaseReference horgRef = database.getReference("sensor_proximity_suhu_kelembaban/sensor_proximity/sensor_a");
        DatabaseReference forgRef = database.getReference("sensor_proximity_suhu_kelembaban/sensor_proximity/sensor_b");
        DatabaseReference hangRef = database.getReference("sensor_proximity_suhu_kelembaban/sensor_proximity/sensor_c");
        DatabaseReference fangRef = database.getReference("sensor_proximity_suhu_kelembaban/sensor_proximity/sensor_d");

        horgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Assuming the value is a floating-point number
                Boolean sensor_a = dataSnapshot.getValue(Boolean.class);
                if (sensor_a != null) {
                    horg.setText(Boolean.toString(sensor_a));
                    horgBoolean = sensor_a;
                    cek(horgBoolean, forgBoolean);
                } else {
                    horg.setText("N/A");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle cancelled event or errors
            }
        });

        forgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Assuming the value is a floating-point number
                Boolean sensor_b = dataSnapshot.getValue(Boolean.class);
                if (sensor_b != null) {
                    forg.setText(Boolean.toString(sensor_b));
                    forgBoolean = sensor_b;
                    cek(horgBoolean, forgBoolean);
                } else {
                    forg.setText("N/A");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle cancelled event or errors
            }
        });

        hangRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Assuming the value is a floating-point number
                Boolean sensor_c = dataSnapshot.getValue(Boolean.class);
                if (sensor_c != null) {
                    hang.setText(Boolean.toString(sensor_c));
                    hangBoolean = sensor_c;
                    check(hangBoolean, fangBoolean);
                } else {
                    hang.setText("N/A");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle cancelled event or errors
            }
        });

        fangRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Assuming the value is a floating-point number
                Boolean sensor_d = dataSnapshot.getValue(Boolean.class);
                if (sensor_d != null) {
                    fang.setText(Boolean.toString(sensor_d));
                    fangBoolean = sensor_d;
                    check(hangBoolean, fangBoolean);
                } else {
                    fang.setText("N/A");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle cancelled event or errors
            }
        });

        createNotificationChannel();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String displayName = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();

            View headerView = navigationView.getHeaderView(0);
            TextView headerName = headerView.findViewById(R.id.header_name);
            TextView headerEmail = headerView.findViewById(R.id.header_email);

            headerName.setText(displayName);
            headerEmail.setText(email);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "IoT Notifications";
            String description = "Receive notifications for IoT events";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String content) {
        // Create an explicit intent that opens your app's main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your notification icon
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent) // Set the PendingIntent
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // Auto dismiss the notification when clicked

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());

        // Increment the notification ID to ensure the next notification is unique
        notificationId++;
    }

    private void cek(Boolean horg, Boolean forg) {
        if (horg && forg) {
            imgOrg.setImageResource(R.drawable.forg); // true + true
            showNotification("Smart Trash Bank", "Your Organic Bank is Full Now");
        } else if (horg || forg) {
            imgOrg.setImageResource(R.drawable.horg); // true + false
        } else {
            imgOrg.setImageResource(R.drawable.norg);// false false
        }
    }

    private void check(Boolean hang, Boolean fang) {
        if (hang && fang) {
            imgArg.setImageResource(R.drawable.farg); // true + true
            showNotification("Smart Trash Bank", "Your Inorganic Bank is Full Now");
        } else if (hang || fang) {
            imgArg.setImageResource(R.drawable.harg); // true + false
        } else {
            imgArg.setImageResource(R.drawable.narg);// false false
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (securityPopup.isDialogOpen()) {
            securityPopup.dismiss(); // Close the dialog
        } else {
            super.onBackPressed(); // Proceed with the default back action
        }
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        if (itemId == R.id.nav_home) {
            // Handle the home navigation
        } else if (itemId == R.id.nav_profile) {
            Intent intent = new Intent(MainActivity.this, Profile.class);
            startActivity(intent);
        } else if (itemId == R.id.nav_chart) {
            // Tampilkan popup keamanan sebelum beralih ke GraphActivity
            onDrawerItemClicked();
        } else if (itemId == R.id.nav_trash) {
            Intent intent = new Intent(MainActivity.this, Graph.class);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onDrawerItemClicked() {
        // Tampilkan popup keamanan untuk memasukkan kata sandi
        securityPopup.show();
    }

}
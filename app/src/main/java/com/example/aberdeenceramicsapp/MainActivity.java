package com.example.aberdeenceramicsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import java.util.Date;
import java.util.Locale;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    FirebaseFirestore firestore;
    private static Instant start;
    private static boolean loggedIn;
    BottomNavigationView bottomNavigationView;

    public void setStart(Instant start){
        this.start = start;
    };
    public Instant getStart(){
        return this.start;
    };



    public boolean isLoggedIn() {
        return loggedIn;
    }

    public static void setLoggedIn(boolean loggedInSet) {
        loggedIn = loggedInSet;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLoggedIn(false);
        this.instance = new MainActivity();
        setContentView(R.layout.activity_main);
        firestore = FirebaseFirestore.getInstance();

        // this call should be moved to sign in once that is implemented instant start
        //Instant start = startTimer();

        //adding a user
        Map<String, Object> users = new HashMap<>(); //each key/value pair in the map corresponds to a line of JSON. key being the variable name and value being the data

        users.put("email", "newuser@email.com");
        users.put("lastLog", new SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.ENGLISH).format(new Date(System.currentTimeMillis()))); // this is for timestamp. Im going to have a look at formatting it better but for now dont worry about it
        String membership = "pt"; //using a variable instead of just putting pt straight in as I am reusing it later
        users.put("membership", membership);
        users.put("password", "Password2");
        int time = 21600; // some stuff for membership time (6 hours in seconds)
        if(membership == "ft" || membership == "admin"){
            time = 43200; //12 hours
        }
        users.put("time remaining", time);
        //line below is for adding. think the listener isn't needed but it can't hurt
        firestore.collection("users").add(users).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                System.out.println("success");
            }
        });
        //stuff for pulling from firebase. whereEqualTo searches the collection for records with the field that matches the specified values
        String input = "newuser@email.com";
        firestore.collection("users").whereEqualTo("email", input).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful()){
                    System.out.println("success");
                    for(QueryDocumentSnapshot document : task.getResult()){
                        //this will need properly parsed if we need to use any of the data (which we should)
                        System.out.println(document.getId() + " = " + document.getData());
                    }
                }else{
                    System.out.println("didnt work");
                }
            }
        });



        //navstuff

        //define fragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        //create nav controller
        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNav = findViewById(R.id.navbarView);
        NavigationUI.setupWithNavController(bottomNav, navController);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            //temp vars Can be changed later
            boolean clockedIn = false;

            if(itemId == R.id.profile_menu){
                if(loggedIn){
                    navController.navigate(R.id.profile_fragment);
                } else if (loggedIn == false){
                    navController.navigate(R.id.signIn_fragment);
                }
            } else if (itemId == R.id.clockIn_menu){
                if(clockedIn){
                    navController.navigate(R.id.clockOut_fragment);
                } else if (clockedIn == false) {
                    navController.navigate(R.id.clockIn_fragment);
                }
            } else if (itemId == R.id.safety_menu){
                navController.navigate(R.id.safety_fragment);
            } else if (itemId == R.id.classes_menu){
                navController.navigate(R.id.classes_fragment);
            }

            return true;

        });
    }

    public Instant startTimer(){
        TextView clock = (TextView)findViewById(R.id.userTimer);
        Instant start = Instant.now();
        setStart(start);
        Thread timer;
        timer = new Thread(){
            @Override public void run(){
                for (;;) {
                    try { Thread.sleep(1000L);
                    } catch (InterruptedException ex) {/* ignore */ }

                    long seconds = Duration.between(start, Instant.now()).getSeconds();
                    long absSeconds = Math.abs(seconds);
                    String formattedTime = String.format("%d:%02d:%02d", absSeconds / 3600, (absSeconds % 3600) / 60, absSeconds % 60); // https://stackoverflow.com/a/266846 accessed 05/11/2023

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            clock.setText(formattedTime);
                        }
                    });

                }}
        };
        timer.setDaemon(true);
        timer.start();
        return start;
    }

    }

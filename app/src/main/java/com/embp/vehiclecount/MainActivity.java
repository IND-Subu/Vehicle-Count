package com.embp.vehiclecount;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.text.DateFormat;
import java.util.TimeZone;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends AppCompatActivity {
    private AdView adView;
    private Context context;
    private EditText setNewCount;
    private TextView counterView;
    private int updatedCount;
    private TextView alerts;
    private String  getLocation;
    private String  getOption;
    private static String storedIstDate, storedIstTime;
    Button setCounterBtn;
    private GestureDetector gestureDetector;
    private static final String TAG = "VehicleCount";


    @SuppressLint({"SetTextI18n", "ResourceType", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        setNewCount = findViewById(R.id.setNewCount);
        alerts = findViewById(R.id.alerts);

        // Checking for Updates..
        UpdateChecker.checkForUpdate(this);

        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

            }
        });


        Date currentUtcDateTime = new Date();

        // Set the timezone to Indian Standard Time (IST)
        TimeZone istTimeZone = TimeZone.getTimeZone("Asia/Kolkata");
        @SuppressLint("SimpleDateFormat") DateFormat istDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        @SuppressLint("SimpleDateFormat") DateFormat istTimeFormat = new SimpleDateFormat("HH");
        istDateFormat.setTimeZone(istTimeZone);
        istTimeFormat.setTimeZone(istTimeZone);

        // Convert UTC date and time to IST
        storedIstDate = istDateFormat.format(currentUtcDateTime);
        storedIstTime = istTimeFormat.format(currentUtcDateTime);




        Spinner location = findViewById(R.id.location);
        Spinner options = findViewById(R.id.options);
        Button submitBtn = findViewById(R.id.submit);
        Button resetBtn = findViewById(R.id.resetBtn);
        counterView = findViewById(R.id.counter);

        setCounterBtn = findViewById(R.id.setNewCountBtn);
        setNewCount.setVisibility(View.GONE);
        setCounterBtn.setVisibility(View.GONE);

        ArrayAdapter<CharSequence> location_adaptor = ArrayAdapter.createFromResource(this, R.array.locations, android.R.layout.simple_spinner_item);
        location_adaptor.setDropDownViewResource(android.R.layout.simple_list_item_checked);
        location.setAdapter(location_adaptor);
        ArrayAdapter<CharSequence> option_adaptor = ArrayAdapter.createFromResource(this, R.array.options, android.R.layout.simple_spinner_item);
        option_adaptor .setDropDownViewResource(android.R.layout.simple_list_item_checked);
        options.setAdapter(option_adaptor);


        // Initialize Gesture Detection..
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e){
                showEditTextAndButton();
                return true;
            }
        });

        // Set touch listener for the TextView
        counterView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });


        location.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                getLocation = parentView.getItemAtPosition(position).toString();
                // Do something
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView){
                //Do Nothing
            }
        });

        options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                getOption = parentView.getItemAtPosition(position).toString();
                updateCounter();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView){
                //Do Nothing
            }
        });


        updateCounter();

        TextView dateView = findViewById(R.id.dateView);
        String[] today = today();
        dateView.setText("Today: "+today[0]+" Time: "+today[1]);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getLocation.equals("Location")) {
                    alerts.setText("Choose a Location");
                } else if (getOption.equals("Options") ){
                    alerts.setText("Choose an Option");
                } else {
                    alerts.setText("");
                    incrementCounter();

                    //sendDataToServer();
                    Map<String, Object> data = new HashMap<>();
                    data.put("location", getLocation);
                    data.put("option", getOption);
                    data.put("count", updatedCount);
                    data.put("cdate", storedIstDate);
                    data.put("ctime", storedIstTime);

                    SendDataTask sendDataTask = new SendDataTask(new SendDataTask.OnTaskCompleted() {
                        @Override
                        public void onTaskCompleted(String result) {
                            //alerts.setText(result);
                            //Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                        }
                    });
                    sendDataTask.execute(data);
                }
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                confirmReset();
            }
        });

        setCounterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String matchCounter = setNewCount.getText().toString();
                    if (!matchCounter.isEmpty()){
                        int newMatchCounter = Integer.parseInt(matchCounter);
                        VehicleCount.matchCounter(MainActivity.this, getLocation, getOption, newMatchCounter);
                        counterView.setText("Counter\n"+newMatchCounter);
                        setNewCount.setText("");
                        hideEditTextAndButton();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Please Enter a Number", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e){
                    Toast.makeText(MainActivity.this, "Invalid input. Please enter a Number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.action_settings){
            openSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void  openSettings(){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
   @SuppressLint("SetTextI18n")
   private void incrementCounter(){
       //count++;
       VehicleCount.incrementCount(context, getLocation, getOption );
       Map<String, Map<String, Integer>> allCounts = VehicleCount.getAllCounts(context);
       updatedCount = VehicleCount.getCount(context, getLocation, getOption);
       updateCounter();
    }
    @SuppressLint("SetTextI18n")
    private void updateCounter(){
        int updateCount = VehicleCount.getCount(MainActivity.this, getLocation, getOption);
        counterView.setText("Counter\n"+updateCount);
    }
        public static String[] today(){
            Date currentDate = new Date();
            Date currentTime = new Date();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            String newDate = dateFormat.format(currentDate);
            String newTime = timeFormat.format(currentTime);
            return new String[] {newDate, newTime};
        }

        private void showEditTextAndButton(){
            setNewCount.setVisibility(View.VISIBLE);
            setCounterBtn.setVisibility(View.VISIBLE);
        }
        private void hideEditTextAndButton(){
            setNewCount.setVisibility(View.GONE);
            setCounterBtn.setVisibility(View.GONE);
        }
        private void confirmReset(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Reset Counter");
            builder.setMessage("Are you sure to Reset");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    VehicleCount.resetCount(MainActivity.this, getLocation, getOption);
                    updateCounter();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }

    @Override
    protected void onDestroy() {
        if (adView != null){
            adView.destroy();
        }
        super.onDestroy();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null) {
            adView.pause();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }
}

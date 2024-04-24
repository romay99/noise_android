package com.example.noise;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import java.util.ArrayList;

public class SoundMeter {
    private static final int SAMPLE_RATE = 44100; // 표본화 속도 (Hz)
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO; // 단일 채널
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT; // 16비트 PCM 인코딩
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private static final String TAG = "SoundMeter";

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private ArrayList<Double> decibelValues;

    public SoundMeter() {
        decibelValues = new ArrayList<>();
    }

    public void start() {
        if (audioRecord != null && audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            isRecording = true;
            audioRecord.startRecording();

            byte[] buffer = new byte[BUFFER_SIZE];
            while (isRecording) {
                int numBytesRead = audioRecord.read(buffer, 0, BUFFER_SIZE);
                if (numBytesRead > 0) {
                    double db = getDecibelLevel(buffer, numBytesRead);
                    decibelValues.add(db);
                    // ArrayList의 크기가 60을 초과하면 가장 오래된 데이터 삭제
                    if (decibelValues.size() > 60) {
                        decibelValues.remove(0);
                    }
                }
            }
        } else {
            Log.e(TAG, "AudioRecord is not initialized properly.");
        }
    }

    public void stop() {
        isRecording = false;
        if (audioRecord != null && audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    private double getDecibelLevel(byte[] audioData, int length) {
        double sum = 0;
        for (int i = 0; i < length; i += 2) {
            short sample = (short)((audioData[i + 1] << 8) | audioData[i]);
            sum += sample * sample;
        }
        double amplitude = sum / (length / 2);
        return 20 * Math.log10(Math.sqrt(amplitude));
    }

    public void initialize() {
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
    }

    // 현재 데시벨 값을 반환하는 메서드
    public double getCurrentDecibel() {
        if (decibelValues.isEmpty()) {
            return 0.0; // 데이터가 없으면 0 dB로 반환
        }
        return decibelValues.get(decibelValues.size() - 1); // 최근 데이터 반환
    }

    // 최근 60개의 데시벨 값의 평균을 계산하여 반환하는 메서드
    public double getAverageDecibel() {
        if (decibelValues.isEmpty()) {
            return 0.0; // 데이터가 없으면 0 dB로 반환
        }
        double sum = 0;
        for (Double db : decibelValues) {
            sum += db;
        }
        return sum / decibelValues.size(); // 평균값 반환
    }
}

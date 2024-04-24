package com.example.noise;

import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private SoundMeter soundMeter;
    private TextView currentDecibelTextView;
    private TextView averageDecibelTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentDecibelTextView = findViewById(R.id.mainDb);
        averageDecibelTextView = findViewById(R.id.avgDb);

        // SoundMeter 객체 생성 및 초기화
        soundMeter = new SoundMeter();
        soundMeter.initialize();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 소음 측정 시작
        soundMeter.start();
        // 데시벨 표시 시작
        startDecibelDisplay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 소음 측정 중지
        soundMeter.stop();
        // 데시벨 표시 중지
        stopDecibelDisplay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 소음 측정 중지 및 리소스 해제
        soundMeter.stop();
    }

    // 데시벨을 화면 중앙의 TextView에 표시하는 메서드
    private void startDecibelDisplay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000); // 1초마다 업데이트
                        final double currentDecibel = soundMeter.getCurrentDecibel(); // 현재 데시벨 가져오기
                        final double averageDecibel = soundMeter.getAverageDecibel(); // 평균 데시벨 가져오기
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentDecibelTextView.setText(String.format("현재 데시벨: %.2f dB", currentDecibel)); // 현재 데시벨 표시
                                averageDecibelTextView.setText(String.format("평균 데시벨: %.2f dB", averageDecibel)); // 평균 데시벨 표시
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    // 데시벨 표시 중지하는 메서드
    private void stopDecibelDisplay() {
        // 아무것도 하지 않음
    }
}

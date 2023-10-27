package com.example.ring;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AlarmSettingActivity extends AppCompatActivity {
    private TimePicker timePicker;
    private Uri selectedAudioUri;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int AUDIO_PICKER_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setting);

        timePicker = findViewById(R.id.timePicker);
        Button confirmButton = findViewById(R.id.confirmButton);
        Button chooseToneButton = findViewById(R.id.button);

        chooseToneButton.setOnClickListener(v -> requestAudioPermission());

        confirmButton.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            Intent intent = new Intent();
            intent.putExtra("hour", hour);
            intent.putExtra("minute", minute);
            intent.setData(selectedAudioUri);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private void requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            openAudioPicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openAudioPicker();
            }
        }
    }

    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, AUDIO_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUDIO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedAudioUri = data.getData();
            setAlarmTone(selectedAudioUri);
        }
    }

    private void setAlarmTone(Uri audioUri) {
        RingtoneManager.setActualDefaultRingtoneUri(
                this,
                RingtoneManager.TYPE_ALARM,
                audioUri
        );
    }
}

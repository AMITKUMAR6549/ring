package com.example.ring;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView currentTimeTextView;
    private TextView currentDateTextView;
    private LinearLayout alarmContainer;
    private List<Alarm> alarmsList;
    private AlarmDbHelper dbHelper;
    private boolean isAlarmRinging = false;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private AlertDialog alarmRingingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        currentDateTextView = findViewById(R.id.currentDateTextView);
        alarmContainer = findViewById(R.id.alarmContainer);
        dbHelper = new AlarmDbHelper(this);
        alarmsList = new ArrayList<>();

        loadAlarmsFromDatabase();

        currentTimeTextView.setTextColor(Color.parseColor("#10B3A3"));

        updateUI();
    }

    private void loadAlarmsFromDatabase() {
        List<Alarm> alarms = dbHelper.getAllAlarms();
        alarmsList.addAll(alarms);

        for (Alarm alarm : alarms) {
            addAlarmToLayout(alarm.getHour(), alarm.getMinute(), alarm.isEnabled());
        }
    }

    private void updateUI() {
        new Handler().postDelayed(() -> {
            updateTimeAndDate();

            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            for (Alarm alarm : alarmsList) {
                if (!isAlarmRinging && currentHour == alarm.getHour() && currentMinute == alarm.getMinute() && alarm.isEnabled()) {
                    isAlarmRinging = true;
                    showAlarmRingingDialog(currentHour, currentMinute);
                }
            }

            updateUI();
        }, 200); // Update every second
    }

    private void updateTimeAndDate() {
        long currentTimeMillis = System.currentTimeMillis();
        String formattedTime = timeFormat.format(currentTimeMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(currentTimeMillis);
        currentTimeTextView.setText(formattedTime);
        currentDateTextView.setText(formattedDate);
    }

    public void onSetAlarmButtonClick(View view) {
        Intent intent = new Intent(this, AlarmSettingActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            int hour = data.getIntExtra("hour", -1);
            int minute = data.getIntExtra("minute", -1);

            if (hour != -1 && minute != -1) {
                addAlarmToLayout(hour, minute, true);
                dbHelper.insertAlarm(hour, minute, true);
            }
        }
    }

    private void addAlarmToLayout(int hour, int minute, boolean isAlarmEnabled) {
        RelativeLayout alarmBox = new RelativeLayout(this);
        alarmBox.setBackgroundResource(R.drawable.alarm_background);

        TextView alarmTimeTextView = new TextView(this);
        alarmTimeTextView.setId(View.generateViewId());
        String formattedTime = timeFormat.format(getTimeIn12HourFormat(hour, minute));
        alarmTimeTextView.setText(formattedTime);
        alarmTimeTextView.setTextSize(40);
        alarmTimeTextView.setPadding(25, 25, 0, 25);

        Switch alarmSwitch = new Switch(this);
        alarmSwitch.setId(View.generateViewId());

        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        textParams.addRule(RelativeLayout.CENTER_VERTICAL);
        alarmTimeTextView.setLayoutParams(textParams);

        RelativeLayout.LayoutParams switchParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        switchParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        switchParams.addRule(RelativeLayout.CENTER_VERTICAL);
        alarmSwitch.setLayoutParams(switchParams);

        alarmBox.addView(alarmTimeTextView);
        alarmBox.addView(alarmSwitch);

        RelativeLayout.LayoutParams boxParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        boxParams.setMargins(8, 8, 8, 8);
        alarmBox.setLayoutParams(boxParams);

        alarmSwitch.setChecked(isAlarmEnabled);

        alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                alarmTimeTextView.setTextColor(Color.parseColor("#FF727578"));
                dbHelper.updateAlarmState(hour, minute, true);
                Toast.makeText(MainActivity.this, "Alarm will ring at " + formattedTime, Toast.LENGTH_SHORT).show();
            } else {
                alarmTimeTextView.setTextColor(Color.parseColor("#FF919497"));
                dbHelper.updateAlarmState(hour, minute, false);
            }
        });

        alarmBox.setOnLongClickListener(v -> {
            showDeleteAlarmDialog(hour, minute, alarmBox);
            return true;
        });

        alarmContainer.addView(alarmBox, 0);
        alarmsList.add(new Alarm(hour, minute, isAlarmEnabled));
    }

    private void showDeleteAlarmDialog(int hour, int minute, RelativeLayout alarmBox) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Do you want to delete this alarm?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            alarmContainer.removeView(alarmBox);
            dbHelper.deleteAlarm(hour, minute);
            alarmsList.removeIf(alarm -> alarm.getHour() == hour && alarm.getMinute() == minute);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Do nothing, just close the dialog
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAlarmRingingDialog(int hour, int minute) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alarm Ringing");
        builder.setMessage("Time to wake up!");
        builder.setCancelable(false);

        builder.setPositiveButton("Dismiss", (dialog, which) -> {
            isAlarmRinging = false;
            stopAlarm();

            // Update alarm state to disabled in the database
            dbHelper.updateAlarmState(hour, minute, false);

            // Remove the alarm from the UI
            alarmContainer.removeView(findAlarmBox(hour, minute));

            dialog.dismiss();
        });

        builder.setNegativeButton("Snooze", (dialog, which) -> {
            isAlarmRinging = true;
            startSnoozeTimer(hour, minute);
            dialog.dismiss();
        });

        alarmRingingDialog = builder.create();
        alarmRingingDialog.show();
    }

    private void updateAlarmSwitchUI(int hour, int minute, boolean isEnabled) {
        // Implement this method to update the alarm switch UI based on the provided parameters.
    }

    private void stopAlarm() {
        if (alarmRingingDialog != null && alarmRingingDialog.isShowing()) {
            alarmRingingDialog.dismiss();
        }
    }

    private void startSnoozeTimer(int hour, int minute) {
        long snoozeTimeInMillis = 30 * 1000; // 30 seconds

        new Handler().postDelayed(() -> {
            isAlarmRinging = false;
            showAlarmRingingDialog(hour, minute);
        }, snoozeTimeInMillis);
    }

    private Date getTimeIn12HourFormat(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return calendar.getTime();
    }

    private RelativeLayout findAlarmBox(int hour, int minute) {
        for (int i = 0; i < alarmContainer.getChildCount(); i++) {
            RelativeLayout alarmBox = (RelativeLayout) alarmContainer.getChildAt(i);
            TextView alarmTimeTextView = alarmBox.findViewById(View.generateViewId());
            String formattedTime = timeFormat.format(getTimeIn12HourFormat(hour, minute));
            if (alarmTimeTextView.getText().toString().equals(formattedTime)) {
                return alarmBox;
            }
        }
        return null;
    }
}

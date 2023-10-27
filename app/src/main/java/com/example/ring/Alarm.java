package com.example.ring;

public class Alarm {
    private long id;
    private int hour;
    private int minute;
    private boolean enabled;

    public Alarm() {
        // Default constructor
    }

    public Alarm(int hour, int minute, boolean enabled) {
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

package de.thegame4craft.Core;

public class TimeInterval {

    private int days;
    private int hours;
    private int minutes;
    private int seconds;
    private int milliseconds;

    public TimeInterval(int days, int hours, int minutes, int seconds, int microseconds) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = microseconds;
    }

    public TimeInterval(int days, int hours, int minutes, int seconds) {
        this(days, hours, minutes, seconds, 0);
    }

    public TimeInterval(int days, int hours, int minutes) {
        this(days, hours, minutes, 0);
    }

    public TimeInterval(int days, int hours) {
        this(days, hours, 0);
    }

    public TimeInterval(int days) {
        this(days, 0);
    }

    public TimeInterval() {
        this(0);
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(int microseconds) {
        this.milliseconds = microseconds;
    }

    public long toMillisecond() {
        return days * 86_400_000L + hours * 3_600_000L + minutes * 60_000L + seconds * 1_000L + milliseconds;
    }
}

package com.sjydvlp.elefx.component.statistic;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Duration;

import java.util.function.LongConsumer;

/** A live countdown to an epoch-millisecond target time. */
public class EleFXCountdown extends EleFXStatistic {

    private final LongProperty targetTime = new SimpleLongProperty(this, "targetTime", 0);

    private final StringProperty format = new SimpleStringProperty(this, "format", "HH:mm:ss");

    private final ObjectProperty<LongConsumer> onChange = new SimpleObjectProperty<>(this, "onChange");

    private final ObjectProperty<Runnable> onFinish = new SimpleObjectProperty<>(this, "onFinish");

    private final Timeline ticker = new Timeline(new KeyFrame(Duration.ZERO), new KeyFrame(Duration.millis(100)));

    private boolean finished;

    public EleFXCountdown() {
        initializeCountdown();
    }

    public EleFXCountdown(long targetTime) {
        this();
        setTargetTime(targetTime);
    }

    public EleFXCountdown(String title, long targetTime) {
        this(targetTime);
        setTitle(title);
    }

    /**
     * Element Plus calls the countdown target {@code value}; this familiar
     * alias is available alongside {@link #setTargetTime(long)}.
     */
    @Override
    public double getValue() {
        // EleFXStatistic refreshes during its constructor. At that point this
        // subclass's fields have not yet been initialised, so avoid dereferencing
        // targetTime until Java's superclass construction has completed.
        return targetTime == null ? super.getValue() : getTargetTime();
    }

    @Override
    public void setValue(double value) {
        if (targetTime == null)
            super.setValue(value);
        else
            setTargetTime((long) value);
    }

    public long getTargetTime() {
        return targetTime.get();
    }

    public void setTargetTime(long value) {
        targetTime.set(Math.max(0, value));
    }

    public LongProperty targetTimeProperty() {
        return targetTime;
    }

    public String getFormat() {
        return format.get();
    }

    public void setFormat(String value) {
        format.set(value == null || value.isEmpty() ? "HH:mm:ss" : value);
    }

    public StringProperty formatProperty() {
        return format;
    }

    public LongConsumer getOnChange() {
        return onChange.get();
    }

    public void setOnChange(LongConsumer value) {
        onChange.set(value);
    }

    public ObjectProperty<LongConsumer> onChangeProperty() {
        return onChange;
    }

    public Runnable getOnFinish() {
        return onFinish.get();
    }

    public void setOnFinish(Runnable value) {
        onFinish.set(value);
    }

    public ObjectProperty<Runnable> onFinishProperty() {
        return onFinish;
    }

    /** Restarts rendering; this is useful after changing target time from a callback. */
    public void restart() {
        finished = false;
        render();
        if (targetTime.get() > System.currentTimeMillis()) ticker.play();
    }

    public void stop() {
        ticker.stop();
    }

    private void initializeCountdown() {
        getStyleClass().add("ele-countdown");
        ticker.setCycleCount(Animation.INDEFINITE);
        ticker.setOnFinished(event -> {
        });
        ticker.currentTimeProperty().addListener((observable, oldValue, value) -> render());
        targetTime.addListener((observable, oldValue, value) -> restart());
        format.addListener((observable, oldValue, value) -> render());
        restart();
    }

    private void render() {
        long remaining = Math.max(0, getTargetTime() - System.currentTimeMillis());
        setDisplayValue(renderFormat(remaining));
        LongConsumer change = getOnChange();
        if (change != null) change.accept(remaining);
        if (remaining == 0) {
            ticker.stop();
            if (!finished) {
                finished = true;
                Runnable finish = getOnFinish();
                if (finish != null) finish.run();
            }
        }
    }

    private String renderFormat(long millis) {
        long totalSeconds = millis / 1000;
        String pattern = getFormat();
        // Each displayed unit owns all of the remaining time from omitted
        // larger units. Thus HH:mm:ss shows 47:59:59 for nearly two days,
        // while DD [days] HH:mm:ss shows 01 days 23:59:59.
        boolean showsDays = containsToken(pattern, 'D');
        boolean showsHours = containsToken(pattern, 'H');
        boolean showsMinutes = containsToken(pattern, 'm');
        long days = totalSeconds / 86400;
        long hours = showsDays ? (totalSeconds / 3600) % 24 : totalSeconds / 3600;
        long minutes = showsHours || showsDays ? (totalSeconds / 60) % 60 : totalSeconds / 60;
        long seconds = showsMinutes || showsHours || showsDays ? totalSeconds % 60 : totalSeconds;
        long milliseconds = millis % 1000;
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < pattern.length();) {
            char ch = pattern.charAt(i);
            if (ch == '[') {
                int close = pattern.indexOf(']', i);
                if (close > i) {
                    output.append(pattern, i + 1, close);
                    i = close + 1;
                    continue;
                }
            }
            int end = i + 1;
            while (end < pattern.length() && pattern.charAt(end) == ch)
                end++;
            int width = end - i;
            if (ch == 'D')
                output.append(number(days, width));
            else if (ch == 'H')
                output.append(number(hours, width));
            else if (ch == 'm')
                output.append(number(minutes, width));
            else if (ch == 's')
                output.append(number(seconds, width));
            else if (ch == 'S')
                output.append(String.format("%03d", milliseconds), 0, Math.min(3, width));
            else
                output.append(pattern, i, end);
            i = end;
        }
        return output.toString();
    }

    /** Returns whether a format token occurs outside Element Plus-style [literal] text. */
    private static boolean containsToken(String pattern, char token) {
        boolean literal = false;
        for (int i = 0; i < pattern.length(); i++) {
            char current = pattern.charAt(i);
            if (current == '[')
                literal = true;
            else if (current == ']')
                literal = false;
            else if (!literal && current == token) return true;
        }
        return false;
    }

    private static String number(long value, int width) {
        return width <= 1 ? Long.toString(value) : String.format("%0" + width + "d", value);
    }
}

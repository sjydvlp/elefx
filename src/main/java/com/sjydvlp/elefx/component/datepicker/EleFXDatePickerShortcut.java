package com.sjydvlp.elefx.component.datepicker;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Labelled quick selection for {@link EleFXDatePicker}. */
public final class EleFXDatePickerShortcut {

    private final String text;

    private final Supplier<List<LocalDate>> values;

    public EleFXDatePickerShortcut(String text, Supplier<List<LocalDate>> values) {
        this.text = Objects.requireNonNull(text, "text");
        this.values = Objects.requireNonNull(values, "values");
    }

    public EleFXDatePickerShortcut(String text, LocalDate value) {
        this(text, () -> value == null ? List.of() : List.of(value));
    }

    public EleFXDatePickerShortcut(String text, LocalDate start, LocalDate end) {
        this(text, () -> start == null || end == null ? List.of() : List.of(start, end));
    }

    public String getText() {
        return text;
    }

    public List<LocalDate> getValues() {
        List<LocalDate> supplied = values.get();
        return supplied == null ? List.of() : List.copyOf(supplied);
    }
}

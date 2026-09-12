package com.sjydvlp.elefx.component.datepickerpanel;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** A labelled predefined date or date-range selection. */
public final class EleFXDatePickerShortcut {

    private final String text;

    private final Supplier<List<LocalDate>> valueSupplier;

    public EleFXDatePickerShortcut(String text, Supplier<List<LocalDate>> valueSupplier) {
        this.text = Objects.requireNonNull(text, "text");
        this.valueSupplier = Objects.requireNonNull(valueSupplier, "valueSupplier");
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
        List<LocalDate> values = valueSupplier.get();
        return values == null ? List.of() : List.copyOf(values);
    }
}

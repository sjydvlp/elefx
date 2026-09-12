package com.sjydvlp.elefx.component.datepicker;

/** Date Picker selection modes corresponding to Element Plus {@code type}. */
public enum EleFXDatePickerType {

    DATE,
    DATES,
    WEEK,
    MONTH,
    MONTHS,
    YEAR,
    YEARS,
    QUARTER,
    QUARTERS,
    DATETIME,
    DATE_RANGE,
    DATETIME_RANGE,
    MONTH_RANGE,
    YEAR_RANGE,
    QUARTER_RANGE;

    public boolean isRange() {
        return this == DATE_RANGE || this == DATETIME_RANGE || this == MONTH_RANGE
                || this == YEAR_RANGE || this == QUARTER_RANGE;
    }

    public boolean isMultiple() {
        return this == DATES || this == MONTHS || this == YEARS || this == QUARTERS;
    }

    public boolean usesQuarter() {
        return this == QUARTER || this == QUARTERS || this == QUARTER_RANGE;
    }

    com.sjydvlp.elefx.component.datepickerpanel.EleFXDatePickerPanelType toPanelType() {
        return com.sjydvlp.elefx.component.datepickerpanel.EleFXDatePickerPanelType.valueOf(name());
    }
}

package com.sjydvlp.elefx.component.datepickerpanel;

/** Selection modes supported by {@link EleFXDatePickerPanel}. */
public enum EleFXDatePickerPanelType {

    YEAR,
    YEARS,
    MONTH,
    MONTHS,
    DATE,
    DATES,
    WEEK,
    QUARTER,
    QUARTERS,
    QUARTER_RANGE,
    DATETIME,
    DATETIME_RANGE,
    DATE_RANGE,
    MONTH_RANGE,
    YEAR_RANGE;

    public boolean isRange() {
        return this == QUARTER_RANGE || this == DATETIME_RANGE || this == DATE_RANGE || this == MONTH_RANGE
                || this == YEAR_RANGE;
    }

    public boolean isMultiple() {
        return this == YEARS || this == MONTHS || this == DATES || this == QUARTERS;
    }

    public boolean usesMonthGrid() {
        return this == MONTH || this == MONTHS || this == MONTH_RANGE;
    }

    public boolean usesYearGrid() {
        return this == YEAR || this == YEARS || this == YEAR_RANGE;
    }

    public boolean usesQuarterGrid() {
        return this == QUARTER || this == QUARTERS || this == QUARTER_RANGE;
    }
}

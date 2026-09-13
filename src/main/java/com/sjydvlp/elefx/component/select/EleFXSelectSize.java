package com.sjydvlp.elefx.component.select;

/** Visual sizes supported by {@link EleFXSelect}. */
public enum EleFXSelectSize {

    LARGE("large"),
    DEFAULT("default"),
    SMALL("small");

    private final String cssName;

    EleFXSelectSize(String cssName) {
        this.cssName = cssName;
    }

    public String styleClass() {
        return "ele-select--size-" + cssName;
    }
}

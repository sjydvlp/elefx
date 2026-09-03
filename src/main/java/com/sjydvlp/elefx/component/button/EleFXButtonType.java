package com.sjydvlp.elefx.component.button;

/**
 * Element Plus style semantic button types.
 *
 * @author sjydvlp@163.com
 * @date 2026/8/20 23:45
 */
public enum EleFXButtonType {

    DEFAULT("default"),
    PRIMARY("primary"),
    SUCCESS("success"),
    INFO("info"),
    WARNING("warning"),
    DANGER("danger");

    private final String styleClass;

    EleFXButtonType(String styleClass) {
        this.styleClass = styleClass;
    }

    public String styleClass() {
        return "ele-button--" + styleClass;
    }
}

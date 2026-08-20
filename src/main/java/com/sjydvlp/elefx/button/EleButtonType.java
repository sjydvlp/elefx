package com.sjydvlp.elefx.button;

/**
 * Element Plus style semantic button types.
 *
 * @author sjydvlp@163.com
 * @date 2026/8/20 23:45
 */
public enum EleButtonType {

    DEFAULT("default"),
    PRIMARY("primary"),
    SUCCESS("success"),
    INFO("info"),
    WARNING("warning"),
    DANGER("danger");

    private final String styleClass;

    EleButtonType(String styleClass) {
        this.styleClass = styleClass;
    }

    public String styleClass() {
        return "ele-button--" + styleClass;
    }
}

package com.sjydvlp.elefx.component.avatar;

/** The outline used by an {@link EleFXAvatar}. */
public enum EleFXAvatarShape {

    CIRCLE("ele-avatar--circle"),
    SQUARE("ele-avatar--square");

    private final String styleClass;

    EleFXAvatarShape(String styleClass) {
        this.styleClass = styleClass;
    }

    String styleClass() {
        return styleClass;
    }
}

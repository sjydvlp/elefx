package com.sjydvlp.elefx.component.text;

/** Semantic text treatments corresponding to Element Plus Text tags. */
public enum EleFXTextTag {

    SPAN("span"),
    PARAGRAPH("p"),
    BOLD("b"),
    ITALIC("i"),
    SUBSCRIPT("sub"),
    SUPERSCRIPT("sup"),
    INSERTED("ins"),
    DELETED("del"),
    MARKED("mark");

    private final String value;

    EleFXTextTag(String value) {
        this.value = value;
    }

    public String styleClass() {
        return "ele-text--tag-" + value;
    }
}

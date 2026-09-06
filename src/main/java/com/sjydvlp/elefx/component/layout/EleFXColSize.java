package com.sjydvlp.elefx.component.layout;

import javafx.beans.NamedArg;

import java.util.Objects;

/**
 * 响应式列配置。值为 {@code null} 的属性继承基础配置或较小断点的配置。
 * 所有非空值的范围为 0 到 24。
 */
public final class EleFXColSize {

    private final Integer span;

    private final Integer offset;

    private final Integer push;

    private final Integer pull;

    public EleFXColSize() {
        this(null, null, null, null);
    }

    public EleFXColSize(@NamedArg("span") int span) {
        this(span, null, null, null);
    }

    public EleFXColSize(@NamedArg("span") Integer span,
            @NamedArg("offset") Integer offset,
            @NamedArg("push") Integer push,
            @NamedArg("pull") Integer pull) {
        this.span = validate("span", span);
        this.offset = validate("offset", offset);
        this.push = validate("push", push);
        this.pull = validate("pull", pull);
    }

    public Integer getSpan() {
        return span;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getPush() {
        return push;
    }

    public Integer getPull() {
        return pull;
    }

    /**
     * 将 FXML 中的数值属性（例如 {@code xs="24"}）转换为只指定 span 的配置。
     */
    public static EleFXColSize valueOf(String value) {
        return new EleFXColSize(Integer.parseInt(Objects.requireNonNull(value, "value").trim()));
    }

    private static Integer validate(String name, Integer value) {
        if (value != null && (value < 0 || value > 24)) {
            throw new IllegalArgumentException(name + " must be between 0 and 24");
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EleFXColSize)) {
            return false;
        }
        EleFXColSize size = (EleFXColSize) other;
        return Objects.equals(span, size.span) && Objects.equals(offset, size.offset)
                && Objects.equals(push, size.push) && Objects.equals(pull, size.pull);
    }

    @Override
    public int hashCode() {
        return Objects.hash(span, offset, push, pull);
    }

    @Override
    public String toString() {
        return "EleFXColSize[span=" + span + ", offset=" + offset
                + ", push=" + push + ", pull=" + pull + "]";
    }
}

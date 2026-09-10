package com.sjydvlp.elefx.component.cascader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** One node in an {@link EleFXCascader} option hierarchy. */
public class EleFXCascaderOption<T> {

    private final T value;

    private String label;

    private boolean disabled;

    private final ObservableList<EleFXCascaderOption<T>> children = FXCollections.observableArrayList();

    public EleFXCascaderOption(T value, String label) {
        this.value = value;
        this.label = label == null ? "" : label;
    }

    @SafeVarargs
    public EleFXCascaderOption(T value, String label, EleFXCascaderOption<T>... children) {
        this(value, label);
        this.children.addAll(children);
    }

    public T getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label == null ? "" : label;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public ObservableList<EleFXCascaderOption<T>> getChildren() {
        return children;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }
}

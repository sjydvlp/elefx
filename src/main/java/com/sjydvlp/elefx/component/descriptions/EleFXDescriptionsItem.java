package com.sjydvlp.elefx.component.descriptions;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;

/** One labelled value in an {@link EleFXDescriptions} grid. */
public class EleFXDescriptionsItem {

    private final StringProperty label = new SimpleStringProperty(this, "label", "");

    private final ObjectProperty<Node> labelNode = new SimpleObjectProperty<>(this, "labelNode");

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    private final IntegerProperty span = new SimpleIntegerProperty(this, "span", 1);

    private final IntegerProperty rowSpan = new SimpleIntegerProperty(this, "rowSpan", 1);

    private final DoubleProperty width = new SimpleDoubleProperty(this, "width", -1);

    private final DoubleProperty minWidth = new SimpleDoubleProperty(this, "minWidth", -1);

    private final DoubleProperty labelWidth = new SimpleDoubleProperty(this, "labelWidth", -1);

    private final ObjectProperty<Pos> alignment = new SimpleObjectProperty<>(this, "alignment", Pos.CENTER_LEFT);

    private final ObjectProperty<Pos> labelAlignment = new SimpleObjectProperty<>(this, "labelAlignment");

    private final StringProperty styleClass = new SimpleStringProperty(this, "styleClass", "");

    private final StringProperty labelStyleClass = new SimpleStringProperty(this, "labelStyleClass", "");

    public EleFXDescriptionsItem() {
    }

    public EleFXDescriptionsItem(String label, Node content) {
        setLabel(label);
        setContent(content);
    }

    public EleFXDescriptionsItem(String label, String value) {
        this(label, new javafx.scene.control.Label(value == null ? "" : value));
    }

    public String getLabel() {
        return label.get();
    }

    public StringProperty labelProperty() {
        return label;
    }

    public void setLabel(String value) {
        label.set(value == null ? "" : value);
    }

    public Node getLabelNode() {
        return labelNode.get();
    }

    public ObjectProperty<Node> labelNodeProperty() {
        return labelNode;
    }

    public void setLabelNode(Node value) {
        labelNode.set(value);
    }

    public Node getContent() {
        return content.get();
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public void setContent(Node value) {
        content.set(value);
    }

    public int getSpan() {
        return span.get();
    }

    public IntegerProperty spanProperty() {
        return span;
    }

    public void setSpan(int value) {
        span.set(Math.max(1, value));
    }

    public int getRowSpan() {
        return rowSpan.get();
    }

    public IntegerProperty rowSpanProperty() {
        return rowSpan;
    }

    public void setRowSpan(int value) {
        rowSpan.set(Math.max(1, value));
    }

    public double getWidth() {
        return width.get();
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public void setWidth(double value) {
        width.set(value < 0 ? -1 : value);
    }

    public double getMinWidth() {
        return minWidth.get();
    }

    public DoubleProperty minWidthProperty() {
        return minWidth;
    }

    public void setMinWidth(double value) {
        minWidth.set(value < 0 ? -1 : value);
    }

    public double getLabelWidth() {
        return labelWidth.get();
    }

    public DoubleProperty labelWidthProperty() {
        return labelWidth;
    }

    public void setLabelWidth(double value) {
        labelWidth.set(value < 0 ? -1 : value);
    }

    public Pos getAlignment() {
        return alignment.get();
    }

    public ObjectProperty<Pos> alignmentProperty() {
        return alignment;
    }

    public void setAlignment(Pos value) {
        alignment.set(value == null ? Pos.CENTER_LEFT : value);
    }

    public Pos getLabelAlignment() {
        return labelAlignment.get();
    }

    public ObjectProperty<Pos> labelAlignmentProperty() {
        return labelAlignment;
    }

    public void setLabelAlignment(Pos value) {
        labelAlignment.set(value);
    }

    public String getStyleClass() {
        return styleClass.get();
    }

    public StringProperty styleClassProperty() {
        return styleClass;
    }

    public void setStyleClass(String value) {
        styleClass.set(value == null ? "" : value.trim());
    }

    public String getLabelStyleClass() {
        return labelStyleClass.get();
    }

    public StringProperty labelStyleClassProperty() {
        return labelStyleClass;
    }

    public void setLabelStyleClass(String value) {
        labelStyleClass.set(value == null ? "" : value.trim());
    }
}

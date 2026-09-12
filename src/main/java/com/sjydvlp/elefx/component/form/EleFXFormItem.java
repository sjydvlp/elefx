package com.sjydvlp.elefx.component.form;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.function.Function;

/**
 * A labelled field within an {@link EleFXForm}. Add one or more controls through
 * {@link #getFieldChildren()}; the item renders its required indicator and validation message.
 */
public class EleFXFormItem extends VBox implements Themable {

    private static final String STYLE_CLASS = "ele-form-item";

    private final ObjectProperty<String> label = new SimpleObjectProperty<>(this, "label", "");

    private final ObjectProperty<String> prop = new SimpleObjectProperty<>(this, "prop", "");

    private final BooleanProperty required = new SimpleBooleanProperty(this, "required", false);

    private final ObjectProperty<EleFXFormLabelPosition> labelPosition = new SimpleObjectProperty<>(this,
            "labelPosition");

    private final DoubleProperty labelWidth = new SimpleDoubleProperty(this, "labelWidth", -1);

    private final ObjectProperty<String> error = new SimpleObjectProperty<>(this, "error", "");

    private final ObjectProperty<Function<EleFXFormItem, String>> validator = new SimpleObjectProperty<>(this,
            "validator");

    private final HBox labelBox = new HBox();

    private final Label requiredMark = new Label("*");

    private final Label labelNode = new Label();

    private final Label errorNode = new Label();

    private final HBox row = new HBox(12);

    private final HBox field = new HBox(8);

    private final VBox fieldArea = new VBox(2);

    private EleFXFormLabelPosition inheritedLabelPosition = EleFXFormLabelPosition.RIGHT;

    private double inheritedLabelWidth = -1;

    public EleFXFormItem() {
        initialize();
    }

    public EleFXFormItem(String label, Node... controls) {
        this();
        setLabel(label);
        getFieldChildren().addAll(controls);
    }

    public String getLabel() {
        return label.get();
    }

    public ObjectProperty<String> labelProperty() {
        return label;
    }

    public void setLabel(String value) {
        label.set(value == null ? "" : value);
    }

    /** Optional field identifier used by {@link EleFXForm#validateField(String)}. */
    public String getProp() {
        return prop.get();
    }

    public ObjectProperty<String> propProperty() {
        return prop;
    }

    public void setProp(String value) {
        prop.set(value == null ? "" : value);
    }

    public boolean isRequired() {
        return required.get();
    }

    public BooleanProperty requiredProperty() {
        return required;
    }

    public void setRequired(boolean value) {
        required.set(value);
    }

    /** A null value inherits the containing form's label position. */
    public EleFXFormLabelPosition getLabelPosition() {
        return labelPosition.get();
    }

    public ObjectProperty<EleFXFormLabelPosition> labelPositionProperty() {
        return labelPosition;
    }

    public void setLabelPosition(EleFXFormLabelPosition value) {
        labelPosition.set(value);
    }

    /** A negative value uses the containing form's computed label width. */
    public double getLabelWidth() {
        return labelWidth.get();
    }

    public DoubleProperty labelWidthProperty() {
        return labelWidth;
    }

    public void setLabelWidth(double value) {
        labelWidth.set(value < 0 ? -1 : value);
    }

    public String getError() {
        return error.get();
    }

    public ObjectProperty<String> errorProperty() {
        return error;
    }

    public void setError(String value) {
        error.set(value == null ? "" : value);
    }

    /** Returns null or blank when the item is valid; otherwise returns the validation message. */
    public Function<EleFXFormItem, String> getValidator() {
        return validator.get();
    }

    public ObjectProperty<Function<EleFXFormItem, String>> validatorProperty() {
        return validator;
    }

    public void setValidator(Function<EleFXFormItem, String> value) {
        validator.set(value);
    }

    public ObservableList<Node> getFieldChildren() {
        return field.getChildren();
    }

    /** Runs required and custom validation, returning whether this item is valid. */
    public boolean validate() {
        if (isRequired() && isFieldEmpty()) {
            setError(getLabel().isBlank() ? "This field is required" : getLabel() + " is required");
            return false;
        }
        Function<EleFXFormItem, String> rule = getValidator();
        String result = rule == null ? null : rule.apply(this);
        setError(result);
        return result == null || result.isBlank();
    }

    public void clearValidate() {
        setError("");
    }

    void applyFormDefaults(EleFXFormLabelPosition position, double width) {
        inheritedLabelPosition = Objects.requireNonNullElse(position, EleFXFormLabelPosition.RIGHT);
        inheritedLabelWidth = width;
        updateLayout();
    }

    double labelPrefWidth() {
        return labelBox.isManaged() ? labelBox.prefWidth(-1) : 0;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.FORM;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        labelBox.getStyleClass().add("ele-form-item__label");
        requiredMark.getStyleClass().add("ele-form-item__required");
        labelNode.getStyleClass().add("ele-form-item__label-text");
        labelBox.getChildren().addAll(requiredMark, labelNode);
        errorNode.getStyleClass().add("ele-form-item__error");
        errorNode.setManaged(false);
        errorNode.setVisible(false);
        row.getStyleClass().add("ele-form-item__row");
        row.setAlignment(Pos.TOP_LEFT);
        row.setFillHeight(false);
        field.getStyleClass().add("ele-form-item__content");
        field.setAlignment(Pos.CENTER_LEFT);
        field.setMaxWidth(Double.MAX_VALUE);
        fieldArea.getChildren().addAll(field, errorNode);
        HBox.setHgrow(fieldArea, Priority.ALWAYS);
        field.getChildren().addListener((ListChangeListener<Node>) observable -> {
            boolean singleField = field.getChildren().size() == 1;
            for (Node child : field.getChildren())
                HBox.setHgrow(child, singleField ? Priority.ALWAYS : Priority.NEVER);
        });
        label.addListener((observable, oldValue, newValue) -> updateLayout());
        required.addListener((observable, oldValue, newValue) -> updateLayout());
        labelPosition.addListener((observable, oldValue, newValue) -> updateLayout());
        labelWidth.addListener((observable, oldValue, newValue) -> updateLayout());
        error.addListener((observable, oldValue, newValue) -> updateError());
        updateLayout();
        updateError();
        sceneBuilderIntegration();
    }

    private void updateLayout() {
        EleFXFormLabelPosition position = getLabelPosition() == null ? inheritedLabelPosition : getLabelPosition();
        double width = getLabelWidth() < 0 ? inheritedLabelWidth : getLabelWidth();
        labelNode.setText(getLabel());
        labelBox.setVisible(!getLabel().isBlank() || isRequired());
        labelBox.setManaged(labelBox.isVisible());
        requiredMark.setVisible(isRequired());
        requiredMark.setManaged(isRequired());
        labelBox.setMinWidth(width < 0 ? 0 : width);
        labelBox.setPrefWidth(width < 0 ? Region.USE_COMPUTED_SIZE : width);
        labelBox.setMaxWidth(width < 0 ? Double.MAX_VALUE : width);
        labelBox.setMaxHeight(position == EleFXFormLabelPosition.TOP ? 22 : 32);
        labelBox.setAlignment(position == EleFXFormLabelPosition.RIGHT ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        getStyleClass().removeAll("ele-form-item--label-left", "ele-form-item--label-right",
                "ele-form-item--label-top");
        getStyleClass().add("ele-form-item--label-" + position.name().toLowerCase());
        getChildren().clear();
        if (position == EleFXFormLabelPosition.TOP) {
            setSpacing(8);
            getChildren().addAll(labelBox, fieldArea);
        } else {
            setSpacing(0);
            row.getChildren().setAll(labelBox, fieldArea);
            getChildren().add(row);
        }
    }

    private void updateError() {
        boolean invalid = !getError().isBlank();
        errorNode.setText(getError());
        errorNode.setVisible(invalid);
        errorNode.setManaged(invalid);
        if (invalid) {
            if (!getStyleClass().contains("ele-form-item--error")) getStyleClass().add("ele-form-item--error");
        } else
            getStyleClass().remove("ele-form-item--error");
    }

    private boolean isFieldEmpty() {
        return field.getChildren().stream().allMatch(this::isEmpty);
    }

    private boolean isEmpty(Node node) {
        if (node instanceof TextInputControl input) return input.getText() == null || input.getText().isBlank();
        if (node instanceof ComboBoxBase<?> comboBox) return comboBox.getValue() == null;
        if (node instanceof CheckBox checkBox) return !checkBox.isSelected();
        if (node instanceof Parent parent) return parent.getChildrenUnmodifiable().stream().allMatch(this::isEmpty);
        return false;
    }
}

package com.sjydvlp.elefx.component.statistic;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

/**
 * Element Plus inspired statistical value display.
 *
 * <p>
 * Text properties provide the standard title, prefix and suffix, while the
 * corresponding node properties allow arbitrary JavaFX content in each slot.
 * </p>
 */
public class EleFXStatistic extends VBox implements Themable {

    private final javafx.beans.property.DoubleProperty value = new javafx.beans.property.SimpleDoubleProperty(this,
            "value", 0);

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final StringProperty prefix = new SimpleStringProperty(this, "prefix", "");

    private final StringProperty suffix = new SimpleStringProperty(this, "suffix", "");

    private final StringProperty decimalSeparator = new SimpleStringProperty(this, "decimalSeparator", ".");

    private final StringProperty groupSeparator = new SimpleStringProperty(this, "groupSeparator", ",");

    private final IntegerProperty precision = new SimpleIntegerProperty(this, "precision", 0);

    private final ObjectProperty<Function<Double, String>> formatter = new SimpleObjectProperty<>(this, "formatter");

    private final StringProperty valueStyle = new SimpleStringProperty(this, "valueStyle", "");

    private final ObjectProperty<Node> titleNode = new SimpleObjectProperty<>(this, "titleNode");

    private final ObjectProperty<Node> prefixNode = new SimpleObjectProperty<>(this, "prefixNode");

    private final ObjectProperty<Node> suffixNode = new SimpleObjectProperty<>(this, "suffixNode");

    private final ReadOnlyStringWrapper displayValue = new ReadOnlyStringWrapper(this, "displayValue", "0");

    private final HBox titleBox = new HBox();

    private final HBox contentBox = new HBox();

    private final Label titleLabel = new Label();

    private final Label prefixLabel = new Label();

    private final Label valueLabel = new Label();

    private final Label suffixLabel = new Label();

    public EleFXStatistic() {
        initialize();
    }

    public EleFXStatistic(double value) {
        this();
        setValue(value);
    }

    public EleFXStatistic(String title, double value) {
        this(value);
        setTitle(title);
    }

    public double getValue() {
        return value.get();
    }

    public void setValue(double value) {
        this.value.set(value);
    }

    public javafx.beans.property.DoubleProperty valueProperty() {
        return value;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String value) {
        title.set(value == null ? "" : value);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getPrefix() {
        return prefix.get();
    }

    public void setPrefix(String value) {
        prefix.set(value == null ? "" : value);
    }

    public StringProperty prefixProperty() {
        return prefix;
    }

    public String getSuffix() {
        return suffix.get();
    }

    public void setSuffix(String value) {
        suffix.set(value == null ? "" : value);
    }

    public StringProperty suffixProperty() {
        return suffix;
    }

    public String getDecimalSeparator() {
        return decimalSeparator.get();
    }

    public void setDecimalSeparator(String value) {
        decimalSeparator.set(value == null ? "." : value);
    }

    public StringProperty decimalSeparatorProperty() {
        return decimalSeparator;
    }

    public String getGroupSeparator() {
        return groupSeparator.get();
    }

    public void setGroupSeparator(String value) {
        groupSeparator.set(value == null ? "," : value);
    }

    public StringProperty groupSeparatorProperty() {
        return groupSeparator;
    }

    public int getPrecision() {
        return precision.get();
    }

    public void setPrecision(int value) {
        precision.set(Math.max(0, Math.min(20, value)));
    }

    public IntegerProperty precisionProperty() {
        return precision;
    }

    public Function<Double, String> getFormatter() {
        return formatter.get();
    }

    public void setFormatter(Function<Double, String> value) {
        formatter.set(value);
    }

    public ObjectProperty<Function<Double, String>> formatterProperty() {
        return formatter;
    }

    public String getValueStyle() {
        return valueStyle.get();
    }

    /** JavaFX CSS declarations applied to the numerical value label. */
    public void setValueStyle(String value) {
        valueStyle.set(value == null ? "" : value);
    }

    public StringProperty valueStyleProperty() {
        return valueStyle;
    }

    public Node getTitleNode() {
        return titleNode.get();
    }

    public void setTitleNode(Node value) {
        titleNode.set(value);
    }

    public ObjectProperty<Node> titleNodeProperty() {
        return titleNode;
    }

    public Node getPrefixNode() {
        return prefixNode.get();
    }

    public void setPrefixNode(Node value) {
        prefixNode.set(value);
    }

    public ObjectProperty<Node> prefixNodeProperty() {
        return prefixNode;
    }

    public Node getSuffixNode() {
        return suffixNode.get();
    }

    public void setSuffixNode(Node value) {
        suffixNode.set(value);
    }

    public ObjectProperty<Node> suffixNodeProperty() {
        return suffixNode;
    }

    public String getDisplayValue() {
        return displayValue.get();
    }

    public ReadOnlyStringProperty displayValueProperty() {
        return displayValue.getReadOnlyProperty();
    }

    public Label getValueLabel() {
        return valueLabel;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.STATISTIC;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    /** Lets specialised displays, such as {@link EleFXCountdown}, render their own value. */
    protected final void setDisplayValue(String value) {
        displayValue.set(value == null ? "" : value);
        valueLabel.setText(displayValue.get());
    }

    private void initialize() {
        getStyleClass().add("ele-statistic");
        setAlignment(Pos.TOP_CENTER);
        titleBox.getStyleClass().add("ele-statistic__head");
        // A fixed title-row height gives text and icon title slots an
        // identical baseline-to-content offset in an HBox of statistics.
        titleBox.setMinHeight(20);
        titleBox.setPrefHeight(20);
        titleBox.setAlignment(Pos.CENTER);
        contentBox.getStyleClass().add("ele-statistic__content");
        contentBox.setAlignment(Pos.CENTER);
        titleLabel.getStyleClass().add("ele-statistic__head-title");
        prefixLabel.getStyleClass().add("ele-statistic__prefix");
        valueLabel.getStyleClass().add("ele-statistic__number");
        suffixLabel.getStyleClass().add("ele-statistic__suffix");
        getChildren().addAll(titleBox, contentBox);
        javafx.beans.InvalidationListener refresh = ignored -> refresh();
        value.addListener(refresh);
        decimalSeparator.addListener(refresh);
        groupSeparator.addListener(refresh);
        precision.addListener(refresh);
        formatter.addListener(refresh);
        valueStyle.addListener(refresh);
        title.addListener(refresh);
        prefix.addListener(refresh);
        suffix.addListener(refresh);
        titleNode.addListener(refresh);
        prefixNode.addListener(refresh);
        suffixNode.addListener(refresh);
        refresh();
        sceneBuilderIntegration();
    }

    private void refresh() {
        titleLabel.setText(getTitle());
        prefixLabel.setText(getPrefix());
        suffixLabel.setText(getSuffix());
        Node shownTitle = getTitleNode() == null ? titleLabel : getTitleNode();
        Node shownPrefix = getPrefixNode() == null ? prefixLabel : getPrefixNode();
        Node shownSuffix = getSuffixNode() == null ? suffixLabel : getSuffixNode();
        titleBox.getChildren().setAll(shownTitle);
        titleBox.setManaged(getTitleNode() != null || !getTitle().isEmpty());
        titleBox.setVisible(titleBox.isManaged());
        contentBox.getChildren().clear();
        if (getPrefixNode() != null || !getPrefix().isEmpty()) contentBox.getChildren().add(shownPrefix);
        contentBox.getChildren().add(valueLabel);
        if (getSuffixNode() != null || !getSuffix().isEmpty()) contentBox.getChildren().add(shownSuffix);
        valueLabel.setStyle(getValueStyle());
        Function<Double, String> custom = getFormatter();
        setDisplayValue(custom == null ? format(getValue()) : safeFormat(custom, getValue()));
        requestLayout();
    }

    private static String safeFormat(Function<Double, String> formatter, double value) {
        String result = formatter.apply(value);
        return result == null ? "" : result;
    }

    private String format(double number) {
        if (!Double.isFinite(number)) return Double.toString(number);
        BigDecimal amount = BigDecimal.valueOf(number).setScale(getPrecision(), RoundingMode.HALF_UP);
        String raw = amount.abs().toPlainString();
        String[] parts = raw.split("\\.", -1);
        String integer = parts[0];
        StringBuilder grouped = new StringBuilder();
        for (int index = 0; index < integer.length(); index++) {
            if (index > 0 && (integer.length() - index) % 3 == 0) grouped.append(getGroupSeparator());
            grouped.append(integer.charAt(index));
        }
        String sign = amount.signum() < 0 ? "-" : "";
        return sign + grouped + (getPrecision() == 0 ? "" : getDecimalSeparator() + parts[1]);
    }
}

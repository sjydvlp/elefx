package com.sjydvlp.elefx.component.rate;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * Element Plus inspired rating control.
 *
 * <p>
 * It supports hover preview, keyboard selection, half values, level-specific
 * colours and icons, read-only display, score/text output, and clear-on-repeat.
 * The {@code value} property is the JavaFX equivalent of Element Plus v-model.
 * </p>
 */
public class EleFXRate extends HBox implements Themable {

    private static final List<Paint> DEFAULT_COLORS = List.of(Color.web("#f7ba2a"), Color.web("#f7ba2a"),
            Color.web("#f7ba2a"));

    private static final List<String> DEFAULT_TEXTS = List.of("Extremely bad", "Disappointed", "Fair", "Satisfied",
            "Surprise");

    private final HBox iconsBox = new HBox();

    private final Label text = new Label();

    private final DoubleProperty value = new SimpleDoubleProperty(this, "value", 0);

    private final ReadOnlyDoubleWrapper currentValue = new ReadOnlyDoubleWrapper(this, "currentValue", 0);

    private final IntegerProperty max = new SimpleIntegerProperty(this, "max", 5);

    private final BooleanProperty allowHalf = new SimpleBooleanProperty(this, "allowHalf", false);

    private final IntegerProperty lowThreshold = new SimpleIntegerProperty(this, "lowThreshold", 2);

    private final IntegerProperty highThreshold = new SimpleIntegerProperty(this, "highThreshold", 4);

    private final ObservableList<Paint> colors = FXCollections.observableArrayList(DEFAULT_COLORS);

    private final ObservableList<EleFXIcon> icons = FXCollections.observableArrayList(
            new EleFXIcon(EleFXIconType.STAR_FILLED), new EleFXIcon(EleFXIconType.STAR_FILLED),
            new EleFXIcon(EleFXIconType.STAR_FILLED));

    private final ObservableMap<Integer, Paint> colorsByThreshold = FXCollections.observableHashMap();

    private final ObservableMap<Integer, EleFXIcon> iconsByThreshold = FXCollections.observableHashMap();

    private final ObjectProperty<EleFXIcon> voidIcon = new SimpleObjectProperty<>(this, "voidIcon",
            new EleFXIcon(EleFXIconType.STAR));

    private final ObjectProperty<EleFXIcon> disabledVoidIcon = new SimpleObjectProperty<>(this, "disabledVoidIcon",
            new EleFXIcon(EleFXIconType.STAR_FILLED));

    private final ObjectProperty<Paint> voidColor = new SimpleObjectProperty<>(this, "voidColor", Color.web("#c6d1de"));

    private final ObjectProperty<Paint> disabledVoidColor = new SimpleObjectProperty<>(this, "disabledVoidColor",
            Color.web("#eff2f7"));

    private final BooleanProperty showText = new SimpleBooleanProperty(this, "showText", false);

    private final BooleanProperty showScore = new SimpleBooleanProperty(this, "showScore", false);

    private final ObjectProperty<Paint> textColor = new SimpleObjectProperty<>(this, "textColor", Color.web("#303133"));

    private final ObservableList<String> texts = FXCollections.observableArrayList(DEFAULT_TEXTS);

    private final StringProperty scoreTemplate = new SimpleStringProperty(this, "scoreTemplate", "{value}");

    private final BooleanProperty clearable = new SimpleBooleanProperty(this, "clearable", false);

    private final ObjectProperty<EleFXRateSize> size = new SimpleObjectProperty<>(this, "size", EleFXRateSize.DEFAULT);

    private final StringProperty ariaLabel = new SimpleStringProperty(this, "ariaLabel", "Rate");

    private final ObjectProperty<EventHandler<EleFXRateEvent>> onChange = new SimpleObjectProperty<>(this, "onChange");

    private boolean updating;

    public EleFXRate() {
        getStyleClass().add("ele-rate");
        iconsBox.getStyleClass().add("ele-rate__icons");
        iconsBox.setAlignment(Pos.CENTER_LEFT);
        text.getStyleClass().add("ele-rate__text");
        getChildren().addAll(iconsBox, text);
        setAlignment(Pos.CENTER_LEFT);
        setFocusTraversable(true);
        setAccessibleRole(javafx.scene.AccessibleRole.SLIDER);
        setAccessibleText(getAriaLabel());

        // Keep interaction on the stable container. Hover rendering replaces icon
        // nodes, so attaching handlers to individual icons would lose a click
        // that follows a hover update.
        iconsBox.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            if (!isDisabled()) setCurrentValue(valueAt(mouseXInIcons(event)));
        });
        iconsBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (!isDisabled()) commit(valueAt(mouseXInIcons(event)));
        });

        value.addListener((o, oldValue, newValue) -> valueChanged(oldValue.doubleValue(), newValue.doubleValue()));
        max.addListener((o, oldValue, newValue) -> {
            validateMax(newValue.intValue());
            normalizeValue();
            rebuild();
        });
        lowThreshold.addListener((o, oldValue, newValue) -> validateThresholds());
        highThreshold.addListener((o, oldValue, newValue) -> validateThresholds());
        size.addListener((o, oldValue, newValue) -> rebuild());
        colors.addListener((javafx.collections.ListChangeListener<Paint>) c -> rebuild());
        icons.addListener((javafx.collections.ListChangeListener<EleFXIcon>) c -> rebuild());
        colorsByThreshold.addListener((javafx.collections.MapChangeListener<Integer, Paint>) c -> rebuild());
        iconsByThreshold.addListener((javafx.collections.MapChangeListener<Integer, EleFXIcon>) c -> rebuild());
        voidIcon.addListener((o, a, b) -> rebuild());
        disabledVoidIcon.addListener((o, a, b) -> rebuild());
        voidColor.addListener((o, a, b) -> rebuild());
        disabledVoidColor.addListener((o, a, b) -> rebuild());
        showText.addListener((o, a, b) -> updateText());
        showScore.addListener((o, a, b) -> updateText());
        textColor.addListener((o, a, b) -> updateText());
        texts.addListener((javafx.collections.ListChangeListener<String>) c -> updateText());
        scoreTemplate.addListener((o, a, b) -> updateText());
        ariaLabel.addListener((o, a, b) -> setAccessibleText(b));
        disableProperty().addListener((o, a, b) -> rebuild());
        setOnKeyPressed(this::handleKeyPressed);
        setOnMouseExited(e -> {
            // Ignore enter/exit events bubbled by replaced icon cells.
            if (e.getTarget() == this) resetCurrentValue();
        });
        rebuild();
        sceneBuilderIntegration();
    }

    public EleFXRate(double value) {
        this();
        setValue(value);
    }

    public double getValue() {
        return value.get();
    }

    public void setValue(double value) {
        this.value.set(clamp(value));
    }

    public DoubleProperty valueProperty() {
        return value;
    }

    public double getCurrentValue() {
        return currentValue.get();
    }

    public ReadOnlyDoubleProperty currentValueProperty() {
        return currentValue.getReadOnlyProperty();
    }

    /** Sets the hover/preview value without committing it. */
    public void setCurrentValue(double value) {
        double normalized = clamp(value);
        if (Double.compare(currentValue.get(), normalized) == 0) return;
        currentValue.set(normalized);
        updateIcons();
    }

    /** Restores preview to the committed {@link #getValue() value}. */
    public void resetCurrentValue() {
        if (Double.compare(currentValue.get(), getValue()) == 0) return;
        currentValue.set(getValue());
        updateIcons();
    }

    public int getMax() {
        return max.get();
    }

    public void setMax(int value) {
        validateMax(value);
        if (value < getHighThreshold()) {
            throw new IllegalArgumentException("max must not be less than highThreshold");
        }
        max.set(value);
    }

    public IntegerProperty maxProperty() {
        return max;
    }

    public boolean isAllowHalf() {
        return allowHalf.get();
    }

    public void setAllowHalf(boolean value) {
        allowHalf.set(value);
    }

    public BooleanProperty allowHalfProperty() {
        return allowHalf;
    }

    public int getLowThreshold() {
        return lowThreshold.get();
    }

    public void setLowThreshold(int value) {
        lowThreshold.set(value);
        validateThresholds();
    }

    public IntegerProperty lowThresholdProperty() {
        return lowThreshold;
    }

    public int getHighThreshold() {
        return highThreshold.get();
    }

    public void setHighThreshold(int value) {
        highThreshold.set(value);
        validateThresholds();
    }

    public IntegerProperty highThresholdProperty() {
        return highThreshold;
    }

    public ObservableList<Paint> getColors() {
        return colors;
    }

    public void setColors(List<? extends Paint> values) {
        setThree(colors, values, "colors");
    }

    /**
     * Configures colours by inclusive score threshold, for example
     * {@code Map.of(2, gray, 4, yellow, 5, orange)}. This mirrors Element
     * Plus's object-form {@code colors} attribute.
     */
    public void setColors(Map<Integer, ? extends Paint> values) {
        setThresholds(colorsByThreshold, values, "colors");
    }

    public ObservableMap<Integer, Paint> getColorsByThreshold() {
        return colorsByThreshold;
    }

    /** Returns the selected icon templates. */
    public ObservableList<EleFXIcon> getIcons() {
        return icons;
    }

    /**
     * Sets selected icon templates. One icon is reused for every score; three
     * icons use Element Plus's low/medium/high levels; a list whose size equals
     * max assigns one icon to each star. The supplied nodes are templates and
     * are not added to this control, so they remain safe to reuse elsewhere.
     */
    public void setIcons(List<? extends EleFXIcon> values) {
        if (values == null || values.isEmpty() || values.stream().anyMatch(value -> value == null)) {
            throw new IllegalArgumentException("icons must contain non-null values");
        }
        if (values.size() != 1 && values.size() != 3 && values.size() != getMax()) {
            throw new IllegalArgumentException("icons must contain 1, 3, or max values");
        }
        icons.setAll(values);
    }

    /** Convenience overload for a single selected icon template. */
    public void setIcons(EleFXIcon value) {
        setIcons(List.of(value));
    }

    /** Configures selected icons by inclusive score threshold. */
    public void setIcons(Map<Integer, EleFXIcon> values) {
        setThresholds(iconsByThreshold, values, "icons");
    }

    public ObservableMap<Integer, EleFXIcon> getIconsByThreshold() {
        return iconsByThreshold;
    }

    public EleFXIcon getVoidIcon() {
        return voidIcon.get();
    }

    /** Sets the void-icon template. */
    public void setVoidIcon(EleFXIcon value) {
        voidIcon.set(value == null ? new EleFXIcon(EleFXIconType.STAR) : value);
    }

    public ObjectProperty<EleFXIcon> voidIconProperty() {
        return voidIcon;
    }

    public EleFXIcon getDisabledVoidIcon() {
        return disabledVoidIcon.get();
    }

    /** Sets the disabled void-icon template. */
    public void setDisabledVoidIcon(EleFXIcon value) {
        disabledVoidIcon.set(value == null ? new EleFXIcon(EleFXIconType.STAR_FILLED) : value);
    }

    public ObjectProperty<EleFXIcon> disabledVoidIconProperty() {
        return disabledVoidIcon;
    }

    public Paint getVoidColor() {
        return voidColor.get();
    }

    public void setVoidColor(Paint value) {
        voidColor.set(value == null ? Color.web("#c6d1de") : value);
    }

    public ObjectProperty<Paint> voidColorProperty() {
        return voidColor;
    }

    public Paint getDisabledVoidColor() {
        return disabledVoidColor.get();
    }

    public void setDisabledVoidColor(Paint value) {
        disabledVoidColor.set(value == null ? Color.web("#eff2f7") : value);
    }

    public ObjectProperty<Paint> disabledVoidColorProperty() {
        return disabledVoidColor;
    }

    public boolean isShowText() {
        return showText.get();
    }

    public void setShowText(boolean value) {
        showText.set(value);
        if (value) showScore.set(false);
    }

    public BooleanProperty showTextProperty() {
        return showText;
    }

    public boolean isShowScore() {
        return showScore.get();
    }

    public void setShowScore(boolean value) {
        showScore.set(value);
        if (value) showText.set(false);
    }

    public BooleanProperty showScoreProperty() {
        return showScore;
    }

    public Paint getTextColor() {
        return textColor.get();
    }

    public void setTextColor(Paint value) {
        textColor.set(value == null ? Color.web("#303133") : value);
    }

    public ObjectProperty<Paint> textColorProperty() {
        return textColor;
    }

    public ObservableList<String> getTexts() {
        return texts;
    }

    public void setTexts(List<String> values) {
        texts.setAll(values == null ? DEFAULT_TEXTS : values);
        updateText();
    }

    public String getScoreTemplate() {
        return scoreTemplate.get();
    }

    public void setScoreTemplate(String value) {
        scoreTemplate.set(value == null ? "{value}" : value);
    }

    public StringProperty scoreTemplateProperty() {
        return scoreTemplate;
    }

    public boolean isClearable() {
        return clearable.get();
    }

    public void setClearable(boolean value) {
        clearable.set(value);
    }

    public BooleanProperty clearableProperty() {
        return clearable;
    }

    public EleFXRateSize getSize() {
        return size.get();
    }

    public void setSize(EleFXRateSize value) {
        size.set(value == null ? EleFXRateSize.DEFAULT : value);
    }

    public ObjectProperty<EleFXRateSize> sizeProperty() {
        return size;
    }

    public String getAriaLabel() {
        return ariaLabel.get();
    }

    public void setAriaLabel(String value) {
        ariaLabel.set(value == null ? "Rate" : value);
    }

    public StringProperty ariaLabelProperty() {
        return ariaLabel;
    }

    /** Deprecated Element Plus-compatible alias for {@link #setAriaLabel(String)}. */
    @Deprecated
    public String getLabel() {
        return getAriaLabel();
    }

    @Deprecated
    public void setLabel(String value) {
        setAriaLabel(value);
    }

    @Deprecated
    public StringProperty labelProperty() {
        return ariaLabelProperty();
    }

    public EventHandler<EleFXRateEvent> getOnChange() {
        return onChange.get();
    }

    public void setOnChange(EventHandler<EleFXRateEvent> value) {
        onChange.set(value);
    }

    public ObjectProperty<EventHandler<EleFXRateEvent>> onChangeProperty() {
        return onChange;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.RATE;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void rebuild() {
        getStyleClass().removeIf(style -> style.startsWith("ele-rate--size-"));
        getStyleClass().add(getSize().styleClass());
        iconsBox.getChildren().clear();
        double displayValue = currentValue.get();
        for (int i = 1; i <= getMax(); i++)
            iconsBox.getChildren().add(createIcon(i, displayValue));
        updateText();
    }

    private Node createIcon(int index, double displayValue) {
        StackPane cell = new StackPane();
        cell.getStyleClass().add("ele-rate__item");
        EleFXIcon base = icon(EleFXIconType.STAR, getVoidColor());
        EleFXIcon overlay = icon(iconFor(index, displayValue), levelColor(displayValue));
        overlay.setClip(new Rectangle());
        cell.getChildren().addAll(base, overlay);
        updateIcon(cell, index, displayValue);
        return cell;
    }

    /** Updates icon paint, type and half-star clip without replacing scene nodes. */
    private void updateIcons() {
        for (int i = 0; i < iconsBox.getChildren().size(); i++) {
            Node node = iconsBox.getChildren().get(i);
            if (node instanceof StackPane cell) updateIcon(cell, i + 1, currentValue.get());
        }
    }

    private void updateIcon(StackPane cell, int index, double displayValue) {
        EleFXIcon base = (EleFXIcon) cell.getChildren().get(0);
        EleFXIcon overlay = (EleFXIcon) cell.getChildren().get(1);
        double fraction = Math.max(0, Math.min(1, displayValue - index + 1));
        boolean selected = fraction > 0;
        base.setType(selected
                ? iconFor(index, displayValue)
                : (isDisabled() ? getDisabledVoidIcon().getType() : getVoidIcon().getType()));
        base.setFill(selected ? levelColor(displayValue) : (isDisabled() ? getDisabledVoidColor() : getVoidColor()));
        overlay.setType(iconFor(index, displayValue));
        overlay.setFill(levelColor(displayValue));
        overlay.setVisible(selected && fraction < 1);
        if (overlay.isVisible()) {
            Rectangle clip = (Rectangle) overlay.getClip();
            clip.setWidth(getSize().iconSize() * fraction);
            clip.setHeight(getSize().iconSize());
            base.setFill(isDisabled() ? getDisabledVoidColor() : getVoidColor());
        }
    }

    private EleFXIcon icon(EleFXIconType type, Paint fill) {
        EleFXIcon icon = new EleFXIcon(type, getSize().iconSize());
        icon.setFill(fill);
        icon.setMouseTransparent(true);
        return icon;
    }

    private double valueFor(int index, double x, double width) {
        return isAllowHalf() && x < width / 2 ? index - .5 : index;
    }

    private double valueAt(double x) {
        for (int i = 0; i < iconsBox.getChildren().size(); i++) {
            Node item = iconsBox.getChildren().get(i);
            if (x >= item.getLayoutX() && x <= item.getLayoutX() + item.getBoundsInParent().getWidth()) {
                return valueFor(i + 1, x - item.getLayoutX(), item.getBoundsInParent().getWidth());
            }
        }
        return getValue();
    }

    private double mouseXInIcons(MouseEvent event) {
        return iconsBox.sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
    }

    private void commit(double newValue) {
        requestFocus();
        if (isClearable() && Double.compare(getValue(), newValue) == 0) newValue = 0;
        setValue(newValue);
        resetCurrentValue();
    }

    private void handleKeyPressed(KeyEvent event) {
        if (isDisabled()) return;
        double increment = isAllowHalf() ? .5 : 1;
        if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.UP) {
            setValue(getValue() + increment);
            event.consume();
        } else if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.DOWN) {
            setValue(getValue() - increment);
            event.consume();
        } else if (event.getCode() == KeyCode.HOME) {
            setValue(0);
            event.consume();
        } else if (event.getCode() == KeyCode.END) {
            setValue(getMax());
            event.consume();
        }
        resetCurrentValue();
    }

    private void valueChanged(double oldValue, double newValue) {
        if (updating) return;
        double normalized = clamp(newValue);
        if (Double.compare(newValue, normalized) != 0) {
            updating = true;
            value.set(normalized);
            updating = false;
            return;
        }
        currentValue.set(newValue);
        updateIcons();
        updateText();
        if (Double.compare(oldValue, newValue) != 0) {
            EleFXRateEvent event = new EleFXRateEvent(this, this, oldValue, newValue);
            EventHandler<EleFXRateEvent> handler = getOnChange();
            if (handler != null) handler.handle(event);
            fireEvent(event);
        }
    }

    private void normalizeValue() {
        setValue(getValue());
    }

    private double clamp(double candidate) {
        return !Double.isFinite(candidate) ? 0 : Math.max(0, Math.min(getMax(), candidate));
    }

    private Paint levelColor(double rating) {
        return resolveThreshold(colorsByThreshold, rating, colors.get(levelIndex(rating)));
    }

    private EleFXIconType iconFor(int starIndex, double rating) {
        EleFXIcon thresholdIcon = resolveThreshold(iconsByThreshold, rating, null);
        if (thresholdIcon != null) return thresholdIcon.getType();
        if (icons.size() == 1) return icons.get(0).getType();
        if (icons.size() == getMax()) return icons.get(starIndex - 1).getType();
        return icons.get(levelIndex(rating)).getType();
    }

    private int levelIndex(double rating) {
        return rating <= getLowThreshold() ? 0 : rating < getHighThreshold() ? 1 : 2;
    }

    private void updateText() {
        boolean visible = isShowText() || isShowScore();
        text.setVisible(visible);
        text.setManaged(visible);
        text.setTextFill(getTextColor());
        if (!visible) return;
        if (isShowText()) {
            int index = (int) Math.ceil(getValue()) - 1;
            text.setText(index >= 0 && index < texts.size() ? texts.get(index) : "");
        } else
            text.setText(getScoreTemplate().replace("{value}", new DecimalFormat("0.##").format(getValue())));
    }

    private void validateMax(int value) {
        if (value < 1) throw new IllegalArgumentException("max must be at least 1");
    }

    private void validateThresholds() {
        if (getLowThreshold() < 0 || getHighThreshold() < getLowThreshold() || getHighThreshold() > getMax())
            throw new IllegalArgumentException("thresholds must satisfy 0 <= low <= high <= max");
    }

    private <T> void setThree(ObservableList<T> target, List<? extends T> values, String name) {
        if (values == null || values.size() != 3 || values.stream().anyMatch(v -> v == null))
            throw new IllegalArgumentException(name + " must contain exactly three non-null values");
        target.setAll(values);
    }

    private <T> void setThresholds(ObservableMap<Integer, T> target, Map<Integer, ? extends T> values, String name) {
        if (values == null || values.isEmpty() || values.entrySet().stream()
                .anyMatch(entry -> entry.getKey() == null || entry.getKey() < 1 || entry.getValue() == null)) {
            throw new IllegalArgumentException(
                    name + " threshold map must contain positive, non-null thresholds and values");
        }
        target.clear();
        target.putAll(values);
    }

    private <T> T resolveThreshold(Map<Integer, T> values, double rating, T fallback) {
        return values.entrySet().stream().filter(entry -> rating <= entry.getKey())
                .min(Map.Entry.comparingByKey()).map(Map.Entry::getValue).orElse(fallback);
    }
}

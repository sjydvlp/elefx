package com.sjydvlp.elefx.component.colorpicker;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Standalone Element Plus inspired colour selection panel.
 *
 * <p>
 * The panel exposes both a JavaFX {@link Color} and a bindable text value.
 * Its value accepts CSS hexadecimal, rgb(), and rgba() strings. The hue and
 * saturation/value areas are canvases so this control remains a compact,
 * reusable panel instead of depending on a native popup colour picker.
 * </p>
 */
public class EleFXColorPickerPanel extends VBox implements Themable {

    private static final String STYLE_CLASS = "ele-color-picker-panel";

    private static final String BORDERLESS_STYLE_CLASS = "ele-color-picker-panel--borderless";

    private static final double PALETTE_WIDTH = 280;

    private static final double PALETTE_HEIGHT = 180;

    private static final double HUE_WIDTH = 12;

    private static final double SLIDER_HEIGHT = 12;

    private static final double PANEL_WIDTH = PALETTE_WIDTH + HUE_WIDTH + 8 + 24;

    private static final double DEFAULT_OPACITY = 0.8;

    private final Canvas saturationValueCanvas = new Canvas(PALETTE_WIDTH, PALETTE_HEIGHT);

    private final Canvas hueCanvas = new Canvas(HUE_WIDTH, PALETTE_HEIGHT);

    private final Canvas alphaCanvas = new Canvas(PALETTE_WIDTH, SLIDER_HEIGHT);

    private final TextField valueInput = new TextField();

    private final HBox alphaRow = new HBox(8);

    private final FlowPane predefinedColorsPane = new FlowPane(8, 8);

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color",
            Color.web("#409EFF", DEFAULT_OPACITY));

    private final StringProperty value = new SimpleStringProperty(this, "value", "#409EFF");

    private final BooleanProperty showAlpha = new SimpleBooleanProperty(this, "showAlpha", false);

    private final BooleanProperty bordered = new SimpleBooleanProperty(this, "bordered", true);

    private final ObjectProperty<EleFXColorFormat> colorFormat = new SimpleObjectProperty<>(this, "colorFormat",
            EleFXColorFormat.HEX);

    private final ObservableList<Color> predefinedColors = FXCollections.observableArrayList();

    private final ObjectProperty<EventHandler<ActionEvent>> onChange = new SimpleObjectProperty<>(this, "onChange");

    private double hue = 211;

    private double saturation = 75;

    private double brightness = 100;

    private boolean updating;

    public EleFXColorPickerPanel() {
        initialize();
    }

    public EleFXColorPickerPanel(Color color) {
        this();
        setColor(applyDefaultOpacity(color));
    }

    public Color getColor() {
        return color.get();
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public void setColor(Color color) {
        this.color.set(color == null ? Color.TRANSPARENT : color);
    }

    /** Bindable Element-style model value. Invalid externally supplied text is ignored. */
    public String getValue() {
        return value.get();
    }

    public StringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value == null ? "" : value);
    }

    public boolean isShowAlpha() {
        return showAlpha.get();
    }

    public BooleanProperty showAlphaProperty() {
        return showAlpha;
    }

    public void setShowAlpha(boolean showAlpha) {
        this.showAlpha.set(showAlpha);
    }

    public boolean isBordered() {
        return bordered.get();
    }

    public BooleanProperty borderedProperty() {
        return bordered;
    }

    public void setBordered(boolean bordered) {
        this.bordered.set(bordered);
    }

    /**
     * Element Plus-compatible name for {@link #isBordered()}.
     *
     * @return whether the panel draws its outer border
     */
    public boolean isBorder() {
        return isBordered();
    }

    /**
     * Element Plus-compatible setter. For an observable boolean use
     * {@link #borderedProperty()}, since JavaFX Region already reserves the
     * final {@code borderProperty()} name for its CSS Border object.
     */
    public void setBorder(boolean border) {
        setBordered(border);
    }

    public EleFXColorFormat getColorFormat() {
        return colorFormat.get();
    }

    public ObjectProperty<EleFXColorFormat> colorFormatProperty() {
        return colorFormat;
    }

    public void setColorFormat(EleFXColorFormat format) {
        colorFormat.set(format == null ? EleFXColorFormat.HEX : format);
    }

    /** Mutable list of swatches shown below the editor. */
    public ObservableList<Color> getPredefinedColors() {
        return predefinedColors;
    }

    public EventHandler<ActionEvent> getOnChange() {
        return onChange.get();
    }

    public ObjectProperty<EventHandler<ActionEvent>> onChangeProperty() {
        return onChange;
    }

    public void setOnChange(EventHandler<ActionEvent> handler) {
        onChange.set(handler);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.COLOR_PICKER_PANEL;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setSpacing(0);
        setPadding(new Insets(12));
        setPrefWidth(PANEL_WIDTH);
        setMaxWidth(PANEL_WIDTH);

        saturationValueCanvas.getStyleClass().add("ele-color-picker-panel__palette");
        hueCanvas.getStyleClass().add("ele-color-picker-panel__slider");
        alphaCanvas.getStyleClass().add("ele-color-picker-panel__slider");
        alphaCanvas.getStyleClass().add("ele-color-picker-panel__alpha-slider");
        installCanvasHandler(saturationValueCanvas, this::selectSaturationBrightness);
        installCanvasHandler(hueCanvas, this::selectHue);
        installCanvasHandler(alphaCanvas, this::selectAlpha);

        valueInput.getStyleClass().add("ele-color-picker-panel__input");
        valueInput.setPrefWidth(160);
        valueInput.setMaxWidth(160);
        valueInput.setOnAction(event -> applyInputValue());
        valueInput.focusedProperty().addListener((observable, oldValue, focused) -> {
            if (!focused) applyInputValue();
        });
        HBox valueRow = new HBox(valueInput);
        valueRow.getStyleClass().add("ele-color-picker-panel__footer");
        valueRow.setAlignment(Pos.CENTER_LEFT);

        alphaRow.getStyleClass().add("ele-color-picker-panel__alpha-row");
        alphaRow.getChildren().add(alphaCanvas);

        // Element Plus declares the hue slider first but floats it right; in
        // JavaFX an HBox expresses that visual order directly.
        HBox wrapper = new HBox(8, saturationValueCanvas, hueCanvas);
        wrapper.getStyleClass().add("ele-color-picker-panel__wrapper");
        predefinedColorsPane.getStyleClass().add("ele-color-picker-panel__predefine");
        getChildren().addAll(wrapper, alphaRow, predefinedColorsPane, valueRow);
        VBox.setMargin(wrapper, new Insets(0, 0, 6, 0));
        VBox.setMargin(predefinedColorsPane, new Insets(8, 0, 0, 0));
        VBox.setMargin(valueRow, new Insets(12, 0, 0, 0));

        color.addListener((observable, oldValue, newValue) -> syncFromColor(newValue, true));
        value.addListener((observable, oldValue, newValue) -> syncFromValue(newValue));
        showAlpha.addListener((observable, oldValue, newValue) -> updateAlphaVisibility());
        bordered.addListener((observable, oldValue, newValue) -> updateBorderStyle());
        colorFormat.addListener((observable, oldValue, newValue) -> syncValueFromColor());
        predefinedColors
                .addListener((javafx.collections.ListChangeListener<Color>) change -> rebuildPredefinedColors());
        disableProperty().addListener((observable, oldValue, newValue) -> rebuildPredefinedColors());
        updateBorderStyle();
        updateAlphaVisibility();
        syncFromColor(getColor(), false);
        sceneBuilderIntegration();
    }

    private void installCanvasHandler(Canvas canvas, java.util.function.Consumer<MouseEvent> selection) {
        canvas.setOnMousePressed(event -> selection.accept(event));
        canvas.setOnMouseDragged(event -> selection.accept(event));
    }

    private void selectSaturationBrightness(MouseEvent event) {
        if (isDisable()) return;
        saturation = clamp(event.getX() / saturationValueCanvas.getWidth() * 100, 0, 100);
        brightness = clamp((1 - event.getY() / saturationValueCanvas.getHeight()) * 100, 0, 100);
        commit(Color.hsb(hue, saturation / 100, brightness / 100, getColor().getOpacity()));
    }

    private void selectHue(MouseEvent event) {
        if (isDisable()) return;
        hue = clamp(event.getY() / hueCanvas.getHeight() * 360, 0, 360);
        commit(Color.hsb(hue, saturation / 100, brightness / 100, getColor().getOpacity()));
    }

    private void selectAlpha(MouseEvent event) {
        if (isDisable() || !isShowAlpha()) return;
        commit(Color.hsb(hue, saturation / 100, brightness / 100,
                clamp(event.getX() / alphaCanvas.getWidth(), 0, 1)));
    }

    private void applyInputValue() {
        if (isDisable()) return;
        Color parsed = parseColor(valueInput.getText());
        if (parsed == null) {
            valueInput.setText(format(getColor()));
            return;
        }
        commit(parsed);
    }

    private void syncFromValue(String candidate) {
        if (updating) return;
        Color parsed = parseColor(candidate);
        if (parsed == null) {
            syncValueFromColor();
            return;
        }
        updating = true;
        try {
            color.set(parsed);
        } finally {
            updating = false;
        }
        syncFromColor(parsed, false);
    }

    private void commit(Color selected) {
        Color old = getColor();
        setColor(selected);
        if (!old.equals(getColor())) fireChange();
    }

    private void syncFromColor(Color selected, boolean updateValue) {
        Color safe = selected == null ? Color.TRANSPARENT : selected;
        if (safe.getSaturation() > 0.0001) hue = safe.getHue();
        saturation = safe.getSaturation() * 100;
        brightness = safe.getBrightness() * 100;
        repaint();
        if (updateValue)
            syncValueFromColor();
        else
            valueInput.setText(format(safe));
    }

    private void syncValueFromColor() {
        String formatted = format(getColor());
        updating = true;
        try {
            value.set(formatted);
            valueInput.setText(formatted);
        } finally {
            updating = false;
        }
    }

    private void repaint() {
        paintPalette();
        paintHue();
        paintAlpha();
    }

    private void paintPalette() {
        GraphicsContext graphics = saturationValueCanvas.getGraphicsContext2D();
        double width = saturationValueCanvas.getWidth(), height = saturationValueCanvas.getHeight();
        graphics.setFill(Color.hsb(hue, 1, 1));
        graphics.fillRect(0, 0, width, height);
        graphics.setFill(new javafx.scene.paint.LinearGradient(0, 0, 1, 0, true,
                javafx.scene.paint.CycleMethod.NO_CYCLE, new javafx.scene.paint.Stop(0, Color.WHITE),
                new javafx.scene.paint.Stop(1, Color.TRANSPARENT)));
        graphics.fillRect(0, 0, width, height);
        graphics.setFill(new javafx.scene.paint.LinearGradient(0, 0, 0, 1, true,
                javafx.scene.paint.CycleMethod.NO_CYCLE, new javafx.scene.paint.Stop(0, Color.TRANSPARENT),
                new javafx.scene.paint.Stop(1, Color.BLACK)));
        graphics.fillRect(0, 0, width, height);
        drawHandle(graphics, saturation / 100 * width, (1 - brightness / 100) * height);
    }

    private void paintHue() {
        GraphicsContext graphics = hueCanvas.getGraphicsContext2D();
        double width = hueCanvas.getWidth(), height = hueCanvas.getHeight();
        for (int y = 0; y < height; y++) {
            graphics.setStroke(Color.hsb(y / height * 360, 1, 1));
            graphics.strokeLine(0, y, width, y);
        }
        drawHorizontalHandle(graphics, hue / 360 * height, width);
    }

    private void paintAlpha() {
        GraphicsContext graphics = alphaCanvas.getGraphicsContext2D();
        double width = alphaCanvas.getWidth(), height = alphaCanvas.getHeight();
        for (int y = 0; y < height; y += 4)
            for (int x = 0; x < width; x += 4) {
                graphics.setFill(((x + y) / 4) % 2 == 0 ? Color.WHITE : Color.rgb(220, 223, 230));
                graphics.fillRect(x, y, 4, 4);
            }
        Color base = Color.hsb(hue, saturation / 100, brightness / 100);
        graphics.setFill(new javafx.scene.paint.LinearGradient(0, 0, 1, 0, true,
                javafx.scene.paint.CycleMethod.NO_CYCLE, new javafx.scene.paint.Stop(0, Color.TRANSPARENT),
                new javafx.scene.paint.Stop(1, base)));
        graphics.fillRect(0, 0, width, height);
        drawVerticalHandle(graphics, getColor().getOpacity() * width, height);
    }

    private void drawHandle(GraphicsContext graphics, double x, double y) {
        graphics.setStroke(Color.WHITE);
        graphics.setLineWidth(2);
        graphics.strokeOval(x - 5, y - 5, 10, 10);
        graphics.setStroke(Color.rgb(48, 49, 51));
        graphics.setLineWidth(1);
        graphics.strokeOval(x - 6, y - 6, 12, 12);
    }

    private void drawVerticalHandle(GraphicsContext graphics, double x, double height) {
        graphics.setStroke(Color.WHITE);
        graphics.setLineWidth(2);
        graphics.strokeLine(x, 0, x, height);
        graphics.setStroke(Color.rgb(48, 49, 51));
        graphics.setLineWidth(1);
        graphics.strokeLine(x - 1.5, 0, x - 1.5, height);
        graphics.strokeLine(x + 1.5, 0, x + 1.5, height);
    }

    private void drawHorizontalHandle(GraphicsContext graphics, double y, double width) {
        graphics.setStroke(Color.WHITE);
        graphics.setLineWidth(2);
        graphics.strokeLine(0, y, width, y);
        graphics.setStroke(Color.rgb(48, 49, 51));
        graphics.setLineWidth(1);
        graphics.strokeLine(0, y - 1.5, width, y - 1.5);
        graphics.strokeLine(0, y + 1.5, width, y + 1.5);
    }

    private void updateAlphaVisibility() {
        alphaRow.setVisible(isShowAlpha());
        alphaRow.setManaged(isShowAlpha());
        repaint();
        // Enabling alpha changes the Element Plus default model presentation
        // from Hex to rgba(), even before the user changes the colour.
        syncValueFromColor();
    }

    private void updateBorderStyle() {
        if (isBordered())
            getStyleClass().remove(BORDERLESS_STYLE_CLASS);
        else if (!getStyleClass().contains(BORDERLESS_STYLE_CLASS)) getStyleClass().add(BORDERLESS_STYLE_CLASS);
    }

    private void rebuildPredefinedColors() {
        predefinedColorsPane.getChildren().clear();
        for (Color predefined : predefinedColors) {
            Button swatch = new Button();
            swatch.getStyleClass().add("ele-color-picker-panel__swatch");
            swatch.setStyle("-fx-background-color: " + toCss(predefined) + ";");
            swatch.setDisable(isDisable());
            swatch.setOnAction(event -> commit(predefined));
            predefinedColorsPane.getChildren().add(swatch);
        }
        predefinedColorsPane.setVisible(!predefinedColors.isEmpty());
        predefinedColorsPane.setManaged(!predefinedColors.isEmpty());
    }

    private void fireChange() {
        ActionEvent event = new ActionEvent(this, this);
        fireEvent(event);
        if (getOnChange() != null) getOnChange().handle(event);
    }

    private String format(Color value) {
        int red = (int) Math.round(value.getRed() * 255), green = (int) Math.round(value.getGreen() * 255),
                blue = (int) Math.round(value.getBlue() * 255);
        return switch (getColorFormat()) {
            case RGB -> String.format(Locale.ROOT, "rgb(%d, %d, %d)", red, green, blue);
            case RGBA -> String.format(Locale.ROOT, "rgba(%d, %d, %d, %.2f)", red, green, blue, value.getOpacity());
            // Element Plus represents its default model as rgba() whenever
            // alpha selection is enabled, rather than appending alpha to Hex.
            case HEX -> isShowAlpha()
                    ? String.format(Locale.ROOT, "rgba(%d, %d, %d, %.2f)", red, green, blue, value.getOpacity())
                    : String.format(Locale.ROOT, "#%02X%02X%02X", red, green, blue);
        };
    }

    private static String toCss(Color color) {
        return String.format(Locale.ROOT, "rgba(%d,%d,%d,%.4f)",
                (int) Math.round(color.getRed() * 255), (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255), color.getOpacity());
    }

    private static Color parseColor(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Color.web(value.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Color applyDefaultOpacity(Color color) {
        if (color == null || color.getOpacity() < 1) return color;
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), DEFAULT_OPACITY);
    }
}

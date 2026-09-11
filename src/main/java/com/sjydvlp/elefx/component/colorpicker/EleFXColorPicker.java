package com.sjydvlp.elefx.component.colorpicker;

import com.sjydvlp.elefx.component.colorpickerpanel.EleFXColorPickerPanel;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.application.Platform;
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
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PopupControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.Locale;

/** Element Plus inspired colour picker with a compact trigger and popup editor. */
public class EleFXColorPicker extends HBox implements Themable {

    private static final String STYLE_CLASS = "ele-color-picker";

    private final Button trigger = new Button();

    private final Region preview = new Region();

    private final EleFXIcon indicator = new EleFXIcon(EleFXIconType.CLOSE, 12);

    private final Button popupClearButton = new Button("Clear");

    private final Button popupOkButton = new Button("OK");

    private final EleFXColorPickerPanel panel = new EleFXColorPickerPanel();

    private final PopupControl popup = new PopupControl();

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color");

    private final StringProperty value = new SimpleStringProperty(this, "value", "");

    private final BooleanProperty showAlpha = new SimpleBooleanProperty(this, "showAlpha", false);

    private final BooleanProperty clearable = new SimpleBooleanProperty(this, "clearable", true);

    private final ObjectProperty<EleFXColorFormat> colorFormat = new SimpleObjectProperty<>(this, "colorFormat",
            EleFXColorFormat.HEX);

    private final ObservableList<Color> predefinedColors = FXCollections.observableArrayList();

    private final ObjectProperty<EventHandler<ActionEvent>> onChange = new SimpleObjectProperty<>(this, "onChange");

    private boolean updating;

    private Color pendingColor;

    private String pendingValue;

    public EleFXColorPicker() {
        initialize();
    }

    public EleFXColorPicker(Color color) {
        this();
        setColor(color);
    }

    public Color getColor() {
        return color.get();
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

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

    public boolean isClearable() {
        return clearable.get();
    }

    public BooleanProperty clearableProperty() {
        return clearable;
    }

    public void setClearable(boolean clearable) {
        this.clearable.set(clearable);
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

    public void show() {
        if (isDisable() || popup.isShowing()) return;
        pendingColor = getColor();
        pendingValue = getValue();
        if (pendingColor != null) panel.setColor(pendingColor);
        Bounds bounds = trigger.localToScreen(trigger.getBoundsInLocal());
        if (bounds != null) popup.show(trigger, bounds.getMinX(), bounds.getMaxY() + 4);
    }

    public void hide() {
        popup.hide();
        clearTriggerFocus();
    }

    public void clear() {
        boolean changed = getColor() != null;
        if (changed) setColor(null);
        hide();
        if (changed) fireChange();
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.COLOR_PICKER;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        trigger.getStyleClass().add("ele-color-picker__trigger");
        preview.getStyleClass().add("ele-color-picker__preview");
        indicator.getStyleClass().add("ele-color-picker__indicator");
        indicator.setMouseTransparent(true);
        StackPane triggerContent = new StackPane(preview, indicator);
        triggerContent.getStyleClass().add("ele-color-picker__trigger-content");
        trigger.setGraphic(triggerContent);
        trigger.setOnAction(event -> {
            if (popup.isShowing())
                hide();
            else
                show();
        });
        getChildren().add(trigger);

        popupClearButton.getStyleClass().add("ele-color-picker__action");
        popupOkButton.getStyleClass().addAll("ele-color-picker__action", "ele-color-picker__action--primary");
        popupClearButton.setOnAction(event -> clear());
        popupOkButton.setOnAction(event -> confirmSelection());
        Region footerSpacer = new Region();
        footerSpacer.getStyleClass().add("ele-color-picker__footer-spacer");
        HBox.setHgrow(footerSpacer, javafx.scene.layout.Priority.ALWAYS);
        panel.getFooter().setSpacing(8);
        panel.getFooter().getChildren().addAll(footerSpacer, popupClearButton, popupOkButton);
        panel.setBordered(false);
        StackPane popupRoot = new StackPane(panel);
        popupRoot.getStyleClass().add("ele-color-picker__popup");
        popupRoot.getStylesheets().add(EleFXThemes.COLOR_PICKER.toData());
        popupRoot.getStylesheets().add(EleFXThemes.COLOR_PICKER_PANEL.toData());
        popup.getScene().setRoot(popupRoot);
        popup.setAutoHide(true);
        popup.setOnAutoHide(event -> {
            updatePreview();
            clearTriggerFocus();
        });

        panel.setOnChange(event -> updatePendingSelection());
        panel.disableProperty().bind(disableProperty());
        color.addListener((observable, oldValue, newValue) -> syncFromColor(newValue));
        value.addListener((observable, oldValue, newValue) -> syncFromValue(newValue));
        showAlpha.addListener((observable, oldValue, newValue) -> {
            panel.setShowAlpha(newValue);
            syncFromColor(getColor());
        });
        clearable.addListener((observable, oldValue, newValue) -> updatePreview());
        colorFormat.addListener((observable, oldValue, newValue) -> {
            panel.setColorFormat(newValue);
            syncFromColor(getColor());
        });
        predefinedColors.addListener((javafx.collections.ListChangeListener<Color>) change -> panel
                .getPredefinedColors().setAll(predefinedColors));
        updatePreview();
        sceneBuilderIntegration();
    }

    private void syncFromColor(Color selected) {
        if (updating) return;
        updating = true;
        try {
            if (selected == null)
                value.set("");
            else {
                panel.setColor(selected);
                value.set(panel.getValue());
            }
            updatePreview();
        } finally {
            updating = false;
        }
    }

    private void syncFromValue(String candidate) {
        if (updating) return;
        if (candidate == null || candidate.isBlank()) {
            updating = true;
            try {
                color.set(null);
                updatePreview();
            } finally {
                updating = false;
            }
            return;
        }
        Color parsed = parseColor(candidate);
        if (parsed == null)
            syncFromColor(getColor());
        else
            setColor(parsed);
    }

    private void updatePendingSelection() {
        pendingColor = panel.getColor();
        pendingValue = panel.getValue();
        updatePreview(pendingColor);
    }

    private void confirmSelection() {
        Color previous = getColor();
        if (pendingColor != null) {
            updating = true;
            try {
                color.set(pendingColor);
                value.set(pendingValue);
                updatePreview();
            } finally {
                updating = false;
            }
        }
        hide();
        if (pendingColor != null && !pendingColor.equals(previous)) fireChange();
    }

    private void updatePreview() {
        updatePreview(getColor());
    }

    /** Updates the trigger preview without changing the confirmed model value. */
    private void updatePreview(Color selected) {
        preview.setStyle(selected == null
                ? "-fx-background-color: transparent;"
                : "-fx-background-color: " + toCss(selected) + ";");
        indicator.setType(selected == null ? EleFXIconType.CLOSE : EleFXIconType.ARROW_DOWN);
        if (selected == null)
            indicator.getStyleClass().remove("ele-color-picker__indicator--selected");
        else if (!indicator.getStyleClass().contains("ele-color-picker__indicator--selected"))
            indicator.getStyleClass().add("ele-color-picker__indicator--selected");
        popupClearButton.setVisible(isClearable());
        popupClearButton.setManaged(isClearable());
    }

    private void fireChange() {
        ActionEvent event = new ActionEvent(this, this);
        fireEvent(event);
        if (getOnChange() != null) getOnChange().handle(event);
    }

    private void clearTriggerFocus() {
        Platform.runLater(() -> {
            if (getScene() != null && getScene().getRoot() != null) getScene().getRoot().requestFocus();
        });
    }

    private static Color parseColor(String value) {
        try {
            return Color.web(value.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String toCss(Color color) {
        return String.format(Locale.ROOT, "rgba(%d,%d,%d,%.4f)", (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255), (int) Math.round(color.getBlue() * 255), color.getOpacity());
    }
}

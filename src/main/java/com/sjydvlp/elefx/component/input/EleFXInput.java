package com.sjydvlp.elefx.component.input;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.Objects;
import java.util.function.Function;

/** A single-line, Element Plus styled input. */
public class EleFXInput extends HBox implements Themable {

    private static final String STYLE_CLASS = "ele-input";

    private static final PseudoClass FOCUSED = PseudoClass.getPseudoClass("focused");

    private final EventHandler<MouseEvent> outsideClickHandler = this::handleSceneMousePressed;

    private final TextField field = new TextField();

    private final PasswordField passwordField = new PasswordField();

    private final TextArea textArea = new TextArea();

    private final StackPane textareaWrapper = new StackPane();

    private final HBox groupInputWrapper = new HBox();

    private final StackPane prependContainer = new StackPane();

    private final StackPane appendContainer = new StackPane();

    private final StackPane clearButton = new StackPane();

    private final StackPane passwordButton = new StackPane();

    private final Label wordLimit = new Label();

    private final StringProperty text = new javafx.beans.property.SimpleStringProperty(this, "text", "");

    private final BooleanProperty clearable = new SimpleBooleanProperty(this, "clearable", false);

    private final BooleanProperty readOnly = new SimpleBooleanProperty(this, "readOnly", false);

    private final BooleanProperty showWordLimit = new SimpleBooleanProperty(this, "showWordLimit", false);

    private final IntegerProperty maxLength = new SimpleIntegerProperty(this, "maxLength", -1);

    private final IntegerProperty minLength = new SimpleIntegerProperty(this, "minLength", -1);

    private final IntegerProperty rows = new SimpleIntegerProperty(this, "rows", 2);

    private final BooleanProperty autoSize = new SimpleBooleanProperty(this, "autoSize", false);

    private final IntegerProperty autoSizeMinRows = new SimpleIntegerProperty(this, "autoSizeMinRows", 2);

    private final IntegerProperty autoSizeMaxRows = new SimpleIntegerProperty(this, "autoSizeMaxRows", -1);

    private final ObjectProperty<Node> prefix = new SimpleObjectProperty<>(this, "prefix");

    private final ObjectProperty<Node> suffix = new SimpleObjectProperty<>(this, "suffix");

    private final ObjectProperty<Node> prepend = new SimpleObjectProperty<>(this, "prepend");

    private final ObjectProperty<Node> append = new SimpleObjectProperty<>(this, "append");

    private final ObjectProperty<Node> clearIcon = new SimpleObjectProperty<>(this, "clearIcon",
            new EleFXIcon(EleFXIconType.CIRCLE_CLOSE, 16));

    private final ObjectProperty<EleFXInputSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXInputSize.DEFAULT);

    private final ObjectProperty<EleFXInputType> type = new SimpleObjectProperty<>(this, "type", EleFXInputType.TEXT);

    private final BooleanProperty showPassword = new SimpleBooleanProperty(this, "showPassword", false);

    private final BooleanProperty passwordVisible = new SimpleBooleanProperty(this, "passwordVisible", false);

    private final ObjectProperty<Function<String, String>> formatter = new SimpleObjectProperty<>(this, "formatter");

    private final ObjectProperty<Function<String, String>> parser = new SimpleObjectProperty<>(this, "parser");

    /** Prevents programmatic formatter output from re-entering the parser pipeline. */
    private boolean synchronizingDisplay;

    public EleFXInput() {
        getStyleClass().add(STYLE_CLASS);
        field.getStyleClass().add("ele-input__inner");
        passwordField.getStyleClass().add("ele-input__inner");
        textArea.getStyleClass().addAll("ele-input__inner", "ele-input__textarea");
        textareaWrapper.getStyleClass().add("ele-input__textarea-wrapper");
        groupInputWrapper.getStyleClass().add("ele-input__group-wrapper");
        groupInputWrapper.setAlignment(Pos.CENTER_LEFT);
        prependContainer.getStyleClass().add("ele-input__prepend");
        appendContainer.getStyleClass().add("ele-input__append");
        passwordField.promptTextProperty().bind(field.promptTextProperty());
        textArea.promptTextProperty().bind(field.promptTextProperty());
        field.setTextFormatter(createTextFormatter());
        passwordField.setTextFormatter(createTextFormatter());
        textArea.setTextFormatter(createTextFormatter());
        HBox.setHgrow(field, Priority.ALWAYS);
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        HBox.setHgrow(textArea, Priority.ALWAYS);

        clearButton.getStyleClass().add("ele-input__clear");
        clearButton.setFocusTraversable(false);
        clearButton.setOnMouseClicked(event -> {
            clear();
            event.consume();
        });
        passwordButton.getStyleClass().add("ele-input__password-toggle");
        passwordButton.setFocusTraversable(false);
        passwordButton.setOnMouseClicked(event -> {
            setPasswordVisible(!isPasswordVisible());
            event.consume();
        });
        wordLimit.getStyleClass().add("ele-input__count");

        field.focusedProperty().addListener((observable, oldValue, focused) -> {
            updateFocusedStyle();
            updateClearButton();
        });
        passwordField.focusedProperty().addListener((observable, oldValue, focused) -> {
            updateFocusedStyle();
            updateClearButton();
        });
        textArea.focusedProperty().addListener((observable, oldValue, focused) -> {
            updateFocusedStyle();
            updateClearButton();
        });
        hoverProperty().addListener((observable, oldValue, hovered) -> updateClearButton());
        field.textProperty().addListener((observable, oldValue, value) -> updateTextFromField(value));
        passwordField.textProperty().addListener((observable, oldValue, value) -> updateTextFromField(value));
        textArea.textProperty().addListener((observable, oldValue, value) -> updateTextFromField(value));
        text.addListener((observable, oldValue, value) -> {
            updateDisplayedText();
            updateClearButton();
            updateWordLimit();
            updateAutoSize();
        });
        clearable.addListener((observable, oldValue, value) -> updateClearButton());
        disabledProperty().addListener((observable, oldValue, value) -> updateClearButton());
        showWordLimit.addListener((observable, oldValue, value) -> updateWordLimit());
        maxLength.addListener((observable, oldValue, value) -> updateWordLimit());
        rows.addListener((observable, oldValue, value) -> updateRows(value.intValue()));
        autoSize.addListener((observable, oldValue, value) -> {
            updateAutoSizeStyle();
            updateRows(getRows());
        });
        autoSizeMinRows.addListener((observable, oldValue, value) -> updateAutoSize());
        autoSizeMaxRows.addListener((observable, oldValue, value) -> {
            updateAutoSizeStyle();
            updateAutoSize();
        });
        textArea.widthProperty().addListener((observable, oldValue, value) -> updateAutoSize());
        readOnly.addListener((observable, oldValue, value) -> field.setEditable(!value));
        readOnly.addListener((observable, oldValue, value) -> passwordField.setEditable(!value));
        readOnly.addListener((observable, oldValue, value) -> textArea.setEditable(!value));
        prefix.addListener((observable, oldValue, value) -> rebuildChildren());
        suffix.addListener((observable, oldValue, value) -> rebuildChildren());
        prepend.addListener((observable, oldValue, value) -> rebuildChildren());
        append.addListener((observable, oldValue, value) -> rebuildChildren());
        clearIcon.addListener((observable, oldValue, value) -> updateClearIcon());
        size.addListener((observable, oldValue, value) -> updateSizeStyle(oldValue, value));
        type.addListener((observable, oldValue, value) -> updateType());
        showPassword.addListener((observable, oldValue, value) -> updatePasswordButton());
        passwordVisible.addListener((observable, oldValue, value) -> updateType());
        formatter.addListener((observable, oldValue, value) -> updateDisplayedText());
        parser.addListener((observable, oldValue, value) -> updateDisplayedText());
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (oldScene != null) oldScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsideClickHandler);
            if (newScene != null) newScene.addEventFilter(MouseEvent.MOUSE_PRESSED, outsideClickHandler);
        });

        updateClearIcon();
        updateRows(getRows());
        updatePasswordButton();
        updateType();
        updateSizeStyle(null, getSize());
        updateWordLimit();
        updateClearButton();
        sceneBuilderIntegration();
    }

    public String getText() {
        return text.get();
    }

    public void setText(String value) {
        text.set(value == null ? "" : value);
    }

    public StringProperty textProperty() {
        return text;
    }

    public String getPlaceholder() {
        return field.getPromptText();
    }

    public void setPlaceholder(String value) {
        field.setPromptText(value);
    }

    public StringProperty placeholderProperty() {
        return field.promptTextProperty();
    }

    public String getPromptText() {
        return getPlaceholder();
    }

    public void setPromptText(String value) {
        setPlaceholder(value);
    }

    public StringProperty promptTextProperty() {
        return placeholderProperty();
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

    public boolean isReadOnly() {
        return readOnly.get();
    }

    public void setReadOnly(boolean value) {
        readOnly.set(value);
    }

    public BooleanProperty readOnlyProperty() {
        return readOnly;
    }

    public int getMaxLength() {
        return maxLength.get();
    }

    /** A negative value removes the limit. */
    public void setMaxLength(int value) {
        maxLength.set(value);
    }

    public IntegerProperty maxLengthProperty() {
        return maxLength;
    }

    public int getMinLength() {
        return minLength.get();
    }

    /** A negative value means no minimum is declared. */
    public void setMinLength(int value) {
        minLength.set(value);
    }

    public IntegerProperty minLengthProperty() {
        return minLength;
    }

    /** Number of visible rows used when {@link #getType()} is TEXTAREA. */
    public int getRows() {
        return rows.get();
    }

    public void setRows(int value) {
        if (value < 1) throw new IllegalArgumentException("rows must be at least 1");
        rows.set(value);
    }

    public IntegerProperty rowsProperty() {
        return rows;
    }

    /** Whether a textarea grows and shrinks with its content. */
    public boolean isAutoSize() {
        return autoSize.get();
    }

    public void setAutoSize(boolean value) {
        autoSize.set(value);
    }

    public BooleanProperty autoSizeProperty() {
        return autoSize;
    }

    public int getAutoSizeMinRows() {
        return autoSizeMinRows.get();
    }

    public void setAutoSizeMinRows(int value) {
        if (value < 1) throw new IllegalArgumentException("autoSizeMinRows must be at least 1");
        if (getAutoSizeMaxRows() != -1 && value > getAutoSizeMaxRows())
            throw new IllegalArgumentException("autoSizeMinRows must not exceed autoSizeMaxRows");
        autoSizeMinRows.set(value);
    }

    public IntegerProperty autoSizeMinRowsProperty() {
        return autoSizeMinRows;
    }

    /** Maximum automatic rows; {@code -1} leaves the height unbounded. */
    public int getAutoSizeMaxRows() {
        return autoSizeMaxRows.get();
    }

    public void setAutoSizeMaxRows(int value) {
        if (value != -1 && value < getAutoSizeMinRows())
            throw new IllegalArgumentException("autoSizeMaxRows must be -1 or at least autoSizeMinRows");
        autoSizeMaxRows.set(value);
    }

    public IntegerProperty autoSizeMaxRowsProperty() {
        return autoSizeMaxRows;
    }

    public boolean isShowWordLimit() {
        return showWordLimit.get();
    }

    public void setShowWordLimit(boolean value) {
        showWordLimit.set(value);
    }

    public BooleanProperty showWordLimitProperty() {
        return showWordLimit;
    }

    public Node getPrefix() {
        return prefix.get();
    }

    public void setPrefix(Node value) {
        prefix.set(value);
    }

    public ObjectProperty<Node> prefixProperty() {
        return prefix;
    }

    public Node getSuffix() {
        return suffix.get();
    }

    public void setSuffix(Node value) {
        suffix.set(value);
    }

    public ObjectProperty<Node> suffixProperty() {
        return suffix;
    }

    /** Content displayed before the input, such as a protocol label or select. */
    public Node getPrepend() {
        return prepend.get();
    }

    public void setPrepend(Node value) {
        prepend.set(value);
    }

    public ObjectProperty<Node> prependProperty() {
        return prepend;
    }

    /** Content displayed after the input, such as a domain suffix or action button. */
    public Node getAppend() {
        return append.get();
    }

    public void setAppend(Node value) {
        append.set(value);
    }

    public ObjectProperty<Node> appendProperty() {
        return append;
    }

    public Node getClearIcon() {
        return clearIcon.get();
    }

    public void setClearIcon(Node value) {
        clearIcon.set(value);
    }

    public ObjectProperty<Node> clearIconProperty() {
        return clearIcon;
    }

    public EleFXInputSize getSize() {
        return size.get();
    }

    public void setSize(EleFXInputSize value) {
        size.set(value == null ? EleFXInputSize.DEFAULT : value);
    }

    public ObjectProperty<EleFXInputSize> sizeProperty() {
        return size;
    }

    /** Input mode. Password mode masks the value unless it is revealed. */
    public EleFXInputType getType() {
        return type.get();
    }

    public void setType(EleFXInputType value) {
        type.set(value == null ? EleFXInputType.TEXT : value);
    }

    public ObjectProperty<EleFXInputType> typeProperty() {
        return type;
    }

    public boolean isPassword() {
        return getType() == EleFXInputType.PASSWORD;
    }

    public void setPassword(boolean value) {
        setType(value ? EleFXInputType.PASSWORD : EleFXInputType.TEXT);
    }

    public boolean isShowPassword() {
        return showPassword.get();
    }

    /** Enables the eye icon that lets users reveal a password temporarily. */
    public void setShowPassword(boolean value) {
        showPassword.set(value);
    }

    public BooleanProperty showPasswordProperty() {
        return showPassword;
    }

    public boolean isPasswordVisible() {
        return passwordVisible.get();
    }

    public void setPasswordVisible(boolean value) {
        passwordVisible.set(value);
    }

    public BooleanProperty passwordVisibleProperty() {
        return passwordVisible;
    }

    /** Formats the model value shown in a text input. */
    public Function<String, String> getFormatter() {
        return formatter.get();
    }

    public void setFormatter(Function<String, String> value) {
        formatter.set(value);
    }

    public ObjectProperty<Function<String, String>> formatterProperty() {
        return formatter;
    }

    /**
     * Extracts the model value from formatted text.
     *
     * <p>
     * Matching Element Plus, this function is applied only when a
     * {@linkplain #setFormatter(Function) formatter} is also configured and
     * the input type is {@link EleFXInputType#TEXT}.
     * </p>
     */
    public Function<String, String> getParser() {
        return parser.get();
    }

    public void setParser(Function<String, String> value) {
        parser.set(value);
    }

    public ObjectProperty<Function<String, String>> parserProperty() {
        return parser;
    }

    public TextField getTextField() {
        return field;
    }

    /** Returns the masked control used while {@link #isPassword()} is true. */
    public PasswordField getPasswordField() {
        return passwordField;
    }

    /** Returns the multiline control used while the type is TEXTAREA. */
    public TextArea getTextArea() {
        return textArea;
    }

    /** Clears the current text. */
    public void clear() {
        text.set("");
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.INPUT;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void rebuildChildren() {
        getChildren().setAll();
        // A control can move between the plain input, group, and textarea
        // containers at runtime; detach adornments before re-parenting them.
        groupInputWrapper.getChildren().clear();
        textareaWrapper.getChildren().clear();
        boolean textarea = getType() == EleFXInputType.TEXTAREA;
        if (textarea) {
            updateGroupStyle(false);
            textareaWrapper.getChildren().setAll(textArea, clearButton, wordLimit);
            StackPane.setAlignment(clearButton, Pos.CENTER_RIGHT);
            StackPane.setAlignment(wordLimit, Pos.BOTTOM_RIGHT);
            getChildren().add(textareaWrapper);
            HBox.setHgrow(textareaWrapper, Priority.ALWAYS);
            return;
        }
        boolean grouped = getPrepend() != null || getAppend() != null;
        updateGroupStyle(grouped);
        if (grouped) {
            updateGroupContainer(prependContainer, getPrepend());
            updateGroupContainer(appendContainer, getAppend());
            rebuildInputContent(groupInputWrapper);
            if (getPrepend() != null) getChildren().add(prependContainer);
            getChildren().add(groupInputWrapper);
            HBox.setHgrow(groupInputWrapper, Priority.ALWAYS);
            if (getAppend() != null) getChildren().add(appendContainer);
        } else {
            rebuildInputContent(this);
        }
    }

    private void rebuildInputContent(HBox container) {
        container.getChildren().setAll();
        if (getPrefix() != null) {
            if (!getPrefix().getStyleClass().contains("ele-input__prefix"))
                getPrefix().getStyleClass().add("ele-input__prefix");
            container.getChildren().add(getPrefix());
        }
        container.getChildren().add(activeField());
        // Element Plus renders clear before a suffix icon in the suffix area.
        container.getChildren().add(clearButton);
        if (getSuffix() != null) {
            if (!getSuffix().getStyleClass().contains("ele-input__suffix"))
                getSuffix().getStyleClass().add("ele-input__suffix");
            container.getChildren().add(getSuffix());
        }
        container.getChildren().addAll(passwordButton, wordLimit);
    }

    private void updateGroupContainer(StackPane container, Node content) {
        container.getChildren().setAll();
        if (content != null) container.getChildren().add(content);
    }

    private void updateClearIcon() {
        clearButton.getChildren().setAll();
        if (getClearIcon() != null) clearButton.getChildren().add(getClearIcon());
        rebuildChildren();
    }

    private TextFormatter<String> createTextFormatter() {
        return new TextFormatter<>(change -> {
            int limit = getMaxLength();
            return limit < 0 || change.getControlNewText().length() <= limit ? change : null;
        });
    }

    private TextInputControl activeField() {
        if (getType() == EleFXInputType.TEXTAREA) return textArea;
        return isPassword() && !isPasswordVisible() ? passwordField : field;
    }

    private void updateTextFromField(String displayedText) {
        if (synchronizingDisplay) return;
        // Element Plus treats formatter/parser as a pair. A parser on its own
        // must not mutate the model, and password fields bypass the pair.
        boolean usesFormatPair = getType() == EleFXInputType.TEXT && getFormatter() != null && getParser() != null;
        String parsed = usesFormatPair ? getParser().apply(displayedText) : displayedText;
        parsed = parsed == null ? "" : parsed;
        if (Objects.equals(getText(), parsed))
            updateDisplayedText();
        else
            text.set(parsed);
    }

    private void updateDisplayedText() {
        String modelValue = getText();
        String display = getType() != EleFXInputType.TEXT || getFormatter() == null
                ? modelValue
                : getFormatter().apply(modelValue);
        display = display == null ? "" : display;
        synchronizingDisplay = true;
        try {
            if (!field.getText().equals(display)) field.setText(display);
            if (!passwordField.getText().equals(display)) passwordField.setText(display);
            if (!textArea.getText().equals(display)) textArea.setText(display);
        } finally {
            synchronizingDisplay = false;
        }
    }

    private void updateType() {
        boolean wasFocused = field.isFocused() || passwordField.isFocused() || textArea.isFocused();
        if (!isPassword()) passwordVisible.set(false);
        updateTextareaStyle();
        updateAutoSizeStyle();
        updatePasswordButton();
        updateDisplayedText();
        updateWordLimit();
        updateClearButton();
        updateRows(getRows());
        rebuildChildren();
        if (wasFocused) activeField().requestFocus();
    }

    private void updatePasswordButton() {
        passwordButton.getChildren().setAll(new EleFXIcon(
                isPasswordVisible() ? EleFXIconType.HIDE : EleFXIconType.VIEW, 16));
        boolean enabled = isPassword() && isShowPassword() && !isDisabled();
        passwordButton.setVisible(enabled);
        // Reserve the trailing slot in password mode so toggling showPassword
        // cannot resize the input, without adding unused space to text inputs.
        passwordButton.setManaged(isPassword());
    }

    private void updateRows(int value) {
        if (isAutoSize())
            updateAutoSize();
        else
            textArea.setPrefRowCount(Math.max(1, value));
    }

    private void updateAutoSize() {
        if (!isAutoSize() || getType() != EleFXInputType.TEXTAREA) return;
        int rows = visualRowCount(getText());
        rows = Math.max(getAutoSizeMinRows(), rows);
        if (getAutoSizeMaxRows() != -1) rows = Math.min(getAutoSizeMaxRows(), rows);
        textArea.setPrefRowCount(rows);
    }

    private int visualRowCount(String value) {
        double availableWidth = textArea.getWidth() - 22;
        if (availableWidth <= 0) return Math.max(1, value.split("\\R", -1).length);
        Text metrics = new Text();
        metrics.setFont(textArea.getFont());
        int rows = 0;
        for (String line : value.split("\\R", -1)) {
            metrics.setText(line.isEmpty() ? " " : line);
            rows += Math.max(1, (int) Math.ceil(metrics.getLayoutBounds().getWidth() / availableWidth));
        }
        return Math.max(1, rows);
    }

    private void updateTextareaStyle() {
        String textareaStyle = "ele-input--textarea";
        if (getType() == EleFXInputType.TEXTAREA && !getStyleClass().contains(textareaStyle))
            getStyleClass().add(textareaStyle);
        else if (getType() != EleFXInputType.TEXTAREA) getStyleClass().remove(textareaStyle);
    }

    private void updateGroupStyle(boolean grouped) {
        updateStyleClass("ele-input--group", grouped);
        updateStyleClass("ele-input--group-prepend", grouped && getPrepend() != null);
        updateStyleClass("ele-input--group-append", grouped && getAppend() != null);
    }

    private void updateStyleClass(String styleClass, boolean enabled) {
        if (enabled && !getStyleClass().contains(styleClass))
            getStyleClass().add(styleClass);
        else if (!enabled) getStyleClass().remove(styleClass);
    }

    private void updateAutoSizeStyle() {
        String style = "ele-input--textarea-autosize";
        boolean unboundedAutoSize = getType() == EleFXInputType.TEXTAREA && isAutoSize()
                && getAutoSizeMaxRows() == -1;
        if (unboundedAutoSize && !getStyleClass().contains(style))
            getStyleClass().add(style);
        else if (!unboundedAutoSize) getStyleClass().remove(style);
    }

    private void updateClearButton() {
        boolean visible = isClearable() && !isDisabled() && !getText().isEmpty()
                && (isHover() || activeField().isFocused());
        clearButton.setVisible(visible);
        // Keep the trailing icon's layout slot even while it is hidden. This
        // mirrors Element Plus's overlay behavior and prevents the text area
        // from changing width when clearable or the current value changes.
        clearButton.setManaged(true);
    }

    private void updateWordLimit() {
        boolean visible = isShowWordLimit() && getMaxLength() >= 0 && !isPassword();
        wordLimit.setText(visible ? getText().length() + " / " + getMaxLength() : "");
        wordLimit.setVisible(visible);
        wordLimit.setManaged(visible);
    }

    private void updateFocusedStyle() {
        pseudoClassStateChanged(FOCUSED, field.isFocused() || passwordField.isFocused() || textArea.isFocused());
    }

    private void handleSceneMousePressed(MouseEvent event) {
        if (!field.isFocused() && !passwordField.isFocused() && !textArea.isFocused()
                || isInsideInput(event.getTarget()))
            return;
        Scene scene = getScene();
        if (scene != null && scene.getRoot() != null) scene.getRoot().requestFocus();
    }

    private boolean isInsideInput(Object target) {
        if (!(target instanceof Node node)) return false;
        for (Node current = node; current != null; current = current.getParent())
            if (current == this) return true;
        return false;
    }

    private void updateSizeStyle(EleFXInputSize oldSize, EleFXInputSize newSize) {
        if (oldSize != null) getStyleClass().remove(oldSize.styleClass());
        EleFXInputSize resolved = newSize == null ? EleFXInputSize.DEFAULT : newSize;
        if (!getStyleClass().contains(resolved.styleClass())) getStyleClass().add(resolved.styleClass());
    }
}

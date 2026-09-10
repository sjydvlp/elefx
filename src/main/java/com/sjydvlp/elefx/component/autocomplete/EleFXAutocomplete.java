package com.sjydvlp.elefx.component.autocomplete;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An Element Plus inspired text input with query-driven suggestions.
 *
 * <p>
 * Set {@link #setSuggestionsProvider(Function)} for local data, or
 * {@link #setAsyncSuggestionsProvider(BiConsumer)} when results arrive later.
 * The component owns the popup and its keyboard/mouse selection behavior.
 * </p>
 */
public class EleFXAutocomplete<T> extends HBox implements Themable {

    private static final String STYLE_CLASS = "ele-autocomplete";

    private final TextField input = new TextField();

    private final Button clearButton = new Button("×");

    private final ContextMenu popup = new ContextMenu();

    private final PauseTransition debounceTimer = new PauseTransition();

    private final ObjectProperty<Function<String, ? extends Collection<T>>> suggestionsProvider = new SimpleObjectProperty<>(
            this, "suggestionsProvider");

    private final ObjectProperty<BiConsumer<String, Consumer<Collection<T>>>> asyncSuggestionsProvider = new SimpleObjectProperty<>(
            this, "asyncSuggestionsProvider");

    private final ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<>(this, "converter",
            new StringConverter<>() {

                @Override
                public String toString(T item) {
                    return item == null ? "" : item.toString();
                }

                @Override
                public T fromString(String value) {
                    throw new UnsupportedOperationException("Suggestions determine values");
                }
            });

    private final ObjectProperty<Callback<T, Node>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory");

    private final ObjectProperty<T> selectedItem = new SimpleObjectProperty<>(this, "selectedItem");

    private final BooleanProperty clearable = new SimpleBooleanProperty(this, "clearable", false);

    private final BooleanProperty triggerOnFocus = new SimpleBooleanProperty(this, "triggerOnFocus", true);

    private final BooleanProperty selectWhenUnmatched = new SimpleBooleanProperty(this, "selectWhenUnmatched", false);

    private final BooleanProperty loading = new SimpleBooleanProperty(this, "loading", false);

    private final IntegerProperty debounce = new SimpleIntegerProperty(this, "debounce", 300);

    private final ObjectProperty<EventHandler<EleFXAutocompleteEvent<T>>> onSelect = new SimpleObjectProperty<>(this,
            "onSelect");

    private String pendingQuery = "";

    private long pendingRequestVersion;

    private long requestVersion;

    private boolean selecting;

    public EleFXAutocomplete() {
        initialize();
    }

    public EleFXAutocomplete(Function<String, ? extends Collection<T>> suggestionsProvider) {
        this();
        setSuggestionsProvider(suggestionsProvider);
    }

    public String getText() {
        return input.getText();
    }

    public StringProperty textProperty() {
        return input.textProperty();
    }

    public void setText(String text) {
        input.setText(text);
    }

    public String getPromptText() {
        return input.getPromptText();
    }

    public void setPromptText(String promptText) {
        input.setPromptText(promptText);
    }

    public StringProperty promptTextProperty() {
        return input.promptTextProperty();
    }

    public Function<String, ? extends Collection<T>> getSuggestionsProvider() {
        return suggestionsProvider.get();
    }

    public ObjectProperty<Function<String, ? extends Collection<T>>> suggestionsProviderProperty() {
        return suggestionsProvider;
    }

    public void setSuggestionsProvider(Function<String, ? extends Collection<T>> provider) {
        suggestionsProvider.set(provider);
    }

    public BiConsumer<String, Consumer<Collection<T>>> getAsyncSuggestionsProvider() {
        return asyncSuggestionsProvider.get();
    }

    public ObjectProperty<BiConsumer<String, Consumer<Collection<T>>>> asyncSuggestionsProviderProperty() {
        return asyncSuggestionsProvider;
    }

    public void setAsyncSuggestionsProvider(BiConsumer<String, Consumer<Collection<T>>> provider) {
        asyncSuggestionsProvider.set(provider);
    }

    public StringConverter<T> getConverter() {
        return converter.get();
    }

    public ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    public void setConverter(StringConverter<T> converter) {
        this.converter.set(converter == null ? this.converter.get() : converter);
    }

    public Callback<T, Node> getCellFactory() {
        return cellFactory.get();
    }

    public ObjectProperty<Callback<T, Node>> cellFactoryProperty() {
        return cellFactory;
    }

    public void setCellFactory(Callback<T, Node> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    public T getSelectedItem() {
        return selectedItem.get();
    }

    public ObjectProperty<T> selectedItemProperty() {
        return selectedItem;
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

    public boolean isTriggerOnFocus() {
        return triggerOnFocus.get();
    }

    public BooleanProperty triggerOnFocusProperty() {
        return triggerOnFocus;
    }

    public void setTriggerOnFocus(boolean triggerOnFocus) {
        this.triggerOnFocus.set(triggerOnFocus);
    }

    public boolean isSelectWhenUnmatched() {
        return selectWhenUnmatched.get();
    }

    public BooleanProperty selectWhenUnmatchedProperty() {
        return selectWhenUnmatched;
    }

    public void setSelectWhenUnmatched(boolean selectWhenUnmatched) {
        this.selectWhenUnmatched.set(selectWhenUnmatched);
    }

    public boolean isLoading() {
        return loading.get();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading.set(loading);
    }

    public int getDebounce() {
        return debounce.get();
    }

    public IntegerProperty debounceProperty() {
        return debounce;
    }

    public void setDebounce(int debounce) {
        if (debounce < 0) throw new IllegalArgumentException("debounce must not be negative");
        this.debounce.set(debounce);
    }

    public EventHandler<EleFXAutocompleteEvent<T>> getOnSelect() {
        return onSelect.get();
    }

    public ObjectProperty<EventHandler<EleFXAutocompleteEvent<T>>> onSelectProperty() {
        return onSelect;
    }

    public void setOnSelect(EventHandler<EleFXAutocompleteEvent<T>> handler) {
        onSelect.set(handler);
    }

    /** Immediately runs the configured query provider using the current input text. */
    public void requestSuggestions() {
        requestSuggestions(getText(), ++requestVersion);
    }

    /** Clears the input and hides suggestions. */
    public void clear() {
        requestVersion++;
        debounceTimer.stop();
        selecting = true;
        input.clear();
        selecting = false;
        selectedItem.set(null);
        popup.hide();
    }

    public TextField getInput() {
        return input;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.AUTOCOMPLETE;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        input.getStyleClass().add("ele-autocomplete__input");
        clearButton.getStyleClass().add("ele-autocomplete__clear");
        clearButton.setFocusTraversable(false);
        clearButton.setOnAction(event -> clear());
        getChildren().addAll(input, clearButton);
        HBox.setHgrow(input, Priority.ALWAYS);
        popup.getStyleClass().add("ele-autocomplete__popup");
        input.textProperty().addListener((observable, oldValue, value) -> scheduleSuggestions(value));
        input.focusedProperty().addListener((observable, oldValue, focused) -> {
            if (focused && isTriggerOnFocus()) requestSuggestions();
            if (!focused) popup.hide();
        });
        input.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN && !popup.isShowing()) {
                requestSuggestions();
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER && isSelectWhenUnmatched() && !popup.isShowing())
                select(null);
            else if (event.getCode() == KeyCode.ESCAPE) popup.hide();
        });
        clearable.addListener((observable, oldValue, value) -> clearButton.setManaged(value));
        clearable.addListener((observable, oldValue, value) -> clearButton.setVisible(value));
        clearButton.setManaged(isClearable());
        clearButton.setVisible(isClearable());
        debounce.addListener(
                (observable, oldValue, value) -> debounceTimer.setDuration(Duration.millis(value.intValue())));
        debounceTimer.setDuration(Duration.millis(getDebounce()));
        debounceTimer.setOnFinished(event -> requestSuggestions(pendingQuery, pendingRequestVersion));
        loading.addListener((observable, oldValue, value) -> updateLoadingStyle(value));
        updateLoadingStyle(isLoading());
        sceneBuilderIntegration();
    }

    private void scheduleSuggestions(String query) {
        if (selecting) return;
        pendingQuery = query == null ? "" : query;
        pendingRequestVersion = ++requestVersion;
        debounceTimer.playFromStart();
    }

    private void requestSuggestions(String query, long version) {
        Function<String, ? extends Collection<T>> provider = getSuggestionsProvider();
        if (provider != null)
            showSuggestions(provider.apply(query));
        else if (getAsyncSuggestionsProvider() != null) {
            setLoading(true);
            String requestedQuery = query;
            getAsyncSuggestionsProvider().accept(query, results -> {
                Runnable display = () -> {
                    if (version == requestVersion && requestedQuery.equals(getText())) showSuggestions(results);
                };
                if (Platform.isFxApplicationThread())
                    display.run();
                else
                    Platform.runLater(display);
            });
        }
    }

    private void showSuggestions(Collection<T> results) {
        setLoading(false);
        popup.getItems().clear();
        if (results == null || results.isEmpty()) {
            popup.hide();
            return;
        }
        for (T item : results)
            popup.getItems().add(menuItem(item));
        if (input.getScene() != null && input.isFocused()) popup.show(input, Side.BOTTOM, 0, 2);
    }

    private MenuItem menuItem(T item) {
        Node content = getCellFactory() == null
                ? new javafx.scene.control.Label(getConverter().toString(item))
                : getCellFactory().call(item);
        CustomMenuItem menuItem = new CustomMenuItem(content, true);
        menuItem.getStyleClass().add("ele-autocomplete__suggestion");
        menuItem.setOnAction(event -> select(item));
        return menuItem;
    }

    private void select(T item) {
        requestVersion++;
        debounceTimer.stop();
        selecting = true;
        try {
            if (item != null) {
                input.setText(getConverter().toString(item));
                input.positionCaret(input.getLength());
            }
        } finally {
            selecting = false;
        }
        selectedItem.set(item);
        popup.hide();
        EleFXAutocompleteEvent<T> event = new EleFXAutocompleteEvent<>(this, this, item);
        fireEvent(event);
        if (getOnSelect() != null) getOnSelect().handle(event);
    }

    private void updateLoadingStyle(boolean loading) {
        if (loading && !getStyleClass().contains("ele-autocomplete--loading")) {
            getStyleClass().add("ele-autocomplete--loading");
        } else if (!loading) {
            getStyleClass().remove("ele-autocomplete--loading");
        }
    }
}

package com.sjydvlp.elefx.component.breadcrumb;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.function.Supplier;

/**
 * Element Plus-inspired breadcrumb navigation.
 *
 * <p>
 * Use {@link #setSeparatorIcon(Node)} to override the text {@link #setSeparator(String) separator}.
 * Register {@link #setOnNavigate(EventHandler)} to connect item targets to your application's router.
 * </p>
 */
public class EleFXBreadcrumb extends HBox implements Themable {

    private final StringProperty separator = new SimpleStringProperty(this, "separator", "/");

    private final ObjectProperty<Node> separatorIcon = new SimpleObjectProperty<>(this, "separatorIcon");

    private final ObjectProperty<Supplier<? extends Node>> separatorIconFactory = new SimpleObjectProperty<>(this,
            "separatorIconFactory");

    private final ObservableList<EleFXBreadcrumbItem> items = FXCollections.observableArrayList();

    private final ObjectProperty<EventHandler<EleFXBreadcrumbEvent>> onNavigate = new SimpleObjectProperty<>(this,
            "onNavigate");

    public EleFXBreadcrumb() {
        initialize();
    }

    public EleFXBreadcrumb(EleFXBreadcrumbItem... items) {
        this();
        if (items != null) getItems().addAll(items);
    }

    public String getSeparator() {
        return separator.get();
    }

    public void setSeparator(String value) {
        separator.set(value == null ? "/" : value);
    }

    public StringProperty separatorProperty() {
        return separator;
    }

    /** Icon used between items; when set, it takes precedence over {@link #getSeparator()}. */
    public Node getSeparatorIcon() {
        return separatorIcon.get();
    }

    public void setSeparatorIcon(Node value) {
        separatorIcon.set(value);
    }

    public ObjectProperty<Node> separatorIconProperty() {
        return separatorIcon;
    }

    /**
     * Supplies a fresh separator icon for each gap. Prefer this when there are more than two items,
     * because JavaFX nodes cannot be attached to multiple parents at once.
     */
    public Supplier<? extends Node> getSeparatorIconFactory() {
        return separatorIconFactory.get();
    }

    public void setSeparatorIconFactory(Supplier<? extends Node> value) {
        separatorIconFactory.set(value);
    }

    public ObjectProperty<Supplier<? extends Node>> separatorIconFactoryProperty() {
        return separatorIconFactory;
    }

    public ObservableList<EleFXBreadcrumbItem> getItems() {
        return items;
    }

    public EventHandler<EleFXBreadcrumbEvent> getOnNavigate() {
        return onNavigate.get();
    }

    public void setOnNavigate(EventHandler<EleFXBreadcrumbEvent> value) {
        onNavigate.set(value);
    }

    public ObjectProperty<EventHandler<EleFXBreadcrumbEvent>> onNavigateProperty() {
        return onNavigate;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.BREADCRUMB;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    void navigate(EleFXBreadcrumbItem item) {
        EleFXBreadcrumbEvent event = new EleFXBreadcrumbEvent(this, this, item);
        fireEvent(event);
        if (!event.isConsumed() && getOnNavigate() != null) getOnNavigate().handle(event);
    }

    private void initialize() {
        getStyleClass().add("ele-breadcrumb");
        setAlignment(Pos.CENTER_LEFT);
        items.addListener((ListChangeListener<EleFXBreadcrumbItem>) change -> refresh());
        separator.addListener(o -> refresh());
        separatorIcon.addListener(o -> refresh());
        separatorIconFactory.addListener(o -> refresh());
        disabledProperty().addListener(o -> refresh());
        refresh();
        sceneBuilderIntegration();
    }

    private void refresh() {
        getChildren().clear();
        for (int index = 0; index < items.size(); index++) {
            EleFXBreadcrumbItem item = items.get(index);
            if (item == null) throw new IllegalStateException("Breadcrumb items must not contain null");
            item.setOwner(this);
            item.setDisable(isDisabled());
            item.setLast(index == items.size() - 1);
            getChildren().add(item);
            if (index < items.size() - 1) getChildren().add(createSeparator());
        }
    }

    private Node createSeparator() {
        Supplier<? extends Node> factory = getSeparatorIconFactory();
        if (factory != null) {
            Node generated = factory.get();
            if (generated == null) throw new IllegalStateException("separatorIconFactory must not return null");
            generated.getStyleClass().add("ele-breadcrumb__separator-icon");
            return generated;
        }
        Node icon = getSeparatorIcon();
        if (icon != null) {
            // A node can have only one parent. Reuse is supported by cloning only through a factory-less API,
            // so the supplied icon is used for the first separator and later separators retain the text fallback.
            if (icon.getParent() == null) {
                icon.getStyleClass().add("ele-breadcrumb__separator-icon");
                return icon;
            }
        }
        Label value = new Label(getSeparator());
        value.getStyleClass().add("ele-breadcrumb__separator");
        return value;
    }
}

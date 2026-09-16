package com.sjydvlp.elefx.component.carousel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/** A customizable page in an {@link EleFXCarousel}. */
public class EleFXCarouselItem extends StackPane {

    private final StringProperty name = new SimpleStringProperty(this, "name", "");

    private final StringProperty label = new SimpleStringProperty(this, "label", "");

    public EleFXCarouselItem() {
        getStyleClass().add("ele-carousel__item");
    }

    public EleFXCarouselItem(Node... content) {
        this();
        getChildren().addAll(content);
    }

    /** Optional programmatic identifier, usable with {@link EleFXCarousel#setActiveItem(String)}. */
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String value) {
        name.set(value == null ? "" : value);
    }

    /** Optional text shown instead of the numeric indicator. */
    public String getLabel() {
        return label.get();
    }

    public StringProperty labelProperty() {
        return label;
    }

    public void setLabel(String value) {
        label.set(value == null ? "" : value);
    }
}

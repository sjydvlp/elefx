package com.sjydvlp.elefx.component.avatar;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * An Element Plus-inspired horizontal Avatar Group.
 *
 * <p>
 * Add {@link EleFXAvatar} instances through {@link #getChildren()}. When collapse is
 * enabled, members after {@link #getMaxCollapseAvatars()} are represented by a {@code +N} avatar.
 * </p>
 */
public class EleFXAvatarGroup extends HBox implements Themable {

    private static final String STYLE_CLASS = "ele-avatar-group";

    private static final String COLLAPSE_STYLE_CLASS = "ele-avatar-group__collapse";

    private final DoubleProperty size = new SimpleDoubleProperty(this, "size", Double.NaN);

    private final ObjectProperty<EleFXAvatarSize> avatarSize = new SimpleObjectProperty<>(this, "avatarSize");

    private final ObjectProperty<EleFXAvatarShape> avatarShape = new SimpleObjectProperty<>(this, "avatarShape");

    private final BooleanProperty collapseAvatars = new SimpleBooleanProperty(this, "collapseAvatars", false);

    private final IntegerProperty maxCollapseAvatars = new SimpleIntegerProperty(this, "maxCollapseAvatars", 1);

    private final BooleanProperty collapseAvatarsTooltip = new SimpleBooleanProperty(this, "collapseAvatarsTooltip",
            false);

    private final StringProperty collapseClass = new SimpleStringProperty(this, "collapseClass", "");

    private final StringProperty collapseStyle = new SimpleStringProperty(this, "collapseStyle", "");

    private final EleFXAvatar collapseAvatar = new EleFXAvatar();

    private Tooltip collapseTooltip;

    private String appliedCollapseClass;

    private boolean refreshing;

    public EleFXAvatarGroup() {
        initialize();
    }

    public EleFXAvatarGroup(Node... children) {
        this();
        getChildren().addAll(children);
    }

    /** Returns the group-controlled pixel size, or {@link Double#NaN} when members keep their own size. */
    public double getSize() {
        return size.get();
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    public void setSize(double size) {
        if (!Double.isNaN(size) && (!Double.isFinite(size) || size <= 0)) {
            throw new IllegalArgumentException("size must be a positive finite number or NaN");
        }
        avatarSize.set(null);
        this.size.set(size);
    }

    public EleFXAvatarSize getAvatarSize() {
        return avatarSize.get();
    }

    public ObjectProperty<EleFXAvatarSize> avatarSizeProperty() {
        return avatarSize;
    }

    public void setAvatarSize(EleFXAvatarSize size) {
        avatarSize.set(size);
    }

    public void setSize(EleFXAvatarSize size) {
        setAvatarSize(size);
    }

    /** A non-null value overrides every member's shape; null preserves each member's own shape. */
    public EleFXAvatarShape getAvatarShape() {
        return avatarShape.get();
    }

    public ObjectProperty<EleFXAvatarShape> avatarShapeProperty() {
        return avatarShape;
    }

    public void setAvatarShape(EleFXAvatarShape shape) {
        avatarShape.set(shape);
    }

    public boolean isCollapseAvatars() {
        return collapseAvatars.get();
    }

    public BooleanProperty collapseAvatarsProperty() {
        return collapseAvatars;
    }

    public void setCollapseAvatars(boolean collapseAvatars) {
        this.collapseAvatars.set(collapseAvatars);
    }

    public int getMaxCollapseAvatars() {
        return maxCollapseAvatars.get();
    }

    public IntegerProperty maxCollapseAvatarsProperty() {
        return maxCollapseAvatars;
    }

    public void setMaxCollapseAvatars(int maxCollapseAvatars) {
        if (maxCollapseAvatars < 1) throw new IllegalArgumentException("maxCollapseAvatars must be at least 1");
        this.maxCollapseAvatars.set(maxCollapseAvatars);
    }

    public boolean isCollapseAvatarsTooltip() {
        return collapseAvatarsTooltip.get();
    }

    public BooleanProperty collapseAvatarsTooltipProperty() {
        return collapseAvatarsTooltip;
    }

    public void setCollapseAvatarsTooltip(boolean collapseAvatarsTooltip) {
        this.collapseAvatarsTooltip.set(collapseAvatarsTooltip);
    }

    public String getCollapseClass() {
        return collapseClass.get();
    }

    public StringProperty collapseClassProperty() {
        return collapseClass;
    }

    public void setCollapseClass(String collapseClass) {
        this.collapseClass.set(collapseClass == null ? "" : collapseClass.trim());
    }

    public String getCollapseStyle() {
        return collapseStyle.get();
    }

    public StringProperty collapseStyleProperty() {
        return collapseStyle;
    }

    public void setCollapseStyle(String collapseStyle) {
        this.collapseStyle.set(collapseStyle == null ? "" : collapseStyle);
    }

    /** The generated {@code +N} Avatar, useful when further JavaFX customization is needed. */
    public EleFXAvatar getCollapseAvatar() {
        return collapseAvatar;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.AVATAR;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setSpacing(-8);
        collapseAvatar.getStyleClass().add(COLLAPSE_STYLE_CLASS);
        size.addListener(o -> refresh());
        avatarSize.addListener(o -> refresh());
        avatarShape.addListener(o -> refresh());
        collapseAvatars.addListener(o -> refresh());
        maxCollapseAvatars.addListener(o -> refresh());
        collapseAvatarsTooltip.addListener(o -> refresh());
        collapseClass.addListener(o -> updateCollapseAppearance());
        collapseStyle.addListener(o -> updateCollapseAppearance());
        getChildren().addListener((ListChangeListener<Node>) change -> {
            if (!refreshing) refresh();
        });
        refresh();
        sceneBuilderIntegration();
    }

    private void refresh() {
        List<EleFXAvatar> avatars = getChildren().stream()
                .filter(EleFXAvatar.class::isInstance)
                .map(EleFXAvatar.class::cast)
                .filter(avatar -> avatar != collapseAvatar)
                .toList();
        applyGroupValues(avatars);
        boolean collapsed = isCollapseAvatars() && avatars.size() > getMaxCollapseAvatars();
        for (int index = 0; index < avatars.size(); index++) {
            boolean visible = !collapsed || index < getMaxCollapseAvatars();
            avatars.get(index).setVisible(visible);
            avatars.get(index).setManaged(visible);
        }
        refreshing = true;
        try {
            if (collapsed) {
                int hiddenCount = avatars.size() - getMaxCollapseAvatars();
                collapseAvatar.setText("+ " + hiddenCount);
                applyGroupValues(List.of(collapseAvatar));
                if (!getChildren().contains(collapseAvatar)) getChildren().add(collapseAvatar);
                configureTooltip(hiddenCount);
            } else {
                getChildren().remove(collapseAvatar);
                configureTooltip(0);
            }
        } finally {
            refreshing = false;
        }
        updateCollapseAppearance();
        requestLayout();
    }

    private void applyGroupValues(List<EleFXAvatar> avatars) {
        for (EleFXAvatar avatar : avatars) {
            if (getAvatarSize() != null)
                avatar.setSize(getAvatarSize());
            else if (!Double.isNaN(getSize())) avatar.setSize(getSize());
            if (getAvatarShape() != null) avatar.setAvatarShape(getAvatarShape());
        }
    }

    private void configureTooltip(int hiddenCount) {
        if (collapseTooltip != null) Tooltip.uninstall(collapseAvatar, collapseTooltip);
        collapseTooltip = null;
        if (hiddenCount > 0 && isCollapseAvatarsTooltip()) {
            collapseTooltip = new Tooltip(hiddenCount + " hidden avatar" + (hiddenCount == 1 ? "" : "s"));
            Tooltip.install(collapseAvatar, collapseTooltip);
        }
    }

    private void updateCollapseAppearance() {
        if (appliedCollapseClass != null) collapseAvatar.getStyleClass().remove(appliedCollapseClass);
        appliedCollapseClass = getCollapseClass().isBlank() ? null : getCollapseClass();
        if (appliedCollapseClass != null && !collapseAvatar.getStyleClass().contains(appliedCollapseClass)) {
            collapseAvatar.getStyleClass().add(appliedCollapseClass);
        }
        collapseAvatar.setStyle(getCollapseStyle());
    }
}

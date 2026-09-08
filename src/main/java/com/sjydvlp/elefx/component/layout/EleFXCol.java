package com.sjydvlp.elefx.component.layout;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Themable;
import com.sjydvlp.elefx.theme.Theme;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.util.List;

/**
 * 24 栅格布局中的列，内容使用 {@link StackPane} 的对齐与边距约束。
 * 与 {@link EleFXRow} 配合使用；列本身占完整栅格宽度，gutter 只缩进列内内容。
 * 这与 Element Plus 的列 padding 语义一致。
 *
 * <p>
 * 响应式配置使用场景宽度（无场景时使用行宽度）：xs &lt; 768，sm &gt;= 768，
 * md &gt;= 992，lg &gt;= 1200，xl &gt;= 1920。较大断点继承较小已生效断点的非空属性，
 * xs 只在宽度小于 768 时生效。可以使用 Element Plus 的 hidden-xs-only、
 * hidden-sm-only / -and-down / -and-up、hidden-md-*、hidden-lg-*、hidden-xl-only 样式类。
 *
 * <p>
 * span、offset、push、pull 的 setter 要求值为 0 到 24；直接修改或绑定属性时，
 * 超出范围的值仅在布局计算中限制到此范围，不修改绑定源。
 * span 为 0 时不占据栅格空间，并临时设置 visible=false，阻止内容接收键盘焦点。
 * 若 visible 已绑定，则使用未绑定的 clip 进行空裁剪，并临时设置未绑定的 disable=true。
 * 恢复时还原调用方的可见性、裁剪与禁用状态，不修改 managed 或解除任何绑定。
 * visible 与 clip 同时绑定时，可见性由调用方决定；visible 与 disable 同时绑定时，
 * 是否允许获取焦点也由调用方的绑定决定。
 */
public class EleFXCol extends StackPane implements Themable {

    private static final String STYLE_CLASS = "ele-col";

    private static final String CONTENT_STYLE_CLASS = "ele-col__content";

    private final IntegerProperty span = new SimpleIntegerProperty(this, "span", 24);

    private final IntegerProperty offset = new SimpleIntegerProperty(this, "offset", 0);

    private final IntegerProperty push = new SimpleIntegerProperty(this, "push", 0);

    private final IntegerProperty pull = new SimpleIntegerProperty(this, "pull", 0);

    private final ObjectProperty<EleFXColSize> xs = new SimpleObjectProperty<>(this, "xs");

    private final ObjectProperty<EleFXColSize> sm = new SimpleObjectProperty<>(this, "sm");

    private final ObjectProperty<EleFXColSize> md = new SimpleObjectProperty<>(this, "md");

    private final ObjectProperty<EleFXColSize> lg = new SimpleObjectProperty<>(this, "lg");

    private final ObjectProperty<EleFXColSize> xl = new SimpleObjectProperty<>(this, "xl");

    private final StringProperty contentStyle = new SimpleStringProperty(this, "contentStyle", "");

    private double gridGutter;

    private final StackPane contentBackground = new StackPane();

    private boolean gridHidden;

    private boolean updatingHidden;

    private final Rectangle hiddenClip = new Rectangle();

    private Node savedClip;

    private boolean visibilitySuppressed;

    private boolean savedVisible;

    private boolean disableSuppressed;

    private boolean savedDisable;

    public EleFXCol() {
        initialize();
    }

    public EleFXCol(Node... children) {
        super(children);
        initialize();
    }

    public EleFXCol(int span, Node... children) {
        super(children);
        setSpan(span);
        initialize();
    }

    public int getSpan() {
        return span.get();
    }

    public IntegerProperty spanProperty() {
        return span;
    }

    public void setSpan(int span) {
        this.span.set(validate("span", span));
    }

    public int getOffset() {
        return offset.get();
    }

    public IntegerProperty offsetProperty() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset.set(validate("offset", offset));
    }

    public int getPush() {
        return push.get();
    }

    public IntegerProperty pushProperty() {
        return push;
    }

    public void setPush(int push) {
        this.push.set(validate("push", push));
    }

    public int getPull() {
        return pull.get();
    }

    public IntegerProperty pullProperty() {
        return pull;
    }

    public void setPull(int pull) {
        this.pull.set(validate("pull", pull));
    }

    public EleFXColSize getXs() {
        return xs.get();
    }

    public ObjectProperty<EleFXColSize> xsProperty() {
        return xs;
    }

    public void setXs(EleFXColSize xs) {
        this.xs.set(xs);
    }

    public void setXs(int span) {
        setXs(new EleFXColSize(span));
    }

    public EleFXColSize getSm() {
        return sm.get();
    }

    public ObjectProperty<EleFXColSize> smProperty() {
        return sm;
    }

    public void setSm(EleFXColSize sm) {
        this.sm.set(sm);
    }

    public void setSm(int span) {
        setSm(new EleFXColSize(span));
    }

    public EleFXColSize getMd() {
        return md.get();
    }

    public ObjectProperty<EleFXColSize> mdProperty() {
        return md;
    }

    public void setMd(EleFXColSize md) {
        this.md.set(md);
    }

    public void setMd(int span) {
        setMd(new EleFXColSize(span));
    }

    public EleFXColSize getLg() {
        return lg.get();
    }

    public ObjectProperty<EleFXColSize> lgProperty() {
        return lg;
    }

    public void setLg(EleFXColSize lg) {
        this.lg.set(lg);
    }

    public void setLg(int span) {
        setLg(new EleFXColSize(span));
    }

    public EleFXColSize getXl() {
        return xl.get();
    }

    public ObjectProperty<EleFXColSize> xlProperty() {
        return xl;
    }

    public void setXl(EleFXColSize xl) {
        this.xl.set(xl);
    }

    public void setXl(int span) {
        setXl(new EleFXColSize(span));
    }

    /**
     * Styles the gutter-aware content background. Unlike {@link #setStyle(String)},
     * this style is confined to the column's inner content area.
     */
    public String getContentStyle() {
        return contentStyle.get();
    }

    public StringProperty contentStyleProperty() {
        return contentStyle;
    }

    public void setContentStyle(String contentStyle) {
        this.contentStyle.set(contentStyle == null ? "" : contentStyle);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.LAYOUT;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    EleFXColSize resolveSize(double viewportWidth) {
        EleFXColSize result = new EleFXColSize(clamp(getSpan()), clamp(getOffset()),
                clamp(getPush()), clamp(getPull()));
        if (viewportWidth < 768) {
            result = merge(result, getXs());
        } else {
            result = merge(result, getSm());
            if (viewportWidth >= 992) {
                result = merge(result, getMd());
            }
            if (viewportWidth >= 1200) {
                result = merge(result, getLg());
            }
            if (viewportWidth >= 1920) {
                result = merge(result, getXl());
            }
        }
        if (hiddenAt(viewportWidth)) {
            return new EleFXColSize(0, result.getOffset(), result.getPush(), result.getPull());
        }
        return result;
    }

    void setGridHidden(boolean hidden) {
        gridHidden = hidden;
        updateGridHidden();
    }

    void setGridGutter(double gutter) {
        double resolvedGutter = Double.isFinite(gutter) && gutter >= 0 ? gutter : 0;
        if (Double.compare(gridGutter, resolvedGutter) != 0) {
            gridGutter = resolvedGutter;
            requestLayout();
        }
    }

    @Override
    protected double computeMinWidth(double height) {
        return super.computeMinWidth(height) + gridGutter;
    }

    @Override
    protected double computePrefWidth(double height) {
        return super.computePrefWidth(height) + gridGutter;
    }

    @Override
    protected double computeMinHeight(double width) {
        return super.computeMinHeight(contentWidth(width));
    }

    @Override
    protected double computePrefHeight(double width) {
        return super.computePrefHeight(contentWidth(width));
    }

    @Override
    protected void layoutChildren() {
        List<Node> managed = getManagedChildren();
        Pos alignment = getAlignment() == null ? Pos.CENTER : getAlignment();
        HPos horizontalAlignment = alignment.getHpos();
        VPos verticalAlignment = alignment.getVpos();
        Insets insets = getInsets();
        double left = insets.getLeft() + gridGutter / 2;
        double top = insets.getTop();
        double contentWidth = Math.max(0, getWidth() - left - insets.getRight() - gridGutter / 2);
        double contentHeight = Math.max(0, getHeight() - top - insets.getBottom());
        contentBackground.resizeRelocate(left, top, contentWidth, contentHeight);
        for (Node child : managed) {
            Pos childAlignment = StackPane.getAlignment(child);
            layoutInArea(child, left, top, contentWidth, contentHeight, 0, StackPane.getMargin(child),
                    childAlignment == null ? horizontalAlignment : childAlignment.getHpos(),
                    childAlignment == null ? verticalAlignment : childAlignment.getVpos());
        }
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        contentBackground.getStyleClass().add(CONTENT_STYLE_CLASS);
        contentBackground.setManaged(false);
        contentBackground.setMouseTransparent(true);
        getChildren().add(0, contentBackground);
        InvalidationListener layoutListener = observable -> requestGridLayout();
        span.addListener(layoutListener);
        offset.addListener(layoutListener);
        push.addListener(layoutListener);
        pull.addListener(layoutListener);
        xs.addListener(layoutListener);
        sm.addListener(layoutListener);
        md.addListener(layoutListener);
        lg.addListener(layoutListener);
        xl.addListener(layoutListener);
        contentStyle.addListener(
                (observable, oldValue, newValue) -> contentBackground.setStyle(newValue == null ? "" : newValue));
        contentBackground.setStyle(getContentStyle() == null ? "" : getContentStyle());
        getStyleClass().addListener(layoutListener);
        clipProperty().addListener(observable -> updateGridHidden());
        visibleProperty().addListener(observable -> updateGridHidden());
        disableProperty().addListener(observable -> updateGridHidden());
        sceneBuilderIntegration();
    }

    private void requestGridLayout() {
        requestLayout();
        if (getParent() != null) {
            getParent().requestLayout();
        }
    }

    private double contentWidth(double width) {
        return width < 0 ? width : Math.max(0, width - gridGutter);
    }

    private boolean hiddenAt(double width) {
        for (String styleClass : getStyleClass()) {
            switch (styleClass) {
                case "hidden-xs-only":
                    if (width < 768) return true;
                    break;
                case "hidden-sm-only":
                    if (width >= 768 && width < 992) return true;
                    break;
                case "hidden-sm-and-down":
                    if (width < 992) return true;
                    break;
                case "hidden-sm-and-up":
                    if (width >= 768) return true;
                    break;
                case "hidden-md-only":
                    if (width >= 992 && width < 1200) return true;
                    break;
                case "hidden-md-and-down":
                    if (width < 1200) return true;
                    break;
                case "hidden-md-and-up":
                    if (width >= 992) return true;
                    break;
                case "hidden-lg-only":
                    if (width >= 1200 && width < 1920) return true;
                    break;
                case "hidden-lg-and-down":
                    if (width < 1920) return true;
                    break;
                case "hidden-lg-and-up":
                    if (width >= 1200) return true;
                    break;
                case "hidden-xl-only":
                    if (width >= 1920) return true;
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    private void updateGridHidden() {
        if (updatingHidden) {
            return;
        }
        updatingHidden = true;
        try {
            if (gridHidden) {
                if (!visibleProperty().isBound()) {
                    restoreClip();
                    restoreDisable();
                    if (!visibilitySuppressed || isVisible()) {
                        savedVisible = isVisible();
                    }
                    visibilitySuppressed = true;
                    setVisible(false);
                } else {
                    restoreVisibility();
                    if (!clipProperty().isBound() && getClip() != hiddenClip) {
                        savedClip = getClip();
                        setClip(hiddenClip);
                    }
                    if (!disableProperty().isBound()) {
                        if (!disableSuppressed || !isDisable()) {
                            savedDisable = isDisable();
                        }
                        disableSuppressed = true;
                        setDisable(true);
                    } else {
                        // A newly installed binding owns the current disabled state.
                        disableSuppressed = false;
                    }
                }
            } else {
                restoreClip();
                restoreVisibility();
                restoreDisable();
            }
        } finally {
            updatingHidden = false;
        }
    }

    private void restoreClip() {
        if (!clipProperty().isBound() && getClip() == hiddenClip) {
            setClip(savedClip);
        }
        savedClip = null;
    }

    private void restoreVisibility() {
        if (visibilitySuppressed) {
            visibilitySuppressed = false;
            if (!visibleProperty().isBound() && !isVisible()) {
                setVisible(savedVisible);
            }
        }
    }

    private void restoreDisable() {
        if (disableSuppressed) {
            disableSuppressed = false;
            if (!disableProperty().isBound() && isDisable()) {
                setDisable(savedDisable);
            }
        }
    }

    private static EleFXColSize merge(EleFXColSize base, EleFXColSize override) {
        if (override == null) {
            return base;
        }
        return new EleFXColSize(override.getSpan() == null ? base.getSpan() : override.getSpan(),
                override.getOffset() == null ? base.getOffset() : override.getOffset(),
                override.getPush() == null ? base.getPush() : override.getPush(),
                override.getPull() == null ? base.getPull() : override.getPull());
    }

    private static int validate(String name, int value) {
        if (value < 0 || value > 24) {
            throw new IllegalArgumentException(name + " must be between 0 and 24");
        }
        return value;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(24, value));
    }
}

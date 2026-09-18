package com.sjydvlp.elefx.component.result;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Group;
import javafx.scene.shape.SVGPath;

/**
 * An Element Plus-inspired result view for communicating an operation outcome
 * or an access exception.
 *
 * <p>
 * The {@linkplain #getIconNode() icon}, {@linkplain #getTitleNode() title},
 * {@linkplain #getSubTitleNode() subtitle}, and {@linkplain #getExtraChildren()
 * extra children} provide JavaFX equivalents of Element Plus's named slots.
 * </p>
 */
public class EleFXResult extends VBox implements Themable {

    public static final double DEFAULT_ICON_SIZE = 64;

    private static final String STYLE_CLASS = "ele-result";

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final StringProperty subTitle = new SimpleStringProperty(this, "subTitle", "");

    private final ObjectProperty<EleFXResultIcon> icon = new SimpleObjectProperty<>(this, "icon", EleFXResultIcon.INFO);

    private final ObjectProperty<Node> iconNode = new SimpleObjectProperty<>(this, "iconNode");

    private final ObjectProperty<Node> titleNode = new SimpleObjectProperty<>(this, "titleNode");

    private final ObjectProperty<Node> subTitleNode = new SimpleObjectProperty<>(this, "subTitleNode");

    private final StackPane iconBox = new StackPane();

    private final StackPane titleBox = new StackPane();

    private final StackPane subTitleBox = new StackPane();

    private final VBox extra = new VBox();

    private final Label titleLabel = new Label();

    private final Label subTitleLabel = new Label();

    public EleFXResult() {
        initialize();
    }

    public EleFXResult(String title) {
        setTitle(title);
        initialize();
    }

    public EleFXResult(EleFXResultIcon icon, String title, String subTitle) {
        setIcon(icon);
        setTitle(title);
        setSubTitle(subTitle);
        initialize();
    }

    /** Text used when no custom title node is supplied. */
    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String value) {
        title.set(value == null ? "" : value);
    }

    /** Text used when no custom subtitle node is supplied. */
    public String getSubTitle() {
        return subTitle.get();
    }

    public StringProperty subTitleProperty() {
        return subTitle;
    }

    public void setSubTitle(String value) {
        subTitle.set(value == null ? "" : value);
    }

    /** Semantic icon state; {@code null} is resolved to {@link EleFXResultIcon#INFO}. */
    public EleFXResultIcon getIcon() {
        return icon.get();
    }

    public ObjectProperty<EleFXResultIcon> iconProperty() {
        return icon;
    }

    public void setIcon(EleFXResultIcon value) {
        icon.set(value == null ? EleFXResultIcon.INFO : value);
    }

    /** Custom icon content, corresponding to Element Plus's {@code icon} slot. */
    public Node getIconNode() {
        return iconNode.get();
    }

    public ObjectProperty<Node> iconNodeProperty() {
        return iconNode;
    }

    public void setIconNode(Node value) {
        iconNode.set(value);
    }

    /** Custom title content, corresponding to Element Plus's {@code title} slot. */
    public Node getTitleNode() {
        return titleNode.get();
    }

    public ObjectProperty<Node> titleNodeProperty() {
        return titleNode;
    }

    public void setTitleNode(Node value) {
        titleNode.set(value);
    }

    /** Custom subtitle content, corresponding to Element Plus's {@code sub-title} slot. */
    public Node getSubTitleNode() {
        return subTitleNode.get();
    }

    public ObjectProperty<Node> subTitleNodeProperty() {
        return subTitleNode;
    }

    public void setSubTitleNode(Node value) {
        subTitleNode.set(value);
    }

    /** Container for content following the result message, like Element Plus's {@code extra} slot. */
    public VBox getExtra() {
        return extra;
    }

    public ObservableList<Node> getExtraChildren() {
        return extra.getChildren();
    }

    /** Replaces the content of the extra area with one node. */
    public void setExtra(Node value) {
        if (value == null) {
            extra.getChildren().clear();
        } else {
            extra.getChildren().setAll(value);
        }
    }

    /** Replaces the content of the extra area with the supplied nodes. */
    public void setExtra(Node... values) {
        extra.getChildren().setAll(values == null ? new Node[0] : values);
    }

    public StackPane getIconBox() {
        return iconBox;
    }

    public StackPane getTitleBox() {
        return titleBox;
    }

    public StackPane getSubTitleBox() {
        return subTitleBox;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.RESULT;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setAlignment(Pos.TOP_CENTER);
        setFillWidth(false);
        iconBox.getStyleClass().add("ele-result__icon");
        titleBox.getStyleClass().add("ele-result__title");
        subTitleBox.getStyleClass().add("ele-result__subtitle");
        extra.getStyleClass().add("ele-result__extra");
        extra.setAlignment(Pos.TOP_CENTER);
        titleLabel.getStyleClass().add("ele-result__title-text");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        subTitleLabel.getStyleClass().add("ele-result__subtitle-text");
        subTitleLabel.setWrapText(true);
        subTitleLabel.setAlignment(Pos.CENTER);
        getChildren().addAll(iconBox, titleBox, subTitleBox, extra);

        title.addListener(o -> refreshTitle());
        subTitle.addListener(o -> refreshSubTitle());
        icon.addListener((o, oldValue, value) -> refreshIcon());
        iconNode.addListener(o -> refreshIcon());
        titleNode.addListener(o -> refreshTitle());
        subTitleNode.addListener(o -> refreshSubTitle());
        extra.getChildren().addListener((javafx.collections.ListChangeListener<Node>) change -> refreshExtra());
        refreshIcon();
        refreshTitle();
        refreshSubTitle();
        refreshExtra();
        sceneBuilderIntegration();
    }

    private void refreshIcon() {
        EleFXResultIcon state = getIcon() == null ? EleFXResultIcon.INFO : getIcon();
        getStyleClass().removeIf(style -> style.startsWith("ele-result--"));
        getStyleClass().add("ele-result--" + state.name().toLowerCase(java.util.Locale.ROOT));
        iconBox.getChildren().setAll(getIconNode() == null ? createDefaultIcon(state) : getIconNode());
    }

    private void refreshTitle() {
        Node content = getTitleNode();
        if (content == null) {
            titleLabel.setText(getTitle());
            content = titleLabel;
        }
        boolean present = content != titleLabel || !getTitle().isBlank();
        titleBox.getChildren().setAll(content);
        titleBox.setManaged(present);
        titleBox.setVisible(present);
    }

    private void refreshSubTitle() {
        Node content = getSubTitleNode();
        if (content == null) {
            subTitleLabel.setText(getSubTitle());
            content = subTitleLabel;
        }
        boolean present = content != subTitleLabel || !getSubTitle().isBlank();
        subTitleBox.getChildren().setAll(content);
        subTitleBox.setManaged(present);
        subTitleBox.setVisible(present);
    }

    private void refreshExtra() {
        boolean present = !extra.getChildren().isEmpty();
        extra.setManaged(present);
        extra.setVisible(present);
    }

    /** Result's dedicated 48px SVG glyphs, scaled to Element Plus's 64px default. */
    private static Node createDefaultIcon(EleFXResultIcon state) {
        SVGPath path = new SVGPath();
        path.getStyleClass().add("ele-result__icon-svg");
        path.setContent(switch (state) {
            case PRIMARY -> "M24,4C35.046,4 44,12.954 44,24C44,35.046 35.046,44 24,44C12.954,44 4,35.046 4,24C4,12.954 12.954,4 24,4ZM24,13C23.172,13 22.5,13.672 22.5,14.5V22.5H14.5C13.672,22.5 13,23.172 13,24C13,24.828 13.672,25.5 14.5,25.5H22.5V33.5C22.5,34.328 23.172,35 24,35C24.828,35 25.5,34.328 25.5,33.5V25.5H33.5C34.328,25.5 35,24.828 35,24C35,23.172 34.328,22.5 33.5,22.5H25.5V14.5C25.5,13.672 24.828,13 24,13Z";
            case SUCCESS -> "M24,4C35.046,4 44,12.954 44,24C44,35.046 35.046,44 24,44C12.954,44 4,35.046 4,24C4,12.954 12.954,4 24,4ZM34.555,16.449C33.961,15.85 32.999,15.85 32.405,16.449L21.414,27.581L21.403,27.591C21.01,27.978 20.377,27.973 19.989,27.579L15.595,23.112C15.001,22.514 14.039,22.514 13.445,23.112C12.852,23.71 12.852,24.68 13.445,25.278L19.626,31.551C20.22,32.15 21.182,32.15 21.776,31.551L34.555,18.614C35.148,18.016 35.148,17.047 34.555,16.449Z";
            case WARNING -> "M24,4C35.046,4 44,12.954 44,24C44,35.046 35.046,44 24,44C12.954,44 4,35.046 4,24C4,12.954 12.954,4 24,4ZM24,12.5C23.172,12.5 22.5,13.172 22.5,14V26C22.5,26.828 23.172,27.5 24,27.5C24.828,27.5 25.5,26.828 25.5,26V14C25.5,13.172 24.828,12.5 24,12.5ZM24,31C22.895,31 22,31.895 22,33C22,34.105 22.895,35 24,35C25.105,35 26,34.105 26,33C26,31.895 25.105,31 24,31Z";
            case ERROR -> "M24,4C35.046,4 44,12.954 44,24C44,35.046 35.046,44 24,44C12.954,44 4,35.046 4,24C4,12.954 12.954,4 24,4ZM17.439,17.439C16.854,18.025 16.854,18.975 17.439,19.561L21.879,24L17.439,28.439C16.854,29.025 16.854,29.975 17.439,30.561C18.025,31.146 18.975,31.146 19.561,30.561L24,26.121L28.439,30.561C29.025,31.146 29.975,31.146 30.561,30.561C31.146,29.975 31.146,29.025 30.561,28.439L26.121,24L30.561,19.561C31.146,18.975 31.146,18.025 30.561,17.439C29.975,16.854 29.025,16.854 28.439,17.439L24,21.879L19.561,17.439C18.975,16.854 18.025,16.854 17.439,17.439Z";
            case INFO -> "M24,4C35.046,4 44,12.954 44,24C44,35.046 35.046,44 24,44C12.954,44 4,35.046 4,24C4,12.954 12.954,4 24,4ZM24,19H21C20.172,19 19.5,19.672 19.5,20.5C19.5,21.328 20.172,22 21,22H22.5V31H21C20.172,31 19.5,31.672 19.5,32.5C19.5,33.328 20.172,34 21,34H27C27.828,34 28.5,33.328 28.5,32.5C28.5,31.672 27.828,31 27,31H25.5V20.5C25.5,19.672 24.828,19 24,19ZM24,13C22.895,13 22,13.895 22,15C22,16.105 22.895,17 24,17C25.105,17 26,16.105 26,15C26,13.895 25.105,13 24,13Z";
        });
        Group icon = new Group(path);
        icon.getStyleClass().add("ele-result__default-icon");
        double scale = DEFAULT_ICON_SIZE / 48.0;
        icon.setScaleX(scale);
        icon.setScaleY(scale);
        return icon;
    }
}

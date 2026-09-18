package com.sjydvlp.elefx.component.result;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.component.icon.EleFXIcons;
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
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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
        // Result text uses the available content width so long subtitles wrap.
        setFillWidth(true);
        iconBox.getStyleClass().add("ele-result__icon");
        titleBox.getStyleClass().add("ele-result__title");
        subTitleBox.getStyleClass().add("ele-result__subtitle");
        subTitleBox.setMaxWidth(Double.MAX_VALUE);
        extra.getStyleClass().add("ele-result__extra");
        extra.setAlignment(Pos.TOP_CENTER);
        titleLabel.getStyleClass().add("ele-result__title-text");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        subTitleLabel.getStyleClass().add("ele-result__subtitle-text");
        subTitleLabel.setWrapText(true);
        subTitleLabel.setTextOverrun(OverrunStyle.CLIP);
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

    /** Creates the result state glyph from EleFX's shared icon library. */
    private static EleFXIcon createDefaultIcon(EleFXResultIcon state) {
        EleFXIcon icon = EleFXIcons.of(switch (state) {
            case PRIMARY -> EleFXIconType.INFO_FILLED;
            case SUCCESS -> EleFXIconType.CIRCLE_CHECK_FILLED;
            case WARNING -> EleFXIconType.WARNING_FILLED;
            case ERROR -> EleFXIconType.CIRCLE_CLOSE_FILLED;
            case INFO -> EleFXIconType.INFO_FILLED;
        }, DEFAULT_ICON_SIZE);
        icon.getStyleClass().add("ele-result__default-icon");
        return icon;
    }
}

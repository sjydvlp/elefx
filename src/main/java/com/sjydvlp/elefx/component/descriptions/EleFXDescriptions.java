package com.sjydvlp.elefx.component.descriptions;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Element Plus inspired labelled data grid. Items are exposed by {@link #getItems()}.
 * Custom title, extra, label and content nodes mirror the corresponding named slots.
 */
public class EleFXDescriptions extends VBox implements Themable {

    /** Horizontal padding (12px on each side) applied by the bordered label CSS. */
    private static final double BORDERED_LABEL_HORIZONTAL_PADDING = 24;

    /** Default label column width used when no component or item width is supplied. */
    private static final double DEFAULT_LABEL_MIN_WIDTH = 140;

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final ObjectProperty<Node> titleNode = new SimpleObjectProperty<>(this, "titleNode");

    private final StringProperty extra = new SimpleStringProperty(this, "extra", "");

    private final ObjectProperty<Node> extraNode = new SimpleObjectProperty<>(this, "extraNode");

    private final BooleanProperty bordered = new SimpleBooleanProperty(this, "bordered", false);

    private final IntegerProperty column = new SimpleIntegerProperty(this, "column", 3);

    private final ObjectProperty<EleFXDescriptionsDirection> direction = new SimpleObjectProperty<>(this, "direction",
            EleFXDescriptionsDirection.HORIZONTAL);

    private final ObjectProperty<EleFXDescriptionsSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXDescriptionsSize.DEFAULT);

    private final javafx.beans.property.DoubleProperty labelWidth = new javafx.beans.property.SimpleDoubleProperty(this,
            "labelWidth", -1);

    private final ObservableList<EleFXDescriptionsItem> items = FXCollections.observableArrayList();

    private final HBox header = new HBox();

    private final Label titleLabel = new Label();

    private final Label extraLabel = new Label();

    private final GridPane grid = new GridPane();

    private final Map<EleFXDescriptionsItem, InvalidationListener> observedItems = new IdentityHashMap<>();

    public EleFXDescriptions() {
        initialize();
    }

    /** Creates a descriptions grid with a text title. */
    public EleFXDescriptions(String title) {
        this();
        setTitle(title);
    }

    public EleFXDescriptions(EleFXDescriptionsItem... items) {
        this();
        getItems().addAll(items);
    }

    /** Creates a descriptions grid with a text title and initial items. */
    public EleFXDescriptions(String title, EleFXDescriptionsItem... items) {
        this(title);
        getItems().addAll(items);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String value) {
        title.set(value == null ? "" : value);
    }

    public Node getTitleNode() {
        return titleNode.get();
    }

    public ObjectProperty<Node> titleNodeProperty() {
        return titleNode;
    }

    public void setTitleNode(Node value) {
        titleNode.set(value);
    }

    public String getExtra() {
        return extra.get();
    }

    public StringProperty extraProperty() {
        return extra;
    }

    public void setExtra(String value) {
        extra.set(value == null ? "" : value);
    }

    public Node getExtraNode() {
        return extraNode.get();
    }

    public ObjectProperty<Node> extraNodeProperty() {
        return extraNode;
    }

    public void setExtraNode(Node value) {
        extraNode.set(value);
    }

    public boolean isBorder() {
        return bordered.get();
    }

    /** Whether cells are rendered with the Element Plus table border treatment. */
    public BooleanProperty borderedProperty() {
        return bordered;
    }

    public void setBorder(boolean value) {
        bordered.set(value);
    }

    public int getColumn() {
        return column.get();
    }

    public IntegerProperty columnProperty() {
        return column;
    }

    public void setColumn(int value) {
        column.set(Math.max(1, value));
    }

    public EleFXDescriptionsDirection getDirection() {
        return direction.get();
    }

    public ObjectProperty<EleFXDescriptionsDirection> directionProperty() {
        return direction;
    }

    public void setDirection(EleFXDescriptionsDirection value) {
        direction.set(value == null ? EleFXDescriptionsDirection.HORIZONTAL : value);
    }

    public EleFXDescriptionsSize getSize() {
        return size.get();
    }

    public ObjectProperty<EleFXDescriptionsSize> sizeProperty() {
        return size;
    }

    public void setSize(EleFXDescriptionsSize value) {
        size.set(value == null ? EleFXDescriptionsSize.DEFAULT : value);
    }

    public double getLabelWidth() {
        return labelWidth.get();
    }

    public javafx.beans.property.DoubleProperty labelWidthProperty() {
        return labelWidth;
    }

    public void setLabelWidth(double value) {
        labelWidth.set(value < 0 ? -1 : value);
    }

    public ObservableList<EleFXDescriptionsItem> getItems() {
        return items;
    }

    public HBox getHeader() {
        return header;
    }

    public GridPane getGrid() {
        return grid;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.DESCRIPTIONS;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    /**
     * Descriptions is a block-level data component. Prefer the already-laid-out parent width so
     * it fills a row even when hosted by a generic {@link javafx.scene.layout.Pane}.
     */
    @Override
    protected double computePrefWidth(double height) {
        double preferredWidth = super.computePrefWidth(height);
        Parent parent = getParent();
        if (parent == null) return preferredWidth;
        return Math.max(preferredWidth, parent.getLayoutBounds().getWidth());
    }

    private void initialize() {
        getStyleClass().add("ele-descriptions");
        setMaxWidth(Double.MAX_VALUE);
        setFillWidth(true);
        header.getStyleClass().add("ele-descriptions__header");
        titleLabel.getStyleClass().add("ele-descriptions__title");
        extraLabel.getStyleClass().add("ele-descriptions__extra");
        grid.getStyleClass().add("ele-descriptions__body");
        header.setAlignment(Pos.CENTER_LEFT);
        grid.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        getChildren().addAll(header, grid);
        InvalidationListener refresh = ignored -> rebuild();
        title.addListener(refresh);
        titleNode.addListener(refresh);
        extra.addListener(refresh);
        extraNode.addListener(refresh);
        bordered.addListener(refresh);
        column.addListener(refresh);
        direction.addListener(refresh);
        size.addListener(refresh);
        labelWidth.addListener(refresh);
        items.addListener((ListChangeListener<EleFXDescriptionsItem>) change -> {
            while (change.next()) {
                change.getRemoved().forEach(this::unobserve);
                change.getAddedSubList().forEach(this::observe);
            }
            rebuild();
        });
        rebuild();
        sceneBuilderIntegration();
    }

    private void observe(EleFXDescriptionsItem item) {
        InvalidationListener listener = ignored -> rebuild();
        List<javafx.beans.Observable> properties = List.of(item.labelProperty(), item.labelNodeProperty(),
                item.contentProperty(),
                item.spanProperty(), item.rowSpanProperty(), item.widthProperty(), item.minWidthProperty(),
                item.labelWidthProperty(),
                item.alignmentProperty(), item.labelAlignmentProperty(), item.styleClassProperty(),
                item.labelStyleClassProperty());
        properties.forEach(property -> property.addListener(listener));
        observedItems.put(item, listener);
    }

    private void unobserve(EleFXDescriptionsItem item) {
        InvalidationListener listener = observedItems.remove(item);
        if (listener == null) return;
        List.of(item.labelProperty(), item.labelNodeProperty(), item.contentProperty(), item.spanProperty(),
                item.rowSpanProperty(),
                item.widthProperty(), item.minWidthProperty(), item.labelWidthProperty(), item.alignmentProperty(),
                item.labelAlignmentProperty(), item.styleClassProperty(), item.labelStyleClassProperty())
                .forEach(property -> property.removeListener(listener));
    }

    private void rebuild() {
        refreshHeader();
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        int columns = Math.max(1, getColumn());
        for (int i = 0; i < columns; i++) {
            ColumnConstraints constraint = new ColumnConstraints();
            constraint.setPercentWidth(100.0 / columns);
            constraint.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(constraint);
        }
        double resolvedLabelWidth = resolveLabelWidth();
        List<boolean[]> occupied = new java.util.ArrayList<>();
        occupied.add(new boolean[columns]);
        int row = 0, col = 0;
        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            EleFXDescriptionsItem item = items.get(itemIndex);
            while (col >= columns || occupied.get(row)[col]) {
                if (++col >= columns) {
                    row++;
                    col = 0;
                    ensureRows(occupied, row + 1, columns);
                }
            }
            int span = Math.min(columns - col, Math.max(1, item.getSpan()));
            int rowSpan = Math.max(1, item.getRowSpan());
            ensureRows(occupied, row + rowSpan, columns);
            while (!fits(occupied, row, col, span, rowSpan)) {
                if (++col >= columns) {
                    row++;
                    col = 0;
                    ensureRows(occupied, row + rowSpan, columns);
                }
                while (occupied.get(row)[col]) {
                    if (++col >= columns) {
                        row++;
                        col = 0;
                        ensureRows(occupied, row + rowSpan, columns);
                    }
                }
            }
            // Element Plus extends the final item through the unused cells in its row.
            // This closes the table's outer border when the item count is not a multiple of column.
            int remainingSpan = columns - col;
            if (itemIndex == items.size() - 1 && remainingSpan > span
                    && fits(occupied, row, col, remainingSpan, rowSpan)) {
                span = remainingSpan;
            }
            Region cell = createCell(item, resolvedLabelWidth);
            grid.add(cell, col, row, span, rowSpan);
            if (item.getWidth() >= 0)
                cell.setMinWidth(item.getWidth());
            else if (item.getMinWidth() >= 0) cell.setMinWidth(item.getMinWidth());
            for (int r = row; r < row + rowSpan; r++)
                for (int c = col; c < col + span; c++)
                    occupied.get(r)[c] = true;
        }
        updateClasses();
        requestLayout();
    }

    private void refreshHeader() {
        Node left = getTitleNode() == null ? titleLabel : getTitleNode();
        Node right = getExtraNode() == null ? extraLabel : getExtraNode();
        titleLabel.setText(getTitle());
        extraLabel.setText(getExtra());
        boolean hasTitle = getTitleNode() != null || !getTitle().isBlank();
        boolean hasExtra = getExtraNode() != null || !getExtra().isBlank();
        header.getChildren().clear();
        if (hasTitle) {
            header.getChildren().add(left);
            HBox.setHgrow(left, Priority.ALWAYS);
            if (left instanceof Region region) region.setMaxWidth(Double.MAX_VALUE);
        }
        if (hasExtra) header.getChildren().add(right);
        boolean visible = hasTitle || hasExtra;
        header.setManaged(visible);
        header.setVisible(visible);
    }

    private double resolveLabelWidth() {
        if (getLabelWidth() >= 0) return getLabelWidth();
        if (!isBorder() || getDirection() != EleFXDescriptionsDirection.HORIZONTAL) return -1;
        double widestLabel = items.stream().filter(item -> item.getLabelWidth() < 0).mapToDouble(this::labelPrefWidth)
                .max().orElse(-1);
        return widestLabel < 0
                ? DEFAULT_LABEL_MIN_WIDTH
                : Math.max(DEFAULT_LABEL_MIN_WIDTH, widestLabel + BORDERED_LABEL_HORIZONTAL_PADDING);
    }

    private double labelPrefWidth(EleFXDescriptionsItem item) {
        Node label = item.getLabelNode();
        if (label instanceof Region region) return region.prefWidth(-1);
        Text text = new Text(item.getLabel());
        text.setFont(Font.font(switch (getSize()) {
            case LARGE -> 16;
            case SMALL -> 12;
            default -> 14;
        }));
        return Math.ceil(text.getLayoutBounds().getWidth());
    }

    private Region createCell(EleFXDescriptionsItem item, double resolvedLabelWidth) {
        Node labelContent = item.getLabelNode() == null ? new Label(item.getLabel()) : item.getLabelNode();
        StackPane label = new StackPane(labelContent);
        Node content = item.getContent() == null ? new Label() : item.getContent();
        addStyleClass(label, "ele-descriptions__label");
        addStyleClass(content, "ele-descriptions__content");
        applyClasses(label, item.getLabelStyleClass());
        applyClasses(content, item.getStyleClass());
        Pos labelPos = item.getLabelAlignment() == null ? item.getAlignment() : item.getLabelAlignment();
        label.setAlignment(labelPos);
        if (getDirection() == EleFXDescriptionsDirection.VERTICAL) {
            VBox cell = new VBox(label, content);
            cell.getStyleClass().addAll("ele-descriptions__cell", "ele-descriptions__cell--vertical");
            cell.setAlignment(Pos.TOP_LEFT);
            cell.setFillWidth(true);
            label.setMaxWidth(Double.MAX_VALUE);
            VBox.setVgrow(content, Priority.ALWAYS);
            if (content instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
            }
            return cell;
        }
        HBox cell = new HBox(label, content);
        cell.getStyleClass().addAll("ele-descriptions__cell", "ele-descriptions__cell--horizontal");
        cell.setAlignment(Pos.TOP_LEFT);
        cell.setFillHeight(true);
        HBox.setHgrow(content, Priority.ALWAYS);
        label.setMaxHeight(Double.MAX_VALUE);
        double itemLabelWidth = item.getLabelWidth() >= 0 ? item.getLabelWidth() : resolvedLabelWidth;
        if (itemLabelWidth >= 0) {
            label.setMinWidth(itemLabelWidth);
            label.setPrefWidth(itemLabelWidth);
            label.setMaxWidth(itemLabelWidth);
        }
        if (content instanceof Region region) region.setMaxHeight(Double.MAX_VALUE);
        if (labelContent instanceof Label value) {
            value.setAlignment(labelPos);
            value.setWrapText(true);
            value.setTextOverrun(OverrunStyle.CLIP);
        }
        return cell;
    }

    private void updateClasses() {
        getStyleClass().removeIf(value -> value.startsWith("ele-descriptions--"));
        getStyleClass().add("ele-descriptions--" + getSize().name().toLowerCase());
        getStyleClass().add("ele-descriptions--" + getDirection().name().toLowerCase());
        if (isBorder()) getStyleClass().add("ele-descriptions--bordered");
    }

    private static void addStyleClass(Node node, String styleClass) {
        if (!node.getStyleClass().contains(styleClass)) node.getStyleClass().add(styleClass);
    }

    private static void applyClasses(Node node, String classes) {
        if (!classes.isBlank()) node.getStyleClass().addAll(classes.split("\\s+"));
    }

    private static boolean fits(List<boolean[]> used, int row, int col, int width, int height) {
        if (col + width > used.get(0).length) return false;
        for (int r = row; r < row + height; r++)
            for (int c = col; c < col + width; c++)
                if (used.get(r)[c]) return false;
        return true;
    }

    private static void ensureRows(List<boolean[]> used, int required, int columns) {
        while (used.size() < required)
            used.add(new boolean[columns]);
    }
}

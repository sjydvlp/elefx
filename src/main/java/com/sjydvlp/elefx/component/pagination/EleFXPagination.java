package com.sjydvlp.elefx.component.pagination;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Element Plus inspired pagination control.
 *
 * <p>
 * The observable {@link #getLayout() layout} list controls which parts are shown.
 * </p>
 */
public class EleFXPagination extends HBox implements Themable {

    private final IntegerProperty total = new SimpleIntegerProperty(this, "total", 0);

    private final IntegerProperty pageCount = new SimpleIntegerProperty(this, "pageCount", 0);

    private final IntegerProperty pageSize = new SimpleIntegerProperty(this, "pageSize", 10);

    private final IntegerProperty currentPage = new SimpleIntegerProperty(this, "currentPage", 1);

    private final IntegerProperty pagerCount = new SimpleIntegerProperty(this, "pagerCount", 7);

    private final ObservableList<EleFXPaginationLayout> layout = FXCollections.observableArrayList(
            EleFXPaginationLayout.PREV, EleFXPaginationLayout.PAGER, EleFXPaginationLayout.NEXT,
            EleFXPaginationLayout.JUMPER, EleFXPaginationLayout.RIGHT, EleFXPaginationLayout.TOTAL);

    private final ObservableList<Integer> pageSizes = FXCollections.observableArrayList(10, 20, 30, 40, 50, 100);

    private final BooleanProperty background = new SimpleBooleanProperty(this, "background", false);

    private final BooleanProperty hideOnSinglePage = new SimpleBooleanProperty(this, "hideOnSinglePage", false);

    private final ObjectProperty<EleFXPaginationSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXPaginationSize.DEFAULT);

    private final StringProperty prevText = new SimpleStringProperty(this, "prevText", "");

    private final StringProperty nextText = new SimpleStringProperty(this, "nextText", "");

    private final ObjectProperty<Node> slot = new SimpleObjectProperty<>(this, "slot");

    private final ObjectProperty<EventHandler<EleFXPaginationEvent>> onCurrentChange = new SimpleObjectProperty<>(this,
            "onCurrentChange");

    private final ObjectProperty<EventHandler<EleFXPaginationEvent>> onSizeChange = new SimpleObjectProperty<>(this,
            "onSizeChange");

    private final ObjectProperty<EventHandler<EleFXPaginationEvent>> onPrevClick = new SimpleObjectProperty<>(this,
            "onPrevClick");

    private final ObjectProperty<EventHandler<EleFXPaginationEvent>> onNextClick = new SimpleObjectProperty<>(this,
            "onNextClick");

    private boolean normalizing;

    /** Keeps the page-size trigger active after a selection until the user clicks away. */
    private boolean sizesActive;

    public EleFXPagination() {
        getStyleClass().add("ele-pagination");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(8);
        total.addListener((o, a, b) -> refresh());
        pageCount.addListener((o, a, b) -> refresh());
        pageSize.addListener((o, a, b) -> {
            validatePositive(b.intValue(), "pageSize");
            int old = a.intValue();
            refresh();
            fire(onSizeChange.get(), EleFXPaginationEvent.SIZE_CHANGE, old, getPageSize());
        });
        currentPage.addListener((o, a, b) -> {
            normalizeCurrentPage();
            if (!normalizing && a.intValue() != getCurrentPage())
                fire(onCurrentChange.get(), EleFXPaginationEvent.CURRENT_CHANGE, a.intValue(), getCurrentPage());
            refresh();
        });
        pagerCount.addListener((o, a, b) -> {
            validatePagerCount(b.intValue());
            refresh();
        });
        layout.addListener((javafx.collections.ListChangeListener<EleFXPaginationLayout>) change -> refresh());
        pageSizes.addListener((javafx.collections.ListChangeListener<Integer>) c -> refresh());
        background.addListener((o, a, b) -> updateClasses());
        size.addListener((o, a, b) -> updateClasses());
        hideOnSinglePage.addListener((o, a, b) -> refresh());
        prevText.addListener((o, a, b) -> refresh());
        nextText.addListener((o, a, b) -> refresh());
        slot.addListener((o, a, b) -> refresh());
        disableProperty().addListener((o, a, b) -> refresh());
        updateClasses();
        refresh();
        sceneBuilderIntegration();
    }

    public EleFXPagination(int total) {
        this();
        setTotal(total);
    }

    public int getTotal() {
        return total.get();
    }

    public void setTotal(int value) {
        if (value < 0) throw new IllegalArgumentException("total must not be negative");
        total.set(value);
    }

    public IntegerProperty totalProperty() {
        return total;
    }

    /** Explicit page count; a positive value takes priority over {@link #getTotal()}. */
    public int getPageCount() {
        return pageCount.get();
    }

    public void setPageCount(int value) {
        if (value < 0) throw new IllegalArgumentException("pageCount must not be negative");
        pageCount.set(value);
    }

    public IntegerProperty pageCountProperty() {
        return pageCount;
    }

    public int getPageSize() {
        return pageSize.get();
    }

    public void setPageSize(int value) {
        validatePositive(value, "pageSize");
        pageSize.set(value);
    }

    public IntegerProperty pageSizeProperty() {
        return pageSize;
    }

    public int getCurrentPage() {
        return currentPage.get();
    }

    public void setCurrentPage(int value) {
        currentPage.set(value);
    }

    public IntegerProperty currentPageProperty() {
        return currentPage;
    }

    public int getPagerCount() {
        return pagerCount.get();
    }

    public void setPagerCount(int value) {
        validatePagerCount(value);
        pagerCount.set(value);
    }

    public IntegerProperty pagerCountProperty() {
        return pagerCount;
    }

    /** The observable, ordered list of visible pagination sections. */
    public ObservableList<EleFXPaginationLayout> getLayout() {
        return layout;
    }

    /**
     * Sets the visible parts with compile-time checked layout tokens.
     *
     * <pre>
     * {@code
     * pagination.setLayout(EleFXPaginationLayout.PREV,
     *         EleFXPaginationLayout.PAGER, EleFXPaginationLayout.NEXT);
     * }
     * </pre>
     */
    public void setLayout(EleFXPaginationLayout... values) {
        if (values == null) {
            layout.clear();
            return;
        }
        for (EleFXPaginationLayout value : values)
            if (value == null) throw new IllegalArgumentException("layout values must not contain null");
        layout.setAll(values);
    }

    public ObservableList<Integer> getPageSizes() {
        return pageSizes;
    }

    public void setPageSizes(List<Integer> values) {
        for (Integer value : values)
            validatePositive(value == null ? 0 : value, "pageSizes");
        pageSizes.setAll(values);
    }

    public boolean isBackground() {
        return background.get();
    }

    public void setBackground(boolean value) {
        background.set(value);
    }

    /**
     * Whether pager buttons use filled backgrounds. This name avoids the final JavaFX
     * {@link javafx.scene.layout.Region#backgroundProperty()} inherited by the control.
     */
    public BooleanProperty buttonsBackgroundProperty() {
        return background;
    }

    public boolean isHideOnSinglePage() {
        return hideOnSinglePage.get();
    }

    public void setHideOnSinglePage(boolean value) {
        hideOnSinglePage.set(value);
    }

    public BooleanProperty hideOnSinglePageProperty() {
        return hideOnSinglePage;
    }

    public EleFXPaginationSize getSize() {
        return size.get();
    }

    public void setSize(EleFXPaginationSize value) {
        size.set(value == null ? EleFXPaginationSize.DEFAULT : value);
    }

    public ObjectProperty<EleFXPaginationSize> sizeProperty() {
        return size;
    }

    public String getPrevText() {
        return prevText.get();
    }

    public void setPrevText(String value) {
        prevText.set(value == null ? "" : value);
    }

    public StringProperty prevTextProperty() {
        return prevText;
    }

    public String getNextText() {
        return nextText.get();
    }

    public void setNextText(String value) {
        nextText.set(value == null ? "" : value);
    }

    public StringProperty nextTextProperty() {
        return nextText;
    }

    public Node getSlot() {
        return slot.get();
    }

    public void setSlot(Node value) {
        slot.set(value);
    }

    public ObjectProperty<Node> slotProperty() {
        return slot;
    }

    public EventHandler<EleFXPaginationEvent> getOnCurrentChange() {
        return onCurrentChange.get();
    }

    public void setOnCurrentChange(EventHandler<EleFXPaginationEvent> value) {
        onCurrentChange.set(value);
    }

    public ObjectProperty<EventHandler<EleFXPaginationEvent>> onCurrentChangeProperty() {
        return onCurrentChange;
    }

    public EventHandler<EleFXPaginationEvent> getOnSizeChange() {
        return onSizeChange.get();
    }

    public void setOnSizeChange(EventHandler<EleFXPaginationEvent> value) {
        onSizeChange.set(value);
    }

    public ObjectProperty<EventHandler<EleFXPaginationEvent>> onSizeChangeProperty() {
        return onSizeChange;
    }

    public EventHandler<EleFXPaginationEvent> getOnPrevClick() {
        return onPrevClick.get();
    }

    public void setOnPrevClick(EventHandler<EleFXPaginationEvent> value) {
        onPrevClick.set(value);
    }

    public ObjectProperty<EventHandler<EleFXPaginationEvent>> onPrevClickProperty() {
        return onPrevClick;
    }

    public EventHandler<EleFXPaginationEvent> getOnNextClick() {
        return onNextClick.get();
    }

    public void setOnNextClick(EventHandler<EleFXPaginationEvent> value) {
        onNextClick.set(value);
    }

    public ObjectProperty<EventHandler<EleFXPaginationEvent>> onNextClickProperty() {
        return onNextClick;
    }

    public int getEffectivePageCount() {
        return getPageCount() > 0 ? getPageCount() : Math.max(1, (int) Math.ceil(getTotal() / (double) getPageSize()));
    }

    public void prevPage() {
        if (getCurrentPage() > 1) {
            int old = getCurrentPage();
            setCurrentPage(old - 1);
            fire(onPrevClick.get(), EleFXPaginationEvent.PREV_CLICK, old, getCurrentPage());
        }
    }

    public void nextPage() {
        if (getCurrentPage() < getEffectivePageCount()) {
            int old = getCurrentPage();
            setCurrentPage(old + 1);
            fire(onNextClick.get(), EleFXPaginationEvent.NEXT_CLICK, old, getCurrentPage());
        }
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.PAGINATION;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void refresh() {
        normalizeCurrentPage();
        boolean visible = !isHideOnSinglePage() || getEffectivePageCount() > 1;
        setVisible(visible);
        setManaged(visible);
        getChildren().clear();
        boolean right = false;
        for (EleFXPaginationLayout part : getLayout()) {
            if (part == EleFXPaginationLayout.RIGHT) {
                right = true;
                continue;
            }
            Node node = switch (part) {
                case PREV -> navButton(true);
                case NEXT -> navButton(false);
                case PAGER -> pager();
                case TOTAL -> new Label("Total " + getTotal());
                case SIZES -> sizes();
                case JUMPER -> jumper();
                case SLOT -> getSlot();
                case RIGHT -> null;
            };
            if (node != null) {
                if (right) {
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    getChildren().add(spacer);
                    right = false;
                }
                getChildren().add(node);
            }
        }
    }

    private Button navButton(boolean previous) {
        String text = previous ? getPrevText() : getNextText();
        Button b = new Button(text);
        b.getStyleClass().add(previous ? "ele-pagination__prev" : "ele-pagination__next");
        if (text.isBlank()) {
            b.setGraphic(new EleFXIcon(previous ? EleFXIconType.ARROW_LEFT : EleFXIconType.ARROW_RIGHT, 14));
            b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
        b.setDisable(isDisabled() || (previous ? getCurrentPage() <= 1 : getCurrentPage() >= getEffectivePageCount()));
        b.setOnAction(e -> {
            if (previous)
                prevPage();
            else
                nextPage();
        });
        return b;
    }

    private HBox pager() {
        HBox box = new HBox(4);
        box.getStyleClass().add("ele-pagination__pager");
        for (PageItem item : pages()) {
            if (!item.more()) {
                int page = item.page();
                String pageText = String.valueOf(page);
                Button b = new Button(pageText);
                b.getStyleClass().add("ele-pagination__page");
                // Keep the compact baseline while allowing JavaFX to measure and fit
                // larger page labels such as "100" using the active font and padding.
                b.setMinWidth(getSize() == EleFXPaginationSize.SMALL ? 24 : 32);
                b.setPrefWidth(Region.USE_COMPUTED_SIZE);
                if (page == getCurrentPage()) b.getStyleClass().add("ele-pagination__page--active");
                b.setDisable(isDisabled());
                b.setOnAction(e -> setCurrentPage(page));
                box.getChildren().add(b);
            } else {
                Button b = new Button("•••");
                b.getStyleClass().add("ele-pagination__more");
                b.setDisable(isDisabled());
                int target = item.page();
                b.setOnAction(e -> setCurrentPage(target));
                EleFXIcon hoverIcon = new EleFXIcon(target < getCurrentPage()
                        ? EleFXIconType.D_ARROW_LEFT
                        : EleFXIconType.D_ARROW_RIGHT, 14);
                b.setOnMouseEntered(e -> {
                    if (!b.isDisabled()) {
                        b.setGraphic(hoverIcon);
                        b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    }
                });
                b.setOnMouseExited(e -> {
                    b.setGraphic(null);
                    b.setContentDisplay(ContentDisplay.TEXT_ONLY);
                });
                box.getChildren().add(b);
            }
        }
        return box;
    }

    private Node sizes() {
        Button button = new Button(getPageSize() + " / page", new EleFXIcon(EleFXIconType.CARET_BOTTOM, 12));
        button.getStyleClass().add("ele-pagination__sizes");
        if (sizesActive) button.getStyleClass().add("ele-pagination__sizes--open");
        button.setContentDisplay(ContentDisplay.RIGHT);
        button.setMinWidth(72);
        button.setPrefWidth(Region.USE_COMPUTED_SIZE);
        button.setDisable(isDisabled());

        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("ele-pagination__sizes-menu");
        menu.showingProperty().addListener((observable, wasShowing, showing) -> {
            if (showing) {
                sizesActive = true;
                if (!button.getStyleClass().contains("ele-pagination__sizes--open"))
                    button.getStyleClass().add("ele-pagination__sizes--open");
            }
        });
        for (Integer value : pageSizes) {
            MenuItem item = new MenuItem(value + " / page");
            item.getStyleClass().add("ele-pagination__sizes-item");
            item.setOnAction(e -> {
                sizesActive = true;
                setPageSize(value);
            });
            menu.getItems().add(item);
        }
        button.setOnAction(e -> {
            sizesActive = true;
            menu.show(button, Side.BOTTOM, 0, 2);
        });
        EventHandler<MouseEvent> clickAwayHandler = event -> {
            Node target = event.getPickResult().getIntersectedNode();
            while (target != null) {
                if (target == button) return;
                target = target.getParent();
            }
            sizesActive = false;
            button.getStyleClass().remove("ele-pagination__sizes--open");
        };
        button.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (oldScene != null) oldScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, clickAwayHandler);
            if (newScene != null) newScene.addEventFilter(MouseEvent.MOUSE_PRESSED, clickAwayHandler);
        });
        if (button.getScene() != null)
            button.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, clickAwayHandler);
        return button;
    }

    private Node jumper() {
        HBox box = new HBox(4);
        box.getStyleClass().add("ele-pagination__jumper");
        box.setAlignment(Pos.CENTER_LEFT);
        TextField field = new TextField(String.valueOf(getCurrentPage()));
        field.setPrefColumnCount(2);
        field.setDisable(isDisabled());
        field.setOnAction(e -> jump(field));
        field.focusedProperty().addListener((o, a, focused) -> {
            if (!focused) jump(field);
        });
        box.getChildren().addAll(new Label("Go to"), field);
        return box;
    }

    private void jump(TextField field) {
        try {
            setCurrentPage(Integer.parseInt(field.getText().trim()));
        } catch (NumberFormatException ignored) {
        }
        field.setText(String.valueOf(getCurrentPage()));
    }

    private List<PageItem> pages() {
        int count = getEffectivePageCount(), current = getCurrentPage(), limit = getPagerCount();
        List<PageItem> result = new ArrayList<>();
        if (count <= limit) {
            for (int i = 1; i <= count; i++)
                result.add(new PageItem(i, false));
            return result;
        }
        int half = (limit - 1) / 2;
        if (current <= limit - half) {
            // Element Plus keeps pages 1 through 6 visible at the leading edge.
            for (int i = 1; i < limit; i++)
                result.add(new PageItem(i, false));
            result.add(new PageItem(Math.min(count, current + limit - 2), true));
            result.add(new PageItem(count, false));
        } else if (current >= count - half) {
            // At page 100 (with pagerCount 7), this produces: 1 … 95 96 97 98 99 100.
            result.add(new PageItem(1, false));
            result.add(new PageItem(Math.max(1, current - (limit - 2)), true));
            for (int i = count - limit + 2; i <= count; i++)
                result.add(new PageItem(i, false));
        } else {
            result.add(new PageItem(1, false));
            result.add(new PageItem(current - (limit - 2), true));
            for (int i = current - half + 1; i <= current + half - 1; i++)
                result.add(new PageItem(i, false));
            result.add(new PageItem(current + (limit - 2), true));
            result.add(new PageItem(count, false));
        }
        return result;
    }

    private void normalizeCurrentPage() {
        if (normalizing) return;
        int target = Math.max(1, Math.min(getCurrentPage(), getEffectivePageCount()));
        if (target != getCurrentPage()) {
            normalizing = true;
            currentPage.set(target);
            normalizing = false;
        }
    }

    private void updateClasses() {
        getStyleClass().removeAll("ele-pagination--background", "ele-pagination--small");
        if (isBackground()) getStyleClass().add("ele-pagination--background");
        if (getSize() == EleFXPaginationSize.SMALL) getStyleClass().add("ele-pagination--small");
        refresh();
    }

    private void fire(EventHandler<EleFXPaginationEvent> handler, javafx.event.EventType<EleFXPaginationEvent> type,
                      int oldValue, int value) {
        EleFXPaginationEvent event = new EleFXPaginationEvent(this, this, type, oldValue, value);
        fireEvent(event);
        if (handler != null) handler.handle(event);
    }

    private static void validatePositive(int value, String name) {
        if (value <= 0) throw new IllegalArgumentException(name + " must be positive");
    }

    private static void validatePagerCount(int value) {
        if (value < 5 || value % 2 == 0)
            throw new IllegalArgumentException("pagerCount must be an odd number greater than or equal to 5");
    }

    /** A page button, or an ellipsis that jumps toward its hidden range. */
    private record PageItem(int page, boolean more) {
    }
}

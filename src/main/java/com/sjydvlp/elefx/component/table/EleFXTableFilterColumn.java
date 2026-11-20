package com.sjydvlp.elefx.component.table;

import com.sjydvlp.elefx.component.checkbox.EleFXCheckbox;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.component.popover.EleFXPopover;
import com.sjydvlp.elefx.component.popover.EleFXPopoverPlacement;
import com.sjydvlp.elefx.component.popover.EleFXPopoverTrigger;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

/** A column with a multi-select filter menu for use with EleFXTable.setFilterItems(). */
public class EleFXTableFilterColumn<S, T> extends EleFXTableColumn<S, T> {

    public record FilterOption<T> (String text, T value) {
    }

    private final Button trigger = new Button();

    private final VBox choices = new VBox();

    private final ScrollPane optionList = new ScrollPane(choices);

    private final EleFXPopover popover = new EleFXPopover();

    private final Map<T, EleFXCheckbox<T>> options = new LinkedHashMap<>();

    private final Set<T> selectedValues = new HashSet<>();

    private BiPredicate<? super T, ? super S> filterMethod = (value, row) -> Objects.equals(value, getCellData(row));

    public EleFXTableFilterColumn(String title) {
        super(title);
        EleFXIcon chevron = new EleFXIcon(EleFXIconType.ARROW_DOWN, 14);
        chevron.getStyleClass().add("ele-table__filter-chevron");
        chevron.setMouseTransparent(true);
        popover.showingProperty().addListener((observable, previous, showing) -> chevron
                .setType(showing ? EleFXIconType.ARROW_UP : EleFXIconType.ARROW_DOWN));
        trigger.setGraphic(chevron);
        trigger.getStyleClass().add("ele-table__filter-button");
        trigger.accessibleTextProperty().bind(textProperty().concat(" filters"));
        EleFXTable.trackPointerFocus(trigger);

        Button apply = new Button("Confirm");
        EleFXTable.trackPointerFocus(apply);
        apply.setMinWidth(Region.USE_PREF_SIZE);
        apply.setOnAction(event -> {
            selectedValues.clear();
            options.forEach((value, checkBox) -> {
                if (checkBox.isSelected()) {
                    selectedValues.add(value);
                }
            });
            filterChanged();
            popover.hide();
        });
        Button reset = new Button("Reset");
        EleFXTable.trackPointerFocus(reset);
        reset.setMinWidth(Region.USE_PREF_SIZE);
        reset.setOnAction(event -> {
            clearFilterValues();
            filterChanged();
            popover.hide();
        });
        HBox actions = new HBox(apply, reset);
        actions.getStyleClass().add("ele-table__filter-actions");

        choices.getStyleClass().add("ele-table__filter-options");
        optionList.getStyleClass().add("ele-table__filter-scroll");
        optionList.setFitToWidth(true);
        optionList.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        optionList.setFocusTraversable(false);
        VBox panel = new VBox(optionList, actions);
        popover.setContentNode(panel);
        popover.setReference(trigger);
        popover.setTrigger(EleFXPopoverTrigger.CLICK);
        popover.setPlacement(EleFXPopoverPlacement.BOTTOM_START);
        popover.setShowArrow(false);
        popover.setOffset(0);
        popover.setShowAfter(0);
        popover.setHideAfter(0);
        popover.setPopperClass("ele-table__filter-panel");
        popover.setOnBeforeEnter(event -> synchronizeCheckboxes());
        popover.setOnAfterEnter(event -> {
            double missingWidth = actions.prefWidth(-1) - actions.getWidth();
            if (missingWidth > 0) {
                popover.setWidth(popover.getWidth() + Math.ceil(missingWidth));
            }
            panel.requestFocus();
        });
    }

    public final void setFilterPlacement(EleFXPopoverPlacement placement) {
        popover.setPlacement(Objects.requireNonNull(placement, "placement"));
    }

    /** Uses each value's string representation as its menu label. */
    public final void setFilters(List<T> values) {
        setFilterOptions(values.stream()
                .map(value -> new FilterOption<>(String.valueOf(value), value))
                .toList());
    }

    public final void setFilterOptions(List<FilterOption<T>> values) {
        popover.hide();
        options.clear();
        for (FilterOption<T> option : values) {
            options.put(option.value(), new EleFXCheckbox<>(option.text(), option.value()));
        }
        choices.getChildren().setAll(options.values());
        optionList.setPrefViewportHeight(Math.min(280, options.size() * 24 + 16));
        double labelWidth = 0;
        for (var option : options.values()) {
            Text text = new Text(option.getText());
            text.setFont(Font.font(14));
            labelWidth = Math.max(labelWidth, text.getLayoutBounds().getWidth());
        }
        popover.setWidth(Math.max(120, Math.ceil(labelWidth) + 54));
        setGraphic(options.isEmpty() ? null : trigger);
        selectedValues.retainAll(options.keySet());
        filterChanged();
    }

    /** Matches a selected filter value against a row. The default compares the cell value. */
    public final void setFilterMethod(BiPredicate<? super T, ? super S> method) {
        filterMethod = Objects.requireNonNull(method, "filterMethod");
        filterChanged();
    }

    final boolean hasFilterValues() {
        return !selectedValues.isEmpty();
    }

    final boolean hasFilterTrigger() {
        return getGraphic() == trigger;
    }

    final boolean matches(S row) {
        return selectedValues.stream().anyMatch(value -> filterMethod.test(value, row));
    }

    final void clearFilterValues() {
        selectedValues.clear();
        synchronizeCheckboxes();
    }

    private void synchronizeCheckboxes() {
        options.forEach((value, checkBox) -> checkBox.setSelected(selectedValues.contains(value)));
    }

    private void filterChanged() {
        if (getTableView()instanceof EleFXTable<?> table) {
            table.refreshColumnFilters();
        }
    }

}

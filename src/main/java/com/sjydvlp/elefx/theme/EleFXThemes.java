package com.sjydvlp.elefx.theme;

import com.sjydvlp.elefx.EleFXResourcesLoader;

import java.io.InputStream;

public enum EleFXThemes implements Theme {

    DEFAULT("css/self/EleFXDefault.css"),
    BUTTON("css/component/button/EleFXButton.css"),
    ICON("css/component/icon/EleFXIcon.css"),
    LAYOUT("css/component/layout/EleFXLayout.css"),
    CONTAINER("css/component/container/EleFXContainer.css"),
    LINK("css/component/link/EleFXLink.css"),
    TEXT("css/component/text/EleFXText.css"),
    SCROLLBAR("css/component/scrollbar/EleFXScrollbar.css"),
    SPACE("css/component/space/EleFXSpace.css"),
    SPLITTER("css/component/splitter/EleFXSplitter.css"),
    TYPOGRAPHY("css/component/typography/EleFXTypography.css"),
    AUTOCOMPLETE("css/component/autocomplete/EleFXAutocomplete.css"),
    CASCADER("css/component/cascader/EleFXCascader.css"),
    CHECKBOX("css/component/checkbox/EleFXCheckbox.css"),
    COLOR_PICKER("css/component/colorpicker/EleFXColorPicker.css"),
    COLOR_PICKER_PANEL("css/component/colorpickerpanel/EleFXColorPickerPanel.css"),
    // Date-picker styling is distributed through the public default stylesheet.
    DATE_PICKER_PANEL("css/component/datepickerpanel/EleFXDatePickerPanel.css"),
    // LEGACY("css/legacy/LegacyControls.css"),
    // CHECKBOX("css/MFXCheckBox.css"),
    // CHECK_LIST_CELL("css/MFXCheckListCell.css"),
    // CHECK_LIST_VIEW("css/MFXCheckListView.css"),
    // CHECK_TREE_CELL("css/MFXCheckTreeCell.css"),
    // CIRCLE_TOGGLE_NODE("css/MFXCircleToggleNode.css"),
    // COLORS("css/EleFXColor.css"),
    // COMBO_BOX("css/MFXComboBox.css"),
    // COMBO_BOX_CELL("css/MFXComboBoxCell.css"),
    // CONTEXT_MENU("css/MFXContextMenu.css"),
    // CONTEXT_MENU_ITEM("css/MFXContextMenuItem.css"),
    // DATE_CELL("css/MFXDateCell.css"),
    // DATE_PICKER("css/MFXDatePicker.css"),
    // DIALOGS("css/MFXDialogs.css"),
    // FILTER_COMBO_BOX("css/MFXFilterComboBox.css"),
    // FILTER_DIALOG("css/MFXFilterDialog.css"),
    // FILTER_PANE("css/MFXFilterPane.css"),
    // LIST_CELL("css/MFXListCell.css"),
    // LIST_VIEW("css/MFXListView.css"),
    // MAGNIFIER("css/MFXMagnifier.css"),
    // NOTIFICATION_CENTER("css/MFXNotificationCenter.css"),
    // PAGINATION("css/MFXPagination.css"),
    // PASSWORD_FIELD("css/MFXPasswordField.css"),
    // PROGRESS_BAR("css/MFXProgressBar.css"),
    // PROGRESS_SPINNER("css/MFXProgressSpinner.css"),
    // RADIO_BUTTON("css/MFXRadioButton.css"),
    // RECTANGLE_TOGGLE_NODE("css/MFXRectangleToggleNode.css"),
    // SCROLL_PANE("css/MFXScrollPane.css"),
    // SLIDER("css/MFXSlider.css"),
    // SPINNER("css/MFXSpinner.css"),
    // STEPPER("css/MFXStepper.css"),
    // STEPPER_TOGGLE("css/MFXStepperToggle.css"),
    // TABLE_VIEW("css/MFXTableView.css"),
    // TEXT_FIELD("css/MFXTextField.css"),
    // TOGGLE_BUTTON("css/MFXToggleButton.css"),
    // TOOLTIP("css/MFXTooltip.css"),
    // TREE_CELL("css/MFXTreeCell.css"),
    // TREE_ITEM("css/MFXTreeItem.css"),
    // TREE_VIEW("css/MFXTreeView.css"),
    // LEGACY_COMBO("css/legacy/MFXComboBox.css"),
    // LEGACY_LIST_CELL("css/legacy/MFXLegacyListCell.css"),
    // LEGACY_LIST_VIEW("css/legacy/MFXLegacyListView.css"),
    // LEGACY_TABLE_ROW("css/legacy/MFXTableRow.css"),
    // LEGACY_TABLE_VIEW("css/legacy/MFXTableView.css"),
    ;

    private final String path;;

    EleFXThemes(String path) {
        this.path = path;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public InputStream assets() {
        return EleFXResourcesLoader.loadStream("css/self/elefx-assets.zip");
    }

    @Override
    public String deployName() {
        return "elefx-assets";
    }
}

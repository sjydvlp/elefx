package com.sjydvlp.elefx.theme;

import com.sjydvlp.elefx.EleFXResourcesLoader;

import java.io.InputStream;

public enum EleFXThemes implements Theme {

    DEFAULT("css/self/EleFXDefault.css"),
    BUTTON("css/component/button/EleFXButton.css"),
    ICON("css/component/icon/EleFXIcon.css"),
    AVATAR("css/component/avatar/EleFXAvatar.css"),
    IMAGE("css/component/image/EleFXImage.css"),
    CARD("css/component/card/EleFXCard.css"),
    DESCRIPTIONS("css/component/descriptions/EleFXDescriptions.css"),
    TIMELINE("css/component/timeline/EleFXTimeline.css"),
    EMPTY("css/component/empty/EleFXEmpty.css"),
    SKELETON("css/component/skeleton/EleFXSkeleton.css"),
    RESULT("css/component/result/EleFXResult.css"),
    LAYOUT("css/component/layout/EleFXLayout.css"),
    CONTAINER("css/component/container/EleFXContainer.css"),
    LINK("css/component/link/EleFXLink.css"),
    TAG("css/component/tag/EleFXTag.css"),
    TEXT("css/component/text/EleFXText.css"),
    SCROLLBAR("css/component/scrollbar/EleFXScrollbar.css"),
    AFFIX("css/component/affix/EleFXAffix.css"),
    ANCHOR("css/component/anchor/EleFXAnchor.css"),
    BREADCRUMB("css/component/breadcrumb/EleFXBreadcrumb.css"),
    MENU("css/component/menu/EleFXMenu.css"),
    DROPDOWN("css/component/dropdown/EleFXDropdown.css"),
    BACKTOP("css/component/backtop/EleFXBacktop.css"),
    INFINITE_SCROLL("css/component/infinitescroll/EleFXInfiniteScroll.css"),
    SPACE("css/component/space/EleFXSpace.css"),
    SPLITTER("css/component/splitter/EleFXSplitter.css"),
    TYPOGRAPHY("css/component/typography/EleFXTypography.css"),
    AUTOCOMPLETE("css/component/autocomplete/EleFXAutocomplete.css"),
    SELECT("css/component/select/EleFXSelect.css"),
    INPUT("css/component/input/EleFXInput.css"),
    INPUT_NUMBER("css/component/inputnumber/EleFXInputNumber.css"),
    SLIDER("css/component/slider/EleFXSlider.css"),
    RATE("css/component/rate/EleFXRate.css"),
    SWITCH("css/component/switcher/EleFXSwitch.css"),
    CASCADER("css/component/cascader/EleFXCascader.css"),
    CHECKBOX("css/component/checkbox/EleFXCheckbox.css"),
    RADIO("css/component/radio/EleFXRadio.css"),
    COLOR_PICKER("css/component/colorpicker/EleFXColorPicker.css"),
    COLOR_PICKER_PANEL("css/component/colorpickerpanel/EleFXColorPickerPanel.css"),
    DATE_PICKER("css/component/datepicker/EleFXDatePicker.css"),
    DATE_PICKER_PANEL("css/component/datepickerpanel/EleFXDatePickerPanel.css"),
    FORM("css/component/form/EleFXForm.css"),
    TRANSFER("css/component/transfer/EleFXTransfer.css"),
    CAROUSEL("css/component/carousel/EleFXCarousel.css"),
    COLLAPSE("css/component/collapse/EleFXCollapse.css"),
    UPLOAD("css/component/upload/EleFXUpload.css"),
    PAGINATION("css/component/pagination/EleFXPagination.css"),
    PROGRESS("css/component/progress/EleFXProgress.css"),
    STATISTIC("css/component/statistic/EleFXStatistic.css"),
    SEGMENTED("css/component/segmented/EleFXSegmented.css"),
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

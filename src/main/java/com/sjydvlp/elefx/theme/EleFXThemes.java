package com.sjydvlp.elefx.theme;

import com.sjydvlp.elefx.EleFXResourcesLoader;

import java.io.InputStream;

public enum EleFXThemes implements Theme {

    DEFAULT("css/self/EleFXDefault.css"),
    BUTTON("css/component/button/EleFXButton.css"),
    ICON("css/component/icon/EleFXIcon.css"),
    AVATAR("css/component/avatar/EleFXAvatar.css"),
    IMAGE("css/component/image/EleFXImage.css"),
    DIALOG("css/component/dialog/EleFXDialog.css"),
    POPCONFIRM("css/component/popconfirm/EleFXPopconfirm.css"),
    POPOVER("css/component/popover/EleFXPopover.css"),
    TOOLTIP("css/component/tooltip/EleFXTooltip.css"),
    DRAWER("css/component/drawer/EleFXDrawer.css"),
    CARD("css/component/card/EleFXCard.css"),
    DIVIDER("css/component/divider/EleFXDivider.css"),
    DESCRIPTIONS("css/component/descriptions/EleFXDescriptions.css"),
    TIMELINE("css/component/timeline/EleFXTimeline.css"),
    STEPS("css/component/steps/EleFXSteps.css"),
    EMPTY("css/component/empty/EleFXEmpty.css"),
    SKELETON("css/component/skeleton/EleFXSkeleton.css"),
    RESULT("css/component/result/EleFXResult.css"),
    ALERT("css/component/alert/EleFXAlert.css"),
    MESSAGE("css/component/message/EleFXMessage.css"),
    NOTIFICATION("css/component/notification/EleFXNotification.css"),
    MESSAGE_BOX("css/component/messagebox/EleFXMessageBox.css"),
    LOADING("css/component/loading/EleFXLoading.css"),
    WATERMARK("css/component/watermark/EleFXWatermark.css"),
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
    PAGE_HEADER("css/component/pageheader/EleFXPageHeader.css"),
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
    TABS("css/component/tabs/EleFXTabs.css"),
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

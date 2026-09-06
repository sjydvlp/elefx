package com.sjydvlp.elefx.unit;

import com.sjydvlp.elefx.component.layout.EleFXCol;
import com.sjydvlp.elefx.component.layout.EleFXColSize;
import com.sjydvlp.elefx.component.layout.EleFXRow;
import com.sjydvlp.elefx.component.layout.EleFXRowAlign;
import com.sjydvlp.elefx.component.layout.EleFXRowJustify;
import com.sjydvlp.elefx.theme.EleFXThemes;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/** Exercises actual JavaFX layout without opening a window. */
public class EleFXLayoutTest {

    private static final double EPSILON = 0.01;

    @BeforeClass
    public static void startToolkit() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(started::countDown);
        }
        assertTrue("JavaFX toolkit did not start", started.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void defaultsAndThemeAreAvailable() throws Exception {
        onFxThread(() -> {
            EleFXRow row = new EleFXRow();
            EleFXCol col = new EleFXCol();
            assertEquals(0, row.getGutter(), EPSILON);
            assertEquals(EleFXRowJustify.START, row.getJustify());
            assertEquals(EleFXRowAlign.STRETCH, row.getAlign());
            assertEquals(24, col.getSpan());
            assertEquals(0, col.getOffset());
            assertEquals(0, col.getPush());
            assertEquals(0, col.getPull());
            assertSame(row, row.toParent());
            assertSame(col, col.toParent());
            assertSame(EleFXThemes.LAYOUT, row.getTheme());
            assertSame(EleFXThemes.LAYOUT, col.getTheme());
            assertNotNull("Layout CSS must be packaged", EleFXThemes.LAYOUT.get());
            assertTrue(row.getStyleClass().contains("ele-row"));
            assertTrue(col.getStyleClass().contains("ele-col"));
        });
    }

    @Test
    public void spansUseTwentyFourUnitsAndWrapAtTheAvailableWidth() throws Exception {
        onFxThread(() -> {
            EleFXCol first = col(12, 20);
            EleFXCol second = col(12, 10);
            EleFXCol third = col(24, 30);
            EleFXRow row = new EleFXRow(first, second, third);
            assertEquals(50, row.prefHeight(240), EPSILON);
            layout(row, 240, 50);
            bounds(first, 0, 0, 120, 20);
            bounds(second, 120, 0, 120, 20);
            bounds(third, 0, 20, 240, 30);
            layout(row, 480, 50);
            assertEquals(240, first.getWidth(), EPSILON);
            assertEquals(240, second.getLayoutX(), EPSILON);
            assertEquals(480, third.getWidth(), EPSILON);
        });
    }

    @Test
    public void offsetConsumesGridSpaceAndPushPullOnlyShiftPresentation() throws Exception {
        onFxThread(() -> {
            EleFXCol first = col(6, 20);
            first.setOffset(6);
            first.setPush(2);
            first.setPull(1);
            EleFXCol second = col(12, 20);
            EleFXCol third = col(1, 20);
            EleFXRow row = new EleFXRow(first, second, third);
            layout(row, 240, 40);
            bounds(first, 70, 0, 60, 20);
            bounds(second, 120, 0, 120, 20);
            bounds(third, 0, 20, 10, 20);
        });
    }

    @Test
    public void gutterSeparatesContentAndChangesThroughTheProperty() throws Exception {
        onFxThread(() -> {
            Region firstContent = content(20);
            Region secondContent = content(20);
            EleFXCol first = new EleFXCol(12, firstContent);
            EleFXCol second = new EleFXCol(12, secondContent);
            EleFXRow row = new EleFXRow(20, first, second);
            layout(row, 240, 20);
            assertEquals(120, first.getWidth(), EPSILON);
            assertEquals(10, firstContent.getLayoutX(), EPSILON);
            assertEquals(100, firstContent.getWidth(), EPSILON);
            assertEquals(20, contentGap(first, firstContent, second, secondContent), EPSILON);
            row.gutterProperty().set(8);
            row.layout();
            assertEquals(4, firstContent.getLayoutX(), EPSILON);
            assertEquals(112, firstContent.getWidth(), EPSILON);
            assertEquals(8, contentGap(first, firstContent, second, secondContent), EPSILON);
        });
    }

    @Test
    public void justifyDistributesUnusedGridSpace() throws Exception {
        onFxThread(() -> {
            EleFXCol first = col(6, 20);
            EleFXCol second = col(6, 20);
            EleFXRow row = new EleFXRow(first, second);
            EleFXRowJustify[] values = {EleFXRowJustify.START, EleFXRowJustify.CENTER,
                    EleFXRowJustify.END, EleFXRowJustify.SPACE_BETWEEN,
                    EleFXRowJustify.SPACE_AROUND, EleFXRowJustify.SPACE_EVENLY};
            double[][] positions = {{0, 60}, {60, 120}, {120, 180}, {0, 180}, {30, 150}, {40, 140}};
            for (int i = 0; i < values.length; i++) {
                row.setJustify(values[i]);
                layout(row, 240, 20);
                assertEquals(values[i].name(), positions[i][0], first.getLayoutX(), EPSILON);
                assertEquals(values[i].name(), positions[i][1], second.getLayoutX(), EPSILON);
            }
        });
    }

    @Test
    public void alignPositionsShorterColumnsWithinTheirLine() throws Exception {
        onFxThread(() -> {
            EleFXCol shortCol = col(12, 20);
            EleFXCol tallCol = col(12, 60);
            EleFXRow row = new EleFXRow(shortCol, tallCol);
            layout(row, 240, 60);
            bounds(shortCol, 0, 0, 120, 60);
            row.setAlign(EleFXRowAlign.TOP);
            row.layout();
            bounds(shortCol, 0, 0, 120, 20);
            row.setAlign(EleFXRowAlign.MIDDLE);
            row.layout();
            bounds(shortCol, 0, 20, 120, 20);
            row.alignProperty().set(EleFXRowAlign.BOTTOM);
            row.layout();
            bounds(shortCol, 0, 40, 120, 20);
            row.setAlign(EleFXRowAlign.STRETCH);
            shortCol.setMaxHeight(30);
            layout(row, 240, 80);
            bounds(shortCol, 0, 0, 120, 30);
            assertEquals(80, tallCol.getHeight(), EPSILON);
        });
    }

    @Test
    public void gutterPreservesBoundPaddingAndNestedRowsReceiveContentWidth() throws Exception {
        onFxThread(() -> {
            Region content = content(20);
            EleFXCol col = new EleFXCol(12, content);
            Insets padding = new Insets(3, 5, 7, 9);
            col.paddingProperty().bind(new SimpleObjectProperty<>(padding));
            EleFXRow row = new EleFXRow(20, col);
            layout(row, 240, 30);
            assertSame(padding, col.getPadding());
            bounds(content, 19, 3, 86, 20);
            row.setGutter(0);
            row.layout();
            assertTrue(col.paddingProperty().isBound());
            assertSame(padding, col.getPadding());
            bounds(content, 9, 3, 106, 20);

            Region nestedContent = content(20);
            EleFXCol innerCol = new EleFXCol(12, nestedContent);
            EleFXRow innerRow = new EleFXRow(8, innerCol, col(12, 20));
            EleFXCol outerCol = new EleFXCol(12, innerRow);
            EleFXRow outerRow = new EleFXRow(20, outerCol);
            layout(outerRow, 480, 20);
            bounds(outerCol, 0, 0, 240, 20);
            bounds(innerRow, 10, 0, 220, 20);
            bounds(innerCol, 0, 0, 110, 20);
            bounds(nestedContent, 4, 0, 102, 20);
        });
    }

    @Test
    public void baselineAlignmentMatchesStackPaneBeforeAndAfterAlignmentChanges() throws Exception {
        onFxThread(() -> {
            Region[] actual = {baselineContent(20, 10), baselineContent(40, 30), content(30)};
            Region[] expected = {baselineContent(20, 10), baselineContent(40, 30), content(30)};
            actual[2].setMaxHeight(Region.USE_PREF_SIZE);
            expected[2].setMaxHeight(Region.USE_PREF_SIZE);
            EleFXCol col = new EleFXCol(actual);
            col.setPadding(new Insets(4, 6, 8, 10));
            EleFXRow row = new EleFXRow(12, col);
            StackPane reference = new StackPane(expected);
            // A StackPane with equivalent content insets is the reference for column alignment.
            reference.setPadding(new Insets(4, 12, 8, 16));
            for (Pos alignment : new Pos[]{Pos.BASELINE_LEFT, Pos.CENTER, Pos.BASELINE_LEFT}) {
                col.setAlignment(alignment);
                reference.setAlignment(alignment);
                layout(row, 240, 80);
                reference.resize(240, 80);
                reference.layout();
                for (int i = 0; i < actual.length; i++) {
                    bounds(actual[i], expected[i].getLayoutX(), expected[i].getLayoutY(),
                            expected[i].getWidth(), expected[i].getHeight());
                }
            }
        });
    }

    @Test
    public void responsiveBreakpointsCascadeAndUseRowWidthWithoutAScene() throws Exception {
        onFxThread(() -> {
            EleFXCol col = col(24, 20);
            col.setXs(new EleFXColSize(6, 1, null, null));
            col.setSm(new EleFXColSize(12, 2, null, null));
            col.setMd(8);
            col.setLg(4);
            col.setXl(3);
            EleFXRow row = new EleFXRow(col);
            int[] widths = {767, 768, 991, 992, 1199, 1200, 1919, 1920};
            int[] spans = {6, 12, 12, 8, 8, 4, 4, 3};
            for (int i = 0; i < widths.length; i++) {
                layout(row, widths[i], 20);
                assertEquals("span at width " + widths[i], widths[i] * spans[i] / 24.0,
                        col.getWidth(), 1.0);
                assertEquals("cascaded offset at width " + widths[i],
                        widths[i] * (i == 0 ? 1 : 2) / 24.0, col.getLayoutX(), 1.0);
            }
            col.setSm((EleFXColSize) null);
            layout(row, 800, 20);
            assertEquals(800, col.getWidth(), EPSILON);
            assertEquals(0, col.getLayoutX(), EPSILON);
        });
    }

    @Test
    public void sceneViewportTakesPrecedenceAndDetachingRestoresRowWidth() throws Exception {
        onFxThread(() -> {
            EleFXCol col = col(24, 20);
            col.setXs(24);
            col.setSm(12);
            col.setLg(4);
            EleFXRow row = new EleFXRow(col);
            row.setManaged(false);
            Pane host = new Pane(row);
            Scene scene = new Scene(host, 1200, 100);
            layout(row, 240, 20);
            assertEquals(40, col.getWidth(), EPSILON);
            scene.setRoot(new Pane());
            row.layout();
            assertEquals(240, col.getWidth(), EPSILON);
            new Scene(host, 800, 100);
            row.layout();
            assertEquals(120, col.getWidth(), EPSILON);
        });
    }

    @Test
    public void spanBindingsAndChildChangesInvalidateLayout() throws Exception {
        onFxThread(() -> {
            SimpleIntegerProperty span = new SimpleIntegerProperty(6);
            EleFXCol first = col(6, 20);
            first.spanProperty().bind(span);
            EleFXCol second = col(12, 20);
            EleFXRow row = new EleFXRow(first, second);
            layout(row, 240, 20);
            assertEquals(60, second.getLayoutX(), EPSILON);
            span.set(12);
            row.layout();
            assertEquals(120, first.getWidth(), EPSILON);
            assertEquals(120, second.getLayoutX(), EPSILON);
            row.getChildren().remove(first);
            row.layout();
            assertEquals(0, second.getLayoutX(), EPSILON);
            row.getChildren().add(first);
            row.layout();
            assertEquals(120, first.getLayoutX(), EPSILON);
        });
    }

    @Test
    public void zeroSpanDoesNotConsumeSpaceAndCanBeRestored() throws Exception {
        onFxThread(() -> {
            EleFXCol first = col(0, 60);
            first.setOffset(12);
            EleFXCol second = col(12, 20);
            EleFXRow row = new EleFXRow(first, second);
            assertEquals(20, row.prefHeight(240), EPSILON);
            layout(row, 240, 20);
            assertEquals(0, first.getWidth(), EPSILON);
            assertEquals(0, first.getHeight(), EPSILON);
            assertFalse(first.isVisible());
            assertEquals(0, second.getLayoutX(), EPSILON);
            first.setSpan(12);
            layout(row, 240, 80);
            bounds(first, 120, 0, 120, 60);
            bounds(second, 0, 60, 120, 20);
            assertTrue(first.isVisible());
        });
    }

    @Test
    public void responsiveHiddenClassPreservesVisibilityBindingAndRestoresClip() throws Exception {
        onFxThread(() -> {
            EleFXCol first = col(12, 20);
            Rectangle clip = new Rectangle(100, 20);
            first.setClip(clip);
            SimpleBooleanProperty visible = new SimpleBooleanProperty(true);
            first.visibleProperty().bind(visible);
            EleFXCol second = col(12, 20);
            EleFXRow row = new EleFXRow(first, second);
            layout(row, 720, 20);
            first.getStyleClass().add("hidden-xs-only");
            row.layout();
            assertEquals(0, first.getWidth(), EPSILON);
            assertEquals(0, second.getLayoutX(), EPSILON);
            assertTrue(first.isVisible());
            assertTrue(first.visibleProperty().isBound());
            assertTrue(first.isDisabled());
            assertTrue(first.isManaged());
            assertNotSame(clip, first.getClip());
            assertEquals(0, first.getClip().getBoundsInLocal().getWidth(), EPSILON);
            layout(row, 800, 20);
            assertSame(clip, first.getClip());
            assertFalse(first.isDisabled());
            assertEquals(400, second.getLayoutX(), EPSILON);
            layout(row, 720, 20);
            first.setManaged(false);
            row.layout();
            assertSame(clip, first.getClip());
            assertFalse(first.isDisabled());
            first.setManaged(true);
            row.layout();
            assertNotSame(clip, first.getClip());
            assertTrue(first.isDisabled());
            Rectangle replacementClip = new Rectangle(80, 20);
            first.setClip(replacementClip);
            first.getStyleClass().remove("hidden-xs-only");
            row.layout();
            assertSame(replacementClip, first.getClip());
            visible.set(false);
            assertFalse(first.isVisible());
            visible.set(true);
            first.getStyleClass().add("hidden-xs-only");
            row.layout();
            row.getChildren().remove(first);
            assertSame(replacementClip, first.getClip());
            assertFalse(first.isDisabled());
            assertTrue(first.isVisible());
        });
    }

    @Test
    public void hiddenColumnsDoNotAllowButtonsToReceiveFocus() throws Exception {
        onFxThread(() -> {
            Button hiddenButton = new Button("Hidden");
            Button visibleButton = new Button("Visible");
            EleFXCol hiddenCol = new EleFXCol(0, hiddenButton);
            EleFXRow row = new EleFXRow(hiddenCol, new EleFXCol(12, visibleButton));
            Scene scene = new Scene(row, 240, 40);
            row.layout();
            visibleButton.requestFocus();
            assertSame("Focus assertions must work without displaying a Stage", visibleButton, scene.getFocusOwner());
            hiddenButton.requestFocus();
            assertSame(visibleButton, scene.getFocusOwner());
            hiddenCol.visibleProperty().bind(new SimpleBooleanProperty(true));
            row.layout();
            assertTrue(hiddenButton.isDisabled());
            hiddenButton.requestFocus();
            assertSame(visibleButton, scene.getFocusOwner());
            hiddenCol.setSpan(12);
            row.layout();
            assertFalse(hiddenButton.isDisabled());
            hiddenButton.requestFocus();
            assertSame(hiddenButton, scene.getFocusOwner());
        });
    }

    @Test
    public void zeroSpanWithBoundClipRestoresCallerVisibility() throws Exception {
        onFxThread(() -> {
            EleFXCol col = col(0, 20);
            Rectangle clip = new Rectangle(100, 20);
            col.clipProperty().bind(new SimpleObjectProperty<Node>(clip));
            EleFXRow row = new EleFXRow(col);
            layout(row, 240, 20);
            assertSame(clip, col.getClip());
            assertTrue(col.clipProperty().isBound());
            assertFalse(col.isVisible());
            col.setSpan(12);
            row.layout();
            assertTrue(col.isVisible());
            assertSame(clip, col.getClip());
            col.setVisible(false);
            col.setSpan(0);
            row.layout();
            col.setSpan(12);
            row.layout();
            assertFalse(col.isVisible());
        });
    }

    @Test
    public void fxmlSupportsDefaultChildrenAndResponsiveSizeObjects() throws Exception {
        onFxThread(() -> {
            String fxml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <?import com.sjydvlp.elefx.component.layout.*?>
                    <?import javafx.scene.layout.Region?>
                    <EleFXRow xmlns:fx="http://javafx.com/fxml/1" gutter="12"
                              justify="SPACE_BETWEEN" align="MIDDLE">
                        <EleFXCol span="6" xs="24">
                            <sm><EleFXColSize span="12" offset="2" /></sm>
                            <md><EleFXColSize offset="1" /></md>
                            <Region prefHeight="20" />
                        </EleFXCol>
                        <EleFXCol span="6"><Region prefHeight="40" /></EleFXCol>
                    </EleFXRow>
                    """;
            FXMLLoader loader = new FXMLLoader();
            EleFXRow row = loader.load(new ByteArrayInputStream(fxml.getBytes(StandardCharsets.UTF_8)));
            assertEquals(12, row.getGutter(), EPSILON);
            assertEquals(EleFXRowJustify.SPACE_BETWEEN, row.getJustify());
            assertEquals(EleFXRowAlign.MIDDLE, row.getAlign());
            assertEquals(2, row.getChildren().size());
            EleFXCol first = (EleFXCol) row.getChildren().get(0);
            assertEquals(6, first.getSpan());
            assertEquals(1, first.getChildren().size());
            assertNull(first.getSm().getPush());
            assertNull(first.getSm().getPull());
            assertNull(first.getMd().getSpan());
            assertNull(first.getMd().getPush());
            assertNull(first.getMd().getPull());
            layout(row, 800, 60);
            assertEquals(400, first.getWidth(), 1.0);
            layout(row, 1008, 60);
            assertEquals(504, first.getWidth(), EPSILON);
        });
    }

    private static EleFXCol col(int span, double height) {
        return new EleFXCol(span, content(height));
    }

    private static Region content(double height) {
        Region region = new Region();
        region.setMinSize(0, 0);
        region.setPrefSize(10, height);
        return region;
    }

    private static Region baselineContent(double height, double baseline) {
        Region region = new Region() {
            @Override
            public double getBaselineOffset() {
                return baseline;
            }
        };
        region.setMinSize(0, 0);
        region.setPrefSize(10, height);
        region.setMaxHeight(Region.USE_PREF_SIZE);
        return region;
    }

    private static void layout(EleFXRow row, double width, double height) {
        row.resize(width, height);
        row.layout();
    }

    private static double contentGap(EleFXCol first, Region firstContent,
                                     EleFXCol second, Region secondContent) {
        return second.getLayoutX() + secondContent.getLayoutX()
                - first.getLayoutX() - firstContent.getLayoutX() - firstContent.getWidth();
    }

    private static void bounds(Region region, double x, double y, double width, double height) {
        assertEquals("x", x, region.getLayoutX(), EPSILON);
        assertEquals("y", y, region.getLayoutY(), EPSILON);
        assertEquals("width", width, region.getWidth(), EPSILON);
        assertEquals("height", height, region.getHeight(), EPSILON);
    }

    private static void onFxThread(FxAssertion assertion) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            assertion.run();
            return null;
        });
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    @FunctionalInterface
    private interface FxAssertion {

        void run() throws Exception;
    }
}

/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.world;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javafx.scene.input.MouseEvent.MOUSE_ENTERED;
import static javafx.scene.input.MouseEvent.MOUSE_EXITED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;


/**
 * User: hansolo
 * Date: 20.09.16
 * Time: 12:20
 */
@DefaultProperty("children")
public class WorldLR extends Region {
    private static final StyleablePropertyFactory<WorldLR> FACTORY          = new StyleablePropertyFactory<>(Region.getClassCssMetaData());
    private static final double                            EARTH_RADIUS     = 6_371_000;
    private static final double                            PREFERRED_WIDTH  = 1009;
    private static final double                            PREFERRED_HEIGHT = 665;
    private static final double                            MINIMUM_WIDTH    = 100;
    private static final double                            MINIMUM_HEIGHT   = 66;
    private static final double                            MAXIMUM_WIDTH    = 2018;
    private static final double                            MAXIMUM_HEIGHT   = 1330;
    private static final double                            ASPECT_RATIO     = PREFERRED_HEIGHT / PREFERRED_WIDTH;
    private static final CssMetaData<WorldLR, Color>       BACKGROUND_COLOR = FACTORY.createColorCssMetaData("-background-color", s -> s.backgroundColor, Color.web("#3f3f4f"), false);
    private        final StyleableProperty<Color>          backgroundColor;
    private static final CssMetaData<WorldLR, Color>       FILL_COLOR       = FACTORY.createColorCssMetaData("-fill-color", s -> s.fillColor, Color.web("#d9d9dc"), false);
    private        final StyleableProperty<Color>          fillColor;
    private static final CssMetaData<WorldLR, Color>       STROKE_COLOR     = FACTORY.createColorCssMetaData("-stroke-color", s -> s.strokeColor, Color.BLACK, false);
    private        final StyleableProperty<Color>          strokeColor;
    private static final CssMetaData<WorldLR, Color>       HOVER_COLOR      = FACTORY.createColorCssMetaData("-hover-color", s -> s.hoverColor, Color.web("#456acf"), false);
    private        final StyleableProperty<Color>          hoverColor;
    private static final CssMetaData<WorldLR, Color>       PRESSED_COLOR    = FACTORY.createColorCssMetaData("-pressed-color", s -> s.pressedColor, Color.web("#ef6050"), false);
    private        final StyleableProperty<Color>          pressedColor;
    private              double                            width;
    private              double                            height;
    private              Pane                              pane;
    private              Map<String, List<CountryPath>>    countryPaths;
    private              ScalableContentPane               scalableContentPane;
    // internal event handlers
    private              EventHandler<MouseEvent>          _mouseEnterHandler;
    private              EventHandler<MouseEvent>          _mousePressHandler;
    private              EventHandler<MouseEvent>          _mouseReleaseHandler;
    private              EventHandler<MouseEvent>          _mouseExitHandler;
    // exposed event handlers
    private              EventHandler<MouseEvent>          mouseEnterHandler;
    private              EventHandler<MouseEvent>          mousePressHandler;
    private              EventHandler<MouseEvent>          mouseReleaseHandler;
    private              EventHandler<MouseEvent>          mouseExitHandler;


    // ******************** Constructors **************************************
    public WorldLR() {
        backgroundColor      = new StyleableObjectProperty<Color>(BACKGROUND_COLOR.getInitialValue(WorldLR.this)) {
            @Override protected void invalidated() { setBackground(new Background(new BackgroundFill(get(), CornerRadii.EMPTY, Insets.EMPTY))); }
            @Override public Object getBean() { return WorldLR.this; }
            @Override public String getName() { return "backgroundColor"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return BACKGROUND_COLOR; }
        };
        fillColor            = new StyleableObjectProperty<Color>(FILL_COLOR.getInitialValue(WorldLR.this)) {
            @Override protected void invalidated() { setFillAndStroke(); }
            @Override public Object getBean() { return WorldLR.this; }
            @Override public String getName() { return "fillColor"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return FILL_COLOR; }
        };
        strokeColor          = new StyleableObjectProperty<Color>(STROKE_COLOR.getInitialValue(WorldLR.this)) {
            @Override protected void invalidated() { setFillAndStroke(); }
            @Override public Object getBean() { return WorldLR.this; }
            @Override public String getName() { return "strokeColor"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return STROKE_COLOR; }
        };
        hoverColor           = new StyleableObjectProperty<Color>(HOVER_COLOR.getInitialValue(WorldLR.this)) {
            @Override protected void invalidated() { }
            @Override public Object getBean() { return WorldLR.this; }
            @Override public String getName() { return "hoverColor"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return HOVER_COLOR; }
        };
        pressedColor         = new StyleableObjectProperty<Color>(PRESSED_COLOR.getInitialValue(this)) {
            @Override protected void invalidated() { }
            @Override public Object getBean() { return WorldLR.this; }
            @Override public String getName() { return "pressedColor"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return PRESSED_COLOR; }
        };
        countryPaths         = new HashMap<>();

        _mouseEnterHandler   = evt -> handleMouseEvent(evt, mouseEnterHandler);
        _mousePressHandler   = evt -> handleMouseEvent(evt, mousePressHandler);
        _mouseReleaseHandler = evt -> handleMouseEvent(evt, mouseReleaseHandler);
        _mouseExitHandler    = evt -> handleMouseEvent(evt, mouseExitHandler);

        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        getStyleClass().add("world");

        pane = new Pane();
        Color fill   = getFillColor();
        Color stroke = getStrokeColor();

        for(CountryLR country : CountryLR.values()) {
            List<CountryPath> paths = country.getPaths();
            pane.getChildren().addAll(paths);

            countryPaths.put(country.name(), paths);

            for(CountryPath path : paths) {
                path.setFill(fill);
                path.setStroke(stroke);
                path.setStrokeWidth(0.5);
                path.setOnMouseEntered(_mouseEnterHandler);
                path.setOnMousePressed(_mousePressHandler);
                path.setOnMouseReleased(_mouseReleaseHandler);
                path.setOnMouseExited(_mouseExitHandler);
            }
        }

        scalableContentPane = new ScalableContentPane();
        scalableContentPane.setContent(pane);

        getChildren().setAll(scalableContentPane);

        setBackground(new Background(new BackgroundFill(getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY)));
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT)  { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH)  { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT)  { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH)  { return MAXIMUM_HEIGHT; }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public Map<String, List<CountryPath>> getCountryPaths() { return countryPaths; }

    public void setMouseEnterHandler(final EventHandler<MouseEvent> HANDLER) { mouseEnterHandler = HANDLER; }
    public void setMousePressHandler(final EventHandler<MouseEvent> HANDLER) { mousePressHandler = HANDLER; }
    public void setMouseReleaseHandler(final EventHandler<MouseEvent> HANDLER) { mouseReleaseHandler = HANDLER;  }
    public void setMouseExitHandler(final EventHandler<MouseEvent> HANDLER) { mouseExitHandler = HANDLER; }

    public Color getBackgroundColor() { return backgroundColor.getValue(); }
    public void setBackgroundColor(final Color COLOR) { backgroundColor.setValue(COLOR); }
    public ObjectProperty<Color> backgroundColorProperty() { return (ObjectProperty<Color>) backgroundColor; }

    public Color getFillColor() { return fillColor.getValue(); }
    public void setFillColor(final Color COLOR) { fillColor.setValue(COLOR); }
    public ObjectProperty<Color> fillColorProperty() { return (ObjectProperty<Color>) fillColor; }

    public Color getStrokeColor() { return strokeColor.getValue(); }
    public void setStrokeColor(final Color COLOR) { strokeColor.setValue(COLOR); }
    public ObjectProperty<Color> strokeColorProperty() { return (ObjectProperty<Color>) strokeColor; }

    public Color getHoverColor() { return hoverColor.getValue(); }
    public void setHoverColor(final Color COLOR) { hoverColor.setValue(COLOR); }
    public ObjectProperty<Color> hoverColorProperty() { return (ObjectProperty<Color>) hoverColor; }

    public Color getPressedColor() { return pressedColor.getValue(); }
    public void setPressedColor(final Color COLOR) { pressedColor.setValue(COLOR); }
    public ObjectProperty<Color> pressedColorProperty() { return (ObjectProperty<Color>) pressedColor; }

    private void handleMouseEvent(final MouseEvent EVENT, final EventHandler<MouseEvent> HANDLER) {
        final CountryPath COUNTRY_PATH = (CountryPath) EVENT.getSource();
        final String      COUNTRY_NAME = COUNTRY_PATH.getName();

        final EventType TYPE = EVENT.getEventType();
        if (MOUSE_ENTERED == TYPE) {
            for(SVGPath path : CountryLR.valueOf(COUNTRY_NAME).getPaths()) { path.setFill(getHoverColor()); }
        } else if (MOUSE_PRESSED == TYPE) {
            for(SVGPath path : CountryLR.valueOf(COUNTRY_NAME).getPaths()) { path.setFill(getPressedColor()); }
        } else if (MOUSE_RELEASED == TYPE) {
            for(SVGPath path : CountryLR.valueOf(COUNTRY_NAME).getPaths()) { path.setFill(getHoverColor()); }
        } else if (MOUSE_EXITED == TYPE) {
            for(SVGPath path : CountryLR.valueOf(COUNTRY_NAME).getPaths()) { path.setFill(getFillColor()); }
        }

        if (null != HANDLER) HANDLER.handle(EVENT);
    }

    private void setFillAndStroke() {
        for (CountryLR country : CountryLR.values()) {
            for (CountryPath path : country.getPaths()) {
                path.setFill(getFillColor());
                path.setStroke(getStrokeColor());
            }
        }
    }


    // ******************** Style related *************************************
    @Override public String getUserAgentStylesheet() {
        return WorldLR.class.getResource("world.css").toExternalForm();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() { return FACTORY.getCssMetaData(); }

    @Override public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() { return FACTORY.getCssMetaData(); }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();

        if (ASPECT_RATIO * width > height) {
            width = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            pane.setCache(true);
            pane.setCacheHint(CacheHint.SCALE);

            scalableContentPane.setMaxSize(width, height);
            scalableContentPane.setPrefSize(width, height);
            scalableContentPane.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

            pane.setCache(false);
        }
    }
}
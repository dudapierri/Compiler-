module gui {
    requires org.apache.commons.io;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires org.fxmisc.richtext;
    opens gui to javafx.controls, javafx.fxml;
    opens classes;
    exports gui;
}
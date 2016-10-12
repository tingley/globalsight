/*
 * TestToolApp.java
 */

package testtool;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import testUI.Desktop;


/**
 * The main class of the application.
 */
public class TestToolApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
     
    @Override protected void startup() {
       show(new Desktop());

    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of TestToolApp
     */
    public static TestToolApp getApplication() {
        return Application.getInstance(TestToolApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(TestToolApp.class, args);
//         launch(Desktop.class, args);
    }
}

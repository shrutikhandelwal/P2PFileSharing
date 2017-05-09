import javafx.application.Application;
import javafx.stage.Stage;

/*****************************************************************************************
 * <p>
 * Class Name:     CLASS_NAME
 * <p>
 * Purpose:        PURPOSE
 * <p>
 * Create By:      David Wei
 * Date:           7/18/2016
 * Last Modified:  Initial Revision
 * IDE Used:       Intellij 2016.1.3
 * <p>
 ****************************************************************************************/
public class P2PFS extends Application
{
    private GUIManager gui;
    private FileManager fm;
    private UserManager um;
    private NetworkCoordinator nm;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        fm = new FileManager();
        um = new UserManager();
        nm = new NetworkCoordinator();

        gui = new GUIManager(fm, um, primaryStage);

        fm.linkGui(gui);
        fm.linkNC(nm);

        um.linkGui(gui);
        um.linkNC(nm);
        um.linkFM(fm);

        nm.linkGui(gui);
        nm.linkFM(fm);
        nm.linkUM(um);

        gui.start(primaryStage);

    }

}

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static javafx.application.Platform.isFxApplicationThread;
import static javafx.application.Platform.runLater;

/*****************************************************************************************
 *  <p>
 *  Class Name:     GUIElements
 *  <p>
 *  Purpose:        Contain all the GUI elements in one location accessible by multiple
 *                  classes
 *  <p>
 *  Create By:      David Wei
 *  Date:           8/3/2016
 *  Last Modified:  Initial Revision
 *  IDE Used:       Intellij 2016.1.3
 *  <p>
 ****************************************************************************************/
class GUIElements
{
    private ScrollPane SP;
    private Scene scene;
    private Stage stage;

    private FileManagerGUI_IF fm;
    private UserManagerGUI_IF um;

    // Define Array lists for 3 types of lists
    // -- List of users who are local user (Only one user)
    private ArrayList<GUIUser> myList;
    // -- List of users who are we can download from
    private ArrayList<GUIUser> userList;
    // -- List of users who are downloading from us
    private ArrayList<GUIUser> uploadingList;

    final static int MAX_WINDOW_HEIGHT = 500;
    final static int MAX_WINDOW_WIDTH = 510;

    final static int POPUP_WINDOW_HEIGHT = 150;
    final static int POPUP_WINDOW_WIDTH = 300;

    final static int GUI_TIMEOUT_SEC = 600;

    GUIElements(Stage s, FileManagerGUI_IF f, UserManagerGUI_IF u)
    {
        stage = s;

        myList = new ArrayList<GUIUser>();
        userList = new ArrayList<GUIUser>();
        uploadingList = new ArrayList<GUIUser>();

        fm = f;
        um = u;
    }

    Scene getScene()
    {
        return scene;
    }

    ScrollPane getScrollPane()
    {
        return SP;
    }

    Stage getStage()
    {
        return stage;
    }


    UserManagerGUI_IF getUm()
    {
        return um;
    }


    FileManagerGUI_IF getFm()
    {
        return fm;
    }
    private GUIUser getUser(ArrayList<GUIUser> list, String name)
    {
        // Search for User
        for (GUIUser user : list)
        {
            if (user.getUsername().equals(name))
            {
                return user;
            }
        }

        // Throw exception if no user found
        throw new NoSuchElementException();
    }

    GUIUser getMe()
    {
        return myList.get(0);
    }

    GUIUser getUser(String name)
    {
       try
        {
            return getUser(userList, name);
        } catch (NoSuchElementException e)
        {
            throw e;
        }
    }

    GUIUser getUploadUser(String name)
    {
        try
        {
            return getUser(uploadingList, name);
        } catch (NoSuchElementException e)
        {
            throw e;
        }
    }

    void setScene(Scene s)
    {
        scene = s;
    }

    void setScrollPane(ScrollPane s)
    {
        SP = s;
    }

    void setStage(Stage s)
    {
        stage = s;
    }

    void setMe(GUIUser user)
    {
        myList.add(user);
    }

    // Add and Remove users
    void addUser(GUIUser user)
    {
        userList.add(user);
        redraw();
    }

    void removeUser(String name)
    {

        for (GUIUser user : userList)
        {
            if (user.getUsername().equals(name))
            {
                userList.remove(user);
                break;
            }
        }

        redraw();
    }

    void upload(String user, String filename)
    {
        // Create new file
        GUIFile file = new GUIFile(filename, this);
        file.setStatus(GUIFile.FileStatus.uploadingFile);

        // Set User
        GUIUser upldUser;

        // Check if user exists
        try
        {
            // if so, add file
            upldUser = getUser(uploadingList, user);
            upldUser.addFile(file);

        } catch (NoSuchElementException e)
        {
            // else add new user
            upldUser = new GUIUser(user, this);
            upldUser.addFile(file);
            uploadingList.add(upldUser);
        }

        redraw();
    }

    void finishUpload(String user, String filename)
    {
        // Set Download User
        GUIUser upldUser;

        // Check if user exists
        try
        {
            upldUser = getUser(uploadingList, user);
            upldUser.removeFile(filename);

            // if dnldUser has no more files, remove dnld user
            if (upldUser.getNumFiles() == 0)
                uploadingList.remove(upldUser);

        } catch (NoSuchElementException e)
        {
            throw e;
        }

        redraw();
    }
    void finishDownload(String user, String filename)
    {
        // Set Download User
        GUIUser dnldUser;

        // Check if user exists
        try
        {
            dnldUser = getUser(user);
            dnldUser.getFile(filename).setStatus(GUIFile.FileStatus.normalFile);
        } catch (NoSuchElementException e)
        {
            throw e;
        }

        redraw();
    }

    // Redraw the GUI
    void redraw()
    {
        TabPane oldTP = new TabPane();

        // Get current TabPane
        try
        {
            oldTP = (TabPane) SP.getContent();
        } catch (NullPointerException e)
        {
            SP = new ScrollPane();
        }

        // Create TabPane
        TabPane tp = new TabPane();

        // Add Create new tabs for each type of files
        Tab myFilesTab = new Tab();
        Tab networkFilesTab = new Tab();
        Tab requestedFilesTab = new Tab();

        // Create the tabs and create VBox with content from lists
        myFilesTab.setText("My Files");
        myFilesTab.setContent(createVB(myList));
        networkFilesTab.setText("Network Files");
        networkFilesTab.setContent(createVB(userList));
        requestedFilesTab.setText("Requested Files");
        requestedFilesTab.setContent(createVB(uploadingList));

        // Add tabs to TabPane
        tp.getTabs().add(myFilesTab);
        tp.getTabs().add(networkFilesTab);
        tp.getTabs().add(requestedFilesTab);

        // Set tabs to not have any close buttons
        tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Show the tab that was shown before
        try
        {
            tp.getSelectionModel().select(oldTP.getSelectionModel().getSelectedIndex());
        } catch(NullPointerException e)
        {}

        if (isFxApplicationThread())
        {
            SP.setContent(tp);
        }
        else
        {
            runLater(() -> redraw());
        }
    }

    private VBox createVB(ArrayList<GUIUser> list)
    {
        VBox vb = new VBox();
        vb.setLayoutX(5);
        vb.setSpacing(10);
        vb.setMinHeight(MAX_WINDOW_HEIGHT - 30);
        vb.setStyle("-fx-padding: 20;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;");

        for (GUIUser user : list)
        {
            for (HBox hb : user.getHBoxList())
            {
                vb.getChildren().add(hb);
            }
        }

        return vb;
    }

    // Checks if all downloads are finished before closing the GUI
    boolean closeGui()
    {

        um.close();

        System.out.println("Number of Users Downloading Items = " + uploadingList.size());

        if (uploadingList.size() > 0)
        {
            GUINotification notify = new GUINotification();

            notify.createNotification("Users are still downloading files. \nPlease wait until " +
                    "all downloads are finished", "Finishing downloads");

            return false;
        } else
        {
            System.out.println("Closing Gui");
            return true;
        }
    }
}


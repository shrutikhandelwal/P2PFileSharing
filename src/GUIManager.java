import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*****************************************************************************************
 * <p>
 * Class Name:     P2PFSGUI
 * <p>
 * Purpose:        The Coordinator class for the GUI
 * <p>
 * Create By:      David Wei
 * Date:           7/18/2016
 * Last Modified:  Initial Revision
 * IDE Used:       Intellij 2016.1.3
 * <p>
 ****************************************************************************************/

public class GUIManager extends Application implements
        GUIFileManager_IF, GUINetworkCoordinator_IF, GUIUserManager_IF
{
    // Define the GUI Elements
    private GridPane startUpGrid;
    private Scene startUpScene;
    private Stage startUpStage;

    // Username of offline person
    private String username;

    // Define guiElems object to hold all GUI components
    private GUIElements guiElems;

    // Define error element to throw notifications as needed
    private GUINotification notify = new GUINotification();

    // Define regular expression for format of username
    private final String USERNAME_REGEX = "^[A-Z|a-z][A-Z|a-z|0-9]+$";

    // Define Username max and min lengths
    private final int USERNAME_MAX_LENGTH = 10;
    private final int USERNAME_MIN_LENGTH = 3;

    // Bools to determine connection state
    private boolean connection;
    private boolean unameOk;

    // Create Timeout
    private PauseTransition timeout;

    public GUIManager(FileManagerGUI_IF fm, UserManagerGUI_IF um, Stage mainStage)
    {
        // Send GUI Stage element to guiElems
        guiElems = new GUIElements(mainStage, fm, um);

    }

    // Start the GUI
    @Override
    public void start(Stage mainStage)
    {
        connection = false;
        unameOk = false;

        createStartUpScene();
    }

    // Create popup window to ask for username and whether to join network or not
    private void createStartUpScene()
    {
        // Create buttons for join and create network
        Button joinNetworkBtn = new Button();
        joinNetworkBtn.setText("Join Network");

        Button createNetworkBtn = new Button();
        createNetworkBtn.setText("Create Network");

        // Create text and text field to grab username
        Label usernameLabel = new Label("Username: ");
        TextField usernameTextField = new TextField();

        // Add event handler to join network button
        joinNetworkBtn.setOnAction( (ActionEvent e) -> {

            // Grab Username
            username = usernameTextField.getText();
            System.out.println("Joining Network");

            // Grab invitation file
            FileChooser invFileChooser = new FileChooser();
            invFileChooser.setTitle("Select Invitation File");
            File file = invFileChooser.showOpenDialog(startUpStage);

            try
            {
                System.out.println("Opening file: " + file.getName());

                // Check for valid username
                if (checkUsername())
                {
                    System.out.println("Username verified");
                    System.out.println("Username: " + username);

                    try
                    {
                        guiElems.getUm().joinGroup(file, username);

                        timeout = new PauseTransition(Duration.seconds(guiElems.GUI_TIMEOUT_SEC));

                        GUINotification.createLoadingPopup();
                        timeout.setOnFinished(event ->
                        {
                            if (connection && unameOk)
                            {
                                System.out.println("Network joined successfully");

                                createMainStage();

                                GUIUser me = new GUIUser(username, guiElems, true);
                                guiElems.setMe(me);

                                guiElems.getStage().show();
                                startUpStage.close();
                                guiElems.redraw();
                            } else
                            {
                                System.out.println("Failed to join network");
                                notify.createError("Failed to join network");
                            }
                        });

                        timeout.play();
                    } catch (NoIPFoundException e1)
                    {
                        notify.createError("Invalid Invitation file");
                    }

                }
            } catch (NullPointerException e4)
            {}
        });

        createNetworkBtn.setOnAction((ActionEvent e) -> {

            username = usernameTextField.getText();
            System.out.println("Creating Network");

            // Verify Username
            if(checkUsername())
            {
                System.out.println("Username verified");
                System.out.println("Username: " + username);

                guiElems.getUm().createGroup(username);

                createMainStage();

                // Set current user as main user.
                GUIUser me = new GUIUser(username, guiElems, true);
                guiElems.setMe(me);

                guiElems.getStage().show();

                startUpStage.close();
                guiElems.redraw();
            }
        });

        // Setup Grid to hold elements for Startup Pop-Up
        startUpGrid = new GridPane();
        startUpGrid.setAlignment(Pos.CENTER);
        startUpGrid.setHgap(10);
        startUpGrid.setVgap(10);

        // Add created elements to the GridPane
        startUpGrid.add(usernameLabel, 0, 0);
        startUpGrid.add(usernameTextField, 1, 0);
        startUpGrid.add(joinNetworkBtn, 0, 1);
        startUpGrid.add(createNetworkBtn, 1, 1);

        // Set the GridPane as element in a new Scene
        startUpScene = new Scene(startUpGrid, guiElems.POPUP_WINDOW_WIDTH,
                guiElems.POPUP_WINDOW_HEIGHT);

        // Create new Stage and add Scene created earlier
        startUpStage = new Stage();
        startUpStage.setScene(startUpScene);
        // Set Pop-up as temporary
        startUpStage.initModality(Modality.APPLICATION_MODAL);
        startUpStage.setTitle("P2P File Sharing");

        // Show the startup pop-up
        startUpStage.show();
    }

    private void createMainStage()
    {
        // Create Main VBox to contain all main window elements
        VBox mainGroup = new VBox();
        guiElems.setScene(new Scene(mainGroup, guiElems.MAX_WINDOW_WIDTH,
                guiElems.MAX_WINDOW_HEIGHT));

        //
        final Button inviteBtn = new Button("Invite");
        inviteBtn.setOnAction( (ActionEvent e) -> {

            try
            {
                String directory  = new String ("Directory");
                directory = guiElems.getUm().generateInvitationFile();

                notify.createNotification("Invitation file Generated at \n" + directory,
                        "Invitation File Generated");
                System.out.println("Generated Invitation File");
            }
            catch (NoIPFoundException e1)
            {
                notify.createError("Failed to generate Invitation File");
                System.out.println("Failed to generate Invitation File");
            }
        });

        // Create ScrollPane to contain all elements and add scroll bar
        guiElems.setScrollPane(new ScrollPane(new TabPane()));
        guiElems.getScrollPane().setMinHeight(guiElems.MAX_WINDOW_HEIGHT - 30);

        // Add all elements to main group and set the stage
        mainGroup.getChildren().addAll(inviteBtn, guiElems.getScrollPane());
        guiElems.setStage(new Stage());
        guiElems.getStage().setScene(guiElems.getScene());
        guiElems.getStage().setTitle("P2P File Sharing");

        // Prevents GUI from exiting while files are still being downloaded
        guiElems.getScene().getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent ev) {
                if (!guiElems.closeGui())
                {
                    ev.consume();
                }
            }
        });
    }

    private Boolean checkUsername()
    {
        // Used for Regular Expression Pattern Matching
        Pattern p = Pattern.compile(USERNAME_REGEX);
        Matcher m = p.matcher(username);

        // Checks if Username is more than 3 characters
        if(username.length() < USERNAME_MIN_LENGTH)
        {
            notify.createError("Username must be longer than " + USERNAME_MIN_LENGTH
                    + " characters.");
            System.out.println("Username: " + username + " is invalid.");

            return false;
        }
        else if(username.length() > USERNAME_MAX_LENGTH)
        {
            notify.createError("Username must be less than " + USERNAME_MAX_LENGTH
                    + " characters long.");
            System.out.println("Username: " + username + " is invalid.");

            return false;
        }
        // Checks that Username is of pattern (Alpha char) + (Alpha numeric char)*x
        else if (!m.find())
        {
            notify.createError("Usernames may not contain the following characters:\n!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~");
            System.out.println("Username: " + username + " is invalid.");

            return false;
        }

        return true;
    }

    @Override
    public void fileOpSuccess()
    {
        System.out.println("File Operation was successful");
    }

    @Override
    public void fileNotFound()
    {
        notify.createError("File not found");
        System.out.println("File not Found");
    }

    @Override
    public void IOexception()
    {
        notify.createError("I/O Exception");
        System.out.println("I/O Exception encountered");
    }

    @Override
    public void uploadPercentComplete(String user, String filename, double percent)
    {
        // Update percentage of file
        try
        {
            guiElems.getUploadUser(user).getFile(filename).setUploadProgress(percent);
        } catch (NoSuchElementException e)
        {
            System.out.println("Update Percentage file not found. \n    Username: " + user
                    + "\n   Filename: " + filename);
        }

        if(percent == 1)
        {
            guiElems.finishUpload(user, filename);
        }

        System.out.println("Updated Percentage");
    }

    @Override
    public void downloadPercentComplete(String user, String filename, double percent)
    {
        // Update percentage of file
        try
        {
            guiElems.getUser(user).getFile(filename).setDownloadProgress(percent);
        } catch (NoSuchElementException e)
        {
            System.out.println("Update Percentage file not found. \n    Username: " + user
                    + "\n   Filename: " + filename);
        }

        if(percent == 1)
        {
            guiElems.finishDownload(user, filename);
        }

        System.out.println("Updated Percentage");
    }

    @Override
    public void addNewFile(String user, String filename)
    {
        guiElems.getUser(user).addFile(
                new GUIFile(filename, user, guiElems, false));

        System.out.println("Added User: " + user + " Filename: " + filename);
        guiElems.redraw();
    }

    @Override
    public void removeFile(String user, String filename)
    {
        try
        {
            // TODO: fix the file name / username
            guiElems.getUser(user).removeFile(filename);
            System.out.println("Removed User: " + user + " Filename: " + filename);
        } catch (NoSuchElementException e)
        {
            System.out.println("Remove Element not found. \n    Username: " + user
                    + "\n   Filename: " + filename);
        }

        guiElems.redraw();
    }

    @Override
    public void updateFile(String user, String filename)
    {
        // TODO: fix the file name / username
        try
        {
            guiElems.getUser(user).getFile(
                    filename).setStatus(GUIFile.FileStatus.updatedFile);
        }catch (NoSuchElementException e)
        {
            System.out.println("Update Element not found. \n    Username: " + user
                    + "\n   Filename: " + filename);
        }
        System.out.println("Updated User: " + user + " Filename: " + filename);

        guiElems.redraw();
    }

    @Override
    public void addUser(String username)
    {
        guiElems.addUser(new GUIUser(username, guiElems, false));
        System.out.println("Added User: " + username );
    }

    @Override
    public void removeUser(String username)
    {
        guiElems.removeUser(username);
        System.out.println("Removed User: " + username );
    }

    @Override
    public void connectionStatus(boolean established, boolean usernameOk)
    {
        connection = established;
        unameOk = usernameOk;

        if (!established)
        {
            notify.createError("Failed to connect to Network");
            System.out.println("Failed to connect to Network");
        } else
        {
            System.out.println("Connection Successful");

            if(!usernameOk)
            {
                notify.createError("Username already taken");
                System.out.println("Username taken");
            }
        }

        GUINotification.destroyLoadingPopup();
        timeout.jumpTo(Duration.seconds(guiElems.GUI_TIMEOUT_SEC));
    }

    @Override
    public void uploadStarted(String user, String filename)
    {
        guiElems.upload(user, filename);
        System.out.println("Upload Started");
    }
}
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
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
public class P2PFS_test extends Application
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

        // Setup GridPane for Notification PopUp
        GridPane testGrid = new GridPane();
        testGrid.setAlignment(Pos.CENTER);
        testGrid.setHgap(10);
        testGrid.setVgap(10);

        // Add addUser button
        Button addUserBtn = new Button("Add User");
        TextField addUserTextField = new TextField("Username");

        // Add removeUser button
        Button addFileBtn = new Button("Add File");
        TextField addFileUserTF = new TextField("Username");
        TextField addFileFilenameTF = new TextField("Filename");

        // Add removeUser button
        Button removeUserBtn = new Button("Remove User");
        TextField removeUserTextField = new TextField("Username");

        // Add removeUser button
        Button removeFileBtn = new Button("Remove File");
        TextField removeFileUserTF = new TextField("Username");
        TextField removeFileFilenameTF = new TextField("Filename");

        // Add removeUser button
        Button updateFileBtn = new Button("Update File");
        TextField updateFileUserTF = new TextField("Username");
        TextField updateFileFilenameTF = new TextField("Filename");

        // Add removeUser button
        Button uploadPercentBtn = new Button("Download FROM Local Percent");
        TextField uploadPercentUserTF = new TextField("Username");
        TextField uploadPercentFilenameTF = new TextField("Filename");
        TextField uploadPercentTF = new TextField(".1");

        // Download file
        Button uploadFileBtn = new Button("Download File FROM Local User");
        TextField uploadUserTF = new TextField("Username");
        TextField uploadFilenameTF = new TextField("Filename");

        Button downloadPercentBtn = new Button("Download TO Local Percent");
        TextField downloadPercentUserTF = new TextField("Username");
        TextField downloadPercentFilenameTF = new TextField("Filename");
        TextField downloadPercentTF = new TextField(".1");

        Button connSucc = new Button("Connection Ok");
        Button connFail = new Button("Connection Fail");
        Button uTaken = new Button("Username Taken");
        Button fileIOEx = new Button("Throw I/O Ex");
        Button fileNotFound = new Button("Throw File Not Found Ex");
        Button fileOpSucc = new Button("File Op Success");


        // Add elements to GridPane
        testGrid.add(addUserBtn, 0, 0);
        testGrid.add(addUserTextField, 1, 0);

        testGrid.add(addFileBtn, 0, 1);
        testGrid.add(addFileUserTF, 1, 1);
        testGrid.add(addFileFilenameTF, 2, 1);

        testGrid.add(removeFileBtn, 0, 2);
        testGrid.add(removeFileUserTF, 1, 2);
        testGrid.add(removeFileFilenameTF, 2, 2);

        testGrid.add(updateFileBtn, 0, 3);
        testGrid.add(updateFileUserTF, 1, 3);
        testGrid.add(updateFileFilenameTF, 2, 3);

        testGrid.add(removeUserBtn, 0, 4);
        testGrid.add(removeUserTextField, 1, 4);

        testGrid.add(uploadFileBtn, 0, 6);
        testGrid.add(uploadUserTF, 1, 6);
        testGrid.add(uploadFilenameTF, 2, 6);
        testGrid.add(uploadPercentBtn, 0, 7);
        testGrid.add(uploadPercentUserTF, 1, 7);
        testGrid.add(uploadPercentFilenameTF, 2, 7);
        testGrid.add(uploadPercentTF, 3, 7);
        testGrid.add(downloadPercentBtn, 0, 8);
        testGrid.add(downloadPercentUserTF, 1, 8);
        testGrid.add(downloadPercentFilenameTF, 2, 8);
        testGrid.add(downloadPercentTF, 3, 8);

        testGrid.add(connSucc, 0, 10);
        testGrid.add(connFail, 1, 10);
        testGrid.add(uTaken, 2, 10);
        testGrid.add(fileNotFound, 0, 11);
        testGrid.add(fileOpSucc, 1, 11);
        testGrid.add(fileIOEx, 2, 11);

        // Create scene
        Scene testScene = new Scene(testGrid, 700, 400);

        // Create stage
        Stage testStage = new Stage();
        testStage.setScene(testScene);
        testStage.setTitle("P2PFS_Gui Tester");

        // Show Popup
        testStage.show();

        // Set action on clicking OK button to close
        addUserBtn.setOnAction((ActionEvent e) ->
        {
            gui.addUser(addUserTextField.getText());
        });

        addFileBtn.setOnAction((ActionEvent e) ->
        {

            gui.addNewFile( addFileUserTF.getText(), addFileFilenameTF.getText());
        });

        removeUserBtn.setOnAction((ActionEvent e) ->
        {
            gui.removeUser(removeUserTextField.getText());
        });

        removeFileBtn.setOnAction((ActionEvent e) ->
        {

            gui.removeFile( removeFileUserTF.getText(), removeFileFilenameTF.getText());
        });

        updateFileBtn.setOnAction((ActionEvent e) ->
        {

            gui.updateFile( updateFileUserTF.getText(), updateFileFilenameTF.getText());
        });

        uploadFileBtn.setOnAction((ActionEvent e) ->
        {
            gui.uploadStarted(uploadUserTF.getText(),
                            uploadFilenameTF.getText());
        });

        uploadPercentBtn.setOnAction((ActionEvent e) ->
        {
            gui.uploadPercentComplete(uploadPercentUserTF.getText(),
                    uploadPercentFilenameTF.getText(),
                    Double.parseDouble(uploadPercentTF.getText()));
        });

        downloadPercentBtn.setOnAction((ActionEvent e) ->
        {
            gui.downloadPercentComplete(downloadPercentUserTF.getText(),
                    downloadPercentFilenameTF.getText(),
                    Double.parseDouble(downloadPercentTF.getText()));
        });

        connSucc.setOnAction((ActionEvent e) ->
        {
            gui.connectionStatus(true, true);
        });
        connFail.setOnAction((ActionEvent e) ->
        {
            gui.connectionStatus(false, true);
        });
        uTaken.setOnAction((ActionEvent e) ->
        {
            gui.connectionStatus(true, false);
        });
        fileIOEx.setOnAction((ActionEvent e) ->
        {
            gui.IOexception();
        });
        fileNotFound.setOnAction((ActionEvent e) ->
        {
            gui.fileNotFound();
        });
        fileOpSucc.setOnAction((ActionEvent e) ->
        {
            gui.fileOpSuccess();
        });
    }

}

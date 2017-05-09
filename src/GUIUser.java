import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/*****************************************************************************************
 *  <p>
 *  Class Name:     GUIUser
 *  <p>
 *  Purpose:        Class to contain a User
 *  <p>
 *  Create By:      David Wei
 *  Date:           8/3/2016
 *  Last Modified:  Initial Revision
 *  IDE Used:       Intellij 2016.1.3
 *  <p>
 ****************************************************************************************/
class GUIUser
{
    private String username;
    // Boolean to determine if User is local user
    private boolean isMe;
    private GUIElements guiElem;

    // ArrayList to contain all files associated with user
    private ArrayList<GUIFile> filelist;

    GUIUser(String name, GUIElements elems, Boolean me)
    {
        username = name;
        isMe = me;

        guiElem = elems;

        filelist = new ArrayList<GUIFile>();
    }

    GUIUser(String name, GUIElements elems)
    {
        username = name;
        isMe = false;

        guiElem = elems;

        filelist = new ArrayList<GUIFile>();
    }

    String getUsername()
    {
        return username;
    }

    void removeFile(String filename)
    {
        for (GUIFile file : filelist)
        {
            if (file.getFilename().equals(filename))
            {
                filelist.remove(file);
                break;
            }
        }
    }

    GUIFile getFile(String filename)
    {

        // Search for file
        for (GUIFile file : filelist)
        {
            if (file.getFilename().equals(filename))
            {
                return file;
            }
        }

        // Throw exception if no element is found
        throw new NoSuchElementException();
    }

    void addFile(GUIFile file)
    {
        filelist.add(file);
    }

    int getNumFiles()
    {
        return filelist.size();
    }

    // Create ArrayList of HBox elements to add to GUI
    ArrayList<HBox> getHBoxList()
    {
        ArrayList<HBox> list = new ArrayList<HBox>();

        // Define separators for each user
        Separator sep1 = new Separator();
        sep1.setOrientation(Orientation.HORIZONTAL);
        Separator sep2 = new Separator();
        sep2.setOrientation(Orientation.HORIZONTAL);
        sep1.setMinWidth(guiElem.MAX_WINDOW_WIDTH - 70);
        sep2.setMinWidth(guiElem.MAX_WINDOW_WIDTH - 70);

        // Create HBoxes to hold separators
        HBox sep1HB = new HBox();
        HBox sep2HB = new HBox();
        sep1HB.getChildren().add(sep1);
        sep2HB.getChildren().add(sep2);

        // Add user to Arraylist
        list.add(sep1HB);
        list.add(createHBox());
        list.add(sep2HB);

        // Add files
        for (GUIFile element : filelist)
        {
            list.add(element.createHBox());
        }

        return list;
    }

    // Create HBox for User
    private HBox createHBox()
    {
        HBox hb = new HBox();

        // Create Username and Font
        Label user = new Label(username);
        user.setFont(new Font("Arial", 15));
        user.setMinWidth(guiElem.MAX_WINDOW_WIDTH - 110);

        // If user is local user
        if(isMe)
        {
            // Add Add button
            Button addBtn = new Button("Add");

            addBtn.setOnAction( (ActionEvent e) -> {
                FileChooser invFileChooser = new FileChooser();
                invFileChooser.setTitle("Select File to Add");
                File file = invFileChooser.showOpenDialog(guiElem.getStage());


                GUIFile newFile = new GUIFile(file.getName(), username, guiElem, isMe);

                if(filelist.contains(newFile))
                {
                    GUINotification.createError("Duplicate File");
                }
                else
                {
                    filelist.add(newFile);

                    guiElem.getFm().addUserFile(file);

                    guiElem.redraw();
                }
            });

            hb.getChildren().addAll(user, addBtn);
        }
        else
        {
            hb.getChildren().add(user);
        }

        return hb;
    }
}

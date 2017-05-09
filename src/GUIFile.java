import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;

/*****************************************************************************************
 *  <p>
 *  Class Name:     GUIFile
 *  <p>
 *  Purpose:        Class to contain files
 *  <p>
 *  Create By:      David Wei
 *  Date:           8/3/2016
 *  Last Modified:  Initial Revision
 *  IDE Used:       Intellij 2016.1.3
 *  <p>
 ****************************************************************************************/
class GUIFile
{
    private String filename;
    private String username;
    private FileStatus status;
    private double downloadProgress;
    private double uploadProgress;
    private boolean isMe;
    private Image fileImg;
    private GUIElements guiElem;

    // Define images for file icons
    private static final Image newFile = new Image("/resources/newFile.png");
    private static final Image downloadingFile = new Image("/resources/downloadingFile.png");
    private static final Image normalFile = new Image("/resources/normalFile.png");
    private static final Image updatedFile = new Image("/resources/updatedFile.png");

    // Define maximum filename characters
    private final int MAX_FILENAME_CHARS = 50;

    // enum for file status
    enum FileStatus
    {
        newFile, downloadingFile, normalFile, updatedFile, uploadingFile
    };

    GUIFile(String file, String user, GUIElements elem, Boolean me)
    {
        filename = file;
        username = user;
        isMe = me;
        downloadProgress = 0;
        uploadProgress = 0;

        // If file belongs to local user, set icon to normalFile, otherwise newFile
        if(isMe)
        {
            status = FileStatus.normalFile;
            fileImg = normalFile;
        }
        else
        {
            status = FileStatus.newFile;
            fileImg = newFile;
        }

        guiElem = elem;
    }

    // Constructor if file is definitely not for local user
    GUIFile(String file, GUIElements elem)
    {
        filename = file;
        isMe = false;

        status = FileStatus.newFile;
        fileImg = newFile;

        guiElem = elem;
    }

    void setStatus(FileStatus st)
    {
        switch(st)
        {
            case downloadingFile:
                status = FileStatus.downloadingFile;
                fileImg = downloadingFile;
                break;
            case newFile:
                status = FileStatus.newFile;
                fileImg = newFile;
                break;
            case normalFile:
                status = FileStatus.normalFile;
                fileImg = normalFile;
                break;
            case updatedFile:
                status = FileStatus.updatedFile;
                fileImg = updatedFile;
                break;
            case uploadingFile:
                status = FileStatus.uploadingFile;
                fileImg = normalFile;
                break;
        }
    }

    FileStatus getStatus()
    {
        return status;
    }

    String getFilename()
    {
        return filename;
    }

    void setUploadProgress(double progress)
    {
        if(progress < 1 && progress > 0)
            uploadProgress = progress;

        guiElem.redraw();
    }

    void setDownloadProgress(double progress)
    {
        if(progress < 1 && progress > 0)
            downloadProgress = progress;

        guiElem.redraw();
    }

    // Create HBox for file
    HBox createHBox()
    {
        HBox hb = new HBox();

        ImageView img = new ImageView(fileImg);
        img.setFitHeight(10);
        img.setFitWidth(10);
        img.setPreserveRatio(true);

        Label icon = new Label("", img);

        Label file;
        if (filename.length() > MAX_FILENAME_CHARS)
        {
            file = new Label(filename.substring(0, MAX_FILENAME_CHARS - 4)
                    + "...");
        }
        else
        {
            file = new Label(filename);
        }

        icon.setMinWidth(50);
        file.setMinWidth(guiElem.MAX_WINDOW_WIDTH - 235);

        hb.getChildren().add(icon);
        hb.getChildren().add(file);

        if(isMe)
        {
            Button updateBtn = new Button("Update");
            Button removeBtn = new Button("Remove");

            updateBtn.setOnAction( (ActionEvent e) -> {
                FileChooser invFileChooser = new FileChooser();
                invFileChooser.setTitle("Select File to Update");
                File updatedFile = invFileChooser.showOpenDialog(guiElem.getStage());

                if(updatedFile.getName().equals(filename))
                {
                    System.out.println("Updated file: " + updatedFile.getName());
                    guiElem.getFm().updateUserFile(updatedFile);
                }
                else
                {
                    GUINotification.createError("Filenames do not match");
                }
            });

            removeBtn.setOnAction( (ActionEvent e) -> {
                guiElem.getMe().removeFile(filename);
                guiElem.getFm().removeUserFile(filename);

                guiElem.redraw();
            });

            hb.getChildren().add(updateBtn);
            hb.getChildren().add(removeBtn);
        }
        else
        {
            if(status == FileStatus.downloadingFile)
            {
                ProgressBar progressBar = new ProgressBar();
                progressBar.setProgress(downloadProgress);

                hb.getChildren().add(progressBar);
            }
            else if(status == FileStatus.uploadingFile)
            {
                ProgressBar progressBar = new ProgressBar();
                progressBar.setProgress(uploadProgress);

                hb.getChildren().add(progressBar);
            }
            else if(status != FileStatus.normalFile)
            {
                Button getBtn = new Button("Get");
                hb.getChildren().add(getBtn);

                getBtn.setOnAction( (ActionEvent e) -> {
                    guiElem.getFm().getUserFile(filename, username);

                    setStatus(FileStatus.downloadingFile);

                    guiElem.redraw();
                });

            }
        }

        return hb;
    }

}

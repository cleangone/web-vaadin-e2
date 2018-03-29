package xyz.cleangone.e2.web.vaadin.desktop.image;

import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.vaadin.ui.*;
import xyz.cleangone.data.manager.ImageManager;
import xyz.cleangone.e2.web.vaadin.util.MessageDisplayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class ImageUploader implements Upload.Receiver, Upload.SucceededListener
{
    private final ImageManager imgMgr;
    private final ImageDisplayer imgDisplayer;
    private final MessageDisplayer msgDisplayer;

    private String filename;
    private File file;

    public ImageUploader(ImageManager imgMgr, ImageDisplayer imgDisplayer, MessageDisplayer msgDisplayer)
    {
        this.imgMgr = imgMgr;
        this.imgDisplayer = imgDisplayer;
        this.msgDisplayer = msgDisplayer;
    }

    public OutputStream receiveUpload(String filename, String mimeType)
    {
        this.filename = filename;
        FileOutputStream fos = null;
        try
        {
            String[] splitFilename = filename.split("\\.");
            if (splitFilename.length != 2) { throw new Exception("File must be of form name.ext"); }

            // will create a file name_<random>.ext in config'd temp dir
            file = File.createTempFile(splitFilename[0] + "_", "." + splitFilename[1]);
            file.deleteOnExit();

            fos = new FileOutputStream(file);
        }
        catch (Exception e)
        {
            msgDisplayer.displayMessage("Image upload failed");
            throw new RuntimeException("Error uploading file " + filename, e);
        }

        return fos;
    }

    public void uploadSucceeded(Upload.SucceededEvent event)
    {
        // upload to temp has completed, now upload to s3
        S3Link s3Link = imgMgr.uploadImage(filename, file);
        msgDisplayer.displayMessage("Image uploaded");

        // reset the displayed images
        imgDisplayer.setImages();
    }
}

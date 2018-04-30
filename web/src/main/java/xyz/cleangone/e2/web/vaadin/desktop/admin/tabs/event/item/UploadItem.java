package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.item;

import java.io.File;

public class UploadItem
{
    private String itemName;
    private String filename;
    private File tempFile;

    public UploadItem(String itemName, String filename, File tempFile)
    {
        this.itemName = itemName;
        this.filename = filename;
        this.tempFile = tempFile;
    }

    public UploadItem(String filename)
    {
        this.filename = filename;

        String[] splitFilename = filename.split("\\.");
        if (splitFilename.length != 2) { throw new RuntimeException("File must be of form name.ext"); }

        itemName = splitFilename[0];

        try
        {
            // will create a file name_<random>.ext in config'd temp dir
            tempFile = File.createTempFile(itemName + "_", "." + splitFilename[1]);

            // todo - deletes when vm exits - is this when session ends or is alive as long as server?
            tempFile.deleteOnExit();
        }
        catch (Exception e)
        {
            // msgDisplayer.displayMessage("Image upload failed");
            throw new RuntimeException("Error creating UploadItem for file " + filename, e);
        }
    }

    public String getItemName()
        {
            return itemName;
        }
    public String getFilename()
        {
            return filename;
        }
    public File getTempFile()
        {
            return tempFile;
        }
}

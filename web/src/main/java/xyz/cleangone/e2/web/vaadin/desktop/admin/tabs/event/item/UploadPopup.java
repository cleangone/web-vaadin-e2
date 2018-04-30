package xyz.cleangone.e2.web.vaadin.desktop.admin.tabs.event.item;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.AllUploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.ImageManager;
import xyz.cleangone.data.manager.TagManager;
import xyz.cleangone.data.manager.event.ItemManager;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

public class UploadPopup extends HorizontalLayout
{
    private final ItemsAdmin itemsAdmin;
    private ComboBox<OrgTag> categoryComboBox = new ComboBox<>();
    private final List<UploadItem> uploadItems = new ArrayList<>();

    public UploadPopup(ItemsAdmin itemsAdmin)
    {
        this.itemsAdmin = itemsAdmin;

        setMargin(true);
        setSpacing(true);
        setSizeUndefined();

        categoryComboBox.addStyleName(ValoTheme.TEXTFIELD_TINY);
        categoryComboBox.setPlaceholder("Category");
        categoryComboBox.setItemCaptionGenerator(OrgTag::getName);
        categoryComboBox.setEnabled(true);

        MultiFileUpload multiUpload = new MultiFileUpload(new MyUploadFinishedHandler(), new UploadStateWindow());
//        multiUpload.setCaption("Multiple Upload");
        multiUpload.setPanelCaption("Multiple Upload Panel");
        multiUpload.getSmartUpload().setUploadButtonCaptions("Upload File", "Upload Files");
        multiUpload.setAllUploadFinishedHandler(new MyAllUploadFinishedHandler());

        addComponents(categoryComboBox, multiUpload);
    }

    void setCategories(List<OrgTag> categories)
    {
        categoryComboBox.setItems(categories);
    }

    class MyUploadFinishedHandler implements UploadFinishedHandler
    {
        public void handleFile( InputStream inputStream, String filename, String mimeType, long bytesReceived, int uploadQueueSize)
        {
            try
            {
                UploadItem uploadItem = new UploadItem(filename);
                uploadItems.add(uploadItem);

                FileOutputStream outputStream = FileUtils.openOutputStream(uploadItem.getTempFile());
                IOUtils.copy(inputStream, outputStream);
                outputStream.close(); // don't swallow close Exception if copy completes normally
            }
            catch (Exception e)
            {
                // msgDisplayer.displayMessage("Image upload failed");
                throw new RuntimeException("Error uploading file " + filename, e);
            }
        }
    }

    class MyAllUploadFinishedHandler implements AllUploadFinishedHandler
    {
        public void finished()
        {
            for (UploadItem uploadItem : uploadItems)
            {
                OrgTag category = categoryComboBox.getValue();
                String categoryId = category == null ? null : category.getId();

                ItemManager existingItemMgr = itemsAdmin.getItemManager();
                CatalogItem newItem = existingItemMgr.createItem(uploadItem.getItemName(), categoryId);

                ItemManager newItemMgr = new ItemManager(existingItemMgr, newItem);
                ImageManager imageMgr = newItemMgr.getImageManager();

                // upload image and add to item
                imageMgr.uploadImage(uploadItem.getFilename(), uploadItem.getTempFile());

                // todo - needed if temp files kept as long as server up - make part of uploadItem
                // uploadItem.getFile().delete();
            }

            uploadItems.clear();
            itemsAdmin.set();
        }
    }
}

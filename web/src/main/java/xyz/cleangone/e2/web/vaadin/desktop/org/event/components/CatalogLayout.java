package xyz.cleangone.e2.web.vaadin.desktop.org.event.components;

import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.*;
import xyz.cleangone.data.aws.dynamo.entity.item.CatalogItem;
import xyz.cleangone.data.aws.dynamo.entity.organization.OrgTag;
import xyz.cleangone.data.manager.EventManager;
import xyz.cleangone.data.manager.ImageManager;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageDimension;
import xyz.cleangone.e2.web.vaadin.desktop.image.ImageLabel;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.EventUtils;
import xyz.cleangone.e2.web.vaadin.desktop.org.event.ItemPage;

import java.util.List;

public class CatalogLayout extends GridLayout
{
    private static final int ITEM_COLS = 2;  // todo - make configurable

    public CatalogLayout(List<CatalogItem> items, EventManager eventMgr, ImageManager imageMgr)
    {
        int rows = Math.max((items.size()+1)/ITEM_COLS, 1);
        setColumns(ITEM_COLS);
        setRows(rows);
        setMargin(false);

        for (CatalogItem item : items)
        {
            addComponent(getItemLayout(item, eventMgr, imageMgr));
        }
    }

    private Component getItemLayout(CatalogItem item, EventManager eventMgr, ImageManager imageMgr)
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(false);
        layout.setStyleName("category");
        layout.setDefaultComponentAlignment(new Alignment(AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER));

        List<S3Link> images = item.getImages();
        if (images != null && !images.isEmpty())
        {
            String imageUrl = imageMgr.getUrl(images.get(0));
            ImageLabel imageLabel = new ImageLabel(imageUrl, ImageDimension.height(250));
            layout.addComponent(imageLabel);
        }

        layout.addComponent(new Label(item.getName()));

        HorizontalLayout priceLayout = new HorizontalLayout();
        layout.addComponent(priceLayout);
        priceLayout.setMargin(false);
        priceLayout.setSizeUndefined();
        priceLayout.addComponent(new Label(item.getDisplayPrice()));
        if (item.isSold()) { priceLayout.addComponent(EventUtils.getSoldLabel()); }

        layout.addLayoutClickListener( e -> {
            eventMgr.setItem(item);
            getUI().getNavigator().addView(ItemPage.NAME, new ItemPage());
            getUI().getNavigator().navigateTo(ItemPage.NAME);
        });

        return layout;
    }

}
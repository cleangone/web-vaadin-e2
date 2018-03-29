package xyz.cleangone.e2.web.vaadin.util;

import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

public class LeftCenterRightLayout extends HorizontalLayout
{
    private HorizontalLayout leftLayout = new HorizontalLayout();
    private HorizontalLayout centerLayout = new HorizontalLayout();
    private HorizontalLayout rightLayout = new HorizontalLayout();

    public LeftCenterRightLayout(String leftWdith, String centerWidth, String rightWidth)
    {
        setWidth("100%");
        setMargin(false);
        setSpacing(false);

        leftLayout.setWidth(leftWdith);
        centerLayout.setWidth(centerWidth);

        rightLayout.setWidth(rightWidth);
        rightLayout.setSpacing(true);

        addComponents(leftLayout, centerLayout, rightLayout);
        setComponentAlignment(centerLayout, new Alignment(AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER));
        setComponentAlignment(rightLayout, new Alignment(AlignmentInfo.Bits.ALIGNMENT_RIGHT));
        setExpandRatio(leftLayout, 1.0f);
        setExpandRatio(centerLayout, 1.0f);
        setExpandRatio(rightLayout, 3.0f);
    }


    public void setLeftComponent(Component component)
    {
        leftLayout.removeAllComponents();
        leftLayout.addComponent(component);
    }





    public void addLeftComponent(Component component)
    {
        leftLayout.addComponent(component);
    }

    public void addCenterComponent(Component component)
    {
        centerLayout.addComponents(component);
        centerLayout.setComponentAlignment(component,
            new Alignment(AlignmentInfo.Bits.ALIGNMENT_VERTICAL_CENTER & AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER));
    }

    public void addRightComponent(Component component)
    {
        rightLayout.addComponents(component);
        rightLayout.setComponentAlignment(component,
            new Alignment(AlignmentInfo.Bits.ALIGNMENT_VERTICAL_CENTER & AlignmentInfo.Bits.ALIGNMENT_RIGHT));
    }

}
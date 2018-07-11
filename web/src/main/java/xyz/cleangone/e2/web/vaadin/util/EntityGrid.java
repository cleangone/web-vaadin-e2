package xyz.cleangone.e2.web.vaadin.util;

import com.vaadin.data.ValueProvider;
import com.vaadin.server.Setter;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.EditorSaveListener;
import com.vaadin.ui.components.grid.FooterRow;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.DateRenderer;
import org.vaadin.dialogs.ConfirmDialog;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;

import java.util.Date;

import static xyz.cleangone.e2.web.vaadin.util.VaadinUtils.*;

public class EntityGrid<T extends BaseEntity> extends Grid<T>
{
    protected final int ICON_COL_WIDTH = 80;
    protected Label countLabel = new Label();

    protected Grid.Column<T, String> addColumn(EntityField entityField, ValueProvider<T, String> valueProvider)
    {
        return addColumn(entityField, valueProvider, 1);
    }

    protected Grid.Column<T, String> addColumn(EntityField entityField, ValueProvider<T, String> valueProvider, int expandRatio)
    {
        return addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName()).setExpandRatio(expandRatio);
    }

    protected Grid.Column<T, String> addSortColumn(EntityField entityField, ValueProvider<T, String> valueProvider, Setter<T, String> setter)
    {
        Grid.Column<T, String> col = addColumn(entityField, valueProvider, setter);
        sort(col);

        return col;
    }


    protected Grid.Column<T, String> addColumn(EntityField entityField, ValueProvider<T, String> valueProvider, Setter<T, String> setter)
    {
        return addColumn(entityField, valueProvider)
            .setEditorComponent(new TextField(), setter);
    }


//    private void addBooleanColumn(
//        Grid<User> grid, EntityField entityField, ValueProvider<User, Boolean> valueProvider, Setter<User, Boolean> setter)
//    {
//        grid.addColumn(valueProvider)
//            .setId(entityField.getName()).setCaption(entityField.getDisplayName())
//            .setEditorComponent(new CheckBox(), setter);
//    }

    protected Grid.Column<T, Date> addDateColumn(EntityField entityField, ValueProvider<T, Date> valueProvider)
    {
        return addDateColumn(entityField, valueProvider, 1);
    }
    protected Grid.Column<T, Date> addDateColumn(EntityField entityField, ValueProvider<T, Date> valueProvider, int expandRatio)
    {
        return addColumn(valueProvider)
            .setId(entityField.getName()).setCaption(entityField.getDisplayName())
            .setRenderer(new DateRenderer(PageUtils.SDF_ADMIN_GRID))
            .setExpandRatio(expandRatio);
    }

//    private Grid.Column<T, BigDecimal> addBigDecimalColumn(EntityField entityField, ValueProvider<T, BigDecimal> valueProvider, int expandRatio)
//    {
//        return addColumn(valueProvider)
//            .setId(entityField.getName()).setCaption(entityField.getDisplayName()).setExpandRatio(expandRatio);
//    }


    protected Component buildDeleteButton(T entity, String name)
    {
        return createDeleteButton("Delete '" + name + "'", getUI(), new ConfirmDialog.Listener() {
            public void onClose(ConfirmDialog dialog) { if (dialog.isConfirmed()) { delete(entity); } }
        });
    }

    protected Button buildDeleteButton(T entity, String buttonDescription, String confirmDialogCaption, String deleteMsg)
    {
        Button button = createDeleteButton(buttonDescription);
        addDeleteClickListener(entity, button, confirmDialogCaption, deleteMsg);

        return button;
    }

    protected void addDeleteClickListener(T entity, Button button, String confirmDialogCaption, String deleteMsg)
    {
        button.addClickListener(e -> {
            ConfirmDialog.show(getUI(), confirmDialogCaption, deleteMsg, "Delete", "Cancel", new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) { delete(entity); }
                }
            });
        });
    }

    protected void delete(T entity) { }

    protected void setEditor(EditorSaveListener<T> listener)
    {
        getEditor().setEnabled(true);
        getEditor().setBuffered(true);
        getEditor().addSaveListener(listener);
    }

    protected void setColumnFiltering(CountingDataProvider<T> dataProvider)
    {
        setColumnFiltering(new MultiFieldFilter<T>(dataProvider), appendHeaderRow(), dataProvider);
    }

    protected void setColumnFiltering(MultiFieldFilter<T> filter, HeaderRow filterHeader, CountingDataProvider<T> dataProvider) { }

    protected TextField addFilterField(EntityField entityField, ValueProvider<T, String> valueProvider, MultiFieldFilter<T> filter, HeaderRow filterHeader)
    {
        filter.addField(entityField, valueProvider);
        return VaadinUtils.addFilterField(entityField, filter, filterHeader);
    }

    protected void appendCountFooterRow(EntityField field)
    {
        FooterRow footerRow = appendFooterRow();
        footerRow.getCell(field.getName()).setComponent(countLabel);
    }
}

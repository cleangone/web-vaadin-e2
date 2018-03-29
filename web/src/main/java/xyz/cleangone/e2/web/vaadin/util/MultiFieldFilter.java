package xyz.cleangone.e2.web.vaadin.util;

import com.vaadin.data.ValueProvider;
import xyz.cleangone.data.aws.dynamo.entity.base.BaseEntity;
import xyz.cleangone.data.aws.dynamo.entity.base.EntityField;


import java.util.*;

public class MultiFieldFilter<T extends BaseEntity>
{
    private final CountingDataProvider<T> dataProvider;

    private final List<EntityField> entityFields = new ArrayList<>();
    private final Map<EntityField, ValueProvider<T, String>> valueProviders = new HashMap<>();
    private final Map<EntityField, String> filterValues = new HashMap<>();

    public MultiFieldFilter(CountingDataProvider<T> dataProvider)
    {
        this.dataProvider = dataProvider;
    }

    public void addField(EntityField entityField, ValueProvider<T, String> valueProvider)
    {
        entityFields.add(entityField);
        valueProviders.put(entityField, valueProvider);
    }

    public void resetFilters(EntityField entityField, String filterValue)
    {
        // keep the field's last filter value
        filterValues.put(entityField, filterValue);

        // set all filters - filtered values will be an AND of all set filters
        dataProvider.clearFilters();
        for (EntityField field : entityFields)
        {
            addFilter(field);
        }
    }

    // add the filter to field in the dataprovider
    private void addFilter(EntityField entityField)
    {
        String filterValue = filterValues.get(entityField);
        if (filterValue == null ||filterValue.length() == 0) { return; }

        dataProvider.addFilter(
            valueProviders.get(entityField),
            fieldValue ->
            {
                if (fieldValue == null) { return false; }

                if (filterValue.contains(","))
                {
                    // break up csv filter value
                    List<String> splitFilterValues = Arrays.asList(filterValue.split("\\s*,\\s*"));
                    for (String splitValue : splitFilterValues)
                    {
                        if (!contains(fieldValue, splitValue)) { return false; }
                    }

                    return true; // field contains all pieces of split
                }

                return contains(fieldValue, filterValue);
            });
    }

    private boolean contains(String stringToCheck, String containsExpression)
    {
        if (stringToCheck == null && containsExpression == null) { return false; }

        String contains = containsExpression.trim().toLowerCase();
        if (contains.startsWith("!"))
        {
            contains = contains.substring(1);
            return !stringToCheck.toLowerCase().contains(contains);
        }

        return stringToCheck.toLowerCase().contains(contains);
    }
}

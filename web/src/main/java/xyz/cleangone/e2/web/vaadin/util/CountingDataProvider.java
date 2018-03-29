package xyz.cleangone.e2.web.vaadin.util;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.Label;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CountingDataProvider<T> extends ListDataProvider<T>
{
    private final Label countLabel;
    private final Collection<T> items;


    public CountingDataProvider(Collection<T> items, Label countLabel)
    {
        super(items);
        this.countLabel = countLabel;
        this.items = items;

        setCountLabelValue(items.size());
    }

    @Override
    public Stream<T> fetch(Query<T, SerializablePredicate<T>> query)
    {
        Supplier<Stream<T>> supplier = () -> super.fetch(query);

        setCountLabelValue(supplier.get().count());

        return supplier.get();
    }

    public void resetItems(Collection<T> newItems)
    {
        items.clear();
        items.addAll(newItems);
    }

    private void setCountLabelValue(long count)
    {
        countLabel.setValue("Count: " + count);
    }
}


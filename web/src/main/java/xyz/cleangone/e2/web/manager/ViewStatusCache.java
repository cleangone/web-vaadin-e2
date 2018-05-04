package xyz.cleangone.e2.web.manager;

import java.util.*;

public class ViewStatusCache
{
    private Map<String, ViewStatus> viewStatusByEventCategoryId = new HashMap<>();

    public ViewStatus getViewStatus(String eventId, String categoryId)
    {
        ViewStatus viewStatus = viewStatusByEventCategoryId.get(getKey(eventId, categoryId));
        if (viewStatus == null)
        {
            viewStatus = new ViewStatus();
            viewStatusByEventCategoryId.put(getKey(eventId, categoryId), viewStatus);
        }

        return viewStatus;
    }

    private String getKey(String eventId, String categoryId)
    {
        return eventId + "-" +categoryId;
    }
}

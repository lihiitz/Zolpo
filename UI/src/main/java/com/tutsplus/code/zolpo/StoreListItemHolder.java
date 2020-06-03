package com.tutsplus.code.zolpo;

import com.tutsplus.code.zolpo.Models.StoreListItem;

import java.util.ArrayList;
import java.util.List;

public class StoreListItemHolder {

    private List<StoreListItem> mStoreListItems, mFullStoreListItems;
    private int mMaxDistance =7;

    public List<StoreListItem> getmStoreListItems() {
        return mStoreListItems;
    }

    public StoreListItemHolder(){

    }

    public StoreListItemHolder(List<StoreListItem> mStoreListItems, int iMaxDistance) {
        this.mStoreListItems = mStoreListItems;
        this.mMaxDistance =iMaxDistance;
    }

    public void setStoreListItems(List<StoreListItem> mStoreListItems) {
        this.mStoreListItems = mStoreListItems;
        this.mFullStoreListItems = mStoreListItems;
    }

    public int getMaxDistance()
    {
        return mMaxDistance;
    }

    public void setMaxDistance(int mMaxDistance)
    {
            this.mMaxDistance = mMaxDistance;

    }

    public void GetFilteredListByUpToDistance(int iDistance) {
        List<StoreListItem> filteredStoreListItems = new ArrayList<>();
        for(StoreListItem item : mFullStoreListItems )
        {
            if(item.getDistance() <= iDistance)
            {
                filteredStoreListItems.add(item);
            }
        }
        mStoreListItems = filteredStoreListItems;
    }
}

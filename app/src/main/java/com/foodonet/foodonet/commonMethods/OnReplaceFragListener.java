package com.foodonet.foodonet.commonMethods;

/**
 * interface used for replacing the fragment in the activity
 */
public interface OnReplaceFragListener {
    /**
     * implement to get the data needed for replacing the fragment in the activity
     * @param openFragType the fragment tag to open, as set in the activity
     * @param id the item ID to get and load in the new fragment, set to CommonConstants.ITEM_ID_EMPTY if not needed
     *           publications - ID is the publication ID
     *           groups - ID is the group ID
     */
    void onReplaceFrags(String openFragType, long id);
}

package com.foodonet.foodonet.commonMethods;

/**
 * interface for handling situations where the FAB should be changed while still remaining in the fragment
 */
public interface OnFabChangeListener {
    /**
     * implement to handle getting the new desired FAB
     * @param fragmentTag the fragment tag from the Activity class that the fab should be
     * @param setVisible true for setting the FAB visible, false for hiding it
     */
    void onFabChange(String fragmentTag, boolean setVisible);
}

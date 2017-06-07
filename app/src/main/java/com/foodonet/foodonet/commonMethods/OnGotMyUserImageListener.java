package com.foodonet.foodonet.commonMethods;

/**
 * interface for updating the user image after the file was created locally
 */
public interface OnGotMyUserImageListener {
    /**
     * new user image available, update the needed view
     */
    void gotMyUserImage();
}

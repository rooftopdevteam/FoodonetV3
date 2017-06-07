package com.foodonet.foodonet.commonMethods;

/**
 * interface used for sending there was a response from the server (or error) to the activity from the fragment,
 * to enable the button (FAB in the activity) after it was disabled in the activity after a click event
 */
public interface OnReceiveResponseListener {
    /**
     * implement to handle the response, should enable the button after receiving
     */
    void onReceiveResponse();
}

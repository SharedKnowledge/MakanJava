package net.sharksystem.makan;

import net.sharksystem.asap.ASAPException;

import java.io.IOException;

public interface MakanStorage {
    String KEY_MAKAN_NAME = "makanName";
    String KEY_ADMIN_ID = "makanAdminID";

    /**
     *
     * @return number of makan within that storage
     * @throws IOException
     * @throws ASAPException
     */
    int size() throws IOException, ASAPException;

    Makan createMakan(CharSequence uri, CharSequence userFriendlyName, CharSequence adminID)
            throws IOException, ASAPException;

    void removeMakan(CharSequence uri) throws IOException, ASAPException;

    Makan getMakan(int position) throws IOException, ASAPException;

    void refresh() throws IOException, ASAPException;
}

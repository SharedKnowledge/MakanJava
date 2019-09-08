package net.sharksystem.makan;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPStorage;

import java.io.IOException;
import java.util.List;

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

    CharSequence getOwner();

    /**
     * Create a closed / administrated makan - admin can change recipient list
     * @param uri
     * @param userFriendlyName
     * @param adminID
     * @return
     * @throws IOException
     * @throws ASAPException
     */
    Makan createMakan(CharSequence uri, CharSequence userFriendlyName, CharSequence adminID)
            throws IOException, ASAPException;

    /**
     * set up open makan - no admin, no control who is sending what
     * @param uri
     * @param userFriendlyName
     * @return
     * @throws IOException
     * @throws ASAPException
     */
    Makan createMakan(CharSequence uri, CharSequence userFriendlyName)
            throws IOException, ASAPException;

    void removeMakan(CharSequence uri) throws IOException, ASAPException;

    void removeAllMakan() throws IOException;

    /**
     * get makan with a given uri
     * @param uri
     * @return
     * @throws IOException io problems when accessing data
     * @throws ASAPException makan does not exist
     */
    Makan getMakan(CharSequence uri) throws IOException, ASAPException;

    void refresh() throws IOException, ASAPException;

    public ASAPStorage getASAPStorage();
}

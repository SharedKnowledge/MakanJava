package net.sharksystem.makan;

import net.sharksystem.asap.ASAPException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface Makan {
    CharSequence MAKAN_FORMAT = "makan";
    CharSequence MAKAN_APP_NAME = "makan";

    /**
     * user friendly name
     * @return
     */
    CharSequence getName() throws IOException;

    /**
     * get uri of this makan
     * @return
     */
    CharSequence getURI() throws IOException;

    /**
     * Return member ids
     */
    List<CharSequence> getMemberIDs() throws IOException;

    void addMember(CharSequence newMemberID)  throws ASAPException, IOException;

    void removeMember(CharSequence newMemberID)  throws ASAPException, IOException;

    /**
     * @return admin of this makan
     */
    CharSequence getAdminID() throws ASAPException, IOException;

    MakanMessage getMessage(int position, boolean chronologically)
            throws ASAPException, IOException;

    void addMessage(CharSequence contentAsCharacter)
            throws ASAPException, IOException;

    int size() throws IOException;
}

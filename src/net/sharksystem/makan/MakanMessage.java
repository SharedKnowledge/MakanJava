package net.sharksystem.makan;

import net.sharksystem.asap.ASAPException;

import java.io.IOException;
import java.util.Date;

public interface MakanMessage {
    /** return sender UUID */
    CharSequence getSenderID() throws ASAPException;

    /** return message content */
    byte[] getContent() throws ASAPException, IOException;

    CharSequence getContentAsString() throws ASAPException, IOException;

    /** get sent date */
    Date getSentDate() throws ASAPException, IOException;
}

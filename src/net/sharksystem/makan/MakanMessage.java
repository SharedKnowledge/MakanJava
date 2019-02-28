package net.sharksystem.makan;

import java.io.IOException;
import java.util.Date;

public interface MakanMessage {
    /** return sender UUID */
    CharSequence getSenderID() throws MakanException;

    /** return message content */
    byte[] getContent() throws MakanException, IOException;

    CharSequence getContentAsString() throws MakanException, IOException;

    /** get sent date */
    Date getSentDate() throws MakanException, IOException;
}

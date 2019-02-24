package net.sharksystem.makan;

import java.util.Date;

public interface MakanMessage {
    /** return sender UUID */
    String getSender() throws MakanException;

    /** return message content */
    byte[] getContent() throws MakanException;

    /** get sent date */
    Date getSentDate() throws MakanException;
}

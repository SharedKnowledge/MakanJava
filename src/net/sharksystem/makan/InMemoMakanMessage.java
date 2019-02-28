package net.sharksystem.makan;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.StringTokenizer;

class InMemoMakanMessage implements MakanMessage {

    private static final String DELIMITER = "||||";
    private CharSequence senderID;
    private CharSequence contentASString;
    private Date sentDate;
    private CharSequence serializedMessage;

    InMemoMakanMessage(CharSequence senderID, CharSequence contentASString, Date sentDate) {
        this.senderID = senderID;
        this.contentASString = contentASString;
        this.sentDate = sentDate;
    }

    InMemoMakanMessage(CharSequence sender, CharSequence aaspMessage) throws MakanException {
        this.deserializeMessage(aaspMessage); // throws exception if malformed

        // not malformed - set rest of it
        this.senderID = sender;
        this.serializedMessage = aaspMessage;
    }

    CharSequence getSerializedMessage() {
        if(this.serializedMessage == null) {
            this.serializedMessage = this.serializeMessage();
        }

        return this.serializedMessage;
    }

    private CharSequence serializeMessage() {
        DateFormat df = DateFormat.getInstance();

        return df.format(this.sentDate) + DELIMITER + this.contentASString;
    }

    private void deserializeMessage(CharSequence message) throws MakanException {
        // parse aaspMessage
        StringTokenizer st = new StringTokenizer(message.toString(), DELIMITER);
        if(!st.hasMoreTokens()) {
            throw new MakanException("malformed Makan Message in AASP message");
        }

        String dateString = st.nextToken();

        if(!st.hasMoreTokens()) {
            throw new MakanException("malformed Makan Message in AASP message");
        }

        this.contentASString = st.nextToken();

        // no set sent date
        DateFormat df = DateFormat.getInstance();

        try {
            this.sentDate = df.parse(dateString);
        } catch (ParseException e) {
            throw new MakanException("malformed date string in makan message: " +
                    e.getLocalizedMessage());
        }
    }

    @Override
    public CharSequence getSenderID() throws MakanException {
        return this.senderID;
    }

    @Override
    public byte[] getContent() throws MakanException {
        throw new MakanException("not implemented yet");
    }

    @Override
    public CharSequence getContentAsString() throws MakanException, IOException {
        return this.contentASString;
    }

    @Override
    public Date getSentDate() throws MakanException {
        return this.getSentDate();
    }
}

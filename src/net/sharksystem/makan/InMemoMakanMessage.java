package net.sharksystem.makan;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.util.Helper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.StringTokenizer;

public class InMemoMakanMessage implements MakanMessage {

    private static final String DELIMITER = "||||";
    private CharSequence senderID;
    private CharSequence contentASString;
    private Date sentDate;
    private CharSequence serializedMessage;

    public InMemoMakanMessage(CharSequence senderID, CharSequence contentASString, Date sentDate) {
        this.senderID = senderID;
        this.contentASString = contentASString;
        this.sentDate = sentDate;
    }

    InMemoMakanMessage(CharSequence asapMessage) throws ASAPException {
        this.deserializeMessage(asapMessage); // throws exception if malformed

        // not malformed - set rest of it
        this.serializedMessage = asapMessage;
    }

    public CharSequence getSerializedMessage() {
        if(this.serializedMessage == null) {
            this.serializedMessage = this.serializeMessage();
        }

        return this.serializedMessage;
    }

    public byte[] getSerializedASAPMessageAsBytes() {
        return Helper.characterSequence2bytes(this.getSerializedMessage());
    }

    private CharSequence serializeMessage() {
        DateFormat df = DateFormat.getInstance();

        return this.senderID + DELIMITER
                + df.format(this.sentDate) + DELIMITER
                + this.contentASString;
    }

    private void deserializeMessage(CharSequence message) throws ASAPException {
        // parse asapMessage
        StringTokenizer st = new StringTokenizer(message.toString(), DELIMITER);
        if(!st.hasMoreTokens()) {
            throw new ASAPException("malformed Makan Message in ASAP message");
        }

        this.senderID = st.nextToken();

        if(!st.hasMoreTokens()) {
            throw new ASAPException("malformed Makan Message in ASAP message");
        }

        String dateString = st.nextToken();

        if(!st.hasMoreTokens()) {
            throw new ASAPException("malformed Makan Message in ASAP message");
        }

        this.contentASString = st.nextToken();

        // no set sent date
        DateFormat df = DateFormat.getInstance();

        try {
            this.sentDate = df.parse(dateString);
        } catch (ParseException e) {
            throw new ASAPException("malformed date string in makan message: " +
                    e.getLocalizedMessage());
        }
    }

    @Override
    public CharSequence getSenderID() {
        return this.senderID;
    }

    @Override
    public byte[] getContent() throws ASAPException {
        throw new ASAPException("not implemented yet");
    }

    @Override
    public CharSequence getContentAsString() throws ASAPException, IOException {
        return this.contentASString;
    }

    @Override
    public Date getSentDate() {
        return this.sentDate;
    }

    @Override
    public boolean isLaterThan(MakanMessage message) throws ASAPException, IOException {
        Date sentDateMessage = message.getSentDate();
        Date sentDateMe = this.getSentDate();

        return sentDateMe.after(sentDateMessage);
    }
}

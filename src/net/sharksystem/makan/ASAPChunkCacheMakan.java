package net.sharksystem.makan;

import net.sharksystem.asap.ASAPChunkCache;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPStorage;

import java.io.IOException;
import java.util.List;

import static net.sharksystem.makan.MakanStorage.KEY_MAKAN_NAME;

class ASAPChunkCacheMakan implements Makan {
    private final ASAPChunkCache asapChunkStorage;
    private final ASAPStorage asapStorage;
    private final CharSequence uri;

    private int lastPosition;
    private boolean lastChronologically;

    /**
     * open existing makan
     * @param aaspChunkCacheLocal
     * @param asapStorage
     * @param uri
     */
    ASAPChunkCacheMakan(ASAPChunkCache aaspChunkCacheLocal,
                        ASAPStorage asapStorage,
                        CharSequence uri) {

        this.asapChunkStorage = aaspChunkCacheLocal;
        this.asapStorage = asapStorage;
        this.uri = uri;
    }

    @Override
    public CharSequence getName() throws IOException {
        return this.asapStorage.getExtra(this.uri, KEY_MAKAN_NAME);
    }

    @Override
    public CharSequence getURI() throws IOException {
        return this.uri;
    }

    @Override
    public List<CharSequence> getMemberIDs() throws IOException {
        return asapStorage.getRecipients(this.uri);
    }

    @Override
    public void addMember(CharSequence newMemberID) throws IOException {
        this.asapStorage.addRecipient(this.uri, newMemberID);
    }

    @Override
    public void removeMember(CharSequence memberID) throws ASAPException, IOException {
        this.asapStorage.removeRecipient(this.uri, memberID);
    }

    @Override
    public CharSequence getAdminID() throws ASAPException, IOException {
        CharSequence adminID = this.asapStorage.getExtra(this.uri, MakanStorage.KEY_ADMIN_ID);
        if(adminID == null) {
            throw new ASAPException("no admin id found");
        }

        return adminID;
    }

    public MakanMessage getMessage(int position, boolean chronologically)
            throws ASAPException, IOException  {

        this.lastPosition = position;
        this.lastChronologically = chronologically;

        CharSequence aaspMessage = this.asapChunkStorage.getMessage(position, chronologically);
        return new InMemoMakanMessage(aaspMessage);
    }

    @Override
    public void addMessage(CharSequence contentAsCharacter) throws ASAPException, IOException {
        this.asapStorage.add(this.uri, contentAsCharacter);
    }

    MakanMessage getCurrentMessage() throws ASAPException, IOException {
        return this.getMessage(this.lastPosition, this.lastChronologically);
    }

    public void increment() {
        this.lastPosition++;
    }

    public void init(int position, boolean chronologically) {
        this.lastPosition = position;
        this.lastChronologically = chronologically;
    }

    public int size() throws IOException {
        return this.asapChunkStorage.size();
    }
}

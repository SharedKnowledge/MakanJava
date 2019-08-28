package net.sharksystem.makan;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPStorage;

import java.io.IOException;
import java.util.List;

public class MakanStorage_Impl implements MakanStorage {
    private final ASAPStorage asapStorage;

    public MakanStorage_Impl(ASAPStorage asapStorage) {
        this.asapStorage = asapStorage;
    }

    @Override
    public int size() throws IOException {
        return this.asapStorage.getChannelURIs().size();
    }

    @Override
    public Makan createMakan(CharSequence uri, CharSequence userFriendlyName, CharSequence adminID)
            throws IOException, ASAPException {

        // already exists?
        try {
            this.getMakan(uri);
            throw new ASAPException("channel already exists: " + uri);
        }
        catch(ASAPException e) {
            // ok - does not exist - go ahead and create
        }

        this.asapStorage.putExtra(uri, KEY_MAKAN_NAME, userFriendlyName.toString());
        this.asapStorage.putExtra(uri, KEY_ADMIN_ID, userFriendlyName.toString());
        return new ASAPChunkCacheMakan(
                this.asapStorage.getChunkCache(uri),
                this.asapStorage,
                uri);
    }

    @Override
    public Makan getMakan(int position) throws IOException, ASAPException {
        return new ASAPChunkCacheMakan(
                this.asapStorage.getChunkCache(position),
                this.asapStorage,
                this.asapStorage.getChannelURIs().get(position)
        );
    }

    @Override
    public Makan getMakan(CharSequence uri) throws IOException, ASAPException {
        return new ASAPChunkCacheMakan(
                this.asapStorage.getChunkCache(uri),
                this.asapStorage,
                uri);
    }

    public void refresh() throws IOException, ASAPException {
        this.asapStorage.refresh();
    }

    public void removeAllMakan() throws IOException {
        List<CharSequence> channelURIs = this.asapStorage.getChannelURIs();

        for(CharSequence uri : channelURIs) {
            this.removeMakan(uri);
        }
    }

    public void removeMakan(int position) throws IOException, ASAPException {
        this.removeMakan(this.getMakan(position).getURI());
    }

    @Override
    public void removeMakan(CharSequence uri) throws IOException {
        this.asapStorage.removeChannel(uri);
    }
}

package net.sharksystem.makan;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPStorage;

import java.io.IOException;

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
            throws IOException {

        this.asapStorage.putExtra(uri, KEY_MAKAN_NAME, userFriendlyName.toString());
        this.asapStorage.putExtra(uri, KEY_ADMIN_ID, userFriendlyName.toString());
        return new ASAPChunkCacheMakan(
                this.asapStorage.getChunkCache(uri),
                this.asapStorage,
                uri);
    }

    @Override
    public void removeMakan(CharSequence uri) throws IOException {
        this.asapStorage.removeChannel(uri);
    }

    @Override
    public Makan getMakan(int position) throws IOException, ASAPException {
        return new ASAPChunkCacheMakan(
                this.asapStorage.getChunkCache(position),
                this.asapStorage,
                this.asapStorage.getChannelURIs().get(position)
        );
    }
}

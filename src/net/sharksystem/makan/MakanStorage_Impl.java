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

    public ASAPStorage getASAPStorage() {
        return this.asapStorage;
    }

    @Override
    public int size() throws IOException {
        return this.asapStorage.getChannelURIs().size();
    }

    @Override
    public CharSequence getOwner() {
        return this.asapStorage.getOwner();
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
        if(adminID != null) {
            // closed makan
            this.asapStorage.putExtra(uri, KEY_ADMIN_ID, userFriendlyName.toString());
        }
        return new MakanASAPChunkChainWrapper(this, uri);
    }

    @Override
    public Makan createMakan(CharSequence uri, CharSequence userFriendlyName) throws IOException, ASAPException {
        return this.createMakan(uri, userFriendlyName, null);
    }

    @Override
    public Makan getMakan(int position) throws IOException, ASAPException {
        try {
            return this.getMakan(this.getASAPStorage().getChannelURIs().get(position));
        }
        catch(IndexOutOfBoundsException e) {
            throw new ASAPException("position points behind avaiable makan uris: " + position);
        }
    }

    @Override
    public Makan getMakan(CharSequence uri) throws IOException, ASAPException {
        MakanASAPChunkChainWrapper makan = new MakanASAPChunkChainWrapper(this, uri);

        if(makan == null) {
            throw new ASAPException("makan does not exist");
        }

        return makan;
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

    @Override
    public void removeMakan(CharSequence uri) throws IOException {
        this.asapStorage.removeChannel(uri);
    }
}

package net.sharksystem.makan;

import net.sharksystem.aasp.AASPChunkCache;
import net.sharksystem.aasp.AASPStorage;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Implements a makan solely based on an AASP Storage.
 * user management is abstract and must be overwritten by derived
 * classes
 */
abstract class MakanAASPWrapper implements Makan {
    private final CharSequence name;
    private final CharSequence uri;
    private final AASPStorage aaspStorage;

    MakanAASPWrapper(CharSequence name, CharSequence uri, AASPStorage aaspStorage) {
        this.name = name;
        this.uri = uri;
        this.aaspStorage = aaspStorage;
    }

    @Override
    public CharSequence getName() throws IOException {
        return this.name;
    }

    @Override
    public CharSequence getURI() throws IOException {
        return this.uri;
    }

    @Override
    public Iterator<CharSequence> getMessages() throws MakanException, IOException {
        // get local chunk storages
        AASPChunkCache aaspChunkCacheLocal = this.aaspStorage.getChunkStorage().getAASPChunkCache(this.uri,
                this.aaspStorage.getOldestEra(), this.aaspStorage.getEra());

        // find storages from remote

        CharSequence sender = null;
        // TODO
        // this.aaspStorage.getSender();

        // iterate sender
        AASPChunkCache aaspChunkSender = this.aaspStorage.getReceivedChunkStorage(sender).getAASPChunkCache(
                this.uri, this.aaspStorage.getOldestEra(), this.aaspStorage.getEra());

        return null;
    }

    @Override
    public CharSequence getMessage(int position, boolean chronologically) throws MakanException, IOException {
        return null;
    }

    @Override
    public void sync() throws IOException {

    }
}

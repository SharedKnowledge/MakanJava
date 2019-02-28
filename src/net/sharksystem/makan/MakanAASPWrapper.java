package net.sharksystem.makan;

import identity.IdentityStorage;
import identity.Person;
import net.sharksystem.aasp.AASPChunk;
import net.sharksystem.aasp.AASPChunkCache;
import net.sharksystem.aasp.AASPChunkStorage;
import net.sharksystem.aasp.AASPStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
    private final IdentityStorage identityStorage;
    private final Person owner;

    MakanAASPWrapper(CharSequence name, CharSequence uri, AASPStorage aaspStorage,
                     Person owner, IdentityStorage identityStorage) throws IOException {
        this.name = name;
        this.uri = uri;
        this.aaspStorage = aaspStorage;
        this.owner = owner;
        this.identityStorage = identityStorage;

        this.initAASPChunkCaches();
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
    public MakanMessage getMessage(int position, boolean chronologically) throws MakanException, IOException {

        return null;
    }

    private List<MakanMessageCache> makanCaches;

    private void initAASPChunkCaches() throws IOException {
        this.makanCaches = new ArrayList<>();

        // get local chunk storages
        AASPChunkCache aaspChunkCacheLocal = this.aaspStorage.getChunkStorage().getAASPChunkCache(this.uri,
                this.aaspStorage.getOldestEra(), this.aaspStorage.getEra());

        this.makanCaches.add(new MakanMessageCache(this.owner.getName(), aaspChunkCacheLocal));

        // find storages from remote
        for(CharSequence sender : this.aaspStorage.getSender()) {
            AASPChunkStorage incomingChunkStorage = this.aaspStorage.getIncomingChunkStorage(sender);
            AASPChunkCache aaspChunkCache = incomingChunkStorage.getAASPChunkCache(
                    this.uri, this.aaspStorage.getOldestEra(), this.aaspStorage.getEra());

            this.makanCaches.add(new MakanMessageCache(sender, aaspChunkCacheLocal));
        }
    }

    public void addMessage(CharSequence contentAsCharacter) throws IOException {
        InMemoMakanMessage newMessage =
                new InMemoMakanMessage(this.owner.getID(), contentAsCharacter, new Date());

        // current er
        AASPChunkStorage chunkStorage = this.aaspStorage.getChunkStorage();
        AASPChunk chunk = chunkStorage.getChunk(this.uri,
                this.aaspStorage.getEra());


        chunk.add(newMessage.getSerializedMessage());

        this.sync();
    }

    @Override
    public void sync() throws IOException {
        this.initAASPChunkCaches();
    }
}

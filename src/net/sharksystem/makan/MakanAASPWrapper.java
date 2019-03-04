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
    private final CharSequence userFriendlyName;
    private final CharSequence uri;
    private final AASPStorage aaspStorage;
    private final IdentityStorage identityStorage;
    private final Person owner;
    private List<MakanAASPChunkCacheDecorator> remoteMakanChunkCacheList = null;
    private MakanAASPChunkCacheDecorator localMakanChunkCache = null;

    private static final int MAX_MAKAN_MESSAGE_CACHE_SIZE = 1000;
    private int makanMaxCacheSize = MAX_MAKAN_MESSAGE_CACHE_SIZE;
    private List<MakanMessage> makanMessageCache = null;
    private int makanMessageCacheIndexOffset = 0;
    private boolean makanMessageCacheChronologically;


    private int remoteMessageNumber = 0;

    MakanAASPWrapper(CharSequence userFriendlyName, CharSequence uri, AASPStorage aaspStorage,
                     Person owner, IdentityStorage identityStorage) throws IOException {
        this.userFriendlyName = userFriendlyName;
        this.uri = uri;
        this.aaspStorage = aaspStorage;
        this.owner = owner;
        this.identityStorage = identityStorage;
    }

    @Override
    public CharSequence getName() throws IOException {
        return this.userFriendlyName;
    }

    @Override
    public CharSequence getURI() throws IOException {
        return this.uri;
    }

    private boolean isInitialized() {
        return (
                this.localMakanChunkCache != null
                && this.remoteMakanChunkCacheList != null);
    }

    @Override
    public MakanMessage getMessage(int position, boolean chronologically)
            throws MakanException, IOException {

        if(this.localMakanChunkCache == null) this.syncLocalMakanCache();
        if(this.remoteMakanChunkCacheList == null) this.syncRemoteMakanCaches();

        if(chronologically != this.makanMessageCacheChronologically) {
            // internal cache is organized in wrong direction, drop it
            this.makanMessageCache = null;
        }

        // remember direction
        this.makanMessageCacheChronologically = chronologically;

        // makan message still not empty?
        if(this.makanMessageCache != null) {
            // message already in cache?
            int effectivePosition = position - this.makanMessageCacheIndexOffset;
            if(effectivePosition >= 0
                    && effectivePosition < this.makanMessageCache.size()) {
                return this.makanMessageCache.get(effectivePosition);
            }
        }

        // clear and reset message cache
        this.makanMessageCache = new ArrayList<>();
        this.makanMessageCacheIndexOffset = 0;

        // initialize aasp cache decorator
        this.localMakanChunkCache.init(position, chronologically);
        for(MakanAASPChunkCacheDecorator r : this.remoteMakanChunkCacheList) {
            r.init(0, chronologically);
        }

        int currentPosition = 0;
        boolean fillingCache = false;

        do {
            int topIndex = -1;
            int currentIndex = -1;
            MakanMessage topMessage = null;
            try {
                topMessage = this.localMakanChunkCache.getCurrentMessage();
            }
            catch(MakanException e) {
                // no message, go ahead
            }

            for(MakanAASPChunkCacheDecorator r : this.remoteMakanChunkCacheList) {
                currentIndex++;
                MakanMessage currentMessage = null;
                try {
                     currentMessage = r.getCurrentMessage();
                }
                catch(MakanException e) {
                    // no message, go ahead
                }

                if(topMessage == null) {
                    topMessage = currentMessage;
                    topIndex = currentIndex;
                } else if(currentMessage != null) {
                    boolean currentOlderThanTop =
                            currentMessage.getSentDate().before(topMessage.getSentDate());

                    if (currentOlderThanTop && chronologically) {
                        // current message is older than top message and we go chronologically - replace
                        topIndex = currentIndex;
                        topMessage = currentMessage;
                    }

                    if (!currentOlderThanTop && !chronologically) {
                        // currentMessage is newer and we go backward in time - replace
                        topIndex = currentIndex;
                        topMessage = currentMessage;
                    }
                }
            }

            if(topMessage == null) {
                // no messages at all, stop all attempts to find one
                break;
            }

            // we have got our top message - remember that
            if(topIndex == -1) {
                this.localMakanChunkCache.increment();
            } else {
                this.remoteMakanChunkCacheList.get(topIndex).increment();
            }

            // filling cache?
            if(fillingCache) {
                this.makanMessageCache.add(topMessage);
            } else {
                if(position - currentIndex < this.makanMaxCacheSize / 2) {
                    fillingCache = true;
                    this.makanMessageCacheIndexOffset = currentIndex;
                    this.makanMessageCache.add(topMessage);
                }
            }
        } while(this.makanMessageCache.size() <= this.makanMaxCacheSize);

        if(position-this.makanMessageCacheIndexOffset > this.makanMessageCache.size()-1) {
            throw new MakanException("index to high");
        }

        return this.makanMessageCache.get(position-this.makanMessageCacheIndexOffset);
    }

    @Override
    public void addMessage(CharSequence contentAsCharacter) throws IOException, MakanException {
        this.addMessage(contentAsCharacter, new Date());
    }

    public void addMessage(CharSequence contentAsCharacter, Date sentDate)
            throws MakanException, IOException {

        InMemoMakanMessage newMessage =
                new InMemoMakanMessage(this.owner.getID(), contentAsCharacter, sentDate);

        // simply add this message to the local chunk storage
        AASPChunkStorage chunkStorage = this.aaspStorage.getChunkStorage();
        AASPChunk chunk = chunkStorage.getChunk(this.uri,
                this.aaspStorage.getEra());


        chunk.add(newMessage.getSerializedMessage());

        // sync local makan wrapper
        this.syncLocalMakanCache();
    }

    private void syncLocalMakanCache() throws IOException {
        // get local chunk storages
        AASPChunkCache aaspChunkCacheLocal =
                this.aaspStorage.getChunkStorage().getAASPChunkCache(
                        this.uri,
                        this.aaspStorage.getEra());

        this.localMakanChunkCache = new MakanAASPChunkCacheDecorator(this.owner.getName(), aaspChunkCacheLocal);


    }

    private void syncRemoteMakanCaches() throws IOException {

        // create remote makan caches
        this.remoteMakanChunkCacheList = new ArrayList<>();

        // reset message counter
        this.remoteMessageNumber = 0;

        // find storages from remote
        for(CharSequence sender : this.aaspStorage.getSender()) {
            AASPChunkStorage incomingChunkStorage = this.aaspStorage.getIncomingChunkStorage(sender);
            AASPChunkCache aaspChunkCache = incomingChunkStorage.getAASPChunkCache(
                    this.uri, this.aaspStorage.getEra());

            this.remoteMessageNumber += aaspChunkCache.size();

            this.remoteMakanChunkCacheList.add(new MakanAASPChunkCacheDecorator(sender, aaspChunkCache));
        }
    }


    @Override
    public void sync() throws IOException {
        this.syncLocalMakanCache();
        this.syncRemoteMakanCaches();
    }

    public int size() throws IOException {
        if(!this.isInitialized()) {
            this.sync();
        }

        return this.localMakanChunkCache.size() + this.remoteMessageNumber;
    }
}

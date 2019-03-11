package net.sharksystem.makan;

import identity.Person;
import net.sharksystem.aasp.AASPChunkCache;
import net.sharksystem.aasp.AASPException;

import java.io.IOException;

class MakanAASPChunkCacheDecorator {
    private final AASPChunkCache aaspChunkStorage;
    private int lastPosition;
    private boolean lastChronologically;

    MakanAASPChunkCacheDecorator(AASPChunkCache aaspChunkCacheLocal) {
        this.aaspChunkStorage = aaspChunkCacheLocal;
    }

    private MakanMessage getMessage(int position, boolean chronologically)
            throws MakanException, IOException  {

        this.lastPosition = position;
        this.lastChronologically = chronologically;

        try {
            CharSequence aaspMessage = this.aaspChunkStorage.getMessage(position, chronologically);
            return new InMemoMakanMessage(aaspMessage);
        }
        catch(AASPException e) {
            throw new MakanException(e);
        }
    }

    MakanMessage getCurrentMessage() throws MakanException, IOException {
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
        return this.aaspChunkStorage.size();
    }
}

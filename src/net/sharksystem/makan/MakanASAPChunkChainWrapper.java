package net.sharksystem.makan;

import net.sharksystem.asap.ASAPChunkStorage;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPStorage;
import net.sharksystem.asap.apps.ASAPMessages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static net.sharksystem.makan.MakanStorage.KEY_MAKAN_NAME;

class MakanASAPChunkChainWrapper implements Makan {
    private List<ASAPMessages> asapMessagesList;
    private final MakanStorage makanStorage;
    private final CharSequence uri;

    private int currentPosition;
    private boolean lastChronologically;

    /**
     * open existing makan
     * @param makanStorage
     * @param uri
     */
    MakanASAPChunkChainWrapper(MakanStorage makanStorage,
                               CharSequence uri) throws IOException, ASAPException {

        this.makanStorage = makanStorage;
        this.uri = uri;

        this.reset();
    }

    @Override
    public CharSequence getName() throws IOException {
        return this.makanStorage.getASAPStorage().getExtra(this.uri, KEY_MAKAN_NAME);
    }

    @Override
    public CharSequence getURI() throws IOException {
        return this.uri;
    }

    @Override
    public Set<CharSequence> getMemberIDs() throws IOException {
        return makanStorage.getASAPStorage().getRecipients(this.uri);
    }

    @Override
    public void addMember(CharSequence newMemberID) throws IOException {
        this.makanStorage.getASAPStorage().addRecipient(this.uri, newMemberID);
    }

    @Override
    public void removeMember(CharSequence memberID) throws ASAPException, IOException {
        this.makanStorage.getASAPStorage().removeRecipient(this.uri, memberID);
    }

    @Override
    public CharSequence getAdminID() throws ASAPException, IOException {
        CharSequence adminID = this.makanStorage.getASAPStorage().getExtra(this.uri, MakanStorage.KEY_ADMIN_ID);
        if(adminID == null) {
            throw new ASAPException("no admin id found");
        }

        return adminID;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                 a makan is made up of several asap caches - one local and one for each sender         //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    void reset() throws IOException, ASAPException {
        this.asapMessagesList = new ArrayList<>();

        if(this.makanStorage.getASAPStorage().channelExists(this.uri)) {
            // get chunks from local user
            this.asapMessagesList.add(this.makanStorage.getASAPStorage().getChannel(this.uri).getMessages());
        }

        // what sender has participated in that discussion
        for(CharSequence sender : this.makanStorage.getASAPStorage().getSender()) {
            ASAPChunkStorage senderStorage = this.makanStorage.getASAPStorage().getIncomingChunkStorage(sender);

            // no: find anything that was ever received
//            if(senderStorage.existsChunk(this.uri, this.makanStorage.getASAPStorage().getEra())) { // TODO: getEra()??
                this.asapMessagesList.add(
                    senderStorage.getASAPChunkCache(this.uri, this.makanStorage.getASAPStorage().getEra())); // TODO: era??
//            }
        }
    }

    private MakanMessage[] makanMessagesCache = null;
    private int[] makanMessagePosition = null;
    private int rightBoarderIndex;

    public MakanMessage getMessage(int position, boolean chronologically)
            throws ASAPException, IOException  {

        int maxIndex = -1;
        for(ASAPMessages asapChain : this.asapMessagesList) {
            if(asapChain.size() > 0)  maxIndex += asapChain.size();
        }

        if(maxIndex < position) throw new ASAPException("position is outside makan size");

            // we have several potential message sources
        this.currentPosition = position;
        this.lastChronologically = chronologically;

        /**
         * Each asap chunk chain has messages. Makan writes those messages in a
         * chronological order. Job here is to merge those different sources
         * together. How to?
         *
         * First: The message at the overall position x could be (in "best" cast)
         * at position x in either chunk chain. That would imply that one chain
         * contains the youngest / oldest messages. This will rarely be the case
         * but it is a starting point.
         */

        // init structures
        this.makanMessagesCache = new MakanMessage[asapMessagesList.size()];
        this.makanMessagePosition = new int[asapMessagesList.size()];


        int index = 0;
        for(ASAPMessages asapChain : this.asapMessagesList) {
            if(asapChain.size() == 0) {
                // chain is empty
                this.makanMessagePosition[index] = -1;
                makanMessagesCache[index] = null;
            } else {
                int actualPosition = position < asapChain.size() ? position : asapChain.size() - 1;
                CharSequence asapMessage = asapChain.getMessage(actualPosition, chronologically);
                // got message at this position - ok
                this.makanMessagePosition[index] = actualPosition;

                // cannot be null - can it?
                if (asapMessage != null) {
                    makanMessagesCache[index] = new InMemoMakanMessage(asapMessage);
                } else {
                    System.err.println(this.getLogStart() + "empty message from asap chunk chain");
                }
            }
            index++;
        }

        /*
        Example A: 3 sources with several message - order: from left to right
        A 1     2     3         4
        B 1 2 3 4 5 6 7 8 9 10 11 12 13 14
        C            1           2    3     4

        Example B: maybe not enough messages - not any slot can be filled
        A 1
        B 1 2 3 4 5
        C              1                   2                3
         */

        /* squeeze right -
        The winner will be the most right element at the right position.
        That initialization is in most cases and chains far more right than required
        Look for the most left of the right. That's the right boundary. Go left with each
        other chain until we have found the element that's right before the leftest right
         */

        // find most left of the rights
        int leftIndex = this.getElementIndexAtBoarder(true, chronologically);
        if(leftIndex >= position) {
            // in that case, we know for sure that even that chain would contain enough elements
            Date boarderDate = this.makanMessagesCache[leftIndex].getSentDate();

            for (int i = 0; i < this.makanMessagesCache.length; i++) {
                if (i != leftIndex) this.squeezeRight(i, boarderDate, chronologically);
            }
        }

        // initialized
        this.calculatePosition(chronologically);
        while(this.currentPosition != position) {
            if (this.currentPosition < position) {
                throw new ASAPException(
                        this.getLogStart() + "current position smaller than position - internal failure in algorithm");
            }

            // replace most right message with it's successor and try again

            /* more: we have a leader on the right flank. We are going to go left with
            that chain. How far, though? If the other chains have in sum more elements as position -
            we could squeeze that queue until it is no more leader on the right flank. - TODO optimization
             */
            ASAPMessages asapMessages = this.asapMessagesList.get(this.rightBoarderIndex);
            int oldIndex = this.makanMessagePosition[this.rightBoarderIndex];
            if(oldIndex == 0) {
                // it's out
                this.makanMessagesCache[this.rightBoarderIndex] = null;
                this.makanMessagePosition[this.rightBoarderIndex] = -1;
            } else {
                this.makanMessagesCache[this.rightBoarderIndex] =
                        new InMemoMakanMessage(asapMessages.getMessage(oldIndex - 1, chronologically));
                this.makanMessagePosition[this.rightBoarderIndex] = oldIndex - 1;
            }

            // try again
            this.calculatePosition(chronologically);
        }

        return this.makanMessagesCache[this.rightBoarderIndex];
    }

    private void squeezeRight(int asapChainIndex, Date boarderDate, boolean chronologically) throws IOException, ASAPException {
        // get chain
        ASAPMessages asapMessages = this.asapMessagesList.get(asapChainIndex);

        // get current index
        int currentIndex = this.makanMessagePosition[asapChainIndex] - 1;
        MakanMessage oldMessage = this.makanMessagesCache[asapChainIndex];

        while(currentIndex > 0) {
            MakanMessage makanMessage = new InMemoMakanMessage(asapMessages.getMessage(currentIndex, chronologically));

            // in any case - take successor
            this.makanMessagesCache[asapChainIndex] = makanMessage;
            this.makanMessagePosition[asapChainIndex] = currentIndex;

            boolean isBefore = makanMessage.getSentDate().before(boarderDate);
            if( (chronologically && isBefore) || (!chronologically && !isBefore) ) {
                // done
                break;
            }
        }
    }

    private String getLogStart() {
        return this.getClass().getSimpleName() + ": ";
    }

    private int getElementIndexAtBoarder(boolean left, boolean chronologically) throws IOException, ASAPException {
        MakanMessage currentBoarderMessage = null;
        int currentBoarderIndex = -1;
        for (int index = 0; index < this.makanMessagesCache.length; index++) {
            MakanMessage message = this.makanMessagesCache[index];
            if (message == null) continue;
            if (currentBoarderMessage == null) {
                currentBoarderMessage = message;
                currentBoarderIndex = index;
            } else {
                boolean switchMessages = false;
                // compare
                boolean isLaterThanBoarder = message.isLaterThan(currentBoarderMessage);
                /* assume chronological order.
                left = true: we look for the oldest element - if that
                message is older as the current boarder message - we have a new candidate
                 */
                if (left && isLaterThanBoarder) switchMessages = true;

                // if not later - we do nothing

                /*
                left = false: we look for the newest message - if that message
                is not! older that our current boarder - we have a new candidate
                 */
                if (!left && !isLaterThanBoarder) switchMessages = true;

                // in all other cases we do nothing

                /*
                and if chronological perspective is changed, we can simply
                switch the whole thing
                 */
                if (chronologically) switchMessages = !switchMessages;

                if (switchMessages) {
                    currentBoarderMessage = message;
                    currentBoarderIndex = index;
                }
            }
        }
        return currentBoarderIndex;
    }

    private void calculatePosition(boolean chronologically) throws IOException, ASAPException {
        this.rightBoarderIndex = this.getElementIndexAtBoarder(false, chronologically);

        /* calculate position:
        add all indexes + 1 (+1 because index 0 is also a place in the queue)
        decrement because the most right index is not part of the queue.
         */
        int position = 0;
        for(int i = 0; i < this.makanMessagePosition.length; i++) {
            if(this.makanMessagePosition[i] < 0) continue; // empty chain
                position += this.makanMessagePosition[i] + 1;
        }

        this.currentPosition = position - 1;
    }

    public void addMessage(CharSequence contentAsCharacter, Date sentDate) throws ASAPException, IOException {
        // produce makan format
        CharSequence sender = this.makanStorage.getOwner();
        InMemoMakanMessage makanMessage = new InMemoMakanMessage(sender, contentAsCharacter, sentDate);

        // save it with asap storage
        this.makanStorage.getASAPStorage().add(this.uri, makanMessage.getSerializedMessage());
    }

    public void addMessage(CharSequence contentAsCharacter) throws ASAPException, IOException {
        this.addMessage(contentAsCharacter, new Date());
    }

    public int size() throws IOException {
        int size = 0;
        for(ASAPMessages asapChain : this.asapMessagesList) {
            if(asapChain.size() > 0)  size += asapChain.size();
        }

        return size;
    }
}

package net.sharksystem.makan;

import net.sharksystem.asap.ASAPEngine;
import net.sharksystem.asap.ASAPEngineFS;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPStorage;
import net.sharksystem.asap.util.ASAPChunkReceiverTester;
import net.sharksystem.asap.util.ASAPEngineThread;
import net.sharksystem.util.localloop.TCPChannel;
import org.junit.Assert;
import org.junit.Test;


import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class SimpleTests {
    public static final String ALICE_BOB_CHAT_URL = "content://aliceAndBob.talk";
    public static final String ALICE_BOB_MAKAN_NAME = "Alice and Bob talk";
    public static final String ALICE_FOLDER = "tests/alice";
    public static final String BOB_FOLDER = "tests/bob";
    public static final String ALICE = "alice";
    public static final String BOB = "bob";
    public static final String ALICE2BOB_MESSAGE = "Hi Bob";
    public static final String BOB2ALICE_MESSAGE = "Hi Alice";

    @Test
    public void scenario1() throws IOException, ASAPException, InterruptedException, MakanException, ParseException {

        ASAPEngineFS.removeFolder(ALICE_FOLDER); // clean previous version before
        ASAPEngineFS.removeFolder(BOB_FOLDER); // clean previous version before

        // alice writes a message into chunkStorage
        ASAPStorage aliceStorage =
                ASAPEngineFS.getASAPStorage(ALICE, ALICE_FOLDER, Makan.MAKAN_FORMAT);


        MakanDummyChunkStorage aliceMakan = new MakanDummyChunkStorage(ALICE_BOB_MAKAN_NAME, ALICE_BOB_CHAT_URL, aliceStorage,
                new DummyPerson(ALICE), new DummyIdentityStorage());

        // write a message into makan
        // fake sent date
        Date aliceSentDate = DateFormat.getInstance().parse("11.09.01 11:45");

        aliceMakan.addMessage(ALICE2BOB_MESSAGE, aliceSentDate);

        // bob does the same
        ASAPStorage bobStorage =
                ASAPEngineFS.getASAPStorage(BOB, BOB_FOLDER, Makan.MAKAN_FORMAT);

        Makan bobMakan = new MakanDummyChunkStorage(ALICE_BOB_MAKAN_NAME, ALICE_BOB_CHAT_URL, bobStorage,
                new DummyPerson(BOB), new DummyIdentityStorage());

        // wait a second - just to ensure another date entry.
        Thread.sleep(1000);
        // write a message into makan
        bobMakan.addMessage(BOB2ALICE_MESSAGE);

        ////////////// perform AASP exchange ///////////////////

        // now set up both engines / use default reader
        ASAPEngine aliceEngine = ASAPEngineFS.getASAPEngine("Alice", ALICE_FOLDER, Makan.MAKAN_FORMAT);

        ASAPEngine bobEngine = ASAPEngineFS.getASAPEngine("Bob", BOB_FOLDER, Makan.MAKAN_FORMAT);

        ASAPChunkReceiverTester aliceListener = new ASAPChunkReceiverTester();
        ASAPChunkReceiverTester bobListener = new ASAPChunkReceiverTester();

        // create connections for both sides
        TCPChannel aliceChannel = new TCPChannel(7777, true, "a2b");
        TCPChannel bobChannel = new TCPChannel(7777, false, "b2a");

        aliceChannel.start();
        bobChannel.start();

        // wait to connect
        aliceChannel.waitForConnection();
        bobChannel.waitForConnection();

        // run engine as thread
        ASAPEngineThread aliceEngineThread = new ASAPEngineThread(aliceEngine,
                aliceChannel.getInputStream(),
                aliceChannel.getOutputStream(),
                aliceListener);

        aliceEngineThread.start();

        // and better debugging - no new thread
        bobEngine.handleConnection(bobChannel.getInputStream(),
                bobChannel.getOutputStream(), bobListener);

        // wait until communication probably ends
        Thread.sleep(5000);

        // close connections: note AASPEngine does NOT close any connection!!
        aliceChannel.close();
        bobChannel.close();
        Thread.sleep(1000);

        // check results

        // listener must have been informed about new messages
        Assert.assertTrue(aliceListener.chunkReceived());
        Assert.assertTrue(bobListener.chunkReceived());

        /////////////// check on makan abstraction layer ///////////////

        // simulate sync
        bobStorage = ASAPEngineFS.getASAPStorage(BOB, BOB_FOLDER, Makan.MAKAN_FORMAT);
        bobMakan = new MakanDummyChunkStorage(
                ALICE_BOB_MAKAN_NAME,
                ALICE_BOB_CHAT_URL,
                bobStorage,
                new DummyPerson(BOB), new DummyIdentityStorage());

        MakanMessage makanMessage = bobMakan.getMessage(0, true);
        Assert.assertEquals(ALICE2BOB_MESSAGE, makanMessage.getContentAsString());

        makanMessage = bobMakan.getMessage(1, true);
        Assert.assertEquals(BOB2ALICE_MESSAGE, makanMessage.getContentAsString());
    }
}

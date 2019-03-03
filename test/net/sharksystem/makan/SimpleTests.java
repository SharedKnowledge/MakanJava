package net.sharksystem.makan;

import identity.IdentityStorage;
import identity.Person;
import net.sharksystem.aasp.AASPEngine;
import net.sharksystem.aasp.AASPEngineFS;
import net.sharksystem.aasp.AASPException;
import net.sharksystem.aasp.AASPStorage;
import net.sharksystem.aasp.util.AASPChunkReceiverTester;
import net.sharksystem.aasp.util.AASPEngineThread;
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
    public static final String ALICE_FOLDER = "alice";
    public static final String BOB_FOLDER = "bob";
    public static final String ALICE = "alice";
    public static final String BOB = "bob";
    public static final String ALICE2BOB_MESSAGE = "Hi Bob";
    public static final String BOB2ALICE_MESSAGE = "Hi Alice";

    @Test
    public void scenario1() throws IOException, AASPException, InterruptedException, MakanException, ParseException {

        AASPEngineFS.removeFolder(ALICE_FOLDER); // clean previous version before
        AASPEngineFS.removeFolder(BOB_FOLDER); // clean previous version before

        // alice writes a message into chunkStorage
        AASPStorage aliceStorage =
                AASPEngineFS.getAASPChunkStorage(ALICE_FOLDER);


        MakanDummy aliceMakan = new MakanDummy(ALICE_BOB_MAKAN_NAME, ALICE_BOB_CHAT_URL, aliceStorage,
                new DummyPerson(ALICE), new DummyIdentityStorage());

        // write a message into makan
        // fake sent date
        Date aliceSentDate = DateFormat.getInstance().parse("11.09.01 11:45");

        aliceMakan.addMessage(ALICE2BOB_MESSAGE, aliceSentDate);

        // bob does the same
        AASPStorage bobStorage =
                AASPEngineFS.getAASPChunkStorage(BOB_FOLDER);

        Makan bobMakan = new MakanDummy(ALICE_BOB_MAKAN_NAME, ALICE_BOB_CHAT_URL, bobStorage,
                new DummyPerson(BOB), new DummyIdentityStorage());

        // wait a second - just to ensure another date entry.
        Thread.sleep(1000);
        // write a message into makan
        bobMakan.addMessage(BOB2ALICE_MESSAGE);

        ////////////// perform AASP exchange ///////////////////

        // now set up both engines / use default reader
        AASPEngine aliceEngine = AASPEngineFS.getAASPEngine("Alice", ALICE_FOLDER);

        AASPEngine bobEngine = AASPEngineFS.getAASPEngine("Bob", BOB_FOLDER);

        AASPChunkReceiverTester aliceListener = new AASPChunkReceiverTester();
        AASPChunkReceiverTester bobListener = new AASPChunkReceiverTester();

        // create connections for both sides
        TCPChannel aliceChannel = new TCPChannel(7777, true, "a2b");
        TCPChannel bobChannel = new TCPChannel(7777, false, "b2a");

        aliceChannel.start();
        bobChannel.start();

        // wait to connect
        aliceChannel.waitForConnection();
        bobChannel.waitForConnection();

        // run engine as thread
        AASPEngineThread aliceEngineThread = new AASPEngineThread(aliceEngine,
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
        bobStorage = AASPEngineFS.getAASPChunkStorage(BOB_FOLDER);
        bobMakan = new MakanDummy(
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

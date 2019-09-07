package net.sharksystem.makan;

import net.sharksystem.asap.*;
import net.sharksystem.asap.util.ASAPChunkReceivedTester;
import net.sharksystem.asap.util.ASAPEngineThread;
import net.sharksystem.util.localloop.TCPChannel;
import org.junit.Assert;
import org.junit.Test;


import java.io.IOException;

import static net.sharksystem.asap.MultiASAPEngineFS.DEFAULT_MAX_PROCESSING_TIME;

public class SimpleTests {
    public static final String AN_OPEN_MAKAN_URI = "content://someOpenTopic";
    public static final String AN_OPEN_MAKAN_TITLE = "an open topic";
    public static final String ALICE_BOB_CHAT_URL = "content://aliceAndBob.talk";
    public static final String ALICE_BOB_MAKAN_NAME = "Alice and Bob talk";
    public static final String ALICE_FOLDER = "tests/alice";
    public static final String BOB_FOLDER = "tests/bob";
    public static final String ALICE = "alice";
    public static final String BOB = "bob";
    public static final String ALICE2BOB_MESSAGE = "Hi Bob";
    public static final String BOB2ALICE_MESSAGE = "Hi Alice";
    private static final CharSequence ALICE_ID = "42";
    private static final CharSequence BOB_ID = "43";

    private static int portnumber = 7777;

    private int getPortNumber() {
        portnumber++;
        return portnumber;
    }

    @Test
    public void create() throws IOException, ASAPException {
        ASAPEngineFS.removeFolder(ALICE_FOLDER); // clean previous version before
        ASAPEngineFS.removeFolder(BOB_FOLDER); // clean previous version before

        ASAPStorage asapStorage = ASAPEngineFS.getASAPStorage(ALICE, ALICE_FOLDER, Makan.MAKAN_FORMAT);

        MakanStorage makanStorage = new MakanStorage_Impl(asapStorage);
        try {
            makanStorage.getMakan(0);
        }
        catch(ASAPException e) {
            // no makan yet
        }

        makanStorage.createMakan(ALICE_BOB_CHAT_URL, ALICE_BOB_MAKAN_NAME, ALICE_ID);

        Assert.assertNotNull(makanStorage.getMakan(0));

        Assert.assertNotNull(makanStorage.getMakan(ALICE_BOB_CHAT_URL));
    }

    @Test
    public void oneWayExchangeOpenMakan() throws IOException, ASAPException, InterruptedException {
        ASAPEngineFS.removeFolder(ALICE_FOLDER); // clean previous version before
        ASAPEngineFS.removeFolder(BOB_FOLDER); // clean previous version before

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        //                                        prepare multi engines                                  //
        ///////////////////////////////////////////////////////////////////////////////////////////////////

        ASAPChunkReceivedTester aliceListener = new ASAPChunkReceivedTester();
        MultiASAPEngineFS aliceEngine = MultiASAPEngineFS_Impl.createMultiEngine(
                ALICE, ALICE_FOLDER, DEFAULT_MAX_PROCESSING_TIME, aliceListener);

        MultiASAPEngineFS bobEngine = MultiASAPEngineFS_Impl.createMultiEngine(
                BOB, BOB_FOLDER, DEFAULT_MAX_PROCESSING_TIME, null);

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        //                                        create some content                                    //
        ///////////////////////////////////////////////////////////////////////////////////////////////////

        ASAPEngine aliceMakanASAPEngine = aliceEngine.getASAPEngine(Makan.MAKAN_APP_NAME, Makan.MAKAN_FORMAT);
        MakanStorage makanAliceStorage = new MakanStorage_Impl(aliceMakanASAPEngine);

        ASAPEngine bobMakanASAPEngine = bobEngine.getASAPEngine(Makan.MAKAN_APP_NAME, Makan.MAKAN_FORMAT);
        ASAPChunkReceivedListener bobListener = new OpenMakanChunkReceivedListener(bobMakanASAPEngine);
        bobEngine.setASAPChunkReceivedListener(Makan.MAKAN_APP_NAME, bobListener);

        MakanStorage makanBobStorage = new MakanStorage_Impl(bobMakanASAPEngine);

        // create open makan
        Makan openAliceMakan = makanAliceStorage.createMakan(AN_OPEN_MAKAN_URI, AN_OPEN_MAKAN_TITLE);

        // put something in
        String aliceOpenMessage = "I'd like to say something about that open topic";
        openAliceMakan.addMessage(aliceOpenMessage);

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        //                                        setup connection                                       //
        ///////////////////////////////////////////////////////////////////////////////////////////////////

        int portNumber = this.getPortNumber();
        // create connections for both sides
        TCPChannel aliceChannel = new TCPChannel(portNumber, true, "a2b");
        TCPChannel bobChannel = new TCPChannel(portNumber, false, "b2a");

        aliceChannel.start();
        bobChannel.start();

        // wait to connect
        aliceChannel.waitForConnection();
        bobChannel.waitForConnection();

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        //                                        run asap connection                                    //
        ///////////////////////////////////////////////////////////////////////////////////////////////////

        // run engine as thread
        ASAPEngineThread aliceEngineThread = new ASAPEngineThread(aliceEngine,
                aliceChannel.getInputStream(), aliceChannel.getOutputStream());

        aliceEngineThread.start();

        // and better debugging - no new thread
        bobEngine.handleConnection(bobChannel.getInputStream(), bobChannel.getOutputStream());

        // wait until communication probably ends
        System.out.flush();
        System.err.flush();
        Thread.sleep(5000);
        System.out.flush();
        System.err.flush();

        // close connections: note ASAPEngine does NOT close any connection!!
        aliceChannel.close();
        bobChannel.close();
        System.out.flush();
        System.err.flush();
        Thread.sleep(1000);
        System.out.flush();
        System.err.flush();

        // check results
        Makan openBobMakan = makanBobStorage.getMakan(AN_OPEN_MAKAN_URI);
        MakanMessage message = openBobMakan.getMessage(0, true);
        message.getContentAsString().toString().equalsIgnoreCase(aliceOpenMessage);
    }
}

package net.sharksystem.makan;

import net.sharksystem.asap.*;
import net.sharksystem.asap.util.ASAPChunkReceivedTester;
import net.sharksystem.asap.util.ASAPEngineThread;
import net.sharksystem.cmdline.TCPChannel;
import org.junit.Assert;
import org.junit.Test;


import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

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
    public static final String CLARA = "clara";
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

        ASAPStorage asapStorage = ASAPEngineFS.getASAPStorage(ALICE, ALICE_FOLDER, Makan.MAKAN_APP_NAME);

        MakanStorage makanStorage = new MakanStorage_Impl(asapStorage);
        try {
            makanStorage.getMakan(ALICE_BOB_CHAT_URL);
        }
        catch(ASAPException e) {
            // no makan yet
        }

        makanStorage.createMakan(ALICE_BOB_CHAT_URL, ALICE_BOB_MAKAN_NAME, ALICE_ID);

        Assert.assertNotNull(makanStorage.getMakan(ALICE_BOB_CHAT_URL));
    }

    @Test
    public void makanChainTest() throws IOException, ASAPException, ParseException {
        ASAPEngineFS.removeFolder(ALICE_FOLDER); // clean previous version before
        ASAPEngineFS.removeFolder(BOB_FOLDER); // clean previous version before

        ASAPEngine asapAliceStorage = ASAPEngineFS.getASAPStorage(ALICE, ALICE_FOLDER, Makan.MAKAN_APP_NAME);
        MakanStorage_Impl aliceMakanStorage = new MakanStorage_Impl(asapAliceStorage);

        // create incoming storages - somewhat a hack
        ASAPEngineFS.getASAPStorage(BOB, ALICE_FOLDER + "/" + BOB, Makan.MAKAN_APP_NAME);
        ASAPEngineFS.getASAPStorage(CLARA, ALICE_FOLDER + "/" + CLARA, Makan.MAKAN_APP_NAME);

        MakanStorage_Impl bobAtAliceMakanStorage =
                new MakanStorage_Impl(asapAliceStorage.getExistingIncomingStorage(BOB));

        MakanStorage_Impl claraAtAliceMakanStorage =
                new MakanStorage_Impl(asapAliceStorage.getExistingIncomingStorage(CLARA));

        MakanASAPChunkChainWrapper aliceMakan =
                (MakanASAPChunkChainWrapper) aliceMakanStorage.getMakan(AN_OPEN_MAKAN_URI);
        MakanASAPChunkChainWrapper bobMakan =
                (MakanASAPChunkChainWrapper) bobAtAliceMakanStorage.getMakan(AN_OPEN_MAKAN_URI);
        MakanASAPChunkChainWrapper claraMakan =
                (MakanASAPChunkChainWrapper) claraAtAliceMakanStorage.getMakan(AN_OPEN_MAKAN_URI);

        String[] message = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

        /*
        * going to produce this
         * A: 0 1 2 3          8        C D
         * B:         4 5        9 A          E
         * C:             6 7         B          F
         */

        DateFormat df = DateFormat.getInstance();
        String dateString = "01.01.01 10:00";
        Date date = df.parse(dateString);

        aliceMakan.addMessage(message[0], date);

        aliceMakan.addMessage(message[1], df.parse("01.01.01 10:01"));
        aliceMakan.addMessage(message[2], df.parse("01.01.01 10:02"));
        aliceMakan.addMessage(message[3], df.parse("01.01.01 10:03"));
        bobMakan.addMessage(message[4], df.parse("01.01.01 10:04"));
        bobMakan.addMessage(message[5], df.parse("01.01.01 10:05"));
        claraMakan.addMessage(message[6], df.parse("01.01.01 10:06"));
        claraMakan.addMessage(message[7], df.parse("01.01.01 10:07"));
        aliceMakan.addMessage(message[8], df.parse("01.01.01 10:08"));
        bobMakan.addMessage(message[9], df.parse("01.01.01 10:09"));
        bobMakan.addMessage(message[0xA], df.parse("01.01.01 10:10"));
        claraMakan.addMessage(message[0xB], df.parse("01.01.01 10:11"));
        aliceMakan.addMessage(message[0xC], df.parse("01.01.01 10:12"));
        aliceMakan.addMessage(message[0xD], df.parse("01.01.01 10:13"));
        bobMakan.addMessage(message[0xE], df.parse("01.01.01 10:14"));
        claraMakan.addMessage(message[0xF], df.parse("01.01.01 10:15"));

        // now get makan (chain)
        Makan aliceM = aliceMakanStorage.getMakan(AN_OPEN_MAKAN_URI);
        MakanMessage m = null;
        String messageString = null;

        // jump in the middle
        m = aliceM.getMessage(7, true);
        Assert.assertNotNull(m);
        messageString = m.getContentAsString().toString();
        Assert.assertNotNull(messageString);
        Assert.assertTrue(messageString.equalsIgnoreCase(message[7]));

        // and now anything - reset makan before
        aliceM = aliceMakanStorage.getMakan(AN_OPEN_MAKAN_URI);
        for(int i = 0; i < 0x10; i++) {
            /*
            System.out.println("index = " + i);
            if(i == 2) {
                int x = 42;
            }
             */
            m = aliceM.getMessage(i, true);
            Assert.assertNotNull(m);
            messageString = m.getContentAsString().toString();
            Assert.assertNotNull(messageString);
            Assert.assertTrue(messageString.equalsIgnoreCase(message[i]));
        }
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

        ASAPEngine aliceMakanASAPEngine = aliceEngine.getASAPEngine(Makan.MAKAN_APP_NAME);
        MakanStorage makanAliceStorage = new MakanStorage_Impl(aliceMakanASAPEngine);

        ASAPEngine bobMakanASAPEngine = bobEngine.getASAPEngine(Makan.MAKAN_APP_NAME);
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

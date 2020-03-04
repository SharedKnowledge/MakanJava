package net.sharksystem.makan;

import net.sharksystem.asap.*;

import java.io.IOException;

public class OpenMakanChunkReceivedListener implements ASAPChunkReceivedListener {

    private final MakanStorage makanStorage;

    public OpenMakanChunkReceivedListener(MakanStorage makanStorage) {
        this.makanStorage = makanStorage;
    }

    public OpenMakanChunkReceivedListener(ASAPStorage asapStorage) {
        this.makanStorage = new MakanStorage_Impl(asapStorage);
    }

    @Override
    public void chunkReceived(String format, String sender, String uri, int era) {
        // check if makan already exists
        try {
            this.makanStorage.getMakan(uri);
            System.out.println(this.getLogStart() + "makan exists: " + uri);
        } catch (IOException e) {
            System.err.println(this.getLogStart() + "read problems:" + e.getLocalizedMessage());
            return;
        } catch (ASAPException e) {
            System.out.println(this.getLogStart() + "makan does not exist: " + uri);
            try {
                this.makanStorage.createMakan(uri, "From: " + sender);
            } catch (IOException | ASAPException ex) {
                System.err.println(this.getLogStart() + "could not create local copy of open makan"
                        + ex.getLocalizedMessage());
                return;
            }
        }
    }

    private String getLogStart() {
        return "OpenMakanReceivedChunkListener: ";
    }
}

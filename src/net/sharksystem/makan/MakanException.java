package net.sharksystem.makan;

public class MakanException extends Exception {
    MakanException() { super();}

    MakanException(String message) { super(message);}

    MakanException(Exception e) { super(e);}
}

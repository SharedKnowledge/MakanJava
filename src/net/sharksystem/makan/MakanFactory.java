package net.sharksystem.makan;

import java.io.IOException;
import java.util.List;

public interface MakanFactory {
    List<Makan> getMakanList();

    Makan getByURI(CharSequence uri) throws MakanException, IOException;

    Makan createNew(CharSequence name, CharSequence uri) throws MakanException, IOException;

    void removeMakan(Makan makan2remove) throws MakanException, IOException;

    Makan getMakan(int position, boolean lastActionFirst) throws MakanException, IOException;
}

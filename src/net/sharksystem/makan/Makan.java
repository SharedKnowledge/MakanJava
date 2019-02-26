package net.sharksystem.makan;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public interface Makan {
    /**
     * Local name of this makan
     * @return
     */
    CharSequence getName() throws IOException;

    /**
     * get uri of this makan
     * @return
     */
    CharSequence getURI() throws IOException;

    /**
     * Return member
     */
    List<Person> getMember() throws IOException;

    void addMember(Person person2add)  throws MakanException, IOException;

    void removeMember(Person person2add)  throws MakanException, IOException;

    /**
     *
     * @return admin of this makan
     */
    Person getAdmin();

    Iterator<CharSequence> getMessages() throws MakanException, IOException;

    CharSequence getMessage(int position, boolean chronologically)
            throws MakanException, IOException;

    /**
     * Sync with external memory. Depending on implementation, this method
     * can even do nothing. After calling, messages and all other setting are
     * in sync with external memory whatsoever.
     *
     * @throws IOException
     */
    void sync() throws IOException;

    // TODO: get image or something
}

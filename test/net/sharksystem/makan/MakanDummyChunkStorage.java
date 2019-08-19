package net.sharksystem.makan;

import identity.IdentityStorage;
import identity.Person;
import net.sharksystem.asap.ASAPStorage;

import java.io.IOException;
import java.util.List;

class MakanDummyChunkStorage extends MakanASAPChunkStorageWrapper {

    MakanDummyChunkStorage(CharSequence userFriendlyName, CharSequence uri, ASAPStorage aaspStorage,
                           Person owner, IdentityStorage identityStorage) throws IOException {

        super(userFriendlyName, uri, aaspStorage, owner, identityStorage);
    }

    @Override
    public List<Person> getMember() throws IOException {
        throw new IOException("that's a dummy- should be implemeneted in Android version");
    }

    @Override
    public void addMember(Person person2add) throws MakanException, IOException {
        throw new IOException("that's a dummy- should be implemeneted in Android version");

    }

    @Override
    public void removeMember(Person person2add) throws MakanException, IOException {
        throw new IOException("that's a dummy- should be implemeneted in Android version");

    }

    @Override
    public Person getAdmin() throws MakanException, IOException {
        throw new IOException("that's a dummy- should be implemeneted in Android version");
    }
}

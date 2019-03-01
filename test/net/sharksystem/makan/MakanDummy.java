package net.sharksystem.makan;

import identity.IdentityStorage;
import identity.Person;
import net.sharksystem.aasp.AASPStorage;

import java.io.IOException;
import java.util.Date;
import java.util.List;

class MakanDummy extends  MakanAASPWrapper {

    MakanDummy(CharSequence userFriendlyName, CharSequence uri, AASPStorage aaspStorage,
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

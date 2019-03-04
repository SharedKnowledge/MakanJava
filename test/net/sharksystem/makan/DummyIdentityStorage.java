package net.sharksystem.makan;

import identity.IdentityStorage;
import identity.Person;

public class DummyIdentityStorage implements IdentityStorage {
    @Override
    public CharSequence getNameByID(CharSequence userID) {
        return userID;
    }

    @Override
    public Person getPersonByID(CharSequence userID) {
        return null;
    }
}

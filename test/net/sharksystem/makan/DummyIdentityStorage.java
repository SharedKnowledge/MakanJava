package net.sharksystem.makan;

import identity.IdentityStorage;

public class DummyIdentityStorage implements IdentityStorage {
    @Override
    public CharSequence getNameByID(CharSequence userID) {
        return userID;
    }
}

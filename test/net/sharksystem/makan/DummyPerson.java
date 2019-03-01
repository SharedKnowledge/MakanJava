package net.sharksystem.makan;

import identity.Person;

public class DummyPerson implements Person {

    private final CharSequence nameAndID;

    public DummyPerson(CharSequence nameAndID) {
        this.nameAndID = nameAndID;
    }

    @Override
    public CharSequence getName() {
        return this.nameAndID;
    }

    @Override
    public CharSequence getID() {
        return this.nameAndID;
    }
}

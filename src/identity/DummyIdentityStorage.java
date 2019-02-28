package identity;

public class DummyIdentityStorage implements IdentityStorage  {
    @Override
    public CharSequence getNameByID(CharSequence userID) {
        return userID;
    }
}

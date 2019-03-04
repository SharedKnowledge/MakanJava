package identity;

public interface IdentityStorage {
    CharSequence getNameByID(CharSequence userID);
    Person getPersonByID(CharSequence userID);
}

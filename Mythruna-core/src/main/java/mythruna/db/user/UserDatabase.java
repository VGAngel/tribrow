package mythruna.db.user;

import mythruna.PlayerData;

import java.util.Set;

public abstract interface UserDatabase {

    public abstract Set<String> getUserIds();

    public abstract PlayerData createUser(String paramString1, String paramString2);

    public abstract PlayerData getUser(String paramString);

    public abstract PlayerData findUser(String paramString, Object paramObject);
}

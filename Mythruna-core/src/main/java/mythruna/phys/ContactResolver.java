package mythruna.phys;

import java.util.List;

public abstract interface ContactResolver {

    public abstract void resolveContacts(List<Contact> paramList, double paramDouble);
}
package de.PARlib;

import java.util.ArrayList;
import de.PARlib.Items.Item;

public class PlayerCharacter  {
    private WorldObjectManager WOManager;
    private InventoryManager invManager;
    
    public PlayerCharacter(WorldObjectManager wom, InventoryManager inv) {
        this.WOManager = wom;
        this.invManager = inv;
    }
    
    public void addItem(Item item) {
        this.invManager.add(item);
    }
    
    public InventoryManager getInventoryManager() {
        return this.invManager;
    }
    
    public ArrayList getInventoryAsArrayList() {
        return invManager.getArrayList();
    }
    
}

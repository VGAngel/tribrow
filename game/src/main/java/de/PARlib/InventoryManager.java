package de.PARlib;

import de.PARlib.Items.Item;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.Iterator;

public class InventoryManager {
    private ArrayList inventoryItems;
    private Node rootNode;
        
    public InventoryManager(ArrayList items){
        this.inventoryItems = items;
    }
    
    public boolean add(Object item) {
        
        inventoryItems.add(item);
        return true;
    }
    
    public ArrayList getArrayList() {
        return this.inventoryItems;
    }
    
     public void logInventory() {
        Iterator<Item> itr = inventoryItems.iterator();
        System.out.println("<--INVENTORY-->");
        while (itr.hasNext()) { // go through inventory
            Item i = itr.next();
            System.out.println(" ITEM: " + i.Description);
        }
    }
    
}

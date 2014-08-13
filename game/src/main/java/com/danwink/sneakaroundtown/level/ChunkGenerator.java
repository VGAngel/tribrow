package com.danwink.sneakaroundtown.level;

import com.danwink.sneakaroundtown.SneakAroundTown;
import com.jme3.scene.Node;

public interface ChunkGenerator {
    public void generateNodes(Level l, int x, int y, SneakAroundTown app);
}

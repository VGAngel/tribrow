package com.danwink.sneakaroundtown.level;

import java.util.ArrayList;

import com.danwink.sneakaroundtown.SneakAroundTown;

public class Chunk {

    ArrayList<String> attributes = new ArrayList<String>();
    ChunkType type;

    public Chunk(ChunkType type) {
        this.type = type;
    }

    enum ChunkType {
        GRASS(new GrassGenerator()),
        ROAD(new RoadGenerator()),
        WALL(new WallGenerator()),
        GATE(new GateGenerator()),
        VILLAGEHOUSE(new VillageHouseGenerator()),
        TOWER1(new TowerGenerator(1)),
        TOWER2(new TowerGenerator(2)),
        CASTLE(new CastleGenerator());

        ChunkGenerator cg;

        ChunkType(ChunkGenerator cg) {
            this.cg = cg;
        }
    }

    public void generateNodes(Level level, int x, int y, SneakAroundTown app) {
        type.cg.generateNodes(level, x, y, app);
    }
}

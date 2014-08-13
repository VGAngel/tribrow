package com.danwink.sneakaroundtown.level;

import com.danwink.sneakaroundtown.SneakAroundTown;
import com.danwink.sneakaroundtown.level.Chunk.ChunkType;

public class Level {

    public static Chunk falseChunk = new Chunk(ChunkType.GRASS);

    public float chunkSize = 10;

    int cw;
    int ch;
    Chunk[][] chunks;

    public Level(int x, int y) {
        cw = x;
        ch = y;
        chunks = new Chunk[x][y];
    }

    public void generateNodes(SneakAroundTown app) {
        for (int y = 0; y < ch; y++) {
            for (int x = 0; x < cw; x++) {
                chunks[x][y].generateNodes(this, x, y, app);
            }
        }
    }

    public Chunk getChunk(int x, int y) {
        if (x < 0 || x >= cw || y < 0 || y >= ch) return falseChunk;
        else return chunks[x][y];
    }
}

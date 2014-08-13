package com.danwink.sneakaroundtown.level;

import com.danwink.sneakaroundtown.level.Chunk.ChunkType;
import com.jme3.math.FastMath;

public class LevelGeneratorV1 {

    public static Level generateLevel() {
        int width = 30;
        int height = 30;

        Level level = new Level(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                level.chunks[x][y] = new Chunk(ChunkType.GRASS);
            }
        }

        int outerWallMinX = FastMath.nextRandomInt(width / 6, width / 3);
        int outerWallMinY = FastMath.nextRandomInt(height / 6, height / 3);

        int outerWallMaxX = FastMath.nextRandomInt(width - width / 6, width - width / 3);
        int outerWallMaxY = FastMath.nextRandomInt(height - height / 6, height - height / 3);

        for (int x = outerWallMinX; x <= outerWallMaxX; x++) {
            level.chunks[x][outerWallMaxY].type = ChunkType.WALL;
            level.chunks[x][outerWallMinY].type = ChunkType.WALL;
        }

        for (int y = outerWallMinY; y <= outerWallMaxY; y++) {
            level.chunks[outerWallMaxX][y].type = ChunkType.WALL;
            level.chunks[outerWallMinX][y].type = ChunkType.WALL;
        }

        int gateLoc = FastMath.nextRandomInt(outerWallMinX + 2, outerWallMaxX - 3);
        for (int x = gateLoc; x <= gateLoc + 1; x++) {
            level.chunks[x][outerWallMinY].type = ChunkType.GATE;
        }

        level.chunks[outerWallMinX][outerWallMinY].type = ChunkType.TOWER1;
        level.chunks[outerWallMinX][outerWallMaxY].type = ChunkType.TOWER1;
        level.chunks[outerWallMaxX][outerWallMaxY].type = ChunkType.TOWER1;
        level.chunks[outerWallMaxX][outerWallMinY].type = ChunkType.TOWER1;

        for (int y = outerWallMinY - 1; y >= 0; y--) {
            level.chunks[gateLoc][y].type = ChunkType.ROAD;
            level.chunks[gateLoc + 1][y].type = ChunkType.ROAD;
        }

        int innerKeepMinX = FastMath.nextRandomInt(outerWallMinX, outerWallMinX + 4);
        int innerKeepMinY = FastMath.nextRandomInt(outerWallMinY + 4, outerWallMinY + 8);

        int innerKeepMaxX = FastMath.nextRandomInt(outerWallMaxX - 4, outerWallMaxX);
        int innerKeepMaxY = FastMath.nextRandomInt(outerWallMaxY - 4, outerWallMaxY);

        for (int y = innerKeepMinY; y <= innerKeepMaxY; y++) {
            for (int x = innerKeepMinX; x <= innerKeepMaxX; x++) {
                level.chunks[x][y].type = ChunkType.CASTLE;
            }
        }

        for (int i = 0; i < 3; i++) {
            buildHouse:
            while (true) {
                int bx = FastMath.nextRandomInt(2, width - 3);
                int by = FastMath.nextRandomInt(2, height - 3);
                if ((bx > outerWallMinX && bx < outerWallMaxX) || (by > outerWallMinY && by < outerWallMaxY)) {
                    continue buildHouse;
                }
                for (int y = by - 1; y <= by + 1; y++) {
                    for (int x = bx - 1; x <= bx + 1; x++) {
                        if (level.chunks[x][y].type != ChunkType.GRASS) {
                            continue buildHouse;
                        }
                    }
                }

                for (int y = by - 1; y <= by + 1; y++) {
                    for (int x = bx - 1; x <= bx + 1; x++) {
                        level.chunks[x][y].type = ChunkType.VILLAGEHOUSE;
                    }
                }
                level.chunks[bx][by + 1].attributes.add("door");
                break;
            }
        }

        return level;
    }
}

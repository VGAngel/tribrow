package com.pro.tile.tileset;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by Valentyn.Polishchuk on 8/15/2014
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "terrain")
public class TerrainType {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String tile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTile() {
        return tile;
    }

    public void setTile(String tile) {
        this.tile = tile;
    }
}

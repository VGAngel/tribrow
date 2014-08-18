package com.pro.tile;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by Valentyn.Polishchuk on 8/15/2014
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tile")
public class Tile {

    @XmlAttribute(required = true)
    private String id;

    @XmlAttribute(required = true)
    private String terrain;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTerrain() {
        return terrain;
    }

    public void setTerrain(String terrain) {
        this.terrain = terrain;
    }
}

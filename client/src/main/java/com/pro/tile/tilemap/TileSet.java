package com.pro.tile.tilemap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tileset")
public class TileSet {

    @XmlAttribute
    protected Integer firstgid;

    @XmlAttribute
    protected String source;

    public Integer getFirstgid() {
        return firstgid;
    }

    public void setFirstgid(Integer firstgid) {
        this.firstgid = firstgid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}

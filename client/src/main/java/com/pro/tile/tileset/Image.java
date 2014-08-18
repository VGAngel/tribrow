package com.pro.tile.tileset;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by Valentyn.Polishchuk on 8/15/2014
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "image")
public class Image {

    @XmlAttribute(required = true)
    private String source;

    @XmlAttribute(required = true)
    private Integer width;

    @XmlAttribute(required = true)
    private Integer height;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}

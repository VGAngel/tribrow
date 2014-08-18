package com.pro.tile.tilemap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.InputStream;

@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"tileset", "layer"})
public class TileMap {

    @XmlAttribute
    private float version;

    @XmlAttribute
    private String orientation;

    @XmlAttribute
    private Integer width;

    @XmlAttribute
    private Integer height;

    @XmlAttribute
    private Integer tilewidth;

    @XmlAttribute
    private Integer tileheight;

    @XmlElement
    private TileSet tileset;

    @XmlElement
    private Layer layer;

    public float getVersion() {
        return version;
    }

    public void setVersion(float version) {
        this.version = version;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
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

    public Integer getTilewidth() {
        return tilewidth;
    }

    public void setTilewidth(Integer tilewidth) {
        this.tilewidth = tilewidth;
    }

    public Integer getTileheight() {
        return tileheight;
    }

    public void setTileheight(Integer tileheight) {
        this.tileheight = tileheight;
    }

    public TileSet getTileset() {
        return tileset;
    }

    public void setTileset(TileSet tileset) {
        this.tileset = tileset;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public static TileMap unmarshall(InputStream configFileInputStream) {
        try {
            JAXBContext context = JAXBContext.newInstance(TileMap.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (TileMap) unmarshaller.unmarshal(configFileInputStream);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }
}

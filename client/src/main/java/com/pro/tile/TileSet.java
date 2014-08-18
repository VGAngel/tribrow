package com.pro.tile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Valentyn.Polishchuk on 8/15/2014
 */
@XmlRootElement(name = "tileset")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"image", "terraintypes"})
public class TileSet {

    @XmlAttribute
    protected String name;

    @XmlAttribute
    protected Integer tilewidth;

    @XmlAttribute
    protected Integer tileheight;

    @XmlAttribute
    protected Integer spacing;

    @XmlAttribute
    protected Integer margin;

    @XmlElement
    private Image image;

    @XmlElementWrapper(name = "terraintypes")
    @XmlElement(name = "terrain")
    private List<TerrainType> terraintypes = new ArrayList<>();

    @XmlElement(name= "tile")
    @XmlList
    private List<Tile> tiles = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getSpacing() {
        return spacing;
    }

    public void setSpacing(Integer spacing) {
        this.spacing = spacing;
    }

    public Integer getMargin() {
        return margin;
    }

    public void setMargin(Integer margin) {
        this.margin = margin;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<TerrainType> getTerraintypes() {
        return terraintypes;
    }

    public void setTerraintypes(List<TerrainType> terraintypes) {
        this.terraintypes = terraintypes;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
    }

    public static TileSet unmarshall(InputStream configFileInputStream) {
        try {
            JAXBContext context = JAXBContext.newInstance(TileSet.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (TileSet) unmarshaller.unmarshal(configFileInputStream);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

}


import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionMap {

	private Map<Point, Tile> tiles;

	private int width, height;
	private int maxX = 0, minX = 0, maxY = 0, minY = 0;

	public Date sessionDate;

	private Logger logger;

	public SessionMap(Logger logger) {
		this.logger = logger;
		this.tiles = new Hashtable<Point, Tile>();
	}

	public boolean tryLoadTilesFrom(File directory) {
		if (directory.listFiles().length == 0) {
			// Sometimes sessions have no maps, we just ignore them.
			return false;
		}

		try {
			sessionDate = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.ENGLISH).parse(directory.getName());
		} catch (ParseException e) {
			sessionDate = new Date(0);
		}

		// We assume that the only files in the session folders are the map
		// images.
		int numGreyscale = 0;
		for (File mapImage : directory.listFiles()) {
			try {
				if (!mapImage.isFile() || !mapImage.getName().matches("^tile_-?\\d+_-?\\d+\\.png$")) {
					logger.log("Encountered invalid file " + mapImage.getAbsolutePath() + " discarding the session...");
					return false;
				}
				Point mapPoint = findPointFromImageName(mapImage.getName());
				if (mapPoint == null) {
					continue;
				}
				String overlay = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(sessionDate);
				Tile tile = new Tile(mapPoint, mapImage, overlay);
				numGreyscale += tile.findNumberOfGreyScalePixels();
				addTile(tile);
			} catch (Exception e) {
				return false;
			}
		}

		if (notContinguous(new Hashtable<Point, Tile>(tiles))) {
			return false;
		}

		numGreyscale = numGreyscale / tiles.size();

		if (numGreyscale > 9000 || numGreyscale < 530) {
			System.out.println("Because of grayscale...");
			return false;
		}

		return tiles.size() > 5;
	}

	private boolean notContinguous(Hashtable<Point, Tile> tilesCopy) {
		Tile startTile = tilesCopy.get(new Point(0, 0));

		if (startTile != null) {
			HashSet<Point> borderPoints = new HashSet<Point>();
			borderPoints.add(startTile.point);
			tilesCopy.remove(startTile.point);

			boolean foundBorderPoints = true;
			while (!tilesCopy.isEmpty() && foundBorderPoints) {
				HashSet<Point> borderPoints2 = new HashSet<Point>();
				foundBorderPoints = false;
				for (Point point : borderPoints) {
					List<Point> surroundingPoints = new ArrayList<Point>();
					surroundingPoints.add(new Point(point.x, point.y - 1));
					surroundingPoints.add(new Point(point.x, point.y + 1));
					surroundingPoints.add(new Point(point.x + 1, point.y));
					surroundingPoints.add(new Point(point.x - 1, point.y));
					for(Point surroundingPoint : surroundingPoints){
						if(tilesCopy.get(surroundingPoint) != null && !borderPoints2.contains(surroundingPoint)){
							tilesCopy.remove(surroundingPoint);
							borderPoints2.add(surroundingPoint);
							foundBorderPoints = true;
						}
					}
				}
				borderPoints = borderPoints2;
			}
			System.out.println("Size: " + tilesCopy.size());
			return !tilesCopy.isEmpty();

		}
			System.out.println("Failed finding 0,0");
		return false;
	}

	public BufferedImage generateStitchedMap() {
		System.out.println("Width = " + width + " Heigth = " + height);
		BufferedImage stitchedSessionMap = new BufferedImage(width, height, 6);

		for (Map.Entry<Point, Tile> entry : tiles.entrySet()) {
			Tile tile = entry.getValue();
			tile.drawTo(stitchedSessionMap, minX, minY);
		}

		return stitchedSessionMap;
	}

	private static Point findPointFromImageName(String name) throws NumberFormatException {
		int x = 0, y = 0;

		Pattern numberRegex = Pattern.compile("-?\\d+");
		Matcher matcher = numberRegex.matcher(name);

		if (matcher.find()) {
			x = Integer.parseInt(matcher.group());
			if (matcher.find()) {
				y = Integer.parseInt(matcher.group());
			} else {
				return null;
			}
		} else {
			return null;
		}

		return new Point(x, y);
	}

	public int size() {
		return tiles.size();
	}

	public boolean tryStitchWith(SessionMap sessionMap) {
		for (Map.Entry<Point, Tile> entry1 : tiles.entrySet()) {
			for (Map.Entry<Point, Tile> entry2 : sessionMap.tiles.entrySet()) {
				Tile tile1 = entry1.getValue();
				Tile tile2 = entry2.getValue();

				if (tile1.equals(tile2) && matchAtLeastOneSurroundingTile(sessionMap, tile1, tile2)) {
					stitchWith(sessionMap, tile1, tile2);
					return true;
				}
			}
		}
		return false;
	}

	private boolean matchAtLeastOneSurroundingTile(SessionMap sessionMap, Tile tile1, Tile tile2) {
		List<Point> surroundingPoints = new ArrayList<Point>();
		surroundingPoints.add(new Point(0, -1));
		surroundingPoints.add(new Point(0, 1));
		surroundingPoints.add(new Point(1, 0));
		surroundingPoints.add(new Point(-1, 0));

		int numMatching = 0;
		for (Point point : surroundingPoints) {
			Tile a = sessionMap.tiles.get(new Point(tile2.point.x + point.x, tile2.point.y + point.y));
			Tile b = tiles.get(new Point(tile1.point.x + point.x, tile1.point.y + point.y));
			if (a != null && b != null && a.equals(b)) {
				numMatching++;
			}
		}
		return numMatching > 1;
	}

	private void stitchWith(SessionMap sessionMap, Tile map1, Tile map2) {
		int dX = map1.point.x - map2.point.x;
		int dY = map1.point.y - map2.point.y;
		
		String debug = "2012-04-23 22:02:49";
		if(map1.overlay.equals(debug) || map2.overlay.equals(debug)){
			logger.log(debug + " -- " + map1.overlay + " -- " + map2.overlay + " = " + dY);
		}

		for (Map.Entry<Point, Tile> entry : sessionMap.tiles.entrySet()) {
			Tile tile = entry.getValue();

			Point transformedPoint = new Point(tile.point.x + dX, tile.point.y + dY);
			tile.changePoint(transformedPoint);

			if (!tiles.containsKey(transformedPoint)) {
				addTile(tile);
			} else if (sessionMap.sessionDate.after(sessionDate)) {
				tiles.remove(transformedPoint);
				addTile(tile);
			}
		}

	}

	private void addTile(Tile tile) {
		Point point = tile.point;

		if (point.x > maxX) {
			maxX = point.x;
		} else if (point.x < minX) {
			minX = point.x;
		}
		if (point.y < minY) {
			minY = point.y;
		} else if (point.y > maxY) {
			maxY = point.y;
		}

		// System.out.println+ ("maxX = " maxX + "minXminX = " + );
		width = (maxX - minX + 1) * 100;
		height = (maxY - minY + 1) * 100;

		tiles.put(point, tile);
	}

}

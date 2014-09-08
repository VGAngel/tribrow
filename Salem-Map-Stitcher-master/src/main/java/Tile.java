import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.imageio.ImageIO;

class Tile {
	private File imageFile;
	public Point point;
	private int numBlackPoints;
	private HashSet<Point> blackPoints;
	private boolean matchingTile;
	public String overlay;

	private static final int SIZE = 100;

	public Tile(Point mapPoint, File imageFile, String overlay) {
		this.imageFile = imageFile;
		this.point = mapPoint;
		this.overlay = overlay;
		blackPoints = null;
		blackPoints = findAllBlackPoints();
		numBlackPoints = blackPoints.size();
		matchingTile = false;
	}

	public void changePoint(Point transformedPoint) {
		point = transformedPoint;
	}

	public void drawTo(BufferedImage stitchedSessionMap, int minX, int minY) {
		try {
			BufferedImage image = ImageIO.read(imageFile);
			if (matchingTile) {
				int rgbColour = 0xFF000000;
				for (int x = 0; x < SIZE; x++) {
					image.setRGB(x, 0, rgbColour);
					image.setRGB(x, 99, rgbColour);
					image.setRGB(0, x, rgbColour);
					image.setRGB(99, x, rgbColour);
				}
			}
			Graphics2D gfx = stitchedSessionMap.createGraphics();
			int x = (point.x - minX) * SIZE;
			int y = (point.y - minY) * SIZE;
			gfx.drawImage(image, null, x, y);
			gfx.setColor(Color.black);
			gfx.setFont(new Font("SansSerif", Font.PLAIN, 9));
			gfx.drawString(overlay, x+10, y+10);
		} catch (IOException e) {

		}
	}

	public int findNumberOfGreyScalePixels() throws IOException {
		BufferedImage image = ImageIO.read(imageFile);
		int greyscale = 0;
		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				int rgb = image.getRGB(x, y);
				int r = (0x00ff0000 & rgb) >> 16;
				int g = (0x0000ff00 & rgb) >> 8;
				int b = (0x000000ff & rgb);

				if (r == g && g == b)
					greyscale++;
			}
		}
		return greyscale;
	}

	private HashSet<Point> findAllBlackPoints() {
		if (blackPoints != null)
			return blackPoints;

		blackPoints = new HashSet<Point>();
		try {
			BufferedImage image = ImageIO.read(imageFile);
			for (int x = 5; x < SIZE - 5; x++) {
				for (int y = 5; y < SIZE - 5; y++) {
					int rgb = image.getRGB(x, y);

					if ((rgb & 0x00FFFFFF) == 0) {
						blackPoints.add(new Point(x, y));
					}
				}
			}
		} catch (IOException e) {
			return new HashSet<Point>();
		}
		return blackPoints;
	}

	private boolean isValid() {
		return numBlackPoints > 50;
	}

	public boolean equals(Tile tile) {
		float chance = Math.abs((float) numBlackPoints / (float) tile.numBlackPoints - 1);
		// System.out.println(numBlackPoints + " vs " + tile.numBlackPoints +
		// " = " + chance + "%");
		if (isValid() && tile.isValid() && chance < 0.05) {
			if (matchesMost(findAllBlackPoints(), tile.findAllBlackPoints())) {
				// System.out.println(tile.point.x + "," + tile.point.y + " == "
				// + point.x + "," + point.y);
				matchingTile = true;
				return true;
			} else {
				// System.out.println(tile.point.x + "," + tile.point.y + " != "
				// + point.x + "," + point.y);
				return false;
			}

		} else {
			return false;
		}
	}

	private boolean matchesMost(HashSet<Point> set1, HashSet<Point> set2) {
		int numContained = 0;
		for (Point point : set1) {
			if (set2.contains(point)) {
				numContained++;
			}
		}

		float chance = Math.abs((float) numContained / (float) (set1.size()) - 1);
		// System.out.println("Contained chance = " + chance);
		return chance < 0.25;
	}
}

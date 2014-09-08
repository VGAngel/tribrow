import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class FileManager {

	private static final String HOME_DIRECTORY = System.getProperty("user.home");
	private static final String DEFAULT_MAPS_DIRECTORY = HOME_DIRECTORY + "\\Salem\\map";

	private Logger logger;
	private File mapDirectory;
	private boolean mapDirectoryFound;

	public FileManager(Logger logger) {
		this.logger = logger;
		mapDirectoryFound = false;
	}

	public String tryFindMapsDirectory() {
		try {
			Path mapDirectoryPath = Paths.get(DEFAULT_MAPS_DIRECTORY);
			mapDirectoryPath.toRealPath();

			setMapDirectory(mapDirectoryPath.toFile());

			logger.log("Automatically found Salems map directory.");
			return mapDirectoryPath.toString();
		} catch (Exception e) {
			logger.log("Failed to automatically find Salems map directory.");
			return "Select your Salem map directory.";
		}
	}

	public void setMapDirectory(File selectedFile) {
		mapDirectoryFound = true;
		mapDirectory = selectedFile;
	}

	public boolean hasFoundMapDirectory() {
		return mapDirectoryFound ? mapDirectory.exists() : false;
	}
	 
	public File getMapDirectory(){
		return mapDirectory;
	}

	public static File[] getSessionDirectories(File directory) {
		return directory.listFiles(new MapFolderFilter());
	}

	public static boolean tryWriteImage(File directory, BufferedImage image, String name) {
		String imageName = directory.getAbsolutePath() + "\\" + name + ".png";
		System.out.println(imageName);
		try {
			ImageIO.write(image, "png", new File(imageName));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
			return false;
		}
	}

}

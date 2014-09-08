import java.io.File;
import java.io.FilenameFilter;


public class MapFolderFilter implements FilenameFilter {

	public boolean accept(File file, String filename) {
		// Basic regex to ensure we are only matching Salem session folders.
		// Matches in the form "DDDD-DD-DD DD.DD.DD" where D is a digit.
		return filename.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}.\\d{2}.\\d{2}");
	}

}

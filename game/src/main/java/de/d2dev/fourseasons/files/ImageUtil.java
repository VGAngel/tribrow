package de.d2dev.fourseasons.files;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileOutputStream;

public class ImageUtil {

	/**
	 * Helper to write a {@code RenderedImage} as a JPG to a {@link de.schlichtherle.truezip.file.TFile}.
	 * @param img
	 * @param file
	 * @throws IOException
	 */
	public static void writeJpg(RenderedImage img, TFile file) throws IOException {
		TFileOutputStream out = new TFileOutputStream( file );
		
		try {
			ImageIO.write( img, "jpg", out );
		} finally {
		    out.close(); // ALWAYS close the stream!
		}
	}
	
	/**
	 * Helper to write a {@code RenderedImage} as a JPG to a {@code File}.
	 * @param img
	 * @param file
	 * @throws IOException
	 */
	public static void writeJpg(RenderedImage img, File file) throws IOException {
		FileOutputStream out = new FileOutputStream( file );
		
		try {
			ImageIO.write( img, "jpg", out );
		} finally {
		    out.close(); // ALWAYS close the stream!
		}
	}
}

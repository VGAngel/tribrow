package de.d2dev.fourseasons.files.tests;

import java.io.IOException;

import nu.xom.ParsingException;

import de.d2dev.fourseasons.*;
import de.d2dev.fourseasons.files.AbstractContainerFile;
import de.d2dev.fourseasons.files.MagicStringException;

public class ContainerFile2 extends AbstractContainerFile {
	
	public static final String MAGIC_STRING = "CONTAINER_FILE_2";
	
	public static ContainerFile2 createTestContainerFile(String path, VersionNumber version) throws IOException, MagicStringException, ParsingException {
		AbstractContainerFile.createEmptyContainer(path, MAGIC_STRING, version );
		return new ContainerFile2(path);
	}

	public ContainerFile2(String path) throws MagicStringException,
			IOException, ParsingException {
		super(path);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getMagicString() {
		return MAGIC_STRING;
	}
}

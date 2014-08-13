package de.d2dev.fourseasons.files.tests;

import java.io.IOException;

import nu.xom.ParsingException;

import de.d2dev.fourseasons.*;
import de.d2dev.fourseasons.files.AbstractContainerFile;
import de.d2dev.fourseasons.files.MagicStringException;

public class ContainerFile1 extends AbstractContainerFile {
	
	public static final String MAGIC_STRING = "CONTAINER_FILE_1";
	
	public static ContainerFile1 createTestContainerFile(String path, VersionNumber version) throws IOException, MagicStringException, ParsingException {
		AbstractContainerFile.createEmptyContainer(path, MAGIC_STRING, version );
		return new ContainerFile1(path);
	}

	public ContainerFile1(String path) throws MagicStringException,
			IOException, ParsingException {
		super(path);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getMagicString() {
		return MAGIC_STRING;
	}
}

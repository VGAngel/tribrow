package de.d2dev.fourseasons.files.tests;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import de.d2dev.fourseasons.VersionNumber;

public class AbstractContainerFileTest {
	
	static VersionNumber version = new VersionNumber( 23, 4324);
	
	@BeforeClass
	public static void createOne() throws Exception {
		ContainerFile1.createTestContainerFile( "C:/Users/Batti/Desktop/tests/testContainer1.zip", version );
	}
	
	@Test
	public void test() throws Exception {
		ContainerFile1 file = ContainerFile1.createTestContainerFile( "C:/Users/Batti/Desktop/tests/testContainer.zip", version );
		
		assertEquals( version, file.getVersion() );
		assertEquals( ContainerFile1.MAGIC_STRING, file.getMagicString() );	// yes...
	}
	
	@Test(expected= de.d2dev.fourseasons.files.MagicStringException.class)
	public void testLoadOtherException() throws Exception {
		new ContainerFile2( "C:/Users/Batti/Desktop/tests/testContainer1.zip" );
	}
}

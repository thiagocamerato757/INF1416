import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for XMLManager class.
 * <p>
 * Tests cover XML document creation, entry management, collision detection,
 * and file I/O operations.
 */
public class XMLManagerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File xmlFile;
    private XMLManager xmlManager;

    @Before
    public void setUp() throws Exception {
        xmlFile = tempFolder.newFile("catalog.xml");
        xmlManager = new XMLManager(xmlFile.getAbsolutePath());
    }

    /**
     * Test that a new XML document is created with CATALOG root element.
     */
    @Test
    public void testCreateNewXMLDocument() {
        assertNotNull("XML document should be created", xmlManager);
        xmlManager.save();
        assertTrue("XML file should exist", xmlFile.exists());
    }

    /**
     * Test adding a new entry to the XML catalog.
     */
    @Test
    public void testAddNewEntry() {
        xmlManager.addNewEntry("file1.txt", "SHA256", "abc123def456");
        String result = xmlManager.getDigestHex("file1.txt", "SHA256");
        assertEquals("Should retrieve the added digest", "abc123def456", result);
    }

    /**
     * Test adding multiple digest types for the same file.
     */
    @Test
    public void testAddMultipleDigestTypes() {
        xmlManager.addNewEntry("file1.txt", "SHA256", "hash256");
        xmlManager.addNewEntry("file1.txt", "MD5", "hashmd5");
        xmlManager.addNewEntry("file1.txt", "SHA1", "hash1");

        assertEquals("SHA256 digest should be retrieved", "hash256", xmlManager.getDigestHex("file1.txt", "SHA256"));
        assertEquals("MD5 digest should be retrieved", "hashmd5", xmlManager.getDigestHex("file1.txt", "MD5"));
        assertEquals("SHA1 digest should be retrieved", "hash1", xmlManager.getDigestHex("file1.txt", "SHA1"));
    }

    /**
     * Test retrieving non-existent entry returns null.
     */
    @Test
    public void testGetNonExistentDigest() {
        String result = xmlManager.getDigestHex("nonexistent.txt", "SHA256");
        assertNull("Non-existent digest should return null", result);
    }

    /**
     * Test retrieving non-existent digest type for existing file returns null.
     */
    @Test
    public void testGetNonExistentDigestType() {
        xmlManager.addNewEntry("file1.txt", "SHA256", "hash256");
        String result = xmlManager.getDigestHex("file1.txt", "MD5");
        assertNull("Non-existent digest type should return null", result);
    }

    /**
     * Test collision detection for duplicate hashes in current folder.
     */
    @Test
    public void testCheckCollisionInFolder() {
        Map<String, Integer> digestCounts = new HashMap<>();
        digestCounts.put("samehash", 2);

        assertTrue("Should detect collision with count > 1",
            xmlManager.checkCollision("file1.txt", "SHA256", "samehash", digestCounts));
    }

    /**
     * Test no collision when digest count is 1.
     */
    @Test
    public void testNoCollisionWithSingleCount() {
        Map<String, Integer> digestCounts = new HashMap<>();
        digestCounts.put("hash123", 1);

        assertFalse("Should not detect collision with count = 1",
            xmlManager.checkCollision("file1.txt", "SHA256", "hash123", digestCounts));
    }

    /**
     * Test collision detection in XML catalog.
     */
    @Test
    public void testCheckCollisionInXMLCatalog() {
        xmlManager.addNewEntry("file1.txt", "SHA256", "samehash");
        xmlManager.addNewEntry("file2.txt", "SHA256", "samehash");

        Map<String, Integer> digestCounts = new HashMap<>();
        digestCounts.put("samehash", 1);

        assertTrue("Should detect collision in XML catalog",
            xmlManager.checkCollision("file1.txt", "SHA256", "samehash", digestCounts));
    }

    /**
     * Test no collision when same file has different digest type.
     */
    @Test
    public void testNoCollisionDifferentDigestType() {
        xmlManager.addNewEntry("file1.txt", "SHA256", "samehash");

        Map<String, Integer> digestCounts = new HashMap<>();

        assertFalse("Should not detect collision with different digest type",
            xmlManager.checkCollision("file1.txt", "MD5", "samehash", digestCounts));
    }

    /**
     * Test no collision when same file is being checked.
     */
    @Test
    public void testNoCollisionForSameFile() {
        xmlManager.addNewEntry("file1.txt", "SHA256", "hash123");

        Map<String, Integer> digestCounts = new HashMap<>();

        assertFalse("Should not detect collision for same file",
            xmlManager.checkCollision("file1.txt", "SHA256", "hash123", digestCounts));
    }

    /**
     * Test saving and loading XML document persistence.
     */
    @Test
    public void testSaveAndLoadXML() {
        xmlManager.addNewEntry("file1.txt", "SHA256", "hash123");
        xmlManager.save();

        // Create new XMLManager to load from file
        XMLManager xmlManager2 = new XMLManager(xmlFile.getAbsolutePath());
        String result = xmlManager2.getDigestHex("file1.txt", "SHA256");

        assertEquals("Saved digest should be retrievable after loading", "hash123", result);
    }

    /**
     * Test adding multiple files to catalog.
     */
    @Test
    public void testAddMultipleFiles() {
        xmlManager.addNewEntry("file1.txt", "SHA256", "hash1");
        xmlManager.addNewEntry("file2.txt", "SHA256", "hash2");
        xmlManager.addNewEntry("file3.txt", "SHA256", "hash3");

        assertEquals("File 1 digest", "hash1", xmlManager.getDigestHex("file1.txt", "SHA256"));
        assertEquals("File 2 digest", "hash2", xmlManager.getDigestHex("file2.txt", "SHA256"));
        assertEquals("File 3 digest", "hash3", xmlManager.getDigestHex("file3.txt", "SHA256"));
    }

    /**
     * Test null digestCounts parameter in collision check.
     */
    @Test
    public void testCheckCollisionWithNullDigestCounts() {
        xmlManager.addNewEntry("file1.txt", "SHA256", "samehash");
        xmlManager.addNewEntry("file2.txt", "SHA256", "samehash");

        assertTrue("Should detect collision even with null digestCounts",
            xmlManager.checkCollision("file1.txt", "SHA256", "samehash", null));
    }

    /**
     * Test adding entries with special characters in filenames.
     */
    @Test
    public void testAddEntryWithSpecialCharacters() {
        String filename = "arquivo-teste_2024.txt";
        xmlManager.addNewEntry(filename, "SHA256", "hash123");
        String result = xmlManager.getDigestHex(filename, "SHA256");

        assertEquals("Should handle special characters in filenames", "hash123", result);
    }
}


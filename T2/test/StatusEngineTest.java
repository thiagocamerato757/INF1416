import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for StatusEngine class.
 * <p>
 * Tests cover status determination based on file digests, catalog entries,
 * and collision detection.
 */
public class StatusEngineTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private XMLManager xmlManager;
    private Map<String, Integer> digestCounts;

    @Before
    public void setUp() throws Exception {
        File xmlFile = tempFolder.newFile("catalog.xml");
        xmlManager = new XMLManager(xmlFile.getAbsolutePath());
        digestCounts = new HashMap<>();
    }

    /**
     * Test NOT_FOUND status when file is not in catalog.
     */
    @Test
    public void testStatusNotFound() {
        Status status = StatusEngine.determine("unknown.txt", "SHA256", "hash123", xmlManager, digestCounts);
        assertEquals("Unknown file should have NOT_FOUND status", Status.NOT_FOUND, status);
    }

    /**
     * Test OK status when file exists in catalog with matching hash.
     */
    @Test
    public void testStatusOK() {
        xmlManager.addNewEntry("file.txt", "SHA256", "correcthash");

        Status status = StatusEngine.determine("file.txt", "SHA256", "correcthash", xmlManager, digestCounts);
        assertEquals("Matching hash should have OK status", Status.OK, status);
    }

    /**
     * Test NOT_OK status when file exists in catalog with different hash.
     */
    @Test
    public void testStatusNotOK() {
        xmlManager.addNewEntry("file.txt", "SHA256", "oldhash");

        Status status = StatusEngine.determine("file.txt", "SHA256", "newhash", xmlManager, digestCounts);
        assertEquals("Different hash should have NOT_OK status", Status.NOT_OK, status);
    }

    /**
     * Test COLLISION status when multiple files have same digest in folder.
     */
    @Test
    public void testStatusCollisionInFolder() {
        digestCounts.put("collision_hash", 2);

        Status status = StatusEngine.determine("file1.txt", "SHA256", "collision_hash", xmlManager, digestCounts);
        assertEquals("Duplicate hash in folder should have COLLISION status", Status.COLLISION, status);
    }

    /**
     * Test COLLISION status when another file in catalog has same digest.
     */
    @Test
    public void testStatusCollisionInCatalog() {
        xmlManager.addNewEntry("file1.txt", "SHA256", "samehash");
        xmlManager.addNewEntry("file2.txt", "SHA256", "samehash");

        Status status = StatusEngine.determine("file1.txt", "SHA256", "samehash", xmlManager, digestCounts);
        assertEquals("Duplicate hash in catalog should have COLLISION status", Status.COLLISION, status);
    }

    /**
     * Test COLLISION takes precedence over OK.
     */
    @Test
    public void testCollisionPrecedenceOverOK() {
        xmlManager.addNewEntry("file.txt", "SHA256", "samehash");
        xmlManager.addNewEntry("other.txt", "SHA256", "samehash");

        Status status = StatusEngine.determine("file.txt", "SHA256", "samehash", xmlManager, digestCounts);
        assertEquals("COLLISION should take precedence over OK", Status.COLLISION, status);
    }

    /**
     * Test status with different digest types for same file.
     */
    @Test
    public void testStatusWithMultipleDigestTypes() {
        xmlManager.addNewEntry("file.txt", "SHA256", "hash256");
        xmlManager.addNewEntry("file.txt", "MD5", "hashmd5");

        Status statusSHA256 = StatusEngine.determine("file.txt", "SHA256", "hash256", xmlManager, digestCounts);
        Status statusMD5 = StatusEngine.determine("file.txt", "MD5", "hashmd5", xmlManager, digestCounts);

        assertEquals("SHA256 should match", Status.OK, statusSHA256);
        assertEquals("MD5 should match", Status.OK, statusMD5);
    }

    /**
     * Test NOT_FOUND when file not in catalog but digest is in folder counts.
     */
    @Test
    public void testNotFoundWithoutCollision() {
        digestCounts.put("somehash", 1);

        Status status = StatusEngine.determine("newfile.txt", "SHA256", "somehash", xmlManager, digestCounts);
        assertEquals("New file without collision should be NOT_FOUND", Status.NOT_FOUND, status);
    }

    /**
     * Test status with empty digestCounts.
     */
    @Test
    public void testStatusWithEmptyDigestCounts() {
        xmlManager.addNewEntry("file.txt", "SHA256", "hash123");

        Status status = StatusEngine.determine("file.txt", "SHA256", "hash123", xmlManager, new HashMap<>());
        assertEquals("Should still find OK status with empty digestCounts", Status.OK, status);
    }

    /**
     * Test status determination with null digestCounts parameter.
     */
    @Test
    public void testStatusWithNullDigestCounts() {
        xmlManager.addNewEntry("file.txt", "SHA256", "hash123");

        Status status = StatusEngine.determine("file.txt", "SHA256", "hash123", xmlManager, null);
        assertEquals("Should handle null digestCounts", Status.OK, status);
    }

    /**
     * Test COLLISION detection for three identical files in folder.
     */
    @Test
    public void testCollisionWithMoreThanTwoFiles() {
        digestCounts.put("hash", 3);

        Status status = StatusEngine.determine("file1.txt", "SHA256", "hash", xmlManager, digestCounts);
        assertEquals("Should detect collision with 3+ identical files", Status.COLLISION, status);
    }

    /**
     * Test NOT_OK when catalog has different digest type.
     */
    @Test
    public void testNotOKDifferentDigestType() {
        xmlManager.addNewEntry("file.txt", "MD5", "md5hash");

        Status status = StatusEngine.determine("file.txt", "SHA256", "sha256hash", xmlManager, digestCounts);
        assertEquals("Different digest type not in catalog should be NOT_FOUND", Status.NOT_FOUND, status);
    }

    /**
     * Test status with special characters in filename.
     */
    @Test
    public void testStatusWithSpecialCharactersInFilename() {
        String filename = "arquivo-teste_2024.txt";
        xmlManager.addNewEntry(filename, "SHA256", "hash123");

        Status status = StatusEngine.determine(filename, "SHA256", "hash123", xmlManager, digestCounts);
        assertEquals("Should handle special characters in filenames", Status.OK, status);
    }
}


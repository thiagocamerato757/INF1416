import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;

import static org.junit.Assert.*;

/**
 * Unit tests for DigestCalculator methods.
 * <p>
 * Tests cover digest calculation, algorithm normalization, and error handling.
 */
public class DigestCalculatorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File testFile;

    @Before
    public void setUp() throws Exception {
        // Create a temporary test file with known content
        testFile = tempFolder.newFile("test.txt");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("PUC-RIO");
        }
    }

    /**
     * Test SHA1 digest calculation for a file with known content.
     */
    @Test
    public void testCalculateDigestSHA1() throws Exception {
        String result = DigestCalculator.calculateDigest(testFile, "SHA1");
        assertNotNull("SHA1 digest should not be null", result);
        assertEquals("SHA1 digest length should be 40 hex chars", 40, result.length());
        assertEquals("SHA1 digest should be lowercase", result, result.toLowerCase());
    }

    /**
     * Test SHA256 digest calculation.
     */
    @Test
    public void testCalculateDigestSHA256() throws Exception {
        String result = DigestCalculator.calculateDigest(testFile, "SHA256");
        assertNotNull("SHA256 digest should not be null", result);
        assertEquals("SHA256 digest length should be 64 hex chars", 64, result.length());
        assertEquals("SHA256 digest should be lowercase", result, result.toLowerCase());
    }

    /**
     * Test SHA512 digest calculation.
     */
    @Test
    public void testCalculateDigestSHA512() throws Exception {
        String result = DigestCalculator.calculateDigest(testFile, "SHA512");
        assertNotNull("SHA512 digest should not be null", result);
        assertEquals("SHA512 digest length should be 128 hex chars", 128, result.length());
        assertEquals("SHA512 digest should be lowercase", result, result.toLowerCase());
    }

    /**
     * Test MD5 digest calculation.
     */
    @Test
    public void testCalculateDigestMD5() throws Exception {
        String result = DigestCalculator.calculateDigest(testFile, "MD5");
        assertNotNull("MD5 digest should not be null", result);
        assertEquals("MD5 digest length should be 32 hex chars", 32, result.length());
        assertEquals("MD5 digest should be lowercase", result, result.toLowerCase());
    }

    /**
     * Test that digest is lowercase hexadecimal format.
     */
    @Test
    public void testDigestHexFormat() throws Exception {
        String result = DigestCalculator.calculateDigest(testFile, "SHA256");
        assertTrue("Digest should match hex pattern", result.matches("[0-9a-f]+"));
    }

    /**
     * Test that normalizeAlgorithm converts CLI names to JCA names.
     */
    @Test
    public void testNormalizeAlgorithmMD5() throws Exception {
        assertEquals("MD5 normalization", "MD5", DigestCalculator.normalizeAlgorithm("MD5"));
    }

    /**
     * Test SHA1 normalization.
     */
    @Test
    public void testNormalizeAlgorithmSHA1() throws Exception {
        assertEquals("SHA1 normalization", "SHA-1", DigestCalculator.normalizeAlgorithm("SHA1"));
    }

    /**
     * Test SHA256 normalization.
     */
    @Test
    public void testNormalizeAlgorithmSHA256() throws Exception {
        assertEquals("SHA256 normalization", "SHA-256", DigestCalculator.normalizeAlgorithm("SHA256"));
    }

    /**
     * Test SHA512 normalization.
     */
    @Test
    public void testNormalizeAlgorithmSHA512() throws Exception {
        assertEquals("SHA512 normalization", "SHA-512", DigestCalculator.normalizeAlgorithm("SHA512"));
    }

    /**
     * Test that invalid algorithm throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeAlgorithmInvalid() throws Exception {
        DigestCalculator.normalizeAlgorithm("INVALID");
    }

    /**
     * Test that same file produces same digest (consistency).
     */
    @Test
    public void testDigestConsistency() throws Exception {
        String digest1 = DigestCalculator.calculateDigest(testFile, "SHA256");
        String digest2 = DigestCalculator.calculateDigest(testFile, "SHA256");
        assertEquals("Same file should produce same digest", digest1, digest2);
    }

    /**
     * Test that different files produce different digests.
     */
    @Test
    public void testDifferentFilesProduceDifferentDigests() throws Exception {
        File file1 = tempFolder.newFile("file1.txt");
        File file2 = tempFolder.newFile("file2.txt");

        try (FileWriter w1 = new FileWriter(file1)) {
            w1.write("Content A");
        }
        try (FileWriter w2 = new FileWriter(file2)) {
            w2.write("Content B");
        }

        String digest1 = DigestCalculator.calculateDigest(file1, "SHA256");
        String digest2 = DigestCalculator.calculateDigest(file2, "SHA256");

        assertNotEquals("Different files should produce different digests", digest1, digest2);
    }
}

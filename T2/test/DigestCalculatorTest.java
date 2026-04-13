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

    /**
     * Test digest calculation for empty file.
     */
    @Test
    public void testCalculateDigestEmptyFile() throws Exception {
        File emptyFile = tempFolder.newFile("empty.txt");
        
        String result = DigestCalculator.calculateDigest(emptyFile, "SHA256");
        assertNotNull("Empty file should produce a digest", result);
        assertEquals("SHA256 digest length should be 64 hex chars", 64, result.length());
        assertEquals("Empty SHA256 should have known value", 
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", result);
    }

    /**
     * Test digest calculation for large file.
     */
    @Test
    public void testCalculateDigestLargeFile() throws Exception {
        File largeFile = tempFolder.newFile("large.txt");
        
        try (FileWriter writer = new FileWriter(largeFile)) {
            // Write 1MB of data
            String chunk = "0123456789";
            for (int i = 0; i < 102400; i++) {
                writer.write(chunk);
            }
        }
        
        String result = DigestCalculator.calculateDigest(largeFile, "SHA256");
        assertNotNull("Large file should produce a digest", result);
        assertEquals("SHA256 digest length should be 64 hex chars", 64, result.length());
    }

    /**
     * Test digest calculation with whitespace and special characters.
     */
    @Test
    public void testCalculateDigestSpecialCharacters() throws Exception {
        File specialFile = tempFolder.newFile("special.txt");
        
        try (FileWriter writer = new FileWriter(specialFile)) {
            writer.write("Special chars: !@#$%^&*() \t\n");
        }
        
        String result = DigestCalculator.calculateDigest(specialFile, "SHA256");
        assertNotNull("File with special chars should produce a digest", result);
        assertEquals("SHA256 digest length should be 64 hex chars", 64, result.length());
    }

    /**
     * Test that normalized algorithm is case-insensitive.
     */
    @Test
    public void testNormalizeAlgorithmCaseInsensitive() throws Exception {
        assertEquals("Should normalize lowercase sha256", "SHA-256", DigestCalculator.normalizeAlgorithm("sha256"));
        assertEquals("Should normalize mixed case SHA256", "SHA-256", DigestCalculator.normalizeAlgorithm("Sha256"));
        assertEquals("Should normalize with whitespace", "SHA-1", DigestCalculator.normalizeAlgorithm("  SHA1  "));
    }

    /**
     * Test MD5 digest produces lowercase output.
     */
    @Test
    public void testMD5DigestLowercase() throws Exception {
        String result = DigestCalculator.calculateDigest(testFile, "MD5");
        assertTrue("MD5 should contain only lowercase hex", result.matches("[0-9a-f]{32}"));
        assertFalse("MD5 should not contain uppercase", result.contains(result.toUpperCase().replaceAll("[0-9]", "")));
    }

    /**
     * Test that different algorithms produce different digest lengths.
     */
    @Test
    public void testDifferentAlgorithmLengths() throws Exception {
        String md5 = DigestCalculator.calculateDigest(testFile, "MD5");
        String sha1 = DigestCalculator.calculateDigest(testFile, "SHA1");
        String sha256 = DigestCalculator.calculateDigest(testFile, "SHA256");
        String sha512 = DigestCalculator.calculateDigest(testFile, "SHA512");

        assertEquals("MD5 should be 32 hex chars", 32, md5.length());
        assertEquals("SHA1 should be 40 hex chars", 40, sha1.length());
        assertEquals("SHA256 should be 64 hex chars", 64, sha256.length());
        assertEquals("SHA512 should be 128 hex chars", 128, sha512.length());
    }

    /**
     * Test that all algorithms produce valid hex strings.
     */
    @Test
    public void testAllAlgorithmsProduceValidHex() throws Exception {
        String[] algorithms = {"MD5", "SHA1", "SHA256", "SHA512"};
        
        for (String algo : algorithms) {
            String result = DigestCalculator.calculateDigest(testFile, algo);
            assertTrue("Algorithm " + algo + " should produce lowercase hex", 
                result.matches("[0-9a-f]+"));
        }
    }

    /**
     * Test digest calculation for file with UTF-8 content.
     */
    @Test
    public void testCalculateDigestUTF8Content() throws Exception {
        File utf8File = tempFolder.newFile("utf8.txt");
        
        try (FileWriter writer = new FileWriter(utf8File)) {
            writer.write("Arquivo de teste com acentuação: ãç é");
        }
        
        String result = DigestCalculator.calculateDigest(utf8File, "SHA256");
        assertNotNull("UTF-8 file should produce a digest", result);
        assertEquals("SHA256 digest length should be 64 hex chars", 64, result.length());
    }

    /**
     * Test that invalid algorithm throws exception with helpful message.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCalculateDigestInvalidAlgorithm() throws Exception {
        DigestCalculator.calculateDigest(testFile, "INVALID_ALGO");
    }

    /**
     * Test normalizeAlgorithm with various invalid inputs.
     */
    @Test
    public void testNormalizeAlgorithmVariousInvalid() {
        String[] invalidAlgos = {"INVALID", "SM3", "BLAKE2", "RSA", "DSA"};
        
        for (String algo : invalidAlgos) {
            try {
                DigestCalculator.normalizeAlgorithm(algo);
                fail("Should throw IllegalArgumentException for: " + algo);
            } catch (IllegalArgumentException e) {
                assertTrue("Error message should mention unsupported", e.getMessage().contains("não suportado"));
            }
        }
    }

    /**
     * Test digest of file with newline characters.
     */
    @Test
    public void testCalculateDigestWithNewlines() throws Exception {
        File newlineFile = tempFolder.newFile("newlines.txt");
        
        try (FileWriter writer = new FileWriter(newlineFile)) {
            writer.write("Line 1\nLine 2\nLine 3");
        }
        
        String result = DigestCalculator.calculateDigest(newlineFile, "SHA256");
        assertNotNull("File with newlines should produce a digest", result);
        assertEquals("SHA256 digest length should be 64 hex chars", 64, result.length());
    }
}

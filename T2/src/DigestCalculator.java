import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * PUC-Rio – INF1416 – Segurança da Informação
 * 2212580 - Thiago Pereira Camerato
 * 2212763 - Arthur Augusto Claro Sardella
 */
public class DigestCalculator {

    /**
     * Entry point for the digest calculator CLI.
     *
     * @param args command-line arguments: digest type, folder path, and XML catalog path
     */
    public static void main(String[] args) {

        // Validate CLI arguments
        if (args.length < 3) {
            System.out.println("Erro: Argumentos insuficientes.");
            System.out.println("Uso: java DigestCalculator <Tipo_Digest> <Caminho_Pasta_Arquivos> <Caminho_ArqListaDigest>");
            System.out.println("Algoritmos aceitos: MD5, SHA1, SHA256, SHA512");
            System.exit(1);
        }

        String digestType = args[0];
        String folderPath = args[1];
        String xmlPath = args[2];

        if (!digestType.matches("MD5|SHA1|SHA256|SHA512")) {
            System.err.println("Erro: Algoritmo não suportado. Use MD5, SHA1, SHA256 ou SHA512.");
            System.exit(1);
        }

        try {
            // Load XML catalog (or create if missing)
            XMLManager xmlManager = new XMLManager(xmlPath);

            // Validate input folder
            File folder = new File(folderPath);
            if (!folder.exists() || !folder.isDirectory()) {
                System.err.println("Erro: Caminho da pasta inválido ou inexistente.");
                System.exit(1);
            }
            File[] filesToProcess = folder.listFiles();

            if (filesToProcess == null) {
                System.err.println("Erro: Pasta de arquivos não encontrada ou inacessível.");
                System.exit(1);
            }

            // List input files
            ShowFileList(filesToProcess);

            // Precompute digests and counts
            Map<String, String> folderDigests = new HashMap<>();
            Map<String, Integer> digestCounts = new HashMap<>();

            for (File file : filesToProcess) {
                if (file.isDirectory()) continue; // skip subfolders
                String calculatedHash = calculateDigest(file, digestType);
                folderDigests.put(file.getName(), calculatedHash);
                digestCounts.put(calculatedHash, digestCounts.getOrDefault(calculatedHash, 0) + 1);
            }

            // Process each file
            for (File file : filesToProcess) {
                if (file.isDirectory()) continue; // skip subfolders

                String calculatedHash = folderDigests.get(file.getName());
                System.out.println("Arquivo: " + file.getName() + ", " + digestType + ": " + calculatedHash);

                // Determine status
                Status status = StatusEngine.determine(
                        file.getName(),
                        digestType,
                        calculatedHash,
                        xmlManager,
                        digestCounts
                );

                // Print result
                System.out.println(file.getName() + " " + digestType + " " + calculatedHash + " (" + status + ")");

                // Add missing entries
                if (status == Status.NOT_FOUND) { xmlManager.addNewEntry(file.getName(), digestType, calculatedHash); }
            }

            // Save updated XML
            xmlManager.save();

        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Prints the names of regular files that will be processed.
     *
     * @param files files listed from the input directory
     */
    static void ShowFileList(File[] files) {
        for (File file : files)
            if (!file.isDirectory())
                System.out.println(file.getName());
    }

    /**
     * Normalizes CLI digest names to the standard JCA algorithm names.
     * Example mappings: SHA1 -> SHA-1, SHA256 -> SHA-256, SHA512 -> SHA-512.
     *
     * @param digestType digest type from CLI arguments
     * @return normalized JCA algorithm name
     * @throws IllegalArgumentException if the provided digest type is not supported
     */
    static String normalizeAlgorithm(String digestType) {
        // Normalize to uppercase and trim whitespace for consistent comparison
        digestType = digestType.toUpperCase().trim();
        switch (digestType) {
            case "MD5": return "MD5";
            case "SHA1": return "SHA-1";
            case "SHA256": return "SHA-256";
            case "SHA512": return "SHA-512";
            default:
                throw new IllegalArgumentException("Algoritmo não suportado: " + digestType);
        }
    }

    /**
     * Computes the digest of a file based on its content, reading it in 8KB chunks.
     * <p>
     * The resulting hash is returned as a lowercase hexadecimal string.
     *
     * @param file file whose content will be hashed
     * @param digestType digest type provided by the CLI (MD5, SHA1, SHA256, SHA512)
     * @return file digest as a lowercase hexadecimal string
     * @throws NoSuchAlgorithmException if the normalized algorithm is not available in JCA
     * @throws IOException if an I/O error occurs while reading the file
     */
    static String calculateDigest(File file, String digestType)
            throws NoSuchAlgorithmException, IOException {
        String jcaAlg = normalizeAlgorithm(digestType);
        MessageDigest md = MessageDigest.getInstance(jcaAlg);

        try (FileInputStream in = new FileInputStream(file)) {
            // 8kb buffer
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
        }

        byte[] digest = md.digest();
        // byte -> hex = double characters = double space
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            hex.append(String.format("%02x", b & 0xff));
        }
        return hex.toString();
    }

}

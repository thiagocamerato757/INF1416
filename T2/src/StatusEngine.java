import java.util.Map;

/*
 * PUC-Rio – INF1416 – Segurança da Informação
 * 2212580 - Thiago Pereira Camerato
 * 2212763 - Arthur Augusto Claro Sardella
 */

public class StatusEngine {

    /**
     * Determines the status of a file digest according to collisions and XML catalog data.
     *
     * @param name file name (without path)
     * @param digestType digest algorithm type
     * @param calculatedHash digest value computed from file contents
     * @param xmlManager XML catalog manager
     * @param digestCounts map of digest hex values to their occurrence counts in the folder
     * @return status according to the assignment rules
     */
    static Status determine(
            String name,
            String digestType,
            String calculatedHash,
            XMLManager xmlManager,
            Map<String, Integer> digestCounts
    ) {
        String knownHash = xmlManager.getDigestHex(name, digestType);
        if (xmlManager.checkCollision(name, digestType, calculatedHash, digestCounts)) return Status.COLLISION;
        if (knownHash == null) return Status.NOT_FOUND;
        if (knownHash.equals(calculatedHash)) return Status.OK;
        return Status.NOT_OK;
    }
}

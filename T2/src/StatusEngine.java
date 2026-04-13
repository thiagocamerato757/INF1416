public class StatusEngine {

    static Status determine(String name, String digestType, String calculatedHash, XMLManager xmlManager) {
        String knownHash = xmlManager.getDigestHex(name, digestType);
        if (xmlManager.checkCollision(name, digestType, calculatedHash)) return Status.COLLISION;
        if (knownHash == null) return Status.NOT_FOUND;
        if (knownHash.equals(calculatedHash)) return Status.OK;
        return Status.NOT_OK;
    }
}

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/*
 * PUC-Rio – INF1416 – Segurança da Informação
 * 2212580 - Thiago Pereira Camerato
 * 2212763 - Arthur Augusto Claro Sardella
 */

/**
 * Handles loading and querying the digest catalog XML document.
 */
public class XMLManager {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder;
    Document xmlDoc;
    File XMLfile = null;

    /**
     * Builds an XML manager from a catalog path.
     * <p>
     * If the file does not exist, a new in-memory document with a {@code CATALOG}
     * root element is created.
     *
     * @param xmlPath path to the digest catalog XML file
     */
    public XMLManager(String xmlPath) {
        XMLfile = new File(xmlPath);
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            if (XMLfile.createNewFile()) {
                xmlDoc = docBuilder.newDocument();
                Element catalog = xmlDoc.createElement("CATALOG");
                xmlDoc.appendChild(catalog);
            } else {
                xmlDoc = docBuilder.parse(XMLfile);
            }
        } catch (Exception e) {
            System.err.println("Erro ao abrir o arquivo: " + e.getMessage());
        }
    }

    /**
     * Adds a digest entry for a file in the XML catalog.
     * <p>
     * If the file entry does not exist, it is created. The digest entry is then
     * appended with the provided type and hash.
     *
     * @param name file name (without path)
     * @param DigestType digest algorithm type
     * @param DigestHex digest value in hexadecimal format
     */
    public void addNewEntry(String name, String DigestType, String DigestHex) {
        Element root = xmlDoc.getDocumentElement();
        Element file_entry = findFileEntry(name);
        if (file_entry == null) {
            file_entry = xmlDoc.createElement("FILE_ENTRY");
            root.appendChild(file_entry);

            Element file_name = xmlDoc.createElement("FILE_NAME");
            file_name.setTextContent(name);
            file_entry.appendChild(file_name);
        }

        Element digest_entry = xmlDoc.createElement("DIGEST_ENTRY");
        file_entry.appendChild(digest_entry);

        Element digest_type = xmlDoc.createElement("DIGEST_TYPE");
        digest_type.setTextContent(DigestType);
        digest_entry.appendChild(digest_type);

        Element digest_hex = xmlDoc.createElement("DIGEST_HEX");
        digest_hex.setTextContent(DigestHex);
        digest_entry.appendChild(digest_hex);
    }

    /**
     * Returns the digest hex value for a given file and digest type.
     * <p>
     * Returns {@code null} when the file is not present in the catalog or when
     * the requested digest type is not registered for that file.
     *
     * @param name file name (without path)
     * @param DigestType digest algorithm type
     * @return digest hex value, or {@code null} when not found
     */
    public String getDigestHex(String name, String DigestType) {
        Element file_entry = findFileEntry(name);
        if (file_entry == null) return null;

        NodeList digests = file_entry.getElementsByTagName("DIGEST_ENTRY");
        String currentHex = null;
        for (int i = 0; i < digests.getLength(); i++) {
            Node digest_entry_node = digests.item(i);
            NodeList digest_entry_children = digest_entry_node.getChildNodes();
            Node digest_type = digest_entry_children.item(0);
            Node digest_hex = digest_entry_children.item(1);

            if (digest_type.getTextContent().equals(DigestType)) {
                currentHex = digest_hex.getTextContent();
                break;
            }
        }

        return currentHex;
    }

    /**
     * Checks whether another file has the same digest type and digest value.
     *
     * @param name file name being evaluated (excluded from comparison)
     * @param DigestType digest algorithm type
     * @param DigestHex digest value in hexadecimal format
     * @return {@code true} if a collision is found; otherwise {@code false}
     */
    public boolean checkCollision(String name, String DigestType, String DigestHex) {
        Element root = xmlDoc.getDocumentElement();
        NodeList file_entries = root.getElementsByTagName("FILE_ENTRY");
        for (int i = 0; i < file_entries.getLength(); i++) {
            Node file_entry_node = file_entries.item(i);
            Node file_name_node = file_entry_node.getChildNodes().item(0);
            if (file_name_node.getTextContent().equals(name)) continue;

            NodeList digest_entries = ((Element) file_entry_node).getElementsByTagName("DIGEST_ENTRY");
            for (int j = 0; j < digest_entries.getLength(); j++) {
                Node digest_entry_node = digest_entries.item(j);
                Node digest_type = digest_entry_node.getChildNodes().item(0);
                if (digest_type.getTextContent().equals(DigestType)) {
                    Node digest_hex = digest_entry_node.getChildNodes().item(1);
                    if (digest_hex.getTextContent().equals(DigestHex)) return true;
                }
            }
        }

        return false;
    }

    /**
     * Saves the XML as a file
     */
    public void save() {
        try {
            Transformer transformer = TransformerFactory
                    .newInstance().newTransformer();
            transformer.transform(
                    new DOMSource(xmlDoc),
                    new StreamResult(XMLfile)
            );
        } catch (Exception e) {
            System.err.println("Error saving XML: " + e.getMessage());
        }
    }

    /**
     * Finds the file entry node that matches the provided file name.
     *
     * @param name file name (without path)
     * @return matching {@code FILE_ENTRY} element, or {@code null} if absent
     */
    private Element findFileEntry(String name) {
        Element root = xmlDoc.getDocumentElement();
        NodeList nodelist = root.getChildNodes();
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node file_entry_node = nodelist.item(i);
            Node file_name_node = file_entry_node.getChildNodes().item(0);
            String file_name = file_name_node.getTextContent();
            if (file_name.equals(name)) {
                return (Element) file_entry_node;
            }
        }

        return null;
    }

    /**
     * Finds the digest entry node of a specific type for a given file.
     *
     * @param name file name (without path)
     * @param DigestType digest algorithm type
     * @return matching digest hex element, or {@code null} if absent
     */
    private Element findDigestEntryOfType(String name, String DigestType) {
        Element file_entry = findFileEntry(name);
        if (file_entry == null) return null;

        NodeList digests = file_entry.getElementsByTagName("DIGEST_ENTRY");
        for (int i = 0; i < digests.getLength(); i++) {
            Node digest_entry_node = digests.item(i);
            NodeList digest_entry_children = digest_entry_node.getChildNodes();
            Node digest_type = digest_entry_children.item(0);
            Node digest_hex = digest_entry_children.item(1);
            if (digest_type.getTextContent().equals(DigestType)) {
                return (Element) digest_hex;
            }
        }

        return null;
    }
}

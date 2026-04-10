import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Map;

/*
 * PUC-Rio – INF1416 – Segurança da Informação
 * 2212580 - Thiago Pereira Camerato
 * 2212763 - Arthur Augusto Claro Sardella
 */

/**
 * Classe que gerencia o arquivo XML
 */
public class XMLManager {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder;
    Document xmlDoc;
    File XMLfile = null;

    public XMLManager(String caminhoXML) {
        XMLfile = new File(caminhoXML);
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

    public void addFileEntry(String name, String DigestType, String DigestHex) {
        Element root = xmlDoc.getDocumentElement();
        Element file_entry = xmlDoc.createElement("FILE_ENTRY");
        root.appendChild(file_entry);

        Element file_name = xmlDoc.createElement("FILE_NAME");
        file_name.setTextContent(name);
        file_entry.appendChild(file_name);

        Element digest_entry = xmlDoc.createElement("DIGEST_ENTRY");
        file_entry.appendChild(digest_entry);

        Element digest_type = xmlDoc.createElement("DIGEST_TYPE");
        digest_type.setTextContent(DigestType);
        digest_entry.appendChild(digest_type);

        Element digest_hex = xmlDoc.createElement("DIGEST_HEX");
        digest_hex.setTextContent(DigestHex);
        digest_entry.appendChild(digest_hex);
    }

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

}

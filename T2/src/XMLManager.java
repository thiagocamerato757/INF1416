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

    /**
     * Adiciona novo Digest Entry caso o arquivo exista na base, mas não possui aquele tipo de digest registrado.
     * <p></p>
     * Cria um novo File Entry caso o arquivo não exista na base e registra o digest.
     *
     * @param name Nome do arquivo
     * @param DigestType Tipo do digest
     * @param DigestHex Hash Hex do digest
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
     * Procura na base de dados o DigestHex do tipo especificado do arquivo.
     * <p></p>
     * Returna nulo caso:
     * <ol>
     * <li>O arquivo não exista</li>
     * <li>Não existe registro do hex tipo especificado para o arquivo</li>
     * </ol>
     *
     * @param name Nome do arquivo
     * @param DigestType Tipo do digest
     * @return O Hex ou NULL
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
     * Checa se existe um Hex do mesmo tipo e igual ao digest de qualquer outro arquivo.
     *
     * @param name Nome do arquivo
     * @param DigestType Tipo do digest
     * @param DigestHex Hash Hex do digest
     * @return Se há colisão
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

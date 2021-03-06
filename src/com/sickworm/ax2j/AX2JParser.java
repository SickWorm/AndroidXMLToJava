package com.sickworm.ax2j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Single Android XML file parse, export the root of AXMLNode.
 * @author sickworm
 *
 */
public class AX2JParser {
    private String path;
    private String content;
    private Element rootElement;

    public AX2JParser(File file) {
        this.path = file.getPath();
    }

    public AX2JParser(String content) {
        this.content = content;
    }

    public AX2JParser(Element rootElement) {
        this.rootElement = rootElement;
    }

    /**
     * Parse Android XML file info AXMLNode structure.
     * @return the root of AXMLNode
     */
    public AX2JNode parse() {
        AX2JNode rootNode = null;
        try {
            if (path != null) {
                Document document;
                document = new SAXReader().read(path).getDocument();
                rootElement = document.getRootElement();
            } else if (content != null) {
                Document document;
                document = new SAXReader().read(new ByteArrayInputStream(content.getBytes("UTF-8")));        //TODO GBK?
                rootElement = document.getRootElement();
            }
            rootNode = parseElements(null, rootElement);
        } catch (DocumentException e) {
            throw new AX2JException(AX2JException.AXML_PARSE_ERROR, e.getLocalizedMessage());
        } catch (UnsupportedEncodingException e) {
            throw new AX2JException(AX2JException.AXML_PARSE_ERROR, e.getLocalizedMessage());
        }
        return rootNode;
    }

    /**
     * Recursion to iterate the Android XML elements.
     * @param parent the parent of the return node, null if it has no parent
     * @param rootElement the root elements in this function
     * @param layer the layer of this node to be created
     * @return the AXMLNode of the {@param rootElement}, the child of {@param rootElement}
     */
    private AX2JNode parseElements(AX2JNode parent, Element rootElement) {
        AX2JNode node = new AX2JNode(parent, rootElement);

        @SuppressWarnings("unchecked")
        List<Element> list = rootElement.elements();
        for(Element e : list){
            parseElements(node, e);
        }
        return node;
    }

    /**
     * Print the Android XML tree structure.
     * @param the root elements
     */
    public static void printNode(AX2JNode rootNode) {
        if (rootNode == null) {
            return;
        }
        printNode(rootNode, 0);
    }


    /**
     * Print the Android XML tree structure.
     * @param the root elements
     * @param layer in the root element
     */
    private static void printNode(AX2JNode rootNode, int layer) {
        if (rootNode == null) {
            return;
        }
        String tab = "";
        int i = layer;
        while(i > 0) {
            tab += Config.INDENT;
            i--;
        }
        System.out.println(tab + "<" + rootNode.getLabelName() + ">");
        List<Attribute> attrList = rootNode.getAttributes();
        for (Attribute a : attrList) {
            System.out.println(tab + Config.INDENT + a.getQualifiedName() + " = " + a.getValue());
        }

        List<AX2JNode> children = rootNode.getChildren();
        for (AX2JNode n : children) {
            printNode(n, layer + 1);
        }
    }
}
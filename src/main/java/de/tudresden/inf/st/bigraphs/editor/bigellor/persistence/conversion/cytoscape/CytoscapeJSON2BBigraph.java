package de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.conversion.cytoscape;

import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import org.apache.commons.io.IOUtils;
import org.apache.xalan.processor.TransformerFactoryImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * @author Dominik Grzelak
 */
public class CytoscapeJSON2BBigraph {

    private DefaultDynamicSignature signature;

    private HashMap<Integer, BigraphEntity.RootEntity> newRoots = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.NodeEntity> newNodes = new LinkedHashMap<>();
    private HashMap<Integer, BigraphEntity.SiteEntity> newSites = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.Edge> newEdges = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.OuterName> newOuterNames = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.InnerName> newInnerNames = new LinkedHashMap<>();

    // GraphML Node ID <-> GraphML Node as XOM Node Object
    Map<String, nu.xom.Node> graphMLOuterNames;
    Map<String, nu.xom.Node> graphMLInnerNames;
    Map<String, nu.xom.Node> graphMLEdges;
    Map<String, nu.xom.Node> graphMLRegions;

    private nu.xom.Document doc;
    private nu.xom.Document doc2;
    private MutableBuilder<DefaultDynamicSignature> builder;

    public CytoscapeJSON2BBigraph(String requestBody) throws ParsingException, IOException {

        graphMLOuterNames = new LinkedHashMap<>();
        graphMLInnerNames = new LinkedHashMap<>();
        graphMLEdges = new LinkedHashMap<>();
        graphMLRegions = new LinkedHashMap<>();

        JSONObject jsonRequestBody = new JSONObject(requestBody);

//        System.out.println("controls");

        // extract signature from request and create bigraph signature
//        System.out.println(jsonRequestBody.getJSONArray("controls"));
        signature = parseSignature(jsonRequestBody.getJSONArray("controls"));

        builder = MutableBuilder.newMutableBuilder(signature);
        newRoots.clear();
        newNodes.clear();
        newSites.clear();
        newEdges.clear();
        newOuterNames.clear();
        newInnerNames.clear();
        builder.reset();

//        System.out.println(prettyFormat(jsonRequestBody.get("graphml").toString()));

        // extract the bigraph
        String graphml = jsonRequestBody.get("graphml").toString();

        // extract the bigraph which is stored in graphml xml-schema
        nu.xom.Builder parser = new nu.xom.Builder();
        doc = parser.build(IOUtils.toInputStream(graphml));
        doc2 = parser.build(IOUtils.toInputStream(graphml)); // a "backup" document where nodes are not detached
    }

    public PureBigraph convert() throws IOException {
        // Important: must be parsed all before linkage
        parseOuterNames();
        parseInnerNames();
        parseRegions();

        // The linking all the points to links now
        performLinkage();

        // Create bigraph
        PureBigraphBuilder<DefaultDynamicSignature>.InstanceParameter meta = builder.new InstanceParameter(
                builder.getMetaModel(),
                signature,
                newRoots,
                newSites,
                newNodes,
                newInnerNames,
                newOuterNames,
                newEdges);
        builder.reset();
        PureBigraph bigraph = new PureBigraph(meta);
        return bigraph;
    }


    private void parseInnerNames() {
//        System.out.println("parseInnerNames()");
//        System.out.println(prettyFormat(inner_names.toXML()));
        for (nu.xom.Node n : doc.query("//node[@id='inner-names']/graph/node")) {
            //System.out.println("inner name: " + n.getValue());
            n.detach();
            String name = n.query("/node/data[@key='name']").get(0).getValue();
//            System.out.println("inner name: " + name);
            String id = ((nu.xom.Element) n).getAttributeValue("id");
            graphMLInnerNames.put(id, n);
            BigraphEntity.InnerName x = (BigraphEntity.InnerName) builder.createNewInnerName(name);
            newInnerNames.put(x.getName(), x);
        }
    }

    private void parseOuterNames() {
//        System.out.println("parseOuterNames()");
//        System.out.println(prettyFormat(outer_names.toXML()));

        for (nu.xom.Node n : doc.query("//node[@id='outer-names']/graph/node")) {
            n.detach();
            String name = n.query("/node/data[@key='name']").get(0).getValue();
//            System.out.println("outer name: " + name);
            String id = ((nu.xom.Element) n).getAttributeValue("id");
            graphMLOuterNames.put(id, n);

            BigraphEntity.OuterName newOuterName = (BigraphEntity.OuterName) builder.createNewOuterName(name);
            newOuterNames.put(newOuterName.getName(), newOuterName);
        }
    }

    private void performLinkage() {
        Nodes junctions = doc.query("//node[data='hyperedge-junction']");
//        System.out.println("parseEdges()");
//        System.out.println("junctions: ");
        Set<String> processedEdgesSourceId = new HashSet<>();
        Set<String> processedHyperEdgeJunctionIds = new HashSet<>();
        if (junctions.size() > 0) {
            for (nu.xom.Node junction : junctions) {
                junction.detach();
                String currentJunctionId = ((nu.xom.Element) junction).getAttributeValue("id");
                String sourceId = ((nu.xom.Element) junction).getAttributeValue("source");
                if (processedEdgesSourceId.contains(sourceId)) {
                    continue;
                }
                processedHyperEdgeJunctionIds.add(currentJunctionId);
                processedEdgesSourceId.add(sourceId);
                Map<String, String> tmpNode2Port = new LinkedHashMap<>();
//        List<String> tmpInner = new LinkedList<>();
                Map<String, String> tmpInner = new LinkedHashMap<>();
//                System.out.println("junction-point id: " + currentJunctionId);
                Nodes connected_lines = doc.query("//edge[@source='" + currentJunctionId + "']");
                String tmpTargetLinkName = "";
                for (nu.xom.Node connected_line : connected_lines) {
                    String connectedTargetId = ((nu.xom.Element) connected_line).getAttributeValue("target");
//                System.out.println("connected line has target: " + connected_line_target);
//                System.out.println(prettyFormat(connected_line.toXML()));
                    // Is Outer name
                    if (graphMLOuterNames.get(connectedTargetId) != null) {
                        tmpTargetLinkName = graphMLOuterNames.get(connectedTargetId).query("//data[@key='name']").get(0).getValue();
                        continue;
                    }
                    // Is Inner name
                    if (graphMLInnerNames.get(connectedTargetId) != null) {
                        String tmp = graphMLInnerNames.get(connectedTargetId).query("//data[@key='name']").get(0).getValue();
//                        tmpInner.add(tmp);
                        tmpInner.put(tmp, connectedTargetId);
                        continue;
                    }
                    // Is Port
                    Nodes query = doc2.query("//node[@id='" + connectedTargetId + "']/data[@key='type']");
                    if (query.size() > 0 && query.get(0).getValue().equals("port")) {
                        String realNodeId = query.get(0).getParent().getParent().getParent().query("@id").get(0).getValue();
                        tmpNode2Port.put(realNodeId, connectedTargetId);
                        continue;
                    }
                }

                if (connected_lines.size() > 0) {

                    BigraphEntity.Link linkTarget;

                    if (tmpTargetLinkName != null && !tmpTargetLinkName.isEmpty()) {
                        BigraphEntity.OuterName realOuterName = newOuterNames.get(tmpTargetLinkName);
                        linkTarget = realOuterName;
                    } else {
                        //TODO: use currentJunctionId for edge creation first
//                        System.out.println("closed link connections here!");
                        BigraphEntity.Edge newEdge = (BigraphEntity.Edge) builder.createNewEdge(currentJunctionId);
                        newEdges.put(newEdge.getName(), newEdge);
                        linkTarget = newEdge;
                    }

                    assert linkTarget != null;

                    // Link ports
                    for (Map.Entry<String, String> each : tmpNode2Port.entrySet()) {
                        String realNodeId = each.getKey();
                        String cytoPortId = each.getValue();
                        // Get index of that port
                        String portIndex = doc2.query("//node[@id='" + cytoPortId + "']/data[@key='index']").get(0).getValue();
                        int realIndex = Integer.parseInt(portIndex);
                        if (newNodes.get(realNodeId) != null) {
                            builder.connectToLinkUsingIndex(newNodes.get(realNodeId), linkTarget, realIndex);
                        }
                    }

                    // Link inner names
                    for (Map.Entry<String, String> eachInnerName : tmpInner.entrySet()) {
                        if (newInnerNames.get(eachInnerName.getKey()) != null) {
                            builder.connectInnerToLink(newInnerNames.get(eachInnerName.getKey()), linkTarget);
                        }
                    }
                }


            }
        }
//        else {
        Nodes edgeElements = doc.query("//edge"); //TODO check that an edge source id is not an <data type=\"data\" key=\"type\">hyperedge-junction</data>

        for (nu.xom.Node eachEdgeElement : edgeElements) {
            eachEdgeElement.detach();
            String sourceId = ((Element) eachEdgeElement).getAttributeValue("source");
            String edgeId = ((Element) eachEdgeElement).getAttributeValue("id");
            if (processedEdgesSourceId.contains(sourceId) || processedHyperEdgeJunctionIds.contains(sourceId)) {
                continue;
            }
            processedEdgesSourceId.add(sourceId);
            Map<String, String> tmpNode2Port = new LinkedHashMap<>();
//        List<String> tmpInner = new LinkedList<>();
            Map<String, String> tmpInner = new LinkedHashMap<>();
            // select all elements that have the same source id
            Nodes connectedElements = doc2.query("//edge[@source='" + sourceId + "']");
//            Map<String, String> tmpNode2Port = new LinkedHashMap<>(); //COMMENTED
//            List<String> tmpInner = new LinkedList<>(); //COMMENTED
            String tmpTargetLinkName = "";
            for (nu.xom.Node connected_line : connectedElements) {
                String connectedTargetId = ((nu.xom.Element) connected_line).getAttributeValue("target");
                // Is Outer name
                if (graphMLOuterNames.get(connectedTargetId) != null) {
                    tmpTargetLinkName = graphMLOuterNames.get(connectedTargetId).query("//data[@key='name']").get(0).getValue();
                    continue;
                }
                // Is Inner name
                if (graphMLInnerNames.get(connectedTargetId) != null) {
                    String tmp = graphMLInnerNames.get(connectedTargetId).query("//data[@key='name']").get(0).getValue();
//                    tmpInner.add(tmp);
                    tmpInner.put(tmp, connectedTargetId);
                    continue;
                }
                // Is Port
                Nodes query = doc2.query("//node[@id='" + connectedTargetId + "']/data[@key='type']");
                if (query.size() > 0 && query.get(0).getValue().equals("port")) {
                    String realNodeId = query.get(0).getParent().getParent().getParent().query("@id").get(0).getValue();
                    tmpNode2Port.put(realNodeId, connectedTargetId);
                    continue;
                }
            }

            if (connectedElements.size() > 0) {
                BigraphEntity.Link linkTarget;
                if (tmpTargetLinkName != null && !tmpTargetLinkName.isEmpty()) {
                    BigraphEntity.OuterName realOuterName = newOuterNames.get(tmpTargetLinkName);
                    linkTarget = realOuterName;
                } else {
                    //TODO: use currentJunctionId for edge creation first
//                    System.out.println("closed link connections here!");
                    BigraphEntity.Edge newEdge = (BigraphEntity.Edge) builder.createNewEdge(edgeId);
                    newEdges.put(newEdge.getName(), newEdge);
                    linkTarget = newEdge;
                }
                assert linkTarget != null;
                // Link ports
                for (Map.Entry<String, String> each : tmpNode2Port.entrySet()) {
                    String realNodeId = each.getKey();
                    String cytoPortId = each.getValue();
                    // Get index of that port
                    String portIndex = doc2.query("//node[@id='" + cytoPortId + "']/data[@key='index']").get(0).getValue();
                    int realIndex = Integer.parseInt(portIndex);
                    if (newNodes.get(realNodeId) != null) {
                        builder.connectToLinkUsingIndex(newNodes.get(realNodeId), linkTarget, realIndex);
                    }
                }
                // Link inner names
                for (Map.Entry<String, String> eachInnerName : tmpInner.entrySet()) {
                    if (newInnerNames.get(eachInnerName.getKey()) != null) {
                        builder.connectInnerToLink(newInnerNames.get(eachInnerName.getKey()), linkTarget);
                    }
                }
            }
        }
//        }
    }

    private void parseRegions() {
        // We obtain the index for each region an put it in a sorted map.
        for (nu.xom.Node region : doc.query("//node[@id='regions']/graph/node")) {
//            System.out.println("detecting numbering of regions");
            // Detach the element 'regions' otherwise the query processes the full
            // document, not only the subtree 'regions'.
            region.detach();
            Integer region_index = Integer.parseInt(region.query("/node/data[@key='index']").get(0).getValue());
            graphMLRegions.put("" + region_index, region);

            BigraphEntity<?> newRoot = builder.createNewRoot(region_index);
            newRoots.put(region_index, (BigraphEntity.RootEntity) newRoot);

            traverseNode(region, newRoot);
        }
    }

    private DefaultDynamicSignature parseSignature(JSONArray controls) {
//        System.out.println("converting signature: " + controls);
        // https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework
//        PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();

        for (int i = 0; i < controls.length(); i = i + 1) {
//            System.out.println("adding new signature" + i);
            JSONObject control = controls.getJSONObject(i);
            String name = control.getString("ctrlLbl");
//            System.out.println("control name: " + name);
            Integer arity = control.getInt("portCnt");
//            System.out.println("control arity: " + arity);
            String status = control.getString("status");

            signatureBuilder.newControl().identifier(StringTypedName.of(name)).arity(FiniteOrdinal.ofInteger(arity))
                    .status(ControlStatus.fromString(status))
                    .assign();
        }

        DefaultDynamicSignature signature = signatureBuilder.create();
//        System.out.println("signature builder result: " + signature);
        return signature;
    }

    // 'builder' is the current node (bigraph-framework) and 'childNodes' the 
    // corresponding children from the graphml/xml-model to be converted in to
    // nodes in the bigraph-framework and that are logically the children of 'builder'
    private void traverseNode(nu.xom.Node parentNodeXml, BigraphEntity currentParent) {
        Nodes childrenNodes = parentNodeXml.query("/node/graph/node");
        for (nu.xom.Node n : childrenNodes) {
//            System.out.println("traverseNode loop nodes");
            n.detach();

            //Retrieve ID of node
            String nodeId = n.query("@id").get(0).getValue();
            String nodeType = n.query("data[@key='type']").get(0).getValue();
//            String parentNodeId = n.query("data[@key='parent']").get(0).getValue();
            //Retrieve type of node
            // bigraph nodes in graphML carry a tag <data type = "data" key = "type"> node|site... </data>

//            System.out.println(prettyFormat(n.toXML()));

            boolean isPort = nodeType.equalsIgnoreCase("port");
            boolean isSite = nodeType.equalsIgnoreCase("site");
            boolean isNode = nodeType.equalsIgnoreCase("node");
            BigraphEntity ourNode = null;
            if (isNode) {
                String control = n.query("data[@type='data'][@key='ctrlLabel']").get(0).getValue();
                DefaultDynamicControl controlByName = signature.getControlByName(control);
                BigraphEntity.NodeEntity newNode = (BigraphEntity.NodeEntity) builder.createNewNode(controlByName, nodeId);
                newNodes.put(nodeId, newNode);
                ourNode = newNode;
                builder.setParentOfNode(ourNode, currentParent);
            }

            if (isSite) {
                int siteIndex = Integer.parseInt(n.query("data[@type='data'][@key='index']").get(0).getValue());
                BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) builder.createNewSite(siteIndex);
                newSites.put(newSite.getIndex(), newSite);
                builder.setParentOfNode(newSite, currentParent);
                ourNode = newSite;
            }

            Nodes query = n.query("/node/graph/node");
            if (query.size() > 0 && ourNode != null) {
                for (int i = 0; i < query.size(); i++) {
                    traverseNode(query.get(i), ourNode);
                }
            }
        }
    }

    // https://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
    public static String prettyFormat(String input, int indent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            //TransformerFactory transformerFactory = TransformerFactory.newInstance();
            TransformerFactory transformerFactory = new TransformerFactoryImpl();
            //transformerFactory.setAttribute("indent-number", indent);
            javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new RuntimeException(e); // simple exception handling, please review it
        }
    }

    public static String prettyFormat(String input) {
        return prettyFormat(input, 2);
    }
}

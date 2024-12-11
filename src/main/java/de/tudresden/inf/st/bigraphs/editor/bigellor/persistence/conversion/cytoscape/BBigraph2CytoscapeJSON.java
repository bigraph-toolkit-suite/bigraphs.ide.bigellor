package de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.conversion.cytoscape;

import com.google.common.graph.Traverser;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.conversion.Ecore2JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This converter class produces a flat array of nodes and edges to be consumed by cytoscape.js from a Ecore-based
 * bigraph instance model.
 * <p>
 * The bigraph instance model is converted to the format of the "Elements JSON" as specified by cytoscape, which is
 * a flat array that contains all nodes and edges.
 *
 * @see <a href="https://js.cytoscape.org/#notation/elements-json">https://js.cytoscape.org/#notation/elements-json</a>
 */
public class BBigraph2CytoscapeJSON extends Ecore2JSON {

    int node_counter = 0;
    //    int root_counter = 0;
//    int site_counter = 0;
    Map<BigraphEntity, String> bigraphNode2CytoJsonId = new HashMap<>();

    public BBigraph2CytoscapeJSON() {
        super();
    }

    public static void prettyFormat(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString); // Convert text to object
            System.out.println(json.toString(4)); // Print it with specified indentation
        } catch (JSONException e) {
            JSONArray json = new JSONArray(jsonString); // Convert text to object
            System.out.println(json.toString(4)); // Print it with specified indentation
        }
    }

    @Override
    public String parseBigraph(PureBigraph bigraph) {
        Traverser<BigraphEntity<?>> traverser = Traverser.forTree(x -> {
            List<BigraphEntity<?>> children = bigraph.getChildrenOf(x);
//            System.out.format("%s has %d children\n", x.getType(), children.size());
            return children;
        });
        JSONArray json = new JSONArray();
        Iterable<BigraphEntity<?>> bigraphEntities = traverser.breadthFirst((List) bigraph.getRoots());
        bigraphEntities.forEach(x -> {
//            System.out.println(x);
            String id = createJSONIdOrGet(x, bigraph);
            String parent_id = getParentId(x, bigraph);
//            System.out.println("parent_id for " + x + " (cyto-id=" + id + ") is " + parent_id);
            if (parent_id != null) {
                parsePlace(x, bigraph, json, parent_id);
            }
        });

        parseNames(bigraph, json);
        parseLinks(bigraph, json);

//        System.out.println("json: " + json.toString(4));
        return json.toString(4);
    }

    public String createJSONIdOrGet(BigraphEntity<?> x, PureBigraph bigraph) {
        String suffixId = "";
        switch (x.getType()) {
            case SITE:
                suffixId = "s" + ((BigraphEntity.SiteEntity) x).getIndex();
                break;
            case NODE:
                if (bigraphNode2CytoJsonId.get(x) == null) {
                    suffixId = "v" + node_counter; //((BigraphEntity.NodeEntity) x).getControl().getNamedType().stringValue()
                    node_counter++;
                }
                break;
            case ROOT:
                suffixId = "r" + ((BigraphEntity.RootEntity) x).getIndex();
                break;
            case PORT:
                BigraphEntity.NodeEntity<DefaultDynamicControl> nodeOfPort = bigraph.getNodeOfPort(((BigraphEntity.Port) x));
                if (nodeOfPort != null) {
                    String tmp = createJSONIdOrGet(nodeOfPort, bigraph);
                    suffixId = "p-" + tmp + "-" + ((BigraphEntity.Port) x).getIndex();
                }
                break;
            case OUTER_NAME:
                suffixId = "o" + ((BigraphEntity.OuterName) x).getName();
                break;
            case INNER_NAME:
                suffixId = "i" + ((BigraphEntity.InnerName) x).getName();
                break;
            case EDGE:
                suffixId = ((BigraphEntity.Edge) x).getName();
                break;
            default:
                suffixId = "null";

        }
        if (bigraphNode2CytoJsonId.get(x) == null) {
            bigraphNode2CytoJsonId.put(x, suffixId);
        }
        String id = bigraphNode2CytoJsonId.get(x);
        return id;
    }

    public String getParentId(BigraphEntity place, Bigraph bigraph) {
        if (!BigraphEntityType.isPlaceType(place)) {
            return null;
        }
        switch (place.getType()) {
            case NODE:
            case SITE:
                BigraphEntity parent = bigraph.getParent(place);
                String newId = "";
                if (BigraphEntityType.isRoot(parent)) {
                    if (bigraphNode2CytoJsonId.get(parent) == null) {
                        newId = "r" + ((BigraphEntity.RootEntity) parent).getIndex();
                        bigraphNode2CytoJsonId.put(parent, newId);
                    }
                } else {
                    if (bigraphNode2CytoJsonId.get(parent) == null) {
                        newId = "v" + node_counter;
                        bigraphNode2CytoJsonId.put(parent, newId);
                    }
                }
                return bigraphNode2CytoJsonId.get(parent);
            case ROOT:
            default:
                return "regions";
        }
    }

    @Override
    public void parsePlace(BigraphEntity place, PureBigraph bigraph, JSONArray json, String parent_id) {

        if (BigraphEntityType.isRoot(place)) {
            BigraphEntity.RootEntity region = (BigraphEntity.RootEntity) place;
            String random_id = bigraphNode2CytoJsonId.get(region);
            assert random_id != null;

            JSONObject json_root = new JSONObject();
            //json_root.append("classes", "region");
            json_root.put("group", "nodes");
            json_root.put("classes", "region");
            JSONObject json_root_data = new JSONObject();
            json_root_data.put("id", random_id);
            json_root_data.put("parent", "regions");
            json_root_data.put("index", "" + region.getIndex());
            json_root.put("data", json_root_data);
//            json_root.append("data", json_root_data);
            json.put(json_root);


//            for (BigraphEntity child : bigraph.getChildrenOf(region)) {
//                //System.out.println("child: " + child);
//                parsePlace(child, bigraph, json, random_id);
//            }

        } else if (BigraphEntityType.isNode(place)) {
            BigraphEntity.NodeEntity node = (BigraphEntity.NodeEntity) place;
//            System.out.println("parsePlace: node, " + node);

            JSONObject json_node = new JSONObject();
            // Cytoscape distinguishes visually between nodes and edges.
            json_node.put("group", "nodes");
            String theNodeId = bigraphNode2CytoJsonId.get(node);
            if (theNodeId == null) {
                theNodeId = createJSONIdOrGet(node, bigraph);
            }
            // Set css-class that is also used to query for bigraph nodes in cytoscapes's
            // datamodel.
            json_node.put("classes", "node");
            JSONObject json_node_data = new JSONObject();
            json_node_data.put("id", theNodeId);
            json_node_data.put("parent", parent_id);
            json_node_data.put("type", "node");
            json_node_data.put("ctrlLabel", node.getControl().getNamedType().stringValue());
            //System.out.println("eee" + node.getControl().getNamedType().getValue());
            //System.out.println("control of node: " + node.getControl().toString());
            //json_node_data.put("control", node.getControl().getNamedType());

            json_node.put("data", json_node_data);
            json.put(json_node);

            parsePorts(node, bigraph, json, theNodeId);

//            for (BigraphEntity child : bigraph.getChildrenOf(node)) {
//                parsePlace(child, bigraph, json, theNodeId);
//            }

        } else if (BigraphEntityType.isSite(place)) {

            BigraphEntity.SiteEntity site = (BigraphEntity.SiteEntity) place;
//            System.out.println("parsePlace: site, " + site);
//            System.out.println("site index: " + site.getIndex());
            JSONObject json_site = new JSONObject();
            json_site.put("group", "nodes");
            String site_id = bigraphNode2CytoJsonId.get(site);
            if (site_id == null) {
                site_id = createJSONIdOrGet(site, bigraph);
            }
            int site_index = site.getIndex();
            json_site.put("classes", "site");
            JSONObject json_site_data = new JSONObject();
            json_site_data.put("parent", parent_id);
            json_site_data.put("index", "" + site_index);
            json_site_data.put("type", "site");
            json_site_data.put("id", site_id);
            json_site.put("data", json_site_data);
            json.put(json_site);
        }
    }

    public void parsePorts(BigraphEntity.NodeEntity node, PureBigraph bigraph, JSONArray json, String parent_id) {
        int arityOfControl = node.getControl().getArity().getValue().intValue();

        List<BigraphEntity.Port> ports = bigraph.getPorts(node);
        List<Integer> portIndexUsed = IntStream.range(0, arityOfControl).boxed().collect(Collectors.toList());
        for (int ix = 0; ix < arityOfControl; ix++) {
            JSONObject new_port = new JSONObject();

            BigraphEntity.Port next = null;
            String port_id;
            if (ix < ports.size()) {
                next = ports.get(ix);
            }
            int portIndex = -1;
            if (next != null) {
                // is actually connected (because it exists in the bigraph model),
                // so its also available now in idNodeMap for later to be retrieved in the parse link procedure
                port_id = bigraphNode2CytoJsonId.get(next);
                if (port_id == null) {
                    port_id = createJSONIdOrGet(next, bigraph);
                }
                portIndexUsed.remove((Integer) next.getIndex());
                portIndex = next.getIndex();
            } else {
                // create the port id for the JSON here manually instead of calling createJSONIdOrGet() because port
                // does not exist "physically" but only by definition (is empty)
                String tmpNodeId = createJSONIdOrGet(node, bigraph);
                portIndex = portIndexUsed.get(0);
                portIndexUsed.remove(0);
                port_id = "p-" + tmpNodeId + "-" + portIndex;
            }
            JSONObject new_port_data = new JSONObject();
            new_port.put("group", "nodes");
            new_port.put("classes", "port");
//                new_port.put("id", port_id);
            new_port_data.put("type", "port");
            new_port_data.put("index", "" + portIndex);
            new_port_data.put("parent", parent_id);
            new_port_data.put("id", port_id);
            new_port.put("data", new_port_data);
            json.put(new_port);
        }
    }

    // Create visual elements for inner and outer names. Check if they are connected
    // and record them so that a visual line can be created later.
    @Override
    protected void parseNames(PureBigraph bigraph, JSONArray json) {
        Collection<BigraphEntity.OuterName> outer_names = bigraph.getOuterNames();
        Collection<BigraphEntity.InnerName> inner_names = bigraph.getInnerNames();

        for (BigraphEntity.OuterName outer_name : outer_names) {
            JSONObject json_outer_name = new JSONObject();
            json_outer_name.put("group", "nodes");
            json_outer_name.put("classes", "outer-name");

            JSONObject json_outer_name_data = new JSONObject();
            String random_id = createJSONIdOrGet(outer_name, bigraph);//java.util.UUID.randomUUID().toString();
            json_outer_name_data.put("parent", "outer-names");
            json_outer_name_data.put("name", outer_name.getName());
            json_outer_name_data.put("type", "outer-name");
            json_outer_name_data.put("id", random_id);
            json_outer_name.put("data", json_outer_name_data);

            json.put(json_outer_name);
        }

        for (BigraphEntity.InnerName inner_name : inner_names) {
            JSONObject json_inner_name = new JSONObject();
            json_inner_name.put("group", "nodes");
            json_inner_name.put("classes", "inner-name");

            JSONObject json_inner_name_data = new JSONObject();
            String random_id = createJSONIdOrGet(inner_name, bigraph); //java.util.UUID.randomUUID().toString();
            json_inner_name_data.put("parent", "inner-names");
            json_inner_name_data.put("name", inner_name.getName());
            json_inner_name_data.put("type", "inner-name");
            json_inner_name_data.put("id", random_id);
            json_inner_name.put("data", json_inner_name_data);

            json.put(json_inner_name);
        }
    }

    @Override
    public void parseLinks(PureBigraph bigraph, JSONArray json) {
        List<BigraphEntity.Link> allLinks = bigraph.getAllLinks();
        final int n = allLinks.size();
        for (int i = 0; i < n; i++) {
            BigraphEntity.Link link = allLinks.get(i);
            List<BigraphEntity<?>> pointsFromLink = bigraph.getPointsFromLink(link);
            if (pointsFromLink.size() > 0) {

                // hyperedge-junction is a supporting construct to visualize hyperedges in cytoscape
                // and serve as a handle for manipulation
                JSONObject json_junction_point = new JSONObject();
                String junction_point_id = java.util.UUID.randomUUID().toString();
                if (BigraphEntityType.isEdge(link)) {
                    junction_point_id = createJSONIdOrGet(link, bigraph);
                }
                json_junction_point.put("group", "nodes");
                json_junction_point.put("classes", "hyperedge-junction");
                JSONObject json_junction_point_data = new JSONObject();
                json_junction_point_data.put("type", "hyperedge-junction");
                json_junction_point_data.put("id", junction_point_id);
                json_junction_point.put("data", json_junction_point_data);
                json.put(json_junction_point);

                if (BigraphEntityType.isOuterName(link)) {
                    String targetLinkId = createJSONIdOrGet(link, bigraph);
                    JSONObject json_line = new JSONObject();
                    json_line.put("group", "edges");
                    json_line.put("classes", "bigraph-edge");
                    JSONObject json_line_data = new JSONObject();
//                json_line_data.put("id", line_random_id);
                    json_line_data.put("source", junction_point_id);
                    json_line_data.put("target", targetLinkId);
                    json_line.put("data", json_line_data);
                    json.put(json_line);
                }

                final int m = pointsFromLink.size();
                for (int j = 0; j < m; j++) {
                    BigraphEntity<?> bigraphEntity = pointsFromLink.get(j);
                    String s = bigraphNode2CytoJsonId.get(bigraphEntity);
                    assert s != null || !s.equals("null");

                    String pointId = bigraphNode2CytoJsonId.get(bigraphEntity);
                    assert pointId != null;
                    JSONObject json_line2 = new JSONObject();
                    json_line2.put("group", "edges");
                    json_line2.put("classes", "bigraph-edge");
                    JSONObject json_line_data2 = new JSONObject();
//                    json_line_data2.put("id", line_random_id);
                    json_line_data2.put("source", junction_point_id);
                    json_line_data2.put("target", pointId);
                    json_line2.put("data", json_line_data2);
                    json.put(json_line2);
                }
            }
        }
    }
}

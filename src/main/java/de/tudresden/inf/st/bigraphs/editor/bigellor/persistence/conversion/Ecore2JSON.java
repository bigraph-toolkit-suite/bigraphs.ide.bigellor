/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.conversion;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * @author simon
 */
public class Ecore2JSON {

    protected Map<Integer, String> regions;
    protected MultiValuedMap<String, String> linked_outer_names;
    protected MultiValuedMap<String, String> edges;
    protected Map<String, String> outer_names;
    protected Map<String, String> inner_names;

    public Ecore2JSON() {

        this.regions = new HashMap<>();
        // When we traverse the ports of a node and look whether they are connected
        // to a link (edge or outer name), we save the name of the link and the
        // id of the port linked. Later when we create the visual elements for a
        // hyperedge, we need to create a line for each port connected to it with
        // the port's id as target. I.e. the port id used as id attribute in
        // the JSON-artifact created for the port.
        this.linked_outer_names = new HashSetValuedHashMap<>();
        // Same idea as for 'linked_outer_names'
        this.edges = new HashSetValuedHashMap<>();
        // When the visual elements for inner and outer names are created,
        // collect the names and ids.
        this.outer_names = new HashMap<>();
        this.inner_names = new HashMap<>();
    }


    public PureBigraph createDemoBigraph() throws LinkTypeNotExistsException, InvalidConnectionException, TypeNotExistsException {

        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder.newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(3)).assign();
        signatureBuilder.newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(3)).assign();
        signatureBuilder.newControl().identifier(StringTypedName.of("PC")).arity(FiniteOrdinal.ofInteger(3)).assign();
//        signatureBuilder.newControl().identifier(StringTypedName.of("D")).arity(FiniteOrdinal.ofInteger(3)).assign();
//
//
        DynamicSignature signature = signatureBuilder.create();


        //PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);

        PureBigraphBuilder.Hierarchy r1 = builder.root();

        BigraphEntity.InnerName e1 = builder.createInner("e1");
        BigraphEntity.InnerName tmp = builder.createInner("tmp");
        BigraphEntity.OuterName a = builder.createOuter("a");
        BigraphEntity.OuterName b = builder.createOuter("b");
        BigraphEntity.OuterName c = builder.createOuter("c");

        PureBigraphBuilder.Hierarchy b1 = r1.site().child("User").linkInner(tmp).down().site();
        PureBigraphBuilder.Hierarchy b2 = r1.child("PC").linkInner(tmp).linkOuter(a);

        builder.linkInnerToOuter(e1, c);

        PureBigraph bigraph = builder.create();
        return bigraph;
    }


    // Create JSON-fragment for a Bigraph in Bigraph-Framework-representation
    public String parseBigraph(PureBigraph bigraph) {
        JSONArray json = new JSONArray();

        System.out.println("parseBigraph loop regions");
        //int i = 0;
        for (BigraphEntity.RootEntity region : bigraph.getRoots()) {
            System.out.println("region with index: " + region.getIndex());



            /*
            var json_root = new JSONObject();
            var json_root_data = new JSONObject();
            var random_id = java.util.UUID.randomUUID().toString();
            json_root_data.put("id", random_id);
            json_root_data.put("index", region.getIndex());
            json_root_data.put("parent", "regions");
            //json_root.append("data", json_root_data);
            //json_root.append("classes", "region");
            json_root.put("group","nodes");
            json_root.put("data", json_root_data);
            json_root.put("classes","region");
            json.put(json_root);

            this.regions.put(region.getIndex(), random_id);
*/

            /*
            for (var node : bigraph.getChildrenOf(region) ) {
                System.out.println("parsing node");
                System.out.println(node.getControl());
                System.out.println(node.getType());
                // pass in the randomly generated id for the region so that children
                // (nodes and sites) can use it as 'parent'-attribute
                //parseNode(node, bigraph, json, random_id);

            }*/

            //parseNode(bigraph.getNodes().get(i), bigraph);

            // pass in the randomly generated id for the region so that children
            // (nodes and sites) can use it as 'parent'-attribute
            parsePlace(region, bigraph, json, "regions");
            /*
            System.out.println("parsing node from region");
            for ( var node : bigraph.getChildrenOf(root)) {
                System.out.println(node.getType());
                parseNode(node, bigraph);
            }
            */
            //i = i+1;
        }

        parseNames(bigraph, json);
        parseLinks(bigraph, json);


        // pretty print the result with indentation of four spaces
        System.out.println("json: " + json.toString(4));
//        return ("json: " + json.toString(4));
        return json.toString(4);

        //parseNode(bigraph.getNodes().get(0), bigraph);


    }

    // This method can hit a region, site or node
    public void parsePlace(BigraphEntity place, PureBigraph bigraph, JSONArray json, String parent_id) {

        System.out.println("parsing place");
        //System.out.println
        String p_type = place.getType().toString();

        if (p_type.equals("ROOT")) {
            BigraphEntity.RootEntity region = (BigraphEntity.RootEntity) place;
            System.out.println("parsePlace: region, " + region);
            JSONObject json_root = new JSONObject();
            JSONObject json_root_data = new JSONObject();
            String random_id = java.util.UUID.randomUUID().toString();
            json_root_data.put("id", random_id);
            json_root_data.put("index", region.getIndex());
            json_root_data.put("parent", "regions");
            //json_root.append("data", json_root_data);
            //json_root.append("classes", "region");
            json_root.put("group", "nodes");
            json_root.put("data", json_root_data);
            json_root.put("classes", "region");
            json.put(json_root);


            for (BigraphEntity child : bigraph.getChildrenOf(region)) {
                //System.out.println("child: " + child);
                parsePlace(child, bigraph, json, random_id);
            }

        } else if (p_type.equals("NODE")) {
            BigraphEntity.NodeEntity node = (BigraphEntity.NodeEntity) place;
            System.out.println("parsePlace: node, " + node);

            JSONObject json_node = new JSONObject();
            // Cytoscape distinguishes visually between nodes and edges.
            json_node.put("group", "nodes");
            String random_id = java.util.UUID.randomUUID().toString();
            json_node.put("id", random_id);
            // Set css-class that is also used to query for bigraph nodes in cytoscapes's
            // datamodel.
            json_node.put("classes", "node");
            JSONObject json_node_data = new JSONObject();
            json_node_data.put("parent", parent_id);
            json_node_data.put("type", "node");
            json_node_data.put("control", node.getControl().getNamedType().getValue());
            //System.out.println("eee" + node.getControl().getNamedType().getValue());
            //System.out.println("control of node: " + node.getControl().toString());
            //json_node_data.put("control", node.getControl().getNamedType());

            json_node.put("data", json_node_data);
            json.put(json_node);

            parsePorts(node, bigraph, json, random_id);

            for (BigraphEntity child : bigraph.getChildrenOf(node)) {
                parsePlace(child, bigraph, json, random_id);
            }

        } else if (p_type.equals("SITE")) {

            BigraphEntity.SiteEntity site = (BigraphEntity.SiteEntity) place;
            System.out.println("parsePlace: site, " + site);
            System.out.println("site index: " + site.getIndex());
            JSONObject json_site = new JSONObject();
            json_site.put("group", "nodes");
            String site_id = java.util.UUID.randomUUID().toString();
            int site_index = site.getIndex();
            json_site.put("classes", "site");
            JSONObject json_site_data = new JSONObject();
            json_site_data.put("parent", parent_id);
            json_site_data.put("index", site_index);
            json_site_data.put("type", "site");
            json_site_data.put("id", site_id);
            json_site.put("data", json_site_data);
            json.put(json_site);
        }


        //processPorts()


        // If we process a node and not a region, ports must be handled
            /*
            if (n.getType().toString().equals("NODE")) {
                System.out.println("node or region? node!");
                var nn = (BigraphEntity.NodeEntity) n;

                // For each port, we must check if it is connected to an edge or outer name.
                for ( BigraphEntity.Port p : bigraph.getPorts(nn)) {

                    var port_index = p.getIndex();
                    var link = bigraph.getLinkOfPoint(p);
                    var typeOfPoint = link.getType();
                    System.out.println("link-of-point type: " + typeOfPoint);
                    if (typeOfPoint.toString().equals("EDGE")) {
                        var e = (BigraphEntity.Edge) link;
                        System.out.println("name of edge: " + e.getName());
                    }

                    if (typeOfPoint.toString().equals("OUTER_NAME")) {
                        var o = (BigraphEntity.OuterName) link;
                        System.out.println("name of outer name: " + o.getName());
                    }

                    if (typeOfPoint.toString().equals("INNER_NAME")) {
                        var i = (BigraphEntity.OuterName) link;
                        System.out.println("name of inner name: " + i.getName());
                    }
                }

                System.out.println("parseNode.parseNode");
                parseNode(n, bigraph, json, random_id);

            }



        }*/
    }

    // Create visual elements for inner and outer names. Check if they are connected
    // and record them so that a visual line can be created later.
    protected void parseNames(PureBigraph bigraph, JSONArray json) {

        System.out.println("parseNames()");
        Collection<BigraphEntity.OuterName> outer_names = bigraph.getOuterNames();
        Collection<BigraphEntity.InnerName> inner_names = bigraph.getInnerNames();

        for (BigraphEntity.OuterName outer_name : outer_names) {
            JSONObject json_outer_name = new JSONObject();
            json_outer_name.put("group", "nodes");
            json_outer_name.put("classes", "outer-name");

            JSONObject json_outer_name_data = new JSONObject();
            String random_id = java.util.UUID.randomUUID().toString();
            json_outer_name_data.put("parent", "outer-names");
            json_outer_name_data.put("name", outer_name.getName());
            json_outer_name_data.put("type", "outer-name");
            json_outer_name_data.put("id", random_id);
            json_outer_name.put("data", json_outer_name_data);

            json.put(json_outer_name);

            // Later we use the random_id to draw a line from the junction point
            // to the outer name visual element (carrying the name as label)
            this.outer_names.put(outer_name.getName(), random_id);

            System.out.println("outer name connected to: " + bigraph.getPointsFromLink(outer_name));

            // We don't need to store the 'random_id' of the visual representation
            // of the outer name (the node with the outer name's 'name' as label).
            // When we transform a hyperedge, which is an outer name, to the respective visual
            // elements, we know that a visual element for outer name visual
            // element was created here.

        }

        for (BigraphEntity.InnerName inner_name : inner_names) {
            JSONObject json_inner_name = new JSONObject();
            json_inner_name.put("group", "nodes");
            json_inner_name.put("classes", "outer-name");

            JSONObject json_inner_name_data = new JSONObject();
            String random_id = java.util.UUID.randomUUID().toString();
            json_inner_name_data.put("parent", "inner-names");
            json_inner_name_data.put("name", inner_name.getName());
            json_inner_name_data.put("type", "inner-name");
            json_inner_name_data.put("id", random_id);
            json_inner_name.put("data", json_inner_name_data);

            json.put(json_inner_name);

            this.inner_names.put(inner_name.getName(), random_id);

            // The outer name may be connected to an edge or outer name, so
            // its 'random_id' must be stored under the name of the element it is connected
            // to so that the line from the visual element of the inner name to
            // the junction point can be created. The line's target-attribute is
            // the 'random_id'.
            // Check if it is connected
            BigraphEntity link = bigraph.getLinkOfPoint(inner_name);
            System.out.println("inner name connected to: " + link);
            if (link != null) {
                String type = link.getType().toString();

                if (type.equals("EDGE")) {
                    this.edges.put(((BigraphEntity.Edge) link).getName(), random_id);
                } else if (type.equals("OUTER_NAME")) {
                    this.linked_outer_names.put(((BigraphEntity.OuterName) link).getName(), random_id);
                }
            }
        }
    }

    // For links, we need to create a cytoscape node that is the junction point
    // where the lines to the visual elements representing ports, inner- and outer
    // names meet. From the bigraph perspective, outer names _are_ the link but
    // are displayed as distinct cytoscape nodes.
    public void parseLinks(PureBigraph bigraph, JSONArray json) {
        System.out.println("parseLinks.edges");


        for (String edge : this.edges.keySet()) {

            //var connected_elements = this.edges.get(e.getName());
            Collection<String> connected_elements = this.edges.get(edge);
            String junction_point_id = junctionPointHelper(json, connected_elements);

            //System.out.println("this edge is connected to: " + this.edges.get(e.getName()));
            System.out.println("this hyperedge (edge) is connected to: " + connected_elements);

        }

        for (String outer_name : this.linked_outer_names.keySet()) {

            //var connected_elements = this.edges.get(e.getName());
            Collection<String> connected_elements = this.edges.get(outer_name);
            // Append the id of visual node representing the outer name to list
            // of ids to use as target-attributes for lines to create.
            connected_elements.add(this.outer_names.get(outer_name));
            String junction_point_id = junctionPointHelper(json, connected_elements);

            //System.out.println("this edge is connected to: " + this.edges.get(e.getName()));
            System.out.println("this hyperedge (outer name) is connected to: " + connected_elements);

        }

        /*
        for (BigraphEntity.Edge e : bigraph.getEdges()) {
            System.out.println("edge found: " + e.getName());

            var connected_elements = this.edges.get(e.getName());
            var junction_point_id = junctionPointHelper(json, connected_elements);

            System.out.println("this edge is connected to: " + this.edges.get(e.getName()));

            //for (String port : connected_ports)

            // Now lines must be created for each element visually connected to
            // it. As this link is a outer name, a visual element representing
            // it was already created (parseNames()).
            /*
            for (var connected_element_id : this.edges.get(e.getName()) ) {

                var line_random_id = java.util.UUID.randomUUID().toString();

                var json_line = new JSONObject();
                json_line.put("group","edges");
                json_line.put("classes", "bigraph-edge");
                var json_line_data = new JSONObject();
                json_line_data.put("id", line_random_id);
                json_line_data.put("source", junction_point_id);
                json_line_data.put("target", connected_element_id);
                json_line.put("data", json_line_data);
                json.put(json_line);


            }*/
        //}*/

        /*
        System.out.println("parseLinks.outer-names");
        for (BigraphEntity.OuterName o : bigraph.getOuterNames()) {
            System.out.println("outer name found: " + o.getName());

            var connected_elements = this.edges.get(o.getName());
            var junction_point_id = junctionPointHelper(json, connected_elements);



        }
        */

    }

    private String junctionPointHelper(JSONArray json, Collection<String> connected_elements) {

        // Create junction point (cytoscape node)
        JSONObject json_junction_point = new JSONObject();
        String junction_point_id = java.util.UUID.randomUUID().toString();
        json_junction_point.put("group", "nodes");
        json_junction_point.put("classes", "hyperedge-junction");
        JSONObject json_junction_point_data = new JSONObject();
        json_junction_point_data.put("type", "hyperedge-junction");
        json_junction_point_data.put("id", junction_point_id);

        json.put(json_junction_point);


        // Now lines must be created for each element visually connected to
        // it. As this link is a outer name, a visual element representing
        // it was already created (parseNames()).
        for (String connected_element_id : connected_elements) {

            String line_random_id = java.util.UUID.randomUUID().toString();

            JSONObject json_line = new JSONObject();
            json_line.put("group", "edges");
            json_line.put("classes", "bigraph-edge");
            JSONObject json_line_data = new JSONObject();
            json_line_data.put("id", line_random_id);
            json_line_data.put("source", junction_point_id);
            json_line_data.put("target", connected_element_id);
            json_line.put("data", json_line_data);
            json.put(json_line);


        }

        return junction_point_id;
    }

    private void parsePorts(BigraphEntity.NodeEntity node, PureBigraph bigraph, JSONArray json, String parent_id) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //var ports = java.util.stream.IntStream.range(0,node.getControl().getArity().getValue().intValue());
        int ports = node.getControl().getArity().getValue().intValue();

        String control_name = node.getControl().getNamedType().stringValue();

        // No sense in creating visual nodes for ports if the nodes does not
        // have ports in the first place.
        if (ports > 0) {

            // collect these ports that have a link
            HashMap<Integer, BigraphEntity> linked_ports = new HashMap<Integer, BigraphEntity>();

            // getPorts only returns connected ports
            Iterator<BigraphEntity.Port> iterator = bigraph.getPorts(node).iterator();


            int i = 0;

            // Iterate over ports that are connected with links.
            while (iterator.hasNext()) {
                BigraphEntity.Port next = iterator.next();
                BigraphEntity link = bigraph.getLinkOfPoint(next);
//                System.out.println("link: " + link.toString());
//                System.out.println("port index: " + next.getIndex());

                // Store the index of the linked port. In case ports 0 and 2 are
                // connected but port 1 is not, 'linked_ports' is [0,2].
                //linked_ports.add(next.getIndex());
                linked_ports.put(next.getIndex(), link);
                String type = link.getType().toString();
                /*
                if (type.equals("OUTER_NAME")) {
                var outer_name = (BigraphEntity.OuterName) link;
                System.out.println("name of this outer name: " + outer_name.getName());
                //this.linked_outer_names.put(outer_name.getName(), );
                }
                if (type.equals("EDGE")) {
                var outer_name = (BigraphEntity.Edge) link;
                System.out.println("name of this edge: " + outer_name.getName());
                }*/

                i += 1;

            }

            System.out.println("has " + i + " connected ports");
            int port;

            // Traverse all ports of the node. Look up its index in 'linked_ports'
            // and perform additional steps like adding the pair (link-name, port-id)
            // to the list of lines (visual elements) to create when parsing
            // edges and outer names in a subseqent step.
            for (port = 0; port < ports; port += 1) {
                System.out.println("port found");
                JSONObject new_port = new JSONObject();
                String port_id = java.util.UUID.randomUUID().toString();
                JSONObject new_port_data = new JSONObject();
                new_port.put("group", "nodes");
                new_port.put("classes", "port");
                new_port.put("id", port_id); //TODO NEW
                new_port_data.put("type", "port");
                new_port_data.put("index", port);
                new_port_data.put("parent", parent_id);
                new_port_data.put("id", port_id);
                new_port.put("data", new_port_data);
                json.put(new_port);

                // use the port's index to check if it is connected
                if (linked_ports.containsKey(port)) {
                    BigraphEntity link = linked_ports.get(port);
                    String type = link.getType().toString();

                    if (type.equals("OUTER_NAME")) {
                        BigraphEntity.OuterName outer_name = (BigraphEntity.OuterName) link;
                        System.out.println("port " + port + " is connected to outer name: " + outer_name.getName());
                        // remember the port id so that we can use it as target
                        // for the line, that is created later when the links
                        // are traversed
                        this.linked_outer_names.put(outer_name.getName(), port_id);
                    }

                    if (type.equals("EDGE")) {
                        BigraphEntity.Edge edge = (BigraphEntity.Edge) link;
                        System.out.println("port " + port + " is connected to edge: " + edge.getName());
                        // remember the port id so that we can use it as target
                        // for the line, that is created later when the links
                        // are traversed
                        this.edges.put(edge.getName(), port_id);
                    }
                }
            }
        }


    }


}

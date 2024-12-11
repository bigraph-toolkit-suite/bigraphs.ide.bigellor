//cy.$id('r0').descendants().layout({name: 'fcose', fit: false}).run()

function renderPlaces() {

    // Lets iterate through the regions an use the fcose layout to avoid overlap
    // of nodes an sites in a region.
    for (r of cy.$('.region')) {

        console.log("r id: ", r.id())
        if (r.descendants().length > 0) {
            r.descendants().layout({name: 'fcose', fit: false, animate: false}).run()
        }
    }
}

function renderRegions() {

    // we don't know how the list of regions is ordered, i.e. if the first
    // element is the one actually having id 'r0', being labeled '0'. So we
    // want to follow the theory in that the left most region has '0'.

    // If 'r2' was created first, it will be the first element in the list of
    // nodes matching the class '.region', therefor lets sort them first.
    regions = cy.$('.region').sort(
        // We assume a fixed naming scheme for regions an sites, prepending a 'r'
        // or a 's' so that region 0 and site 0 can have the same label but a
        // different ID. List is sorted on the ID field.
        //(e1, e2) => e1.id().localeCompare(e2.id())
        (e1, e2) => e1.data('index').localeCompare(e2.data('index'))
    );


    // We only need to allign the regions if there are more than two.
    if (regions.length >= 2) {

        // Pick one region to which all remaining regions will be alligned.
        ref_elem = regions[0];

        // Pick the upper edge of 'ref_elem'.
        ref_elem_y1 = ref_elem.boundingBox().y1


        // Exclude 'ref_elem', no need to to reposition the region that is our
        // 'frame of reference'.
        for (s of regions.not(ref_elem)) {


            // We get the current y-position of the region we want to move.
            s_y = s.position('y')

            // Naming according to cytoscape API
            s_y1 = s.boundingBox().y1

            // Reposition 's' on the y-axis by the ammount its upper edge
            // y-coordinate differs from that of the reference region
            // ('ref_elem_y1').
            s.position('y', s_y + (ref_elem_y1 - s_y1));

            // rinse and repeat for the remaining regions
        }

        // Now that all regions are aligned to the upper edge, lets place them such
        // that the region with id 'r0' is the leftmost, etc. and that all regions
        // have a minimum of space between their borders.
        minimum_space = 10

        // Assuming the ref_elem is already in the correct position on the x-axis,
        // lets reposition the others step by step. In case of three elements, it
        // will look even more messy, as the third element looks even less in place
        // − doesn't matter, will be aligned to second one anyways and all is fine.

        // In the loop we need to reference the predecessor to which to align to
        // the current region.
        pred = 0

        for (s of regions.not(ref_elem)) {

            // We get the current x-position of the region we want to move.
            s_x = s.position('x')

            // Get the left border of the region that is being moved.
            s_x1 = s.boundingBox().x1

            // Get the right border of the region that should occur left to the
            // current region under estimation.
            ref_elem_x2 = regions[pred].boundingBox().x2

            // Reposition 's' on the x-axis by the ammount its left edge overlaps
            // with is predecessor regions right edge plus a desired spacing amount
            // regions.
            s.position('x', s_x + (ref_elem_x2 - s_x1) + minimum_space);

            pred += 1
            // rinse and repeat for the remaining regions

        }

    }

}

function renderNames() {


    minimum_space = 10

    top_ = cy.$id('outer-names')
    middle = cy.$id('regions')
    bottom = cy.$id('inner-names')

    //var comperator = function(m,n){n.style('label').localeCompare(m.style('label'))}
    //r.descendants().layout({name: 'fcose', 'fit': false, animate: false}).run()
    cy.$id('outer-names').children().layout({fit: false, name: 'grid', rows: 1, condense: true, spacingFactor: 3}).run()
    cy.$id('inner-names').children().layout({fit: false, name: 'grid', rows: 1, condense: true, spacingFactor: 3}).run()

    // procedure similar to arranging regions but this time on the x-axis
    top_y2 = top_.boundingBox().y2
    middle_y1 = middle.boundingBox().y1
    middle_y = middle.position('y')
    // place regions below outer names
    middle.position('y', middle_y + (top_y2 - middle_y1) + minimum_space)
    middle.position('x', top_.position('x'))

    middle_y2 = middle.boundingBox().y2
    bottom_y1 = bottom.boundingbox().y1
    bottom_y = bottom.position('y')
    // place inner names below regions
    bottom.position('y', bottom_y + (middle_y2 - bottom_y1) + minimum_space)
    bottom.position('x', top_.position('x'))
}

function renderAll() {


    renderPlaces();
    renderRegions();
    renderNames();
    window.cy.center();
    /*
t = 0;

renderPlaces();
setTimeout((e) => {
  renderRegions();
  setTimeout((e) => {
    renderNames();
    setTimeout((e) => {
      cy.center()
    }, t)
  }, t)
}, t)
*/
}

// We are inserting a hyper-edge, even if it is not necessary for cases where
// the connects only two ports/names. This way conform directly to the
// metamodel and don't need to do reformatting before transformation. It is
// assumed, that both nodes have no prior edges (see caller).
function insert_hyper_edge(source_node, target_node, renderedPosition = undefined) {
    var junction = cy.add({group: 'nodes', classes: 'hyperedge-junction', renderedPosition: renderedPosition})
    cy.add({group: 'edges', data: {source: junction.id(), target: source_node.id()}, classes: 'bigraph-edge'})
    cy.add({group: 'edges', data: {source: junction.id(), target: target_node.id()}, classes: 'bigraph-edge'})
}

/* can be expressed by insert-edge and merge-junctions
function reuse_one_hyper_edge(source_node, target_node){
  // Which one is already connected to a (hyper) edge?
  if (source_node.connectedEdges('.bigraph-edge') == 1){
    var junction = source_node.connectedEdges('.bigraph-edge').source()
    var blank_node = target_node
  } else {
    var blank_node = source_node
    var junction = target_node.connectedEdges('.bigraph-edge').source()
  }
  cy.add({group: 'edges', data: {source: junction.id(), target: blank_node.id()}, classes: 'bigraph-edge'})
}
*/

// Connect two ports/names that each are connected to a junction via an edge.
function merge_two_hyper_edges(source_node, target_node) {
    console.debug("merging hyper-edges…")
    // We basically assume, that a port/name is only ever connected to at most
    // one edge − let's add extra safety by checking for the bigraph-edge class.
    // The source of the edge connecting port/name and junction is always the
    // node (cy) representing the junction.
    var junction_source_node = source_node.connectedEdges('.bigraph-edge').source()
    var junction_target_node = target_node.connectedEdges('.bigraph-edge').source()
    //console.debug("junction source: ", junction_source_node.id())

    // Check if both ports/names are already connected to the same hyperedge.
    if (junction_source_node.id() === junction_target_node.id()) {
        console.info("already connected, do nothing")
    } else {

        console.debug("taking all connections of the hyper edge junction the source node is connected to and move them to the hyper edge junction the target node is connected to")
        for (e of junction_source_node.connectedEdges('.bigraph-edge')) {
            console.debug("edge ", e.id(), " connected to ", junction_source_node.id())
            // Get the port/name the edge 'e' connects to the junction.
            port_or_name = e.target()
            e.remove()
            if (junction_target_node.edgesWith(port_or_name).length > 0) {
                console.debug("hyper-edge junction and port/name already connected, skipping")
            } else {
                cy.add({
                    group: 'edges',
                    data: {source: junction_target_node.id(), target: port_or_name.id()},
                    classes: 'bigraph-edge'
                })
            }
        }

        console.debug("all connections moved, removing souperflous hyper-edge junction")
        junction_source_node.remove()
    }
}

// Check if the node is an outer name or is connected to one.
function check_outer_names(node) {
    // Node has at most one edge, which is the one connection it to a hyper-edge
    // junction − the hyper-junction node is always the source of this edge.
    var junction_node = node.connectedEdges('.bigraph-edge').source()


    if (node.classes() === 'outer-name') {
        console.debug(node.id(), " is outer name")
        return true
    } else if (junction_node) {
        console.debug("node is connected to hyperedge, check neighborhood for outer-names")
        if (junction_node.neighborhood('.outer-name').length > 0) {
            console.debug("outer names present")
            return true
        } else {
            console.debug("no outer names here")
            return false
        }
    } else {
        console.debug(node.id(), " is no outer name and is not connected to any other nodes that might be outer names")
        return false
    }

}

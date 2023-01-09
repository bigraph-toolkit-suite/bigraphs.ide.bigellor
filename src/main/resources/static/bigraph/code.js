//import cytoscape from 'cytoscape';
//import coseBilkent from 'cytoscape-cose-bilkent';
//import d3Force from "node_modules/cytoscape-d3-force/cytoscape-d3-force.js";
//import 'lib/cytoscape-automove.js';
//import * as bla from 'lib/cytoscape-automove.js';
//cytoscape.use( d3Force );

var selectedControlOutside = window.selectedControlOutside = {
    ctrlLabel: undefined,
    portCount: undefined,
    status: undefined,
    setValues: function (ctrlLabel, portCount, status) {
        this.ctrlLabel = ctrlLabel;
        this.portCount = portCount;
        this.status = status;
        console.log("set values", ctrlLabel, portCount, status);
    },
    reset: function () {
        this.ctrlLabel = undefined;
        this.portCount = undefined;
        this.status = undefined;
    },
    isUndefined: function () {
        return this.ctrlLabel === undefined ||
            this.portCount === undefined ||
            this.status === undefined;
    }
};

var mySingleton = (function () {

    // Instance stores a reference to the Singleton
    var instance;

    function init() {

        // Private methods and variables
        // var name_counter = 1;
        var privateVariable = "Im also private";
        var privateRandomNumber = Math.random();
        var cy;
        var tappedBefore;
        var tappedTimeout;
        var eh;
        var edgeCreationGestureInProgess = false;

        function privateMethod() {
            console.log("I am private");
        }

        return {
            // Public methods and variables
            publicProperty: "I am also public",
            // getAndIncrementNodeIdCounter: function () {
            //     return name_counter++;
            // },

            getCytoscapeObject: function () {
                return cy;
            },

            initCytoscape: function (param_elements = undefined) {

                let theElements = (param_elements !== undefined) ?
                    param_elements :
                    {
                        nodes: [],
                        edges: []
                    };

                // Add base containers for outer, inner names and regions
                if (theElements.nodes !== undefined) {
                    theElements.nodes.push({group: 'nodes', data: {id: 'outer-names'}, classes: 'name-container'})
                    theElements.nodes.push({group: 'nodes', data: {id: 'regions'}, classes: 'name-container'})
                    theElements.nodes.push({group: 'nodes', data: {id: 'inner-names'}, classes: 'name-container'})
                } else {
                    theElements.push({group: 'nodes', data: {id: 'outer-names'}, classes: 'name-container'})
                    theElements.push({group: 'nodes', data: {id: 'regions'}, classes: 'name-container'})
                    theElements.push({group: 'nodes', data: {id: 'inner-names'}, classes: 'name-container'})
                }

                // console.log("theElements", theElements);
                cy = cytoscape({
                    container: document.getElementById('cy-container'),

                    boxSelectionEnabled: false,
                    // autounselectify: true,
                    minZoom: 0.2,
                    maxZoom: 2,
                    wheelSensitivity: 0.2,

                    style: [
                        // Styles are applied from top to bottom − if style closer to the bottom
                        // matches a node but does not set an attribute, a matching style closer to
                        // the top will be applied for that attribute; those are set in the style
                        // closer to the bottom will override those from the style closer to the
                        // top; all assuming both styles apply to the node under consideration
                        // (node meaning the graphical element in cytoscape).
                        {
                            selector: '.eh-handle',
                            css: {
                                'background-color': '#ff1265',
                                'border-color': 'black',
                                'border-width': 2,
                                'width': '10%',
                                'height': '10%',
                            }
                        },

                        { // cytoscape nodes
                            selector: 'node',
                            css: {
                                //'content': 'data(id)',
                                //'text-valign': 'center',
                                //'text-halign': 'center',
                                //'width': 'label',
                                //'height': 'label',
                                //'background-color': 'white',
                                //'shape': 'round-rectangle',
                                //'border-width': 2,
                                //'border-color': 'red',
                                //'background-color': 'green'
                                //'padding': 0,
                                'ghost': 'no',
                                'ghost-offset-x': 1,
                                'ghost-offset-y': 1,
                                'ghost-opacity': 1,
                            }
                        },

                        {
                            selector: 'edge',
                            css: {
                                'curve-style': 'unbundled-bezier',
                                //'curve-style': 'bezier',
                                'line-color': 'green',
                                'width': 2,
                                /*
                                'source-arrow-shape': 'circle',
                                'target-arrow-shape': 'circle',
                                'source-arrow-color': 'black',
                                'target-arrow-color': 'black',
                                */
                                //'source-distance-from-node': -10,
                                //'target-endpoint': 'inside-to-node',
                                'target-endpoint': 'outside-to-line',
                            }
                        },
                        {
                            selector: '.hyperedge-junction',
                            css: {
                                //'shape': 'diamond',
                                'width': 4,
                                'height': 4,
                                'background-color': 'green',
                                'border-width': 1,
                            }
                        },
                        {
                            selector: '.port',
                            css: {
                                'label': (node) => node.data('index'),
                                width: '7%',
                                height: '7%',
                                'ghost': 'no',
                                'background-color': 'black',
                            }
                        },
                        {
                            selector: '.outer-name',
                            css: {
                                'border-width': 2,
                                'label': "data(name)", // 'label': (node) => node.data('id').substr(1),
                                'font-style': 'italic',
                                'text-valign': 'center',
                                'text-halign': 'center',
                                'ghost': 'no',
                                'background-color': '#25dbc7',
                                'color': 'white',
                                'border-color': '#20cdc2',
                            }
                        },
                        {
                            selector: '.inner-name',
                            css: {
                                'border-width': 2,
                                'label': "data(name)", // 'label': (node) => node.data('id').substr(1),
                                'font-style': 'italic',
                                'text-valign': 'center',
                                'text-halign': 'center',
                                'ghost': 'no',
                                'background-color': '#20cdc2',
                                'border-color': '#25dbc7',
                                'color': 'white',
                            }
                        },
                        { // must be placed after '.outer-name' and '.port', otherwise this style
                            // does not affect them; pertains to the extension
                            // 'cytoscape-edgehandles' that is used to create edges (i.e. bigraph
                            // edges)
                            selector: '.eh-target',
                            css: {
                                'background-color': '#ff1265',
                            }
                        },

                        {
                            selector: ':parent',
                            css: {
                                'text-valign': 'top',
                                'text-halign': 'center',
                                'border-width': 2,
                                'shape': 'roundrectangle',
                                //'shape': 'cutrectangle',
                                'border-color': 'black',
                                //'padding': 30,
                            }
                        },
                        { // has to go after ':parent', so that the border-color is applied to
                            // this class
                            selector: '.name-container',
                            css: {
                                'padding': 10,
                                'shape': 'roundrectangle',
                                'min-width': 55,
                                // even if lightgray is the default color, it needs to be set in order
                                // to override the 'violet' from '.eh-target', making the wrong
                                // impression of '.name-container' being a suitable target for edges
                                'background-color': '#f5f5f5',
                                'border-width': 2,
                                'border-color': '#aa3bbe',
                            }
                        },
                        { // Some cytoscape nodes will be classed '.node', indicating it serves as
                            // node from bigraph perspective.
                            selector: '.node',
                            css: {
                                //'shape': 'round-rectangle',
                                'label': (node) => node.data('ctrlLabel') + ':' + node.data('id'),
                                'border-width': 1,
                                'background-color': 'white',
                                'border-color': 'black',
                            }
                        },
                        {
                            selector: '.site',
                            css: {
                                'background-color': 'darkgray',
                                'border-style': 'dashed',
                                'shape': 'round-rectangle',
                                'border-width': 1,
                                'padding': 10,
                                //'label': 'data(id)',
                                // 'label': (node) => node.data('id').substr(1),
                                'label': (node) => node.data('index'),
                                'text-valign': 'center',
                                'text-halign': 'center',
                                'ghost-offset-x': -3,
                                'ghost-offset-y': -3,
                                'ghost': 'no',
                            }
                        },

                        {
                            selector: '.region',
                            css: {
                                'background-color': 'white',
                                'border-style': 'dashed',
                                'border-width': 1,
                                'shape': 'round-rectangle',
                                'padding': 20,
                                'label': (node) => node.data('index'),
                                // 'label': 'data(index)',
                                //'label': 'data(id)',
                                'text-valign': 'top',
                                'text-halign': 'left',
                                'text-margin-y': 25,
                                'text-margin-x': 20,
                                'ghost': 'no',
                                'ghost-offset-x': 3,
                                'ghost-offset-y': 3,
                                'ghost-opacity': 1,
                            }
                        },
                        {
                            selector: '.padding-container',
                            css: {
                                'position': 'absolute',
                                'bottom': '0',
                                'padding': 5,
                                'background-color': 'pink',
                                'border-width': 0,
                            }
                        },
                        { // needs to be placed after all the other elements, otherwise style is not applied
                            // during run time to a selected element
                            selector: ':selected',
                            css: {
                                'line-color': '#fefefe',
                                'border-color': '#fefefe',
                                'background-color': '#e4edd3',
                            }
                        },
                    ],

                    elements: theElements,

                    /*  layout: {
                        name: 'preset',
                        //padding: 5
                      }*/

                    /*
                                        layout: {
                                            name: 'd3-force',
                                animate: true,
                                linkId: function id(d) {
                                  return d.id;
                                },
                                linkDistance: 80,
                                manyBodyStrength: -300,
                                ready: function(){},
                                stop: function(){},
                                tick: function(progress) {console.log('progress - ', progress);},
                                randomize: true,
                                infinite: true
                                            // some more options here...
                                        },
                    */

                });

                cy.resize();
                cy.fit();
                cy.autounselectify(true)

                //set up containers
                // cy.add({group: 'nodes', data: {id: 'outer-names'}, classes: 'name-container'})
                // cy.add({group: 'nodes', data: {id: 'regions'}, classes: 'name-container'})
                // cy.add({group: 'nodes', data: {id: 'inner-names'}, classes: 'name-container'})

                return cy;
            },

            initEvents: function () {
                cy.on('cxttap', '.bigraph-edge', function (evt) {
                    console.debug("right click on edge, deleting");
                    var junction_edges = evt.target.source().connectedEdges()
                    if (junction_edges.length <= 2) {
                        console.debug("only two edges left on the junction, removing all")
                        evt.target.source().remove();
                    } else {
                        console.debug("hyper-edge has more than two edges, only removing the one clicked on")
                        evt.target.remove();
                    }
                });

                cy.on('cxttap', '.hyperedge-junction', function (evt) {
                    console.debug("right click on junction, deleting")
                    evt.target.remove();
                });

                cy.on('cxttap', '.region, .site, .node', function (evt) {
                    console.log("evt.target.hasClass(\"node\")", evt.target.hasClass("node"));
                    if (evt.target.hasClass("node")) {
                        // console.log("evt.target", evt.target);
                        // console.log("evt.target.children", evt.target.children());
                        // console.log("evt.target.children.port", evt.target.children(".port"));
                        var ports = evt.target.children(".port");
                        if (ports.length > 0) {
                            for (var ix = 0; ix < ports.length; ix++) {
                                var port = ports[ix]; // should only have one
                                // console.log("port", port);
                                // console.log("port.connectedEdges()", port.connectedEdges());
                                if (port.connectedEdges().length > 0) {
                                    // console.log("port.connectedEdges()", port.connectedEdges()[0]);
                                    var edge = port.connectedEdges()[0];
                                    // console.log("port s", edge.source().hasClass("hyperedge-junction"));
                                    if (edge.source().hasClass("hyperedge-junction")) {
                                        var hejunction = edge.source();
                                        if (hejunction.connectedEdges().length <= 1) {
                                            hejunction.remove();
                                        }
                                    }
                                }
                            }
                            evt.target.connectedEdges().remove() // remove connecting edge
                        }
                    }
                    evt.target.remove();
                });

                cy.on('cxttap', '.outer-name, .inner-name', function (evt) {
                    console.log("evt.target.connectedEdges()", evt.target.connectedEdges()[0]);
                    if (evt.target.connectedEdges().length > 0) {
                        var edge = evt.target.connectedEdges()[0]; // should only have one
                        // console.log("edge", edge);
                        // console.log("edge source", edge.source());//hyperedge junction
                        // console.log("edge target", edge.target()); //the outer name
                        // console.log("edge.source()", edge.source());
                        // Check if hyperedge-junction has other edges -> if not remove also this empty edge
                        if (edge.source().hasClass("hyperedge-junction")) {
                            var hejunction = edge.source();
                            // console.log("hejunction", hejunction);
                            // console.log("hejunction.connectedEdges()", hejunction.connectedEdges());
                            if (hejunction.connectedEdges().length <= 1) {
                                hejunction.remove();
                            }
                        }
                        evt.target.connectedEdges().remove() // remove connecting edge
                    }
                    evt.target.remove() // remove element finally
                    // evt.target.connectedEdges().animate({
                    //     style: {lineColor: "red"}
                    // })

                });

// get the name of a port by pointing at it
//                 cy.on('mouseover', '.port', function (evt) {
//                     console.log(evt.target.id());
//                 });
//cy.on('tap', '#cy', function(evt){console.log("linksclick auf hintergrund");});

                // Hide the edge-handle when not in use
                cy.on('mouseout', '.node, #outer-names, #inner-names', function (event) {
                    // console.log("mouse out on", event.target);
                    if (edgeCreationGestureInProgess === false) {
                        eh.hide();
                    }
                });

                cy.on('tap', function (event) {
                    // target holds a reference to the originator
                    // of the event (core or element)
                    var evtTarget = event.target;

                    if (evtTarget === cy) {
                        console.log('tap on background');
                        console.log("pos: ", event.position)
                        console.log("rend. pos: ", event.renderedPosition)
                        eh.hide(); // hide edge handles
                    } else {
                        //console.log('tap on some element');
                    }
                });

                cy.on('tap', function (event) {
                    var tappedNow = event.target;
                    // second click of the double click
                    if (tappedTimeout && tappedBefore) {
                        clearTimeout(tappedTimeout);
                    }
                    // first click of the double click sequence
                    if (tappedBefore === tappedNow) {
                        tappedNow.trigger('doubleTap', [event.renderedPosition]);
                        tappedBefore = null;
                    } else {
                        tappedTimeout = setTimeout(function () {
                            tappedBefore = null;
                            console.debug("just single click");
                            tappedNow.trigger('mySingleTap', [event.renderedPosition])
                        }, 300);
                        tappedBefore = tappedNow;
                    }
                });

                // Try to nest selected node/site into clicked node
                cy.on('mySingleTap', '.region, .node, .site', function (evt, rpos) {
                        var selected = cy.$(':selected')
                        console.log("selected node in CY is:", selected);
                        var target = evt.target
                        if (selected.length === 0) {
                            console.debug("no region/node to renest selected, selecting clicked on node")
                            cy.autounselectify(false)
                            target.select()
                            cy.autounselectify(true)
                        } else {
                            console.debug("try to place ", selected.id(), " in ", evt.target.id(), "...");
                            console.debug("selected // target", selected, selected.classes(), target);
                            if (selected.classes().includes("region") ||
                                (selected.classes().includes("site") && target.classes().includes("site"))
                            ) { // abort here because roots cannot be nested in nodes
                                cy.autounselectify(false)
                                selected.unselect()
                                cy.autounselectify(true)
                                return;
                            }

                            //console.debug("node/site to re-nest: ", cy.$(':selected').id());
                            //console.debug("target of nesting: ", evt.target.id());
                            //console.debug("")
                            //if (cy.$(':selected').isParent(evt.target)) {
                            if (selected.descendants().union(selected).contains(target)) {
                                console.debug("try to nest node into one of its child-nodes, aborting")
                            } else {
                                console.debug("unrelated, start nesting")
                                selected.move({parent: evt.target.id()})
                                // By default, the new parent jumps to the position of its new child,
                                // let's reposition
                                //selected.position(evt.position)
                                selected.renderedPosition(rpos)
                                console.debug("clicked on pos: ", rpos)

                            }
                            // TODO: deselect should also happen if clicked somewhere else than nodes
                            // and sites (bigraph).
                            console.debug("deselect ", selected.id())
                            cy.autounselectify(false)
                            selected.unselect()
                            cy.autounselectify(true)
                        }
                    }
                );


                cy.on('doubleTap', '.region, .node',
                    function (evt, rpos) {
                        cy.resize()
                        console.log("double click on node or region, inserting new node");
                        console.log("id: ", evt.target.id());
                        console.log("rpos: ", rpos);

                        var target = evt.target

                        if (!selectedControlOutside.isUndefined()) { //NEW
                            // let oldCnt = name_counter;
                            var new_index = Math.max(...cy.$('.node').map(e => e.data('id').substr(1)).concat(['0'])) + 1
                            console.log("new_index", new_index);
                            cy.add(createNode(selectedControlOutside.ctrlLabel, new_index, target.id(), rpos))
                            if (selectedControlOutside.portCount > 0) {
                                for (let i = 0; i < selectedControlOutside.portCount; i++) {
                                    cy.add(createPort(i, 'v' + new_index, rpos))
                                    rpos.x += 10;
                                }
                            }
                        } else {
                            var new_index = Math.max(...cy.$('.site').map(e => e.data('index')).concat(['0'])) + 1
                            cy.add(createSite(new_index, target.id(), rpos))
                        }
                    }
                );

                // Insert region when double clicking on the visual compound node containing all the regions
                cy.on('doubleTap', '#regions',
                    function (evt, rpos) {
                        console.debug("creating new region…")
                        var new_index = Math.max(...cy.$('.region').map(e => e.data('index')).concat(['0'])) + 1
                        console.debug("max is: ", new_index)
                        cy.add({
                            group: 'nodes',
                            data: {parent: evt.target.id(), index: new_index},
                            renderedPosition: rpos,
                            classes: 'region',
                        })

                    }
                )
                // outer-names
                // inner-names
                cy.on('doubleTap', '#outer-names',
                    function (evt, rpos) {
                        console.debug("creating new outer name")
                        cy.add(createOuterName(createRandomString(), rpos))
                    }
                )
                cy.on('doubleTap', '#inner-names',
                    function (evt, rpos) {
                        console.debug("creating new inner name")
                        cy.add(createInnerName(createRandomString(), rpos))
                    }
                )
                cy.on('doubleTap', '.outer-name, .inner-name',
                    function (evt, rpos) {
                        var target = evt.target
                        // if (selected.length !== 0) {
                        console.log("double tap on outer or inner name...", evt)
                        var selected = cy.$(':selected')
                        $('#modal-changeLinkLabel #formControlInputLinkLabel').val(target.data("name"))
                        $("#formControlInputLinkLabelSaveChanges").click(function () {
                            var field1value = $("#formControlInputLinkLabel").val()
                            console.log("Modal submitted with text: ", field1value);
                            var prefix = "o";
                            if (target.classes().includes("outer-name")) {
                                prefix = "o";
                            } else if (target.classes().includes("inner-name")) {
                                prefix = "i";
                            }
                            target.data("name", field1value)
                            // target.data("label", field1value)
                            // target.data("id", prefix + field1value)
                            console.log("target", target)
                            // selected.data("name", field1value)
                            // selected.data("id", prefix + field1value)
                            $('#modal-changeLinkLabel').modal('hide');
                            // cy.resize();
                            // cy.fit();
                            cy.autounselectify(true)
                            // selected.removeClass('outer-name').addClass('outer-name')
                            $("#formControlInputLinkLabelSaveChanges").unbind("click")
                        });
                        $('#modal-changeLinkLabel').modal('show');
                        // }
                    });

                // createRandomString

                eh = cy.edgehandles({
                    handleNodes: '.port, .outer-name, .inner-name',
                    preview: false,
                    //edgeType: function(x,y){return 'node';},
                })

                // cy.on('ehhide', (event, sourceNode) => {
                //     console.log("EHAndle hide event", event);
                //     console.log("EHAndle hide event", sourceNode);
                // });
                // cy.on('ehshow', (event, sourceNode) => {
                //     console.log("EHAndle show event", event);
                //     console.log("EHAndle show event", sourceNode);
                // });
                cy.on('ehstart', (event, sourceNode) => {
                    edgeCreationGestureInProgess = true;
                });
                // We already check that the source node is a port or an outer-name, but we
                // also need to verify that the target node is correct: cannot connect port to
                // port, or outer-name to outer-name. Also handle the insertion
                cy.on('ehcomplete', (event, source_node, target_node, inserted_edge) => {
                    // In the best case we keep the edge create by cytoscape-edgehandles,
                    // otherwise we need to find the edge later in order to delete it, therefore
                    // using the class 'maybe-delete' as tag.
                    console.debug("edge drawn with id: ", inserted_edge.id(), ", and class: ", inserted_edge.addClass('maybe-delete bigraph-edge'));
                    inserted_edge.remove();
                    console.debug("... and deleted");
                    var source_class = source_node.classes()[0];
                    var target_class = target_node.classes()[0];
                    console.debug("classes source node: ", source_class);
                    console.debug("classes target node: ", target_class);
                    if (source_class === "outer-name" && target_class === "outer-name") {
                        return true;
                    }

                    // Let's perform some checks to ensure that only the correct entities are
                    // connected (besides already restriction to names and ports).

                    //ttt = [for (point of ['port', 'outer-name', 'inner-name']) for (x of target_node.classes()) true if point == x]
                    // so cytoscape-edgehandles created an edge: let's check if it actually needs
                    // to be a hyper-edge by checking if any of the both points to connect
                    // already has edges; the '[0]' means we assume the target_node has only one
                    // class indicating what type of bigraph element it is (name, port, …)
                    //
                    // We restrict the types of possible source nodes when starting the
                    // edgehandles extension. Now we need to perform the same check for the
                    // target node.
                    if (['port', 'outer-name', 'inner-name'].includes(target_class)) {
                        console.debug("check if source or target already has edges and thus a hyperedge has to be inserted…")
                        console.debug("source_node with id ", source_node.id(), " has the following edges:")
                        //var s_edges = source_node.connectedEdges('.bigraph-edge').not('.maybe-delete')
                        var s_edges = source_node.connectedEdges('.bigraph-edge')
                        console.debug(s_edges)
                        console.debug("target_node with id ", target_node.id(), " has the following edges:")
                        //var t_edges = target_node.connectedEdges('.bigraph-edge').not('.maybe-delete')
                        var t_edges = target_node.connectedEdges('.bigraph-edge')
                        console.debug(t_edges)
                        /*
                            if (source_class == 'outer-name' && target_class == 'outer-name') {
                              console.debug("Trying to connect two outer names − they are links and cannot be connected to each other but only to points (port or inner name), aborting.")
                            } else */
                        if (check_outer_names(source_node) && check_outer_names(target_node)) {
                            console.debug("Trying to connect two nodes of which each is either an outer name or connected to one, aborting")

                        } else if (s_edges.length + t_edges.length === 0) {
                            console.debug("both nodes have no edges, it's save to just insert a flat edge (inserting hyper edge with two ends anyways)")

                            // low level routine, performs no further checks
                            insert_hyper_edge(source_node, target_node, event.position)

                        } else if (s_edges.length + t_edges.length === 1) {

                            console.debug("At least one of source or target already has an edge, inserting new edge anyways and merge their junctions afterwards")
                            insert_hyper_edge(source_node, target_node, event.position)
                            merge_two_hyper_edges(source_node, target_node)

                        } else if (s_edges.length === 1 && t_edges.length === 1) {

                            console.debug("both ports/names already have an edge each, merging their junctions")
                            merge_two_hyper_edges(source_node, target_node)

                        } else {
                            console.debug("each of the to be connected nodes should have at most one edge, please check:")
                            console.debug("s_edges.length: ", s_edges.length)
                            console.debug("t_edges.length: ", t_edges.length)
                        }

                    } else {
                        console.debug("not matching name or port, not inserting an edge")
                    }

                    edgeCreationGestureInProgess = false;
                });
            },
        };

    }

    return {
        getInstance: function (param_elements = undefined) {
            if (!instance) {
                instance = init(param_elements = undefined);
            }
            return instance;
        }
    };
})();

var singleA = window.singleA = mySingleton.getInstance();

// {group: 'nodes', data: {id: 'r0', parent: 'regions', index: "1"}, classes: ['region']}
function createRegion(newRegionIndex) {
    return {
        group: 'nodes',
        data: {id: 'r' + newRegionIndex, parent: 'regions', index: '' + newRegionIndex},
        classes: ['region'],
        selectable: false
    };
}

function createSite(newSiteIndex, parentId, renderedPosition = undefined) {
    return {
        group: 'nodes', data: {
            id: 's' + newSiteIndex, parent: parentId, index: '' + newSiteIndex, type: 'site'
        }, classes: ['site'],
        renderedPosition: renderedPosition
    };
}

function createNode(ctrlLabel, newNodeId, parentId, renderedPosition = undefined) {
    return {
        group: 'nodes',
        data: {
            id: 'v' + newNodeId, ctrlLabel: ctrlLabel, parent: parentId, type: 'node'
        },
        classes: ['node'],
        renderedPosition: renderedPosition
    };
}

function createPort(portIndex, forNodeId, rPos = undefined) {
    return {
        group: 'nodes',
        data: {
            id: 'p-' + forNodeId + '-' + portIndex,
            parent: forNodeId,
            index: '' + portIndex,
            type: 'port'
        },
        renderedPosition: rPos,
        classes: 'port'
    };
}

var createOuterName = function (outerName, renderedPosition = undefined) {
    return {
        group: 'nodes',
        data: {id: 'o' + outerName, parent: 'outer-names', name: outerName},
        classes: 'outer-name',
        renderedPosition: renderedPosition
    };
}

function createInnerName(innerName, renderedPosition = undefined) {
    return {
        group: 'nodes',
        data: {id: 'i' + innerName, parent: 'inner-names', name: innerName},
        classes: 'inner-name',
        renderedPosition: renderedPosition
    };
}

function createRandomString(letterCnt = 2) {
    var text = "";
    var possible = "abcdefghijklmnopqrstuvwxyz";

    for (var i = 0; i < letterCnt; i++)
        text += possible.charAt(Math.floor(Math.random() * possible.length));
    return text;
}

//outer names
// cy.add({group: 'nodes', data: {id: 'oy', parent: 'outer-names'}, classes: 'outer-name'})
// cy.add({group: 'nodes', data: {id: 'ox', parent: 'outer-names'}, classes: 'outer-name'})
// cy.add(createOuterName("y")); //NEW
// cy.add(createOuterName("x")); //NEW

//regions
//cy.add({group: 'nodes', data: {id: 'pc1', parent: 'regions'}, classes: 'padding-container'})
//cy.add({group: 'nodes', data: {id: 'r2', parent: 'pc1', index: "0"}, classes: 'region', selectable: false})

// cy.add({group: 'nodes', data: {id: 'r0', parent: 'regions', index: "1"}, classes: ['region']})
// cy.add({group: 'nodes', data: {id: 'r1', index: "2", parent: 'regions'}, classes: ['region']})
// cy.add({group: 'nodes', data: {id: 'r2', index: "0", parent: 'regions'}, classes: 'region', selectable: false})
// cy.add(createRegion(0))
// cy.add(createRegion(1))
// cy.add(createRegion(2))


// cy.add({group: 'nodes', data: {id: 'A', parent: 'r0'}, classes: ['node']})
// cy.add({group: 'nodes', data: {id: 'C', parent: 'r0'}, classes: ['node']})
// cy.add({group: 'nodes', data: {id: 'B', parent: 'A'}, classes: 'node'})
// cy.add({group: 'nodes', data: {id: 's0', parent: 'A'}, classes: ['site']})
// cy.add({group: 'nodes', data: {id: 's1', parent: 'A'}, classes: ['site']})
// cy.add(createNode("A", singleA.getAndIncrementNodeIdCounter(), "r0"));
// cy.add(createNode("B", singleA.getAndIncrementNodeIdCounter(), "v1"));
// cy.add(createNode("C", singleA.getAndIncrementNodeIdCounter(), "r0"));
// cy.add(createSite(0, "v1"))
// cy.add(createSite(1, "v1"))
//cy.add({group: 'nodes', data: {id: 'pc', parent: 'r2'}, classes: 'padding-container'})
//cy.add({group: 'nodes', data: {id: 'D', parent: 'pc'}, classes: ['node']})


// cy.add({group: 'nodes', data: {id: 'D', parent: 'r1'}, classes: ['node']})
// cy.add(createNode("D", singleA.getAndIncrementNodeIdCounter(), "r1"));

//cy.add({group: 'nodes', data: {id: 'D', parent: 'r1'}, classes: ['node']})
// cy.add({group: 'nodes', data: {id: 'p1', parent: 'D'}, classes: 'port'})
// cy.add({group: 'nodes', data: {id: 'p2', parent: 'D'}, classes: 'port'})
// cy.add({group: 'nodes', data: {id: 'p3', parent: 'D'}, classes: 'port'})
// cy.add(createPort(0, 'v4'))
// cy.add(createPort(1, 'v4'))
// cy.add(createPort(2, 'v4'))

// padding container garbage to illustrate the more complex approach that allows ports to sit on the 
// edge of their parent
// cy.add({group: 'nodes', data: {id: 'pc2', parent: 'D'}, classes: 'padding-container'})
// cy.add({group: 'nodes', data: {id: 'E', parent: 'D'}, classes: 'node'})
// cy.add(createNode("E", singleA.getAndIncrementNodeIdCounter(), "v4"));

//sites
// cy.add({group: 'nodes', data: {id: 'iz', parent: 'inner-names'}, classes: 'inner-name'})
// cy.add({group: 'nodes', data: {id: 'iq', parent: 'inner-names'}, classes: 'inner-name'})
// cy.add({group: 'nodes', data: {id: 'in', parent: 'inner-names'}, classes: 'inner-name'})
// cy.add(createInnerName("z"))
// cy.add(createInnerName("q"))
// cy.add(createInnerName("n"))

// renderAll()

//cy.layout({name: 'fcose', fit: 'false'}).run();
//cy.layout({name: 'random'}).run()
//cy.layout({name: 'cose-bilkent'}).run()
//cy.layout({name: 'grid'}).run()
//cy.layout({name: 'd3-force'}).run()
// layout that is fast an handles compound nodes, so that no overlap occurs
// that looks like a node is a child of a compound node while they just
// overlap
//cy.layout({name: 'fcose', fit: 'false'}).run();
// cytoscape extension: 'no-overlap', https://github.com/mo0om/cytoscape-no-overlap
//cy.nodes().noOverlap({padding: 5});
// cytoscape extension: 'automove', https://github.com/cytoscape/cytoscape.js-automove
// create constraints to enforce position between specific nodes
/*
cy.automove({
    nodesMatching: cy.$('.region'),
    reposition: function( node ){
						var pos = node.position();

						if( node.grabbed() ){ return pos; }

            var otherNode = cy.$('.region').not( node );

						return {
							x: pos.x,
							y: otherNode.position('y')
						};
					},
					when: 'matching'
});
*/
/*
cy.on('tap', function(event){
  console.log("rend. pos: ", event.renderedPosition);
  console.log("XXXXX")
})
*/
//cy.on('tap', '.region', function(evt){console.log("linksclick auf region");});
//cy.on('cxttap', function(evt){console.debug("rechtsclick");});
//var cy = $('#cy').cytoscape('get');
//https://stackoverflow.com/questions/18610621/cytoscape-js-check-for-double-click-on-nodes
/*
cy.on('doubleTap', function(event){
  console.log("YYYYYY")
  console.log(event.position)
})*/
/*
cy.on('select', (evt) => {
  console.log("select evt: ", evt.target.id())
})*/
//eh.enableDrawMode()
// reset port name
//cy.autolock(true);
//cy.fit()
/*
cy.$('.region').layout(
					 {
						name: 'd3-force',
            animate: true,
            linkId: function id(d) {
              return d.id;
            },
            linkDistance: 80,
            manyBodyStrength: -300,
            ready: function(){},
            stop: function(){},
            //tick: function(progress) {console.log('progress - ', progress);},
            randomize: true,
            //infinite: true
						// some more options here...
}).run()
*/
//d3.forceSimulation()
// node.layoutDimensions()
// https://js.cytoscape.org/#style/edge-endpoints
// export {name_counter};
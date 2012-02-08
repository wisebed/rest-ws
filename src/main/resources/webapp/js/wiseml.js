/**
 * Parses WiseML
 * 
 */
var WiseMLParser = function(wisemlParameter, parentDiv) {
	this.wiseml = wisemlParameter;
	this.origin;
	this.predLat = "http://www.w3.org/2003/01/geo/wgs84_pos#lat";
	this.predLong = "http://www.w3.org/2003/01/geo/wgs84_pos#long";
	this.nodes = new Array();
	this.markersArray;
	this.map;

	this.view = null;

	this.parse();
	this.buildView(parentDiv);

};

WiseMLParser.prototype.parse = function() {

	// Calculate origin
	var wiseMLOrigin = new Coordinate(this.wiseml.setup.origin.x,
			this.wiseml.setup.origin.y, this.wiseml.setup.origin.z,
			this.wiseml.setup.origin.phi, this.wiseml.setup.origin.theta);

	this.origin = coordinates.blh2xyz(wiseMLOrigin);

	// Parse every node
	for (key in this.wiseml.setup.node) {
		var node = this.wiseml.setup.node[key];
		if (node.position)
			this.parseNode(node);
	}

};

WiseMLParser.prototype.parseNode = function(node) {
	var id = this.convertIdToIp(node.id);

	var rotCo = coordinates.rotate(new Coordinate(node.position.x,
			node.position.y, node.position.z, this.origin.phi,
			this.origin.theta), this.origin.phi);

	var absCo = coordinates.absolute(this.origin, rotCo);
	var finalCo = coordinates.xyz2blh(absCo);
	var n = new Node(id, node.description, finalCo);

	this.nodes.push(n);
}

WiseMLParser.prototype.convertIdToIp = function(id) {
	// id = id.substring(id.lastIndexOf(":")+3);
	return id;
}

WiseMLParser.prototype.buildView = function(parentDiv) {
	this.view = $("<div style=\"width: 900px; height:1000px;\"/>");
	parentDiv.append(this.view);

	this.initMap();

	// Delete old overlays
	this.deleteOverlays();

	var self = this;
	$.each(this.nodes, function(index, node) {
		self.addMarker(node);
	});

	// Adjust map
	this.setBounds();
}

/**
 * Adds a Marker to the map
 * 
 */
WiseMLParser.prototype.addMarker = function(n) {
	var markerLatLng = new google.maps.LatLng(n.c.x, n.c.y);

	var marker = new google.maps.Marker({
		position : markerLatLng,
		map : this.map,
		title : "Sensor: " + n.id
	});

	var infowindow = new google.maps.InfoWindow({
		content : "<h2>Sensor: " + n.id + "</h2><p>" + n.desc + "</p>"
	});

	var self = this;
	google.maps.event.addListener(marker, 'click', function() {
		infowindow.open(self.map, marker);
	});

	this.markersArray.push(marker);
};

/**
 * Initializes the google map
 * 
 */
WiseMLParser.prototype.initMap = function() {
	// House 64
	var latlng = new google.maps.LatLng(53.8340, 10.7043);

	var myOptions = {
		zoom : 17,
		center : latlng,
		mapTypeId : google.maps.MapTypeId.HYBRID
	};

	this.markersArray = new Array();

	this.map = new google.maps.Map(this.view.get()[0], myOptions);
};

/**
 * Deletes all markers in the array by removing references to them
 * 
 */
WiseMLParser.prototype.deleteOverlays = function() {

	$.each(this.markersArray, function(index, marker) {
		marker.setMap(null);
	});

	this.markersArray = new Array();
};

/**
 * Centers, pans and zooms the map such that all markers are visible
 * 
 */
WiseMLParser.prototype.setBounds = function() {

	if (this.markersArray.length > 1) {
		var bounds = new google.maps.LatLngBounds();
		
		var self = this;
		$.each(this.markersArray, function(index, marker) {
			bounds.extend(marker.getPosition());
		});
		
		this.map.fitBounds(bounds);
		
	} else if (this.markersArray.length == 1) {
		this.map.setCenter(this.markersArray[0].getPosition());
	}
};

/**
 * Produces rdf from the nodes
 * 
 */
WiseMLParser.prototype.rdf = function() {
	var rdf = "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n";

	for ( var j = 0; j < nodes.length; j++) {
		rdf += "<" + nodes[j].id + "> ";
		rdf += "<" + predLat + "> ";
		rdf += "\"" + nodes[j].c.x + "\"^^xsd:double .\n";
		rdf += "<" + nodes[j].id + "> ";
		rdf += "<" + predLong + "> ";
		rdf += "\"" + nodes[j].c.y + "\"^^xsd:double .\n";
	}
	return rdf;
};

/**
 * Represents a location
 * 
 */
function Coordinate(x, y, z, phi, theta) {
	this.x = x;
	this.y = y;
	this.z = z;
	this.phi = phi;
	this.theta = theta;
}

/**
 * Represents a node and its location
 * 
 */
function Node(id, desc, c) {
	this.desc = desc;
	this.id = id;
	this.c = c;
}

/**
 * Helper class with functions for location-calculations
 * 
 */
var coordinates = {
	WGS84_CONST : 298.257222101,
	WGS84_ALPHA : 0.003352810681182319,
	WGS84_A : 6378137.0,
	WGS84_B : 6356752.314140356,
	WGS84_C : 6399593.625864023,

	blh2xyz : function(coordinate) {
		var roh = Math.PI / 180.0;

		var i = (this.WGS84_A * this.WGS84_A) - (this.WGS84_B * this.WGS84_B);
		var e = Math.sqrt(i / (this.WGS84_B * this.WGS84_B));

		var b = coordinate.x * roh;
		var l = coordinate.y * roh;

		var eta2 = e * e * Math.pow(Math.cos(b), 2);
		var v = Math.sqrt(1.0 + eta2);
		var n = this.WGS84_C / v;

		var h = coordinate.z;
		var x = (n + h) * Math.cos(b) * Math.cos(l);
		var y = (n + h) * Math.cos(b) * Math.sin(l);
		var z = (Math.pow(this.WGS84_B / this.WGS84_A, 2) * n + h)
				* Math.sin(b);
		return new Coordinate(x, y, z, coordinate.phi, coordinate.theta);
	},

	xyz2blh : function(coordinate) {
		var x = coordinate.x;
		var y = coordinate.y;
		var z = coordinate.z;

		var roh = 180.0 / Math.PI;

		var e0 = (this.WGS84_A * this.WGS84_A) - (this.WGS84_B * this.WGS84_B);
		var e1 = Math.sqrt(e0 / (this.WGS84_A * this.WGS84_A));
		var e2 = Math.sqrt(e0 / (this.WGS84_B * this.WGS84_B));

		var p = Math.sqrt((x * x) + (y * y));

		var theta = Math.atan((z * this.WGS84_A) / (p * this.WGS84_B));

		var l = Math.atan(y / x) * roh;
		var b = Math
				.atan((z + (e2 * e2 * this.WGS84_B * Math.pow(Math.sin(theta),
						3)))
						/ (p - (e1 * e1 * this.WGS84_A * Math.pow(Math
								.cos(theta), 3))));

		var eta2 = e2 * e2 * Math.pow(Math.cos(b), 2);
		var v = Math.sqrt(1.0 + eta2);
		var n = this.WGS84_C / v;

		var h = (p / Math.cos(b)) - n;
		return new Coordinate(b * roh, l, h, coordinate.phi, coordinate.theta);
	},

	toRad : function(phi) {
		return phi * Math.PI / 180;
	},

	rotate : function(coordinate, phi) {
		var rad = this.toRad(phi);
		var cos = Math.cos(rad);
		var sin = Math.sin(rad);
		var x = coordinate.x * cos - coordinate.y * sin;
		var y = coordinate.y * cos + coordinate.x * sin;
		return new Coordinate(x, y, coordinate.z, coordinate.phi,
				coordinate.theta);
	},

	absolute : function(origin, coordinate) {
		var y = coordinate.y + origin.y;
		var x = coordinate.x + origin.x;
		return new Coordinate(x, y, origin.z, origin.phi, origin.theta);
	}
}

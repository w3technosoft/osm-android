# Introduction #

The OSM android application can be divided into several modules:
  * The reading, storing and interpretation of the binary map data
  * The map engine
  * The rendering module
  * The rounting module


# Details #

**The reading, storing and interpretation of the binary map data**

This module handles the reading of binary data, its interpretation and storing for future access

The classes that belong to this modules are:
  * **MapItem** - represents a single item from the map, like a street, a lake, a POI, etc
  * **MapTile** - represents a collection of map item that belong to a defined area
  * **MapPresets** - every map item is identified by a numeric constant, defined in this class

![http://osm-android.googlecode.com/svn/wiki/binary_data_module-1.jpg](http://osm-android.googlecode.com/svn/wiki/binary_data_module-1.jpg)

**The map engine**

This module handles the following tasks:
  * Manage a cache for the map tiles for fast future access
  * Selecting all the objects within a certain map region
  * Querying the database and extracting the names of the map items

The classes that belong to this modules are:
  * **CacheElement** - a generic implementation of a element within a cache
  * **Cache** - the actual implementation of a caching algorithm
  * **MapRect** - a map rectangle; a container for all map items that belong to this particular area
  * **MapEngine** - manages the caches, and the maps; it's most important task is accepting a Bounding Box and returning a MapRect object containing all the map items withing that region

![http://osm-android.googlecode.com/svn/wiki/map_engine_module-1.jpg](http://osm-android.googlecode.com/svn/wiki/map_engine_module-1.jpg)

**The rendering module**

This module has the task of rendering to screen all the objects from a given MapRect object (a rectangular map area)

The drawing is performed using Android's OpenGL ES implementation.

It's main classes are:
  * **OsmMapRenderer** - the most important class in this modules; receives as input a MapRect objects and draws all the elements inside it
  * **GLTextures** - a helper class for managing textures
  * **LabelsTexture** - a object that represents a transparent bitmap (texture) on which street names and other text is drawn; at the end of the drawing process, this texture will be overlapped on the drawing producing the final image

![http://osm-android.googlecode.com/svn/wiki/render_module-1.jpg](http://osm-android.googlecode.com/svn/wiki/render_module-1.jpg)

**The routing module**

The main task of this module is building a weighted graph and finding out the shortest (optimal) path between two points.

The graph is build from one or three MapRect items, depending on the distance between the terminal points:
  * if the distance is short, the graph will be generated from a single, high-detailed (all streets, e.g.: Service & Residential), MapRect item
  * if the distance is long, the graph will be generated from three MapRect items with a different level of detail: the area between the start/destination point will be in high details, while the bounding box containing the two points will have a lower detail (up to Highway/Tertiary)

The main classes of this module are:
  * **GraphNode** - represents a vertex of the weighted, oriented graph
  * **GraphEdge** - represents an edge of the weighted, oriented graph
  * **DirectedGraph** - the actual routing graph
  * **RoutePoint** - is the start/destination point; it can be positioned on a street or at a certain distance from a street
  * **ShortestPathAlgorithm** - the implementation of a shortest path algorithm in a weighted oriented graph (Diskstra's algorithm for now)
  * **Route** - the class that glues together all the other classes in this module; represents the interface with the outside world for the routing module

![http://osm-android.googlecode.com/svn/wiki/routing_module-1.jpg](http://osm-android.googlecode.com/svn/wiki/routing_module-1.jpg)
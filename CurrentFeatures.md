# Introduction #

This is a brief list of the features supported by osm-android

  * Centralized storage of labels and associated coordinates (street names, lakes, parks, etc.)
  * Searching of names in the label database and focusing the map on the item's coordinates
  * Drawing engine based on OpenGL ES implementation in Android
  * Routing engine based on Dijkstra's algorithm on a weighted oriented graph
  * Track recording feature (still to be tested)


Work to be done in the near future:
  * Add the possibility to save the current location as a POI (at least summary & coordinates)
  * Display Points of Interests on the map
  * Improve the osm-converter, especially regarding memory usage
  * Speed up the rendering engine
  * Optimize the routing algorithm (including routing restrictions)
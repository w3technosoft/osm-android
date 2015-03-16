INSTALL NOTES

There are two applications involed in OSMAndroid:
  1. OSMandroidConverter - takes a raw map in OSM format and coverts it to the binary format used by OsmAndroid
  1. OSMandroid - the actual navigation app designed to run on the Android platform

To be able to compile the application, you need to set up the Android Development
environment at described in http://developer.android.com/sdk/1.6_r1/index.html
After that you should have Eclipse and the Android plug-in set up.

Next step is to import the two projects into Eclipse.

To convert a OSM map into a OSMandroid map, put the raw .osm file under
OSMandroidConverter/osmdata and modify the map file name under OsmConverter.java
Run this project as a standard Java application. This should output the folders:
  1. osm\_android/maps - the map's directory containing binary map tiles
  1. osm\_android/names - directory containing street names and coordinates

The osm\_android folder will need to be pushed into the Android phone/emulator under /sdcard;
the emulator must the started/ the phone must be connected to the host computer
(e.g ./adb push ~/OSMandroidConverter/osm\_android /sdcard/osm\_android)

To run the OSMandroid application on the emulator, select the OSMandroid project and
run OSMandroid as an Android application. You'll whant to set additional command line
options to the emulator for this to start in landscape mode ("-skin HVGA-L")

Whenever updating a map you'll need to update the name database also in the Settings
menu (Settings->Update database)

When in ViewMap mode, the application will start centered on the map you provided at
zoom level 15. Use the Zoom-in Zoom-out buttons or the touch screen to zoom or pan
the map.
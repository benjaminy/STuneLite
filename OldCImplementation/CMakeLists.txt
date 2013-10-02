cmake_minimum_required( VERSION 2.6 )
project( pcat C )

set( CMAKE_BUILD_TYPE Debug )

set( CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/Modules/" )

# Standard pattern for finding libraries:
# find_package(LibXML++ REQUIRED)
# include_directories(${LibXML++_INCLUDE_DIRS})
# set(LIBS ${LIBS} ${LibXML++_LIBRARIES})

find_package( LibYaml REQUIRED )
include_directories( ${LIBYAML_INCLUDE_DIRS} )
set( LIBS ${LIBS} ${LIBYAML_LIBRARIES} )

find_package( GTK2 REQUIRED gtk )
include_directories( ${GTK2_INCLUDE_DIRS} )
set( LIBS ${LIBS} ${GTK2_LIBRARIES} )

# # It's annoying that FindGTK2 doesn't do gmodule... clean it up later maybe
# find_package( GModule REQUIRED )
# include_directories( ${GMODULE_INCLUDE_DIRS} )
# set( LIBS ${LIBS} ${GMODULE_LIBRARIES} )
# set( LIBS ${LIBS} "Junk" )
set( LIBS ${LIBS} "/opt/local/lib/libgmodule-2.0.dylib" )


find_package( GSL REQUIRED )
include_directories( ${GSL_INCLUDE_DIRS} )
set( LIBS ${LIBS} ${GSL_LIBRARIES} )

find_package( Sqlite3 REQUIRED )
include_directories( ${SQLITE3_INCLUDE_DIRS} )
set( LIBS ${LIBS} ${SQLITE3_LIBRARIES} )

find_package( Gnuplot REQUIRED )
#  GNUPLOT_FOUND - system has Gnuplot
#  GNUPLOT_EXECUTABLE - the Gnuplot executable

# Sends the -std=c99 flag to the gcc compiler
add_definitions( -std=c99 )
add_definitions( -Wall )

include_directories( Source )
add_subdirectory( Source )

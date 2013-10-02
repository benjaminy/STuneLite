# - Try to find Libgmodule
# Once done this will define
#  LIBGMODULE_FOUND - System has Libgmodule
#  LIBGMODULE_INCLUDE_DIRS - The Libgmodule include directories
#  LIBGMODULE_LIBRARIES - The libraries needed to use Libgmodule
#  LIBGMODULE_DEFINITIONS - Compiler switches required for using Libgmodule

find_package( PkgConfig )
pkg_check_modules( PC_LIBGMODULE QUIET libgmodule-2.0 )
set( LIBGMODULE_DEFINITIONS ${PC_LIBGMODULE_CFLAGS_OTHER} )

FIND_PATH( LIBGMODULE_INCLUDE_DIR gmodule.h
           HINTS ${PC_LIBGMODULE_INCLUDEDIR} ${PC_LIBGMODULE_INCLUDE_DIRS} /opt/local/include/glib-2.0
           PATH_SUFFIXES libgmodule )

FIND_LIBRARY( LIBGMODULE_LIBRARY NAMES gmodule libgmodule
              HINTS ${PC_LIBGMODULE_LIBDIR} ${PC_LIBGMODULE_LIBRARY_DIRS} /opt/local/lib )

set( LIBGMODULE_LIBRARIES ${LIBGMODULE_LIBRARY} )
set( LIBGMODULE_INCLUDE_DIRS ${LIBGMODULE_INCLUDE_DIR} )

include( FindPackageHandleStandardArgs )
# handle the QUIETLY and REQUIRED arguments and set LIBGMODULE_FOUND to TRUE
# if all listed variables are TRUE
find_package_handle_standard_args( Libgmodule DEFAULT_MSG
                                   LIBGMODULE_LIBRARY LIBGMODULE_INCLUDE_DIR)

mark_as_advanced( LIBGMODULE_INCLUDE_DIR LIBGMODULE_LIBRARY )

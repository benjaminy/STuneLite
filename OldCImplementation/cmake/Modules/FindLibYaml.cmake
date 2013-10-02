# - Try to find LibYaml
# Once done this will define
#  LIBYAML_FOUND - System has LibYaml
#  LIBYAML_INCLUDE_DIRS - The LibYaml include directories
#  LIBYAML_LIBRARIES - The libraries needed to use LibYaml
#  LIBYAML_DEFINITIONS - Compiler switches required for using LibYaml

find_package( PkgConfig )
pkg_check_modules( PC_LIBYAML QUIET libyaml-0.2 )
set( LIBYAML_DEFINITIONS ${PC_LIBYAML_CFLAGS_OTHER} )

FIND_PATH( LIBYAML_INCLUDE_DIR yaml.h
           HINTS ${PC_LIBYAML_INCLUDEDIR} ${PC_LIBYAML_INCLUDE_DIRS}
           PATH_SUFFIXES libyaml )

FIND_LIBRARY( LIBYAML_LIBRARY NAMES yaml libyaml
              HINTS ${PC_LIBYAML_LIBDIR} ${PC_LIBYAML_LIBRARY_DIRS} )

set( LIBYAML_LIBRARIES ${LIBYAML_LIBRARY} )
set( LIBYAML_INCLUDE_DIRS ${LIBYAML_INCLUDE_DIR} )

include( FindPackageHandleStandardArgs )
# handle the QUIETLY and REQUIRED arguments and set LIBYAML_FOUND to TRUE
# if all listed variables are TRUE
find_package_handle_standard_args( LibYaml DEFAULT_MSG
                                   LIBYAML_LIBRARY LIBYAML_INCLUDE_DIR)

mark_as_advanced( LIBYAML_INCLUDE_DIR LIBYAML_LIBRARY )


add_executable( pcat pcat_map.c main.c database_stuff.c pcat_config.c plugin_stuff.c spawn_stuff.c prediction.c client.c )

target_link_libraries( pcat ${LIBS} )


add_executable( test_pcat my_first_child.c pcat_process_interface.c )

# target_link_libraries( test_pcat ${LIBS} )


add_library( plug_01 MODULE libplug_01.c )

target_link_libraries( plug_01 ${LIBS} )

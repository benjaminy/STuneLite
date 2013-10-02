/*
   glib-module.c  --  2006.04.01
   plugin example with GLib: main application
   compile: gcc -Wall glib-module.c `pkg-config --cflags --libs gtk+-2.0 gmodule-2.0` -o glib-module
*/

#include <stdio.h>
#include <stdlib.h>
#include <glib.h>
#include <gmodule.h>

GModule * module = NULL;
gchar * module_path;
gint module_results = 0;
int (*module_func) (gchar *);

int messing_with_plugins()  {
    /* if plugins not supported */
    if (!g_module_supported())  {
      perror ("module not supported");
      return EXIT_FAILURE;
    }
    module_path = g_new0 (gchar, 200);
    module_path = g_module_build_path ("Source", "plug_01");
    module = g_module_open (module_path, G_MODULE_BIND_LAZY);
    if( module != NULL )
    {
        g_message ("open\n");
    }
    else {
          g_error ("module path not found: %s", module_path);
          return EXIT_FAILURE;
    }
    g_message ("module name: %s\n", g_module_name (module));
    g_message ("module path: %s\n", module_path);
    if (g_module_symbol (module, "module_test", (gpointer*)&module_func))  g_message ("symbol\n");
    else {
          g_error ("symbol not found");
          return EXIT_FAILURE;
    }
    module_results = module_func ("function_parameter");
    g_message ("module results: %d\n", module_results);
    if (g_module_close (module))  g_message ("close\n");
    g_free (module_path);
    return EXIT_SUCCESS;
}

#if 0

/* the function signature for 'say_hello' */
typedef void (* SayHelloFunc) (const char *message);

gboolean just_say_hello( const char *filename, GError **error )
{
    SayHelloFunc  say_hello;
    GModule      *module;
    module = g_module_open( filename, G_MODULE_BIND_LAZY );
    if( !module )
    {
        // g_set_error( error, FOO_ERROR, FOO_ERROR_BLAH,
        //              "%s", g_module_error () );
        g_set_error( NULL, 0, 0, 0 );
        return FALSE;
    }
    if( !g_module_symbol( module, "say_hello", (gpointer *)&say_hello ) )
    {
        // g_set_error( error, SAY_ERROR, SAY_ERROR_OPEN,
        //              "%s: %s", filename, g_module_error () );
        g_set_error( NULL, 0, 0, 0 );
        if( !g_module_close( module ) )
            g_warning( "%s: %s", filename, g_module_error() );
        return FALSE;
    }
    if( say_hello == NULL )
    {
        // g_set_error( error, SAY_ERROR, SAY_ERROR_OPEN, "symbol say_hello is NULL" );
        g_set_error( NULL, 0, 0, 0 );
        if ( !g_module_close( module ) )
            g_warning( "%s: %s", filename, g_module_error () );
        return FALSE;
    }
    /* call our function in the module */
    say_hello ("Hello world!");
    if( !g_module_close( module ) )
        g_warning( "%s: %s", filename, g_module_error () );
    return TRUE;
}

#endif

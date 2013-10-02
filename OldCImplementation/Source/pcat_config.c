/*
 *
 */

#include <stdlib.h>
#include <pcat_config.h>
#include <gtk/gtk.h>

gint64 prng_seed = DEFAULT_PRNG_SEED;
char *temp_hack_db_nam = DEFAULT_TEMP_HACK_DB_NAME;
char *temp_hack_sql_cmd = DEFAULT_TEMP_HACK_SQL_CMD;

static GOptionEntry entries[] =
{
    { "prng_seed", 0, 0, G_OPTION_ARG_INT64, &prng_seed, "Initial seed for the pseudorandom number generator", "1" },
    { "temp_hack_db_name", 0, 0, G_OPTION_ARG_FILENAME, &temp_hack_db_nam, "DB name", "blarg" },
    { "temp_hack_sql_cmd", 0, 0, G_OPTION_ARG_STRING, &temp_hack_sql_cmd, "SQL cmd", "bar" },
//     { "repeats",  'r', 0, G_OPTION_ARG_INT,  &repeats,  "Average over N repetitions", "N" },
//     { "max-size", 'm', 0, G_OPTION_ARG_INT,  &max_size, "Test up to 2^M items", "M" },
//     { "verbose",  'v', 0, G_OPTION_ARG_NONE, &verbose,  "Be verbose", NULL },
//     { "beep",     'b', 0, G_OPTION_ARG_NONE, &beep,     "Beep when done", NULL },
//     { "rand",     0,   0, G_OPTION_ARG_NONE, &rand,     "Randomize the data", NULL },
    { NULL }
};

void process_command_line_options(
    int argc,
    char *argv[] )
{
    GError *error = NULL;
    GOptionContext *context;

    context = g_option_context_new( "- test tree model performance" );
    g_option_context_add_main_entries( context, entries, NULL /* GETTEXT_PACKAGE */ );
    g_option_context_add_group( context, NULL /* gtk_get_option_group( TRUE ) */ );
    gtk_get_option_group( TRUE );
    if ( !g_option_context_parse( context, &argc, &argv, &error ) )
    {
        g_print( "option parsing failed: %s\n", error->message );
        exit( 1 );
    }

    /* ... */

}

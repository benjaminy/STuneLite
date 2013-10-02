/*
 *
 */

#include <glib.h>
#include <glib/gprintf.h>
#include <stdio.h>
#include <string.h>
#include <pcat_process_interface.h>
#include <client.h>
#include <unistd.h>

void playing_env_dump( void )
{
    gchar **envp = g_listenv();
    size_t i = 0;
    while( envp[i] != NULL )
    {
        printf( "g_listenv[%2zu]  %s => %s\n", i, envp[i], g_getenv( envp[i] ) );
        i++;
    }
    envp = g_get_environ();
    i = 0;
    while( envp[i] != NULL )
    {
        printf( "g_get_environ[%zu] = %s\n", i, envp[i] );
        i++;
    }
}

typedef struct data_bucket_ data_bucket_, *data_bucket;
struct data_bucket_
{
    GPid child_pid;
    gboolean out_done, err_done, child_done;
    GIOChannel *out_source, *err_source;
    GMainLoop *main_loop;
    guint child_eid, timeout_eid, out_eid, err_eid;
    GRegex *result_regex, *val_regex;
};

void everybody_done( data_bucket b )
{
    if( b->out_done && b->err_done && b-> child_done )
    {
        g_source_remove( b->timeout_eid );
        g_main_loop_quit( b->main_loop );
    }
}

static pcat_rcode parse_value( GMatchInfo *match_info, GError **err, pcat_reported_value rval )
{
    pcat_rcode rv = PCAT_SUCCESS;

    rval->name      = g_match_info_fetch( match_info, 1 );
    gchar *type_val = g_match_info_fetch( match_info, 2 );
    gchar *value    = g_match_info_fetch( match_info, 3 );
    if( rval->name == NULL || type_val == NULL || value == NULL )
    {
        if( type_val )   { g_free( type_val ); }
        if( rval->name ) { g_free( rval->name ); }
        if( value )      { g_free( value ); }
        return PCAT_ERROR;
    }
    
    if( 0 == strcmp( type_val, "REAL" ) )
    {
        rval->val.tag = PCAT_GENERIC_VALUE_REAL;
        int match = sscanf( value, "%lf", &rval->val._.r );
        if( match != 1 )
        {
            /* error */
        }
        rval->original_string = value;
    }
    else if( 0 == strcmp( type_val, "STRING" ) )
    {
        rval->val.tag = PCAT_GENERIC_VALUE_STRING;
        rval->val._.s = value;
        rval->original_string = value;
    }
    else if( 0 == strcmp( type_val, "INTEGRAL" ) )
    {
        rval->val.tag = PCAT_GENERIC_VALUE_INTEGRAL;
        int match = sscanf( value, "%"SCNi64, &rval->val._.i );
        if( match != 1 )
        {
            /* error */
        }
        rval->original_string = value;
    }
    else
    {
        rv = PCAT_ERROR;
        abort();
    }
    g_print( "Found: %s : %s = %s\n",
             rval->name, generic_value_tag_string( rval->val.tag ), rval->original_string );
    g_free( type_val );
    if( g_match_info_next( match_info, err ) )
    {
        rv = PCAT_ERROR;
    }
    return rv;
}

typedef struct read_line_result_ read_line_result_, *read_line_result;

struct read_line_result_
{
    enum
    {
        line_value,
        line_result,
        line_nothing
    } value_result;
    union
    {
        pcat_reported_value_ v;
        struct
        {
            gboolean succeeded;
            int32_t fcode;
        } r;
    } _;
};

static pcat_rcode read_line( data_bucket b, gchar *line, GError **err, read_line_result rval )
{
    pcat_rcode rv = PCAT_SUCCESS;
    
    GMatchInfo *result_match = NULL;
    gboolean matched = g_regex_match( b->result_regex, line, 0 /* match flags */, &result_match );
    if( g_match_info_matches( result_match ) )
    {
        if( !matched )
        {
            abort();
            return PCAT_ERROR;
        }
        gchar *res = g_match_info_fetch_named( result_match, "res" );
        if( res != NULL )
        {
            if( 0 == strcmp( res, "SUCCESS" ) )
            {
                rval->value_result = line_result;
                rval->_.r.succeeded = TRUE;
                g_print( "YAY WE HAVE SUCCESS\n" );
            }
            else if( 0 == strcmp( res, "" ) )
            {
                gchar *second = g_match_info_fetch_named( result_match, "fcode" );
                if( second != NULL )
                {
                    rval->value_result = line_result;
                    rval->_.r.succeeded = FALSE;
                    int match = sscanf( second, "%"SCNi32, &rval->_.r.fcode );
                    if( match != 1 )
                    {
                        printf( "my anger subsides\n" );
                        abort();
                    }
                    printf( "Here we went: %"PRIi32"\n", rval->_.r.fcode );
                    g_free( second );
                }
                else
                {
                    printf( "screw you too\n" );
                    abort();
                }
            }
            else
            {
                printf( "BAD BAD BAD\n" );
                abort();
            }
            g_free( res );
        }
        else
        {
            printf( "screw you two\n" );
            abort();
        }
        // gchar *type_val = g_match_info_fetch( match_info, 2 );
        // gchar *value    = g_match_info_fetch( match_info, 3 );
    }
    else
    {
        GMatchInfo *val_match = NULL;
        matched = g_regex_match( b->val_regex, line, 0 /* match flags */, &val_match );
        if( g_match_info_matches( val_match ) )
        {
            if( !matched )
            {
                abort();
                return PCAT_ERROR;
            }
            parse_value( val_match, err, &rval->_.v );
        }
        else
        {
            //g_print( "NO MATCH: %s", line );
            rv = PCAT_NO_MATCH;
        }
        g_match_info_free( val_match );
    }
    g_match_info_free( result_match );
    return rv;
}

static void end_of_stream( GIOChannel *source, data_bucket b )
{
    if( source == b->out_source )
    {
        g_message( "EOF for std out pipe" );
        b->out_done = TRUE;
    }
    else if( source == b->err_source )
    {
        g_message( "EOF for std err pipe" );
        b->err_done = TRUE;
    }
    else
    {
        g_message( "Got an EOF status, but source doesn't match out or err. Bad?" );
    }
    everybody_done( b );
}

gboolean child_out_handler(
    GIOChannel *source,
    GIOCondition condition,
    gpointer data )
{
    data_bucket b = (data_bucket)data;
    gboolean rv = TRUE;
    switch( condition )
    {
    case G_IO_IN:
    case G_IO_PRI:
    {
        gchar *line;
        gsize len, term_pos;
        GError *err = NULL;
        GIOStatus stat = g_io_channel_read_line(
            source, &line, &len, &term_pos, &err );
        switch( stat )
        {
        case G_IO_STATUS_NORMAL:
        {
            if( source == b->out_source )
            {
                read_line_result_ rval;
                read_line( b, line, &err, &rval );
                /* do something with rval */
                // g_free( rval.name );
                // g_free( rval.original_string );
            }
            else if( source == b->err_source )
            {
                printf( "child stderr: >%s", line );
            }
            break;
        }
        case G_IO_STATUS_EOF:
            end_of_stream( source, b );
            rv = FALSE;
            break;
        case G_IO_STATUS_ERROR:
        case G_IO_STATUS_AGAIN:
            printf( "G_IO error of some sort.  Must improve error handling\n" );
            abort();
            break;
        default: abort(); /* There is a case for every legal value */
        }
        if( line != NULL )
        {
            g_free( line );
        }
        break;
    }
    case G_IO_HUP:
        end_of_stream( source, b );
        rv = FALSE;
        break;
    case G_IO_OUT: printf( "Shouldn't be getting G_IO_OUT events on an input stream!\n" ); break;
    case G_IO_ERR: printf( "err!\n" ); break;
    case G_IO_NVAL: printf( "nval!\n" ); break;
    default: abort(); /* There is a case for every legal value */
    }
    return rv;
}

gboolean timeout_func( gpointer data )
{
    data_bucket b = (data_bucket)data;
    g_message( "Still waiting on client %d\n", (int)b->child_pid );
    return TRUE;
}

static void handle_client_exit(
    GPid pid,
    gint status,
    gpointer data )
{
    data_bucket b = (data_bucket)data;

    g_message( "Client %d %d exited with status %d\n", (int)pid, (int)b->child_pid, status );
    g_spawn_close_pid( pid );
    b->child_done = TRUE;
    everybody_done( b );
}


void run_client( pcat_client c, pcat_point p )
{
    gchar *argv[ 2 ] = {
        c->program_name,
        NULL
    };
    size_t additional_env_vars = 1;
    size_t total_env_slots = c->space.num_knobs + additional_env_vars + 1;
    gchar **child_env =
        (gchar **)g_malloc( total_env_slots * sizeof( child_env[0] ) );
    for( size_t i = 0; i < c->space.num_knobs; i++ )
    {
        size_t buffer_size = 1000; /* way too big? */
        child_env[i] = g_malloc( buffer_size );
        pcat_knob k = &get_space( c )->knobs[i];
        printf( "%s\n", k->name );
        switch( k->kind )
        {
            case PCAT_KNOBK_DISCRETE:
                g_snprintf( child_env[i], (gulong)buffer_size, "%s%s=%"PRIi64,
                            PCAT_ENV_VAR_VALUE_PREFIX, k->name, p[i].d );
                break;
            case PCAT_KNOBK_CONTINUOUS:
                g_snprintf( child_env[i], (gulong)buffer_size, "%s%s=%lf",
                            PCAT_ENV_VAR_VALUE_PREFIX, k->name, p[i].c );
                break;
            case PCAT_KNOBK_UNORDERED:
                g_message( "---> Don't really know what to do about " );
                break;
            default:
                g_message( "---> Wrong kind of knob" );
                break;
        }
    }
    child_env[ c->space.num_knobs ] = PCAT_SENSOR_READINGS_FILENAME "=blam";
    child_env[ c->space.num_knobs + 1 ] = NULL;
    // playing_env_dump();
    
    int chld_std_out_fd, chld_std_err_fd;
    GError *err = NULL;
    data_bucket_ b;
    b.out_done = FALSE;
    b.err_done = FALSE;
    b.child_done = FALSE;
    gchar *result_regex_str = "PCAT_CLIENT_((?<res>SUCCESS)|FAILURE[[:space:]]*\\=[[:space:]]*(?<fcode>[^[:space:]]*))";
    gchar *val_regex_str = PCAT_CLIENT_REPORT_PREFIX "(?<var_name>[[:alpha:]][[:alnum:]_]*)[[:space:]]*\\:[[:space:]]*(?<type_val>REAL|STRING|INTEGRAL)[[:space:]]*\\=[[:space:]]*(?<value>[^[:space:]]*)";
    b.result_regex = g_regex_new( result_regex_str, 0 /* compile flags */, 0 /* match flags*/, &err );
    if( err != NULL )
    {
        printf( "blah: %s\n", err->message );
        abort();
    }
    b.val_regex    = g_regex_new( val_regex_str, 0 /* compile flags */, 0 /* match flags*/, &err );
    b.main_loop = g_main_loop_new( NULL, FALSE );
    gboolean spawn_worked = g_spawn_async_with_pipes(
        NULL, /* const gchar *working_directory */
        argv,
        child_env,
        G_SPAWN_SEARCH_PATH | G_SPAWN_DO_NOT_REAP_CHILD,
        /* GSpawnFlags flags */
        /* G_SPAWN_LEAVE_DESCRIPTORS_OPEN */
        /* G_SPAWN_SEARCH_PATH */
        /* G_SPAWN_STDOUT_TO_DEV_NULL */
        /* G_SPAWN_STDERR_TO_DEV_NULL */
        /* G_SPAWN_CHILD_INHERITS_STDIN */
        /* G_SPAWN_FILE_AND_ARGV_ZERO */
        NULL, /* GSpawnChildSetupFunc child_setup */
        NULL, /* gpointer user_data */
        &b.child_pid,
        NULL, /* gint *standard_input */
        &chld_std_out_fd,
        &chld_std_err_fd,
        &err );

    if( !spawn_worked )
    {
        // error!
    }

    b.child_eid = g_child_watch_add( b.child_pid, handle_client_exit, &b );

    /* Create channels that will be used to read data from pipes. */
#ifdef G_OS_WIN32
    b.out_source = g_io_channel_win32_new_fd( chld_std_out_fd );
    b.err_source = g_io_channel_win32_new_fd( chld_std_err_fd );
#else
    b.out_source = g_io_channel_unix_new( chld_std_out_fd );
    b.err_source = g_io_channel_unix_new( chld_std_err_fd );
#endif

    b.out_eid = g_io_add_watch( b.out_source, G_IO_IN | G_IO_HUP, (GIOFunc)child_out_handler, &b );
    b.err_eid = g_io_add_watch( b.err_source, G_IO_IN | G_IO_HUP, (GIOFunc)child_out_handler, &b );
    // these too
    // G_IO_IN | G_IO_PRI | G_IO_ERR | G_IO_HUP | G_IO_NVAL,
    // G_IO_IN | G_IO_PRI | G_IO_ERR | G_IO_HUP | G_IO_NVAL,

    b.timeout_eid = g_timeout_add_seconds( 10, timeout_func, &b );

    g_main_loop_run( b.main_loop );
    g_message( "Done running client\n" );
    g_io_channel_unref( b.out_source );
    g_io_channel_unref( b.err_source );
    g_main_loop_unref( b.main_loop );
    g_regex_unref( b.val_regex );
    g_regex_unref( b.result_regex );
    for( size_t i = 0; i < c->space.num_knobs; i++ )
    {
        g_free( child_env[i] );
    }
    g_free( child_env );
}

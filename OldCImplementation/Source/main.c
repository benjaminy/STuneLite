/*
 * Configurable Probabilistic Auto-tuning Project
 * Author: Benjamin Ylvisaker
 */

#include <assert.h>
#include <stdio.h>
#include <glib.h>
#include <yaml.h>
#include <glib/gprintf.h>
#include <gsl/gsl_types.h>
#include <gsl/gsl_math.h>
#include <gsl/gsl_complex_math.h>
#include <gsl/gsl_rng.h>
#include <sqlite3.h>
#include <pcat_config.h>
#include <pcat_global_defs.h>
#include <client.h>

void default_gsl_error_handler(
    const char *reason,
    const char *file,
    int line,
    int gsl_errno )
{
    fprintf( stderr, "GSL failed for the following reason: %s\n", reason );
    switch( gsl_errno )
    {
        case GSL_SUCCESS: break;
        case GSL_FAILURE: break;
        case GSL_CONTINUE: break;
        case GSL_EDOM: break;
        case GSL_ERANGE: break;
        case GSL_EFAULT: break;
        case GSL_EINVAL: break;
        case GSL_EFAILED: break;
        case GSL_EFACTOR: break;
        case GSL_ESANITY: break;
        case GSL_ENOMEM: break;
        case GSL_EBADFUNC: break;
        case GSL_ERUNAWAY: break;
        case GSL_EMAXITER: break;
        case GSL_EZERODIV: break;
        case GSL_EBADTOL: break;
        case GSL_ETOL: break;
        case GSL_EUNDRFLW: break;
        case GSL_EOVRFLW: break;
        case GSL_ELOSS: break;
        case GSL_EROUND: break;
        case GSL_EBADLEN: break;
        case GSL_ENOTSQR: break;
        case GSL_ESING: break;
        case GSL_EDIVERGE: break;
        case GSL_EUNSUP: break;
        case GSL_EUNIMPL: break;
        case GSL_ECACHE: break;
        case GSL_ETABLE: break;
        case GSL_ENOPROG: break;
        case GSL_ENOPROGJ: break;
        case GSL_ETOLF: break;
        case GSL_ETOLX: break;
        case GSL_ETOLG: break;
        case GSL_EOF: break;
    }
    abort();
}

gsl_error_handler_t *original_gsl_error_handler = NULL;

int db_test( int argc, char **argv );
int messing_with_plugins();
void playing_with_spawn( void );

void tune_client( void )
{
    gsl_rng *r = gsl_rng_alloc( gsl_rng_taus );
    gsl_rng_set( r, 1 );

    
}

int main( int argc, char** argv )
{
    original_gsl_error_handler = gsl_set_error_handler( default_gsl_error_handler );

    // printf( "hello world %d %f %d %s\n", num, M_E, SQLITE_ERROR, GNUPLOT_EXECUTABLE );
    printf( "Main is running! M_E: %f SQLITE_ERROR: %d\n", M_E, SQLITE_ERROR );
    /* gsl_complex x1 = gsl_complex_rect( 1.2, 3.4 ); */

    yaml_parser_t parser;
    int huh = yaml_parser_initialize( &parser );
    g_message( "yaml_parser_init: %i\n", huh );
	yaml_parser_delete( &parser );

    gsl_rng *r = gsl_rng_alloc( gsl_rng_taus );
    assert( r != 0 );
    gsl_rng_set( r, prng_seed );

    // messing_with_plugins();

    db_test( argc, argv );

    pcat_rcode main_res = run_pcat_client( );

    gsl_rng_free( r );

    g_message( "The very end %i\n", main_res );
    return 0;
}

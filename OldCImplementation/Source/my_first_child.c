/*
 *
 */

#include <inttypes.h>
#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include <pcat_client_api.h>

int pcat_main( int argc_int, char *argv[], pcat_client_state s )
{
    int64_t x, y;
    if( pcat_get_value_integral( "X", &x, s ) != PCAT_SUCCESS )
    {
        printf( "X failed!!\n" );
    }
    if( pcat_get_value_integral( "Y", &y, s ) != PCAT_SUCCESS )
    {
        printf( "Y failed!!\n" );
    }
    printf( "Got x:%"PRIi64" and y:%"PRIi64"\n", x, y );

    double xr = x, yr = y;
    double goodness = x + y, badness = x - y;

    if( pcat_report_real( "badness", badness, s ) != PCAT_SUCCESS )
    {
        printf( "badness failed\n" );
    }

    if( badness > 10.0 )
    {
        pcat_report_failure( 1, s );
    }
    else
    {
        pcat_report_success( s );
        if( pcat_report_real( "goodness", goodness, s ) != PCAT_SUCCESS )
        {
            printf( "goodness failed\n" );
        }
    }

    return 0;
}

int main( int argc_int, char** argv )
{
    return pcat_client_main_wrapper( pcat_main, argc_int, argv );
}

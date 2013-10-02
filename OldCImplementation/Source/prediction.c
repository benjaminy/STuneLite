/*
 *
 */

#include <stdlib.h>
#include <glib.h>
#include <gsl/gsl_math.h>
#include <assert.h>
#include <client.h>

/**
 * Assume three points p1, p2 and p3 in N-dimensional space.  The distance
 * between p1 and p2 is d3, p1 and p3 is d2, p2 and p3 is d1.  Imagine p1 and p2
 * as the end points of one of the axes of the ellipse that also goes through
 * p3.  If p3 is "close" to p1 and p2, then p1-p2 is the major axis; if p3 is
 * "far" from p1 and p2, then p1-p2 is the minor axis.  ellipse_shape computes
 * the shape of this ellipse.  If p3 is on the line between p1 and p2, the shape
 * is 0.  If p3 is positioned such that the ellipse is actually a circle, the
 * shape is 1.  The farther p3 is, the larger the shape value.  If p3 is not
 * between p1 and p2 at all, positive infinity is returned.
 */
double ellipse_shape( double d1, double d2, double d3 )
{
    assert( d1 > 0.0 && d2 > 0.0 && d3 > 0.0 );
    double d1s = d1 * d1;
    double d2s = d2 * d2;
    double d3s = d3 * d3;
    if( fabs( d2s - d1s ) > d3s )
    {
        return GSL_POSINF;
    }
    double perimeter = d1 + d2 + d3;
    double s = perimeter / 2.0;
    /* The following formula is broken into two square roots in a
     * possibly-misguided attempt to preserve as much numerical
     * precision as possible. */
    double area = sqrt( s * ( s - d1 ) ) * sqrt( ( s - d2 ) * ( s - d3 ) );
    double height = 2.0 * area / d3;
    double base_1 = sqrt( d1s - height * height );
    double half_d3 = d3 / 2.0;
    double p3_x = half_d3 - base_1;
    return height / sqrt( half_d3 * half_d3 - p3_x * p3_x );
}

/*
 * This not particularly well founded right now.  The idea is to convert ...
 */
#define SCALER_CONST_A 1.0
#define SCALER_CONST_B 0.25
double distance_scaler( double alpha )
{
    return 1.0 + ( SCALER_CONST_A / ( SCALER_CONST_B + alpha ) );
}

void make_predictions( pcat_client c )
{

    pcat_point p_c;
    pcat_map_iter_ p_n_iter;
    pcat_rcode r_p_n = pcat_points_iterator( NULL, &p_n_iter );
    while( p_n_iter.has_next( &p_n_iter ) )
    {
        pcat_kv_pair_ pv;
        r_p_n = p_n_iter.next( &p_n_iter, &pv );
        pcat_point p_n = (pcat_point)pv.key;
        if( pcat_points_equal( c, p_c, p_n ) )
        {
            continue;
        }
        double d_3 = pcat_distance( get_space( c ), p_c, p_n );
        double d_3_effective = d_3;

        pcat_map_iter_ p_o_iter;
        pcat_rcode r_p_o = pcat_points_iterator( NULL, &p_o_iter );
        g_message( "rpo: %i\n", r_p_o );
        while( p_o_iter.has_next( &p_o_iter ) )
        {
            pcat_kv_pair_ pv;
            r_p_n = p_o_iter.next( &p_o_iter, &pv );
            pcat_point p_o = (pcat_point)pv.key;
            if( pcat_points_equal( c, p_n, p_o )
                || pcat_points_equal( c, p_c, p_o ) )
            {
                continue;
            }
            double d_1 = pcat_distance( get_space( c ), p_c, p_o );
            double d_2 = pcat_distance( get_space( c ), p_n, p_o );
            double alpha = ellipse_shape( d_1, d_2, d_3 );
            if( alpha > 100.0 )
            {
                /* p_o is effectively not between at all */
                continue;
            }
            d_3_effective *= distance_scaler( alpha );
        }
    }
}

void foo( pcat_client c )
{
    for( size_t i = 0; i < 100; i++ )
    {
        
        pcat_rcode random_point_uniform( tuning_space s, pcat_point vs, gsl_rng *r );
    }
}

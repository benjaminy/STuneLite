/**
 *
 */

#include <string.h>
#include <client.h>
#include <glib.h>
#include <assert.h>
#include <gsl/gsl_randist.h>

tuning_space get_space( pcat_client c )
{
    return &c->space;
}

pcat_rcode pcat_knob_init_d( pcat_knob k, gchar* name, int64_t min, int64_t max )
{
    k->name = strdup( name );
    if( min > max || k == NULL )
    {
        return PCAT_ERR_INVALID_INPUT;
    }
    k->kind = PCAT_KNOBK_DISCRETE;
    k->_.d.min = min;
    k->_.d.max = max;
    k->_.d.range = 1 + max - min;
    return PCAT_SUCCESS;
}

pcat_knob_value_ random_knob_value_uniform( pcat_knob k, gsl_rng *r )
{
    pcat_knob_value_ v;
    switch ( k->kind )
    {
        case PCAT_KNOBK_DISCRETE:
        {
            if( k->_.d.range < 1000000 )
            {
                unsigned long int i = gsl_rng_uniform_int( r, (unsigned long int)k->_.d.range );
                v.d = i + k->_.d.min;
                return v;
            }
            assert( FALSE );
        }
        case PCAT_KNOBK_CONTINUOUS:
        {
            v.c = k->_.c.range * gsl_rng_uniform( r ) + k->_.c.min;
            return v;
        }
        case PCAT_KNOBK_UNORDERED:
        {
            v.u = (uint8_t)gsl_rng_uniform_int( r, (unsigned long int)k->_.u._->option_count );
            return v;
        }
        default:
            assert( FALSE );
    }
}

pcat_knob_value_ random_knob_value_gaussian( pcat_knob k, gsl_rng *r, double mean, double sigma )
{
    pcat_knob_value_ v;
    switch ( k->kind )
    {
        case PCAT_KNOBK_DISCRETE:
        {
            do
            {
                double d = gsl_ran_gaussian( r, sigma );
                v.d = (int64_t)round( d + mean );
            } while( v.d < k->_.d.min || v.d < k->_.d.max );
            return v;
        }
        case PCAT_KNOBK_CONTINUOUS:
        {
            do
            {
                v.c = gsl_ran_gaussian( r, sigma ) + mean;
            } while( v.c < k->_.c.min || v.c < k->_.c.max );
            return v;
        }
        case PCAT_KNOBK_UNORDERED:
            assert( FALSE );
        default:
            assert( FALSE );
    }
}

pcat_rcode random_point_uniform( tuning_space s, pcat_point p, gsl_rng *r )
{
    for( size_t i = 0; i < s->num_knobs; i++ )
    {
        p[i] = random_knob_value_uniform( &s->knobs[i], r );
    }
    return PCAT_SUCCESS;
}

gint pcat_compare_knob_values( pcat_knob k, pcat_knob_value_ v1, pcat_knob_value_ v2 )
{
    switch ( k->kind )
    {
        case PCAT_KNOBK_DISCRETE:
        {
            int64_t diff = v1.d - v2.d;
            return diff < 0 ? -1 : ( diff > 0 ? 1 : 0 );
        }
        case PCAT_KNOBK_CONTINUOUS:
        {
            double diff = v1.c - v2.c;
            return diff < -0.000001 ? -1 : ( diff > 0.000001 ? 1 : 0 );
        }
        case PCAT_KNOBK_UNORDERED:
        {
            assert( FALSE );
        }
        default:
            assert( FALSE );
    }
}

gboolean pcat_knob_values_equal( pcat_knob k, pcat_knob_value_ v1, pcat_knob_value_ v2 )
{
    return pcat_compare_knob_values( k, v1, v2 ) == 0;
}

gint pcat_compare_points(
    tuning_space space,
    pcat_point p1,
    pcat_point p2 )
{
    for( size_t i = 0; i < space->num_knobs; i++ )
    {
        gint c = pcat_compare_knob_values( &space->knobs[i], p1[i], p2[i] );
        if( c < 0 )
        {
            return -1;
        }
        else if( c > 0 )
        {
            return 1;
        }
    }
    return 0;
}                                                         

gint pcat_compare_points_glib(
    gconstpointer a,
    gconstpointer b,
    gpointer user_data )
{
    return pcat_compare_points( (tuning_space)user_data, (pcat_point)a, (pcat_point)b );
}

gboolean pcat_points_equal( pcat_client c, pcat_point p1, pcat_point p2 )
{
    return pcat_compare_points( &c->space, p1, p2 ) == 0;
}

static double pcat_distance_one_knob( pcat_knob k, pcat_knob_value_ v1, pcat_knob_value_ v2 )
{
    switch ( k->kind )
    {
        case PCAT_KNOBK_DISCRETE:
            return fabs( (double)( v1.d - v2.d ) );
        case PCAT_KNOBK_CONTINUOUS:
            return fabs( v1.c - v2.c );
        case PCAT_KNOBK_UNORDERED:
            assert( FALSE );
        default:
            assert( FALSE );
    }
}

double pcat_distance( tuning_space s, pcat_point p1, pcat_point p2 )
{
    double d = 0.0;
    for( size_t i = 0; i < s->num_knobs; i++ )
    {
        double d1 = pcat_distance_one_knob( &s->knobs[i], p1[i], p2[i] );
        d += d1 * d1;
    }
    return sqrt( d );
}

pcat_rcode pcat_points_iterator( pcat_client c, pcat_map_iter i )
{
    return pcat_map_iter_init( NULL, i );
}

size_t num_tested_points( pcat_client c )
{
    return pcat_map_size( &c->tested_points );
}

gboolean term_criteria_satisfied( pcat_client c )
{
    return num_tested_points( c ) >= c->num_points_to_test;
}

pcat_point pcat_point_alloc( tuning_space t )
{
    pcat_point pt = ( pcat_point )g_malloc( t->num_knobs * sizeof( pt[0] ) );
    return pt;
}

void pcat_point_free( pcat_point pt )
{
    if( pt != NULL )
    {
        g_free( pt );
    }
}

typedef struct candidate_pt_info_ candidate_pt_info_, *candidate_pt_info;
struct candidate_pt_info_
{
    pcat_point p;
    double value;
};

void pcat_point_data_copy( tuning_space s, pcat_point dst, pcat_point src )
{
    g_memmove( dst, src, s->num_knobs * sizeof( dst[0] ) );
}

pcat_rcode pcat_choose_next_point_to_test( pcat_client c, pcat_point next_pt )
{
    double best_so_far;
    candidate_pt_info candidates =
        (candidate_pt_info)g_malloc( c->candidates_per_trial * sizeof( candidates[0] ) );
    for( size_t i = 0; i < c->candidates_per_trial; i++ )
    {
        candidates[i].p = pcat_point_alloc( &c->space );
        do {
            pcat_rcode res = random_point_uniform( &c->space, candidates[i].p, c->rng );
            //g_message( "blah7 %i", res );
        } while( pcat_map_contains_key( &c->tested_points, candidates[i].p ) );
        candidates[i].value = gsl_rng_uniform( c->rng );
        // make predictions for pt
    }
    // choose best pt
    for( size_t i = 0; i < c->candidates_per_trial; i++ )
    {
        if( i == 0 || candidates[i].value > best_so_far )
        {
            best_so_far = candidates[i].value;
            pcat_point_data_copy( &c->space, next_pt, candidates[i].p );
        }
    }
    for( size_t i = 0; i < c->candidates_per_trial; i++ )
    {
        g_free( candidates[i].p );
    }
    g_free( candidates );
    return PCAT_SUCCESS;
}

struct pcat_pt_test_results_
{
    double quality;
};

pcat_rcode pcat_test_point( pcat_client c, pcat_point p )
{
    pcat_pt_test_results_ r;
    printf( ">>> Calling run_client %p\n", p );
    run_client( c, p );
    printf( "<<< Returned from run_client\n" );
    r.quality = 4.2;
    pcat_map_add( &c->tested_points, p, &r );
    return PCAT_SUCCESS;
}

void pcat_space_cleanup( tuning_space s )
{
    for( size_t i = 0; i < s->num_knobs; i++ )
    {
        g_free( s->knobs[i].name );
    }
}

void pcat_client_cleanup( pcat_client client )
{
    pcat_space_cleanup( &client->space );
    pcat_map_destroy( &client->tested_points );
    gsl_rng_free( client->rng );
}

pcat_rcode run_pcat_client( void )
{
    g_message( "-> Enter: run_pcat_client" );
    pcat_client_ client;
    client.program_name = "./Source/test_pcat";
    client.rng = gsl_rng_alloc( gsl_rng_taus );
    if( client.rng == NULL)
    {
        return PCAT_ERR_NO_MEM;
    }
    client.space.num_knobs = 2;
    pcat_knob_ knobs[ client.space.num_knobs ];
    pcat_knob_init_d( &knobs[0], "X", 10, 110 );
    pcat_knob_init_d( &knobs[1], "Y", -10, 89 );
    client.space.knobs = knobs;
    pcat_rcode rfoo = pcat_map_init( pcat_compare_points_glib, &client.tested_points, &client.space );
    g_message( "blah34 %i", rfoo );
    client.candidates_per_trial = 50;
    client.num_points_to_test = 3;
    
    while( !term_criteria_satisfied( &client ) )
    {
        pcat_point next_pt = pcat_point_alloc( &client.space );
        pcat_rcode r = pcat_choose_next_point_to_test( &client, next_pt );
        if( r != PCAT_SUCCESS )
        {
            abort();
        }
        g_message( "p r:%i", r );
        pcat_test_point( &client, next_pt );
        // pcat_point_free( next_pt );
    }
    pcat_client_cleanup( &client );
    g_message( "<- Finished: run_pcat_client" );
    return PCAT_SUCCESS;
}

#define BLAH 5
#define BLAH2 1000
#define tolerance 0.01

pcat_rcode empty_space_index( tuning_space s, pcat_map ps, gsl_rng *rng, double *res )
{
    size_t closer_to_rand_count = 0, N = pcat_map_size( ps );
    size_t iter;
    pcat_point rand_pts = (pcat_point)malloc( N * s->num_knobs * sizeof( rand_pts[0] ) );
    pcat_point pt = (pcat_point)malloc( s->num_knobs * sizeof( pt[0] ) );
    double history[BLAH];
    for( iter = 1; ; iter++ )
    {
        for( size_t j = 0; j < N; j++ )
        {
            pcat_rcode r = random_point_uniform( s, &rand_pts[ j * s->num_knobs ], rng );
            // g_message( "blah %i", r );
        }
        for( size_t j = 0; j < BLAH2; j++ )
        {
            /* Generate random point pt */
            pcat_rcode r = random_point_uniform( s, pt, rng );
            //g_message( "blah2 %i", r );
            double smallest_dist = G_MAXDOUBLE;
            pcat_map_iter_ iter;
            pcat_rcode r2 = pcat_map_iter_init( ps, &iter );
            //g_message( "blah3 %i", r2 );
            while( iter.has_next( &iter ) )
            {
                pcat_kv_pair_ pv;
                pcat_rcode r3 = iter.next( &iter, &pv );
                //g_message( "blah4 %i", r3 );
                double d = pcat_distance( s, pt, (pcat_point)pv.key );
                smallest_dist = MIN( smallest_dist, d );
            }
            for( size_t k = 0; k < N; k++ )
            {
                if( pcat_distance( s, pt, &rand_pts[ k * s->num_knobs ] ) < smallest_dist )
                {
                    closer_to_rand_count++;
                    break;
                }
            }
        }
        history[ iter % BLAH ] = ((double) closer_to_rand_count ) / ((double) iter * BLAH2 );
        if( iter >= BLAH )
        {
            double min = history[0], max = history[0];
            for( size_t j = 1; j < BLAH; j++ )
            {
                min = MIN( min, history[j]);
                max = MAX( max, history[j]);
            }
            if( max - min < tolerance )
            {
                break;
            }
        }
    }
    *res = history[ iter % BLAH ];
    return PCAT_SUCCESS;
}

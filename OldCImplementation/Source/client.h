/*
 *
 */

#ifndef CLIENT_H_DEFINED
#define CLIENT_H_DEFINED

#include <glib.h>
#include <pcat_global_defs.h>
#include <pcat_map.h>
#include <math.h>
#include <inttypes.h>
#include <gsl/gsl_rng.h>

typedef enum
{
    PCAT_FEATURE_BLAH,
} feature_kind;

typedef enum
{
    PCAT_KNOBK_FIRST = 131, /* Weird value for debugging purposes */
    PCAT_KNOBK_DISCRETE = PCAT_KNOBK_FIRST,
    PCAT_KNOBK_CONTINUOUS,
    PCAT_KNOBK_UNORDERED,
    PCAT_KNOBK_LAST
} pcat_knob_kind;

/* A pcat_knob_value_ is just the raw value.  "Users" have to know what kind it
 * is. */
typedef union pcat_knob_value_ pcat_knob_value_, *pcat_knob_value;
union pcat_knob_value_
{
    int64_t d;
    double c;
    uint8_t u;
};

typedef pcat_knob_value pcat_point;

typedef struct unordered_knob_desc_ unordered_knob_desc_, *unordered_knob_desc;
struct unordered_knob_desc_
{
    uint8_t option_count;
};

typedef struct pcat_knob_ pcat_knob_, *pcat_knob;
struct pcat_knob_
{
    char *name;
    pcat_knob_kind kind;
    union
    {
        struct
        {
            int64_t min, max;
            uint64_t range;
        } d;
        struct
        {
            double min, max;
            double range;
        } c;
        struct
        {
            unordered_knob_desc _;
        } u;
    } _;
};

static inline void print_value( pcat_knob_kind k, pcat_knob_value_ v )
{
    switch( k )
    {
        case PCAT_KNOBK_DISCRETE:
            printf( "%"PRIi64"\n", v.d );
            break;
        case PCAT_KNOBK_CONTINUOUS:
            printf( "%lf\n", v.c );
            break;
        case PCAT_KNOBK_UNORDERED:
        default: abort();
    }
}

pcat_knob_value_ random_knob_value_uniform( pcat_knob k, gsl_rng *r );

#define PCAT_KNOBK_COUNT ( PCAT_KNOBK_LAST - PCAT_KNOBK_FIRST )

typedef struct tuning_space_ tuning_space_, *tuning_space;
struct tuning_space_
{
    size_t num_knobs;
    pcat_knob knobs;
    // feature_t_ *features;
};

pcat_rcode random_point_uniform( tuning_space s, pcat_point vs, gsl_rng *r );

typedef struct pcat_client_ pcat_client_, *pcat_client;
/* move "options" to a separate struct? */
struct pcat_client_
{
    gsl_rng *rng;
    tuning_space_ space;
    pcat_map_ tested_points;
    size_t candidates_per_trial;
    size_t num_points_to_test;
    gchar *program_name;
};

tuning_space get_space( pcat_client c );

pcat_point pcat_point_alloc( tuning_space s );
gboolean pcat_points_equal( pcat_client c, pcat_point p1, pcat_point p2 );
double pcat_distance( tuning_space c, pcat_point p1, pcat_point p2 );

pcat_rcode pcat_points_iterator( pcat_client c, pcat_map_iter i );

typedef struct pcat_pt_test_results_ pcat_pt_test_results_, *pcat_pt_test_results;

pcat_rcode run_pcat_client( void );
void run_client( pcat_client c, pcat_point p );


#endif /* CLIENT_H_DEFINED */

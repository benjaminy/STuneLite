/*
 *
 */

#include <assert.h>
#include <glib.h>
#include <gmodule.h>

G_MODULE_EXPORT extern gint module_test( gchar * szoveg )
{
    g_message ("this is module text: %s\n", szoveg);
    g_message ("this is module variable: %d\n", module_variable);
    return module_variable;
}

typedef struct
{
    gint64 x, y;
} xy_point;

#define OPTION_TYPE( val_type ) typedef struct { gchar exists, val_type val } val_type##_opt;

OPTION_TYPE( gdouble )

typedef struct
{
    gint64 min_x, min_y, max_x, max_y, range_x, range_y;
    gdouble badness_limit;
    GHashTable *values;
} simple_2d_discrete_state;

typedef struct
{
    gdouble_opt goodness;
    gdouble badness;
} good_bad;

guint hash_func( gconstpointer generic_key )
{
    xy_point *key = (xy_point *)generic_key;
    guint64 norm_x = key->x - min_x;
    guint64 norm_y = key->y - min_y;
    guint64 key64 = norm_x + ( range_x * norm_y );
    return (guint)key64;
}

gboolean keys_equal_func(
    gconstpointer generic_a,
    gconstpointer generic_b )
{
    xy_point *a = (xy_point *)generic_a;
    xy_point *b = (xy_point *)generic_b;
    return a->x == b->x && a->y == b->y;
}
                                                         
G_MODULE_EXPORT extern gint cpat_init_client(
    gint64 min_x,
    gint64 min_y,
    gint64 max_x,
    gint64 max_y,
    gdouble badness_limit,
    gpointer *state_ptr )
{
    simple_2d_discrete_state *state = (simple_2d_discrete_state *)gmalloc(
        sizeof( simple_2d_discrete_state ) );
    *state_ptr = state;
    state->min_x = min_x;
    state->min_y = min_y;
    state->max_x = max_x;
    state->max_y = max_y;
    assert( min_x < max_x );
    assert( min_y < max_y );
    state->range_x = max_x - min_x + 1;
    state->range_y = max_y - min_y + 1;

    state->values = g_hash_table_new_full( hash_func, keys_equal_func, g_free, g_free );
    return module_variable;
}

G_MODULE_EXPORT extern cpat_result cpat_eval_client_config(
    gint64 param_x,
    gint64 param_y,
    gdouble_opt &goodness,
    gdouble &badness )
{
    xy_point pt = { param_x, param_y };
    good_bad *gb = (good_bad *)g_hash_table_lookup( values, &pt );
    if( gb != NULL )
    {
        *goodness_opt = gb->goodness_opt;
        *badness = gb->badness;
        return; // success
    }
    /* "else": hash table lookup failed */
    switch()
    {
        case 1:
    }
    g_message ("this is module text: %s\n", szoveg);
    g_message ("this is module variable: %d\n", module_variable);
    return module_variable;
}


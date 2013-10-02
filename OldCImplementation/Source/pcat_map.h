/*
 *
 */

#ifndef PCAT_MAP_H_DEFINED
#define PCAT_MAP_H_DEFINED

#include <pcat_global_defs.h>
#include <glib.h>

typedef struct pcat_map_ pcat_map_, *pcat_map;
struct pcat_map_
{
    GTree *primary_map;
    GSList *key_list;
    GCompareDataFunc compare;
    gpointer compare_data;
    size_t size;
};

pcat_rcode pcat_map_init        ( GCompareDataFunc f, pcat_map m, gpointer comp_data );
pcat_rcode pcat_map_new         ( GCompareDataFunc f, pcat_map *m, gpointer comp_data );
size_t     pcat_map_size        ( pcat_map m );
gboolean   pcat_map_contains_key( pcat_map m, gconstpointer key );
gpointer   pcat_map_lookup      ( pcat_map m, gconstpointer key );
// gboolean    g_tree_lookup_extended  (GTree *tree, gconstpointer lookup_key, gpointer *orig_key, gpointer *value);
pcat_rcode pcat_map_add         ( pcat_map m, gpointer key, gpointer val );
// void        g_tree_foreach          (GTree *tree, GTraverseFunc func, gpointer user_data);
void       pcat_map_destroy     ( pcat_map m );

typedef struct pcat_map_iterator_state_ pcat_map_iterator_state_, *pcat_map_iterator_state;
/* It would be cleaner to put this struct definition in the .c file, but the
 * type needs to be complete for the iterator generator. */
struct pcat_map_iterator_state_
{
    pcat_map m;
    GSList *l;
};

PCAT_TYPED_ITERATOR( pcat_map_iter, pcat_kv_pair_, pcat_map_iterator_state_ )
pcat_rcode pcat_map_iter_init( pcat_map m, pcat_map_iter i );

// GTree*      g_tree_ref              (GTree *tree);
// void        g_tree_unref            (GTree *tree);
// GTree*      g_tree_new_with_data    (GCompareDataFunc f, gpointer d);
// GTree*      g_tree_new_full         (GCompareDataFunc f, gpointer key_compare_data, GDestroyNotify df, GDestroyNotify vf);
// void        g_tree_traverse         (GTree *tree, GTraverseFunc traverse_func, GTraverseType traverse_type, gpointer user_data);
// gpointer    g_tree_search           (GTree *tree, GCompareFunc search_func, gconstpointer user_data);
// gboolean    g_tree_remove           (GTree *tree, gconstpointer key);
// gboolean    g_tree_steal            (GTree *tree, gconstpointer key);
    
#endif

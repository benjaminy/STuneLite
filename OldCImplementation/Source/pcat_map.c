/*
 *
 */

#include <pcat_map.h>

pcat_rcode pcat_map_init( GCompareDataFunc f, pcat_map m, gpointer comp_data )
{
    m->primary_map = g_tree_new_full( f, comp_data, NULL/*key_destroy_func*/, NULL/*value_destroy_func*/ );
    if( m->primary_map == NULL )
        return PCAT_ERR_NO_MEM;
    m->key_list = NULL;
    m->size = 0;
    m->compare = f;
    m->compare_data = comp_data;
    return PCAT_SUCCESS;
}

pcat_rcode pcat_map_new( GCompareDataFunc f, pcat_map *m, gpointer comp_data )
{
    *m = (pcat_map)g_malloc( sizeof( **m ) );
    if( m == NULL )
        return PCAT_ERR_NO_MEM;
    return pcat_map_init( f, *m, comp_data );
}

size_t pcat_map_size( pcat_map m )
{
    return m->size;
}

gpointer pcat_map_lookup( pcat_map m, gconstpointer key )
{
    if( m == NULL || key == NULL )
        return NULL;
    return g_tree_lookup( m->primary_map, key );
}

gboolean pcat_map_contains_key( pcat_map m, gconstpointer key )
{
    return NULL != pcat_map_lookup( m, key );
}

pcat_rcode pcat_map_replace( pcat_map m, gpointer key, gpointer val )
{
    g_tree_insert( m->primary_map, key, val );
    m->size++;
    m->key_list = g_slist_prepend( m->key_list, key );
    if( m->key_list == NULL )
        return PCAT_ERR_NO_MEM;
    return PCAT_SUCCESS;
}

pcat_rcode pcat_map_add( pcat_map m, gpointer key, gpointer val )
{
    if( pcat_map_contains_key( m, key ) )
    {
        return PCAT_ERR_INVALID_INPUT;
    }
    return pcat_map_replace( m, key, val );
}

pcat_rcode pcat_map_remove( pcat_map m, gpointer key )
{
    gboolean had_key = g_tree_remove( m->primary_map, key );
    if( had_key )
    {
        if( m->size < 1 )
        {
            return PCAT_ERR_INVALID_INPUT;
        }
        GSList *l = NULL;
        if( m->compare( key, m->key_list->data, m->compare_data ) == 0 )
        {
            l = m->key_list;
            m->key_list = m->key_list->next;
        }
        else
        {
            GSList *l_prev;
            for( l_prev = m->key_list, l = m->key_list->next;
                 l != NULL;
                 l_prev = l, l = l->next )
            {
                if( m->compare( key, l->data, m->compare_data ) == 0 )
                {
                    l_prev->next = l->next;
                    break;
                }
            }
        }
        if( l == NULL )
        {
            // error
        }
        l->next = NULL;
        g_slist_free( l );
        m->size--;
        return PCAT_SUCCESS;
    }
    /* some other return value? */
    return PCAT_SUCCESS;
}

void pcat_map_destroy( pcat_map m )
{
    g_tree_destroy( m->primary_map );
    g_slist_free( m->key_list );
}


gboolean pcat_map_has_next( pcat_map_iter i )
{
    return NULL != i->state.l;
}

pcat_rcode pcat_map_next( pcat_map_iter i, pcat_kv_pair targ )
{
    if( i->state.l == NULL )
    {
        targ->key = NULL;
        targ->val = NULL;
        return PCAT_ERR_INVALID_INPUT;
    }
    else
    {
        GSList *l = i->state.l;
        i->state.l = i->state.l->next;
        targ->key = l->data;
        targ->val = pcat_map_lookup( i->state.m, targ->key );
        return PCAT_SUCCESS;
    }
}

pcat_rcode pcat_map_iter_init( pcat_map m, pcat_map_iter i )
{
    i->state.m = m;
    i->state.l = m->key_list;
    i->has_next = pcat_map_has_next;
    i->next = pcat_map_next;
    return PCAT_SUCCESS;
}


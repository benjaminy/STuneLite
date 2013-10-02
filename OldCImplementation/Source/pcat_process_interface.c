/**
 * @file   pcat_process_interface.c
 * @Author Ben Ylvisaker
 * @date   March, 2011
 * @brief  Implementation of the PCAT process interface using environment variables
 *         and files.
 *
 * Detailed description of file
 *
 * Copyright stuff
 */

#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <pcat_process_interface.h>

#define PCAT_REPORTED_VALUES_INIT_CAP 10

static pcat_rcode pcat_check_convert_name( char const *name, char *name_conv, size_t n )
{
    if( n < 1
        || !( isalpha( name[0] ) || name[0] == '_' ) )
    {
        return PCAT_ERR_INVALID_INPUT;
    }
    for( size_t i = 0; i < n; i++ )
    {
        if( isalnum( name[i] ) || name[i] == '_' )
        {
            name_conv[i] = toupper( name[i] );
        }
        else
        {
            return PCAT_ERR_INVALID_INPUT;
        }
    }
    name_conv[ n ] = '\0';
    return PCAT_SUCCESS;
}

/**
 *
 */
pcat_rcode pcat_get_value_string( char *name, char **val_str, pcat_client_state s )
{
    PCAT_RET_IF_NULL( PCAT_ERR_INVALID_INPUT, name );
    size_t name_len = strlen( name );
    char *name_upper = (char *)malloc( name_len + 1 );
    if( name_upper == NULL )
    {
        
    }
    PCAT_RET_IF_NOT_SUCCESS( pcat_check_convert_name( name, name_upper, name_len ) );
    size_t long_name_len = name_len + s->value_prefix_len;
    char *long_name = (char *)malloc( long_name_len + 1 );
    if( long_name == NULL )
    {
        free( name_upper );
    }
    snprintf( long_name, long_name_len + 1, "%s%s", PCAT_ENV_VAR_VALUE_PREFIX, name_upper );
    free( name_upper );
    *val_str = getenv( long_name );
    free( long_name );
    return *val_str == NULL ? PCAT_ERR_INVALID_INPUT : PCAT_SUCCESS;
}

/**
 *
 */
pcat_rcode pcat_get_value_real( char *name, double *val, pcat_client_state s )
{
    PCAT_RET_IF_NULL( PCAT_ERR_INVALID_INPUT, val );
    char *val_str;
    PCAT_RET_IF_NOT_SUCCESS( pcat_get_value_string( name, &val_str, s ) );
    int assigned = sscanf( val_str, "%lf", val );
    return assigned == 1 ? PCAT_SUCCESS : PCAT_ERROR;
}

/**
 *
 */
pcat_rcode pcat_get_value_integral( char *name, int64_t *val, pcat_client_state s )
{
    printf( "pcat_get_value_integral %p\n", val );
    PCAT_RET_IF_NULL( PCAT_ERR_INVALID_INPUT, val );
    char *val_str;
    PCAT_RET_IF_NOT_SUCCESS( pcat_get_value_string( name, &val_str, s ) );
    printf( "val_str %s\n", val_str );
    int assigned = sscanf( val_str, "%"SCNi64, val );
    printf( "assigned %i %"PRIi64"\n", assigned, *val );
    return assigned == 1 ? PCAT_SUCCESS : PCAT_ERROR;
}

/**
 *
 */
pcat_rcode pcat_report_failure( int fcode, pcat_client_state s )
{
    s->result.worked = PCAT_CLIENT_FAILURE;
    s->result.fcode = fcode;
    return PCAT_SUCCESS;
}

/**
 *
 */
pcat_rcode pcat_report_success( pcat_client_state s )
{
    s->result.worked = PCAT_CLIENT_SUCCESS;
    return PCAT_SUCCESS;
}

/*
 * 
 */
pcat_rcode pcat_report_generic_value( char *name, pcat_generic_value_t val, pcat_client_state s )
{
    PCAT_RET_IF_NULL( PCAT_ERR_INVALID_INPUT, name );
    size_t name_len = strlen( name );
    char *name_upper = (char *)malloc( name_len + 1 );
    PCAT_RET_IF_NULL( PCAT_ERR_NO_MEM, name_upper );
    PCAT_RET_IF_NOT_SUCCESS( pcat_check_convert_name( name, name_upper, name_len ) );
    PCAT_RET_IF_NULL( PCAT_ERROR, s->values);
    for( size_t i = 0; i < s->values_count; i++ )
    {
        if( strncmp( name_upper, s->values[i].name, name_len + 1 ) == 0 )
        {
            /* Overwrite previously reported value */
            s->values[i].val = val;
            free( name_upper );
            return PCAT_SUCCESS;
        }
    }
    pcat_reported_value_ rval = { name_upper, val };
    PCAT_RET_IF( PCAT_ERROR, s->values_count > s->values_cap );
    if( s->values_count == s->values_cap )
    {
        s->values_cap *= 2;
        s->values =
            (pcat_reported_value)realloc( s->values,
                                          s->values_cap
                                          * sizeof( s->values[0] ) );
        PCAT_RET_IF_NULL( PCAT_ERROR, s->values );
    }
    s->values[ s->values_count ] = rval;
    s->values_count++;
    return PCAT_SUCCESS;
}

/**
 *
 */
pcat_rcode pcat_report_string( char *name, char *s_val, pcat_client_state s )
{
    pcat_generic_value_t val;
    val.tag = PCAT_GENERIC_VALUE_STRING;
    val._.s = strdup( s_val );
    PCAT_RET_IF_NULL( PCAT_ERR_NO_MEM, val._.s );
    return pcat_report_generic_value( name, val, s );
}

/**
 *
 */
pcat_rcode pcat_report_real( char *name, double r_val, pcat_client_state s )
{
    pcat_generic_value_t val;
    val.tag = PCAT_GENERIC_VALUE_REAL;
    val._.r = r_val;
    return pcat_report_generic_value( name, val, s );
}

/**
 *
 */
pcat_rcode pcat_report_integral( char *name, int64_t i_val, pcat_client_state s )
{
    pcat_generic_value_t val;
    val.tag = PCAT_GENERIC_VALUE_INTEGRAL;
    val._.i = i_val;
    return pcat_report_generic_value( name, val, s );
}

pcat_rcode pcat_client_lib_initialize( pcat_client_state s )
{
    s->value_prefix_len = strlen( PCAT_ENV_VAR_VALUE_PREFIX );
    s->values_cap = PCAT_REPORTED_VALUES_INIT_CAP;
    s->values_count = 0;
    s->values = (pcat_reported_value)malloc( s->values_cap * sizeof( s->values[0] ) );
    PCAT_RET_IF_NULL( PCAT_ERROR, s->values );
    return PCAT_SUCCESS;
}

pcat_rcode pcat_client_lib_finalize( pcat_client_state s )
{
    printf( "pcat_atexit running %zu\n", s->values_count );
    switch( s->result.worked )
    {
    case PCAT_CLIENT_SUCCESS:
        printf( "PCAT_CLIENT_SUCCESS\n" );
        break;
    case PCAT_CLIENT_FAILURE:
        printf( "PCAT_CLIENT_FAILURE=%"PRIi32"\n", s->result.fcode );
        break;
    default:
        abort();
    }
    if( s->values_count < 1 )
    {
        return PCAT_SUCCESS;
    }
    printf( "\n" );
    for( size_t i = 0; i < s->values_count; i++ )
    {
        pcat_reported_value_ v = s->values[i];
        printf( "%s%s:", PCAT_CLIENT_REPORT_PREFIX, v.name );
        switch( v.val.tag )
        {
            case PCAT_GENERIC_VALUE_STRING:
                printf( "STRING=%s\n", v.val._.s );
                free( v.val._.s );
                v.val._.s = NULL;
                break;
            case PCAT_GENERIC_VALUE_REAL:
                printf( "REAL=%lf\n", v.val._.r );
                break;
            case PCAT_GENERIC_VALUE_INTEGRAL:
                printf( "INTEGRAL=%"PRIi64"\n", v.val._.i );
                break;
            default:
                /* report error? */
                break;
        }
        free( v.name );
        v.name = NULL;
    }
    s->values_count = 0;
    free( s->values );
    /* Make sure _something_ gets printed to standard error */
    fprintf( stderr, "\n" );
    return PCAT_SUCCESS;
}

int pcat_client_main_wrapper( pcat_main_t m, int argc, char** argv )
{
    pcat_client_state_ s;
    if( argc < 0 )
    {
        fprintf( stderr, "err 1\n" );
        return -1;
    }
    if( pcat_client_lib_initialize( &s ) != PCAT_SUCCESS )
    {
        fprintf( stderr, "initialization failed!!\n" );
        return -1;
    }
    int rv = m( argc, argv, &s );
    if( pcat_client_lib_finalize( &s ) != PCAT_SUCCESS )
    {
        fprintf( stderr, "err 3\n" );
        return -1;
    }
    return rv;
}

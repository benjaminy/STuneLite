/*
 *
 */

#ifndef PCAT_PROCESS_INTERFACE_H_DEFINED
#define PCAT_PROCESS_INTERFACE_H_DEFINED

#include <pcat_global_defs.h>

#define PCAT_ENV_VAR_VALUE_PREFIX "PCAT_VALUE_"
#define PCAT_SENSOR_READINGS_FILENAME "PCAT_SENSOR_READINGS_FILENAME"
#define PCAT_CLIENT_REPORT_PREFIX "PCAT_CLIENT_REPORT_"
#define PCAT_CLIENT_REPORT_TYPE "TYPE_"
#define PCAT_CLIENT_REPORT_VALUE "VALUE_"

typedef enum
{
    PCAT_GENERIC_VALUE_START_OF_ENUM = 117,
    PCAT_GENERIC_VALUE_STRING = PCAT_GENERIC_VALUE_START_OF_ENUM,
    PCAT_GENERIC_VALUE_REAL,
    PCAT_GENERIC_VALUE_INTEGRAL,
    PCAT_GENERIC_VALUE_END_OF_ENUM,
} pcat_generic_value_tag;

typedef struct
{
    enum{ PCAT_CLIENT_SUCCESS, PCAT_CLIENT_FAILURE, } worked;
    int32_t fcode;
} pcat_client_result;

static inline const gchar* generic_value_tag_string( pcat_generic_value_tag t )
{
    /* assert t >= PCAT_GENERIC_VALUE_START_OF_ENUM && t < PCAT_GENERIC_VALUE_END_OF_ENUM */
    switch( t )
    {
        case PCAT_GENERIC_VALUE_STRING:     return "STRING";
        case PCAT_GENERIC_VALUE_REAL:       return "REAL";
        case PCAT_GENERIC_VALUE_INTEGRAL:   return "INTEGRAL";
        default: return "ERRRRRORORORORO";
    }
}

typedef struct
{
    pcat_generic_value_tag tag;
    union
    {
        char *s;
        double r;
        int64_t i;
    } _;
} pcat_generic_value_t;

typedef struct pcat_reported_value_ pcat_reported_value_, *pcat_reported_value;

struct pcat_reported_value_
{
    char *name;
    pcat_generic_value_t val;
    char *original_string;
};

typedef struct pcat_client_state_ pcat_client_state_, *pcat_client_state;
struct pcat_client_state_
{
    size_t value_prefix_len;
    pcat_reported_value values;
    size_t values_cap;
    size_t values_count;
    pcat_client_result result;
};

pcat_rcode pcat_client_lib_initialize( pcat_client_state s );
pcat_rcode pcat_client_lib_finalize( pcat_client_state s );
typedef int (pcat_main_t)( int argc, char** argv, pcat_client_state s );

#endif

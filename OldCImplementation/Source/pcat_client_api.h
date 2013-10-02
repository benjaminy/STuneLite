/*
 *
 */

#ifndef PCAT_CLIENT_API_H_DEFINED
#define PCAT_CLIENT_API_H_DEFINED

#include <inttypes.h>
#include <pcat_global_defs.h>
#include <pcat_process_interface.h>

pcat_rcode pcat_client_lib_initialize( pcat_client_state s );
pcat_rcode pcat_client_lib_finalize( pcat_client_state s );
pcat_rcode pcat_get_value_string  ( char *name, char*   *val, pcat_client_state s );
pcat_rcode pcat_get_value_real    ( char *name, double  *val, pcat_client_state s );
pcat_rcode pcat_get_value_integral( char *name, int64_t *val, pcat_client_state s );
pcat_rcode pcat_report_string  ( char *name, char   *val, pcat_client_state s );
pcat_rcode pcat_report_real    ( char *name, double  val, pcat_client_state s );
pcat_rcode pcat_report_integral( char *name, int64_t val, pcat_client_state s );
pcat_rcode pcat_report_failure( int32_t fcode, pcat_client_state s );
pcat_rcode pcat_report_success( pcat_client_state s );

int pcat_client_main_wrapper( pcat_main_t main, int argc, char** argv );

#endif

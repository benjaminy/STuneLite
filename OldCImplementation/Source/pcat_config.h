/*
 *
 */

#ifndef PCAT_CONFIG_H_DEFINED
#define PCAT_CONFIG_H_DEFINED

#include <glib.h>

#define DEFAULT_PRNG_SEED 2
extern gint64 prng_seed;

#define DEFAULT_TEMP_HACK_DB_NAME "DEFAULT_TEMP_HACK_DB_NAM"
extern char *temp_hack_db_nam;

#define DEFAULT_TEMP_HACK_SQL_CMD "DEFAULT_TEMP_HACK_SQL_"
extern char *temp_hack_sql_cmd;

void process_command_line_option(
    int argc,
    char *argv[] );

#endif // CPAT_CONFIG_H_DEFINED

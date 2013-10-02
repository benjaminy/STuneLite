#include <stdio.h>
#include <stdlib.h>
#include <sqlite3.h>

#include <pcat_config.h>

static int callback( void *NotUsed, int argc, char **argv, char **azColName ) {
    NotUsed=0;
    int i;
    for(i=0; i<argc; i++){
      printf("%s = %s\n", azColName[i], argv[i] ? argv[i]: "NULL");
    }
    printf("\n");
    return 0;
}

int db_test() {
  sqlite3 *db;
  char *zErrMsg = 0;
  int rc;

  rc = sqlite3_open( temp_hack_db_nam, &db );
  if( rc ){
    fprintf(stderr, "Can't open database: %s\n", sqlite3_errmsg(db));
    sqlite3_close(db);
    exit(1);
  }
  rc = sqlite3_exec(db, temp_hack_sql_cmd, callback, 0, &zErrMsg);
  if( rc!=SQLITE_OK ){
    fprintf(stderr, "SQL error: %s\n", zErrMsg);
    /* This will free zErrMsg if assigned */
    if (zErrMsg)
       free(zErrMsg);
  }
  sqlite3_close(db);
  return 0;
}

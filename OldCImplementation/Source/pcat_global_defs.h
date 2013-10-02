/*
 *
 */

#ifndef PCAT_GLOBAL_DEFS_H_DEFINED
#define PCAT_GLOBAL_DEFS_H_DEFINED

#include <stdlib.h>
#include <inttypes.h>
#include <assert.h>
#include <glib.h>

/* Result code */
typedef enum
{
    PCAT_SUCCESS = 0,
    PCAT_NO_MATCH,
    PCAT_ERR_INVALID_INPUT,
    PCAT_ERR_NO_MEM,
    PCAT_ERR_OVERFLOW,
    PCAT_ERROR,
    PCAT_RESULT_END_OF_LIST
} pcat_rcode;


// file system stuff
// [EBADF] Bad file descriptor.
// [EEXIST] File exists.
// [EFBIG] File too large.
// [EISDIR] Is a directory.
// [ELOOP] Too many levels of symbolic links.
// [EMFILE] File descriptor value too large.
// [EMLINK] Too many links.
// [ENAMETOOLONG] Filename too long.
// [ENFILE] Too many files open in system.
// [ENOENT] No such file or directory.
// [ENOEXEC] Executable file format error.
// [ENOTDIR] Not a directory.
// [ENOTEMPTY] Directory not empty.
// [EROFS] Read-only file system.
// [ETXTBSY] Text file busy.
// 
// network stuff
// [EHOSTUNREACH] Host is unreachable.
// [ENETDOWN] Network is down.
// [ENETRESET] Connection aborted by network.
// [ENETUNREACH] Network unreachable.
// [ECONNABORTED] Connection aborted.
// [ECONNREFUSED] Connection refused.
// [ECONNRESET] Connection reset.
// [EISCONN] Socket is connected.
// [ENOTCONN] The socket is not connected.
// [ETIMEDOUT] Connection timed out.
// 
// [E2BIG] Argument list too long.
// [EACCES] Permission denied.
// [EADDRINUSE] Address in use.
// [EADDRNOTAVAIL] Address not available.
// [EAFNOSUPPORT] Address family not supported.
// [EAGAIN] Resource unavailable, try again (may be the same value as [EWOULDBLOCK]).
// [EALREADY] Connection already in progress.
// [EBADMSG] Bad message.
// [EBUSY] Device or resource busy.
// [ECANCELED] Operation canceled.
// [ECHILD] No child processes.
// [EDEADLK] Resource deadlock would occur.
// [EDESTADDRREQ] Destination address required.
// [EDOM] Mathematics argument out of domain of function.
// [EDQUOT] Reserved.
// [EFAULT] Bad address.
// [EIDRM] Identifier removed.
// [EILSEQ] Illegal byte sequence.
// [EINPROGRESS] Operation in progress.
// [EINTR] Interrupted function.
// [EIO] I/O error.
// [EMSGSIZE] Message too large.
// [EMULTIHOP] Reserved.
// [ENOBUFS] No buffer space available.
// [ENODATA]
// [OB XSR]  No message is available on the STREAM head read queue. 
// [ENODEV] No such device.
// [ENOLCK] No locks available.
// [ENOLINK] Reserved.
// [ENOMSG] No message of the desired type.
// [ENOPROTOOPT] Protocol not available.
// [ENOSPC] No space left on device.
// [ENOSR]
// [OB XSR]  No STREAM resources. 
// [ENOSTR]
// [OB XSR]  Not a STREAM. 
// [ENOSYS] Function not supported.
// [ENOTRECOVERABLE] State not recoverable.
// [ENOTSOCK] Not a socket.
// [ENOTSUP] Not supported (may be the same value as [EOPNOTSUPP]).
// [ENOTTY] Inappropriate I/O control operation.
// [ENXIO] No such device or address.
// [EOPNOTSUPP] Operation not supported on socket (may be the same value as [ENOTSUP]).
// [EOWNERDEAD] Previous owner died.
// [EPERM] Operation not permitted.
// [EPIPE] Broken pipe.
// [EPROTO] Protocol error.
// [EPROTONOSUPPORT] Protocol not supported.
// [EPROTOTYPE] Protocol wrong type for socket.
// [ERANGE] Result too large.
// [ESPIPE] Invalid seek.
// [ESRCH] No such process.
// [ESTALE] Reserved.
// [ETIME]
// [OB XSR]  Stream ioctl() timeout. 
// [EWOULDBLOCK] Operation would block (may be the same value as [EAGAIN]).
// [EXDEV] Cross-device link.
// 
// Additional error numbers may be defined on conforming systems; see the System Interfaces volume of POSIX.1-2008.




#define ASRT_SUCCESS(r) \
    do { \
        pcat_rcode res = r; \
        assert( res == PCAT_SUCCESS ); \
    } while( 0 )

#define PCAT_RET_IF_NOT_SUCCESS(r) \
    do { \
        pcat_rcode res = r; \
        if( res != PCAT_SUCCESS ) \
        { \
            return res; \
        } \
    } while( 0 )

#define PCAT_RET_IF_NULL(r,p) \
    do { \
        void *ptr = (void *)p; \
        pcat_rcode res = r; \
        if( ptr == NULL ) \
        { \
            return res; \
        } \
    } while( 0 )

#define PCAT_RET_IF(r,c) \
    do { \
        int cond = c; \
        pcat_rcode res = r; \
        if( cond ) \
        { \
            return res; \
        } \
    } while( 0 )

typedef struct pcat_kv_pair_ pcat_kv_pair_, *pcat_kv_pair;
struct pcat_kv_pair_
{
    gpointer key, val;
};

#define PCAT_TYPED_KV_PAIR( n, kt, vt ) \
typedef struct n##_ n##_, *n; \
struct n##_ \
{ \
    kt key; \
    vt val; \
};

typedef struct generic_iterator_ generic_iterator_, *generic_iterator;

struct generic_iterator_
{
    void *data;
    gboolean (* has_next)( generic_iterator i );
    void *(* next)( generic_iterator i );
};

#define PCAT_TYPED_ITERATOR( n, t, st ) \
typedef struct n##_ n##_, *n; \
struct n##_ \
{ \
    st state; \
    gboolean (* has_next)( n i ); \
    pcat_rcode (* next)( n i, t *targ ); \
};

#endif

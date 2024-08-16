#include <jni.h>
#include <sys/socket.h>
#include <cerrno>
#include <cstring>
#include <iostream>
#include <netinet/in.h>

/**
* https://www.kfu.com/~nsayer/Java/jni-filedesc.html
*/
extern "C"
JNIEXPORT jobject JNICALL Java_com_jasonernst_icmp_1linux_ICMPLinux_socket(JNIEnv *env, jclass _ignore, jint domain, jint type, jint protocol) {
    jfieldID field_fd;
    jmethodID const_fdesc;
    jclass class_fdesc, class_ioex;
    jobject ret;
    int fd;

    class_ioex = env->FindClass("java/io/IOException");
    if (class_ioex == NULL) return NULL;
    class_fdesc = env->FindClass("java/io/FileDescriptor");
    if (class_fdesc == NULL) return NULL;

    fd = socket(domain, type, protocol);

    if (fd < 0) {
        // open returned an error. Throw an IOException with the error string
        char buf[1024];
        sprintf(buf, "open: %s", strerror(errno));
        env->ThrowNew(class_ioex, buf);
        return NULL;
    }

    // construct a new FileDescriptor
    const_fdesc = env->GetMethodID(class_fdesc, "<init>", "()V");
    if (const_fdesc == NULL) return NULL;
    ret = env->NewObject(class_fdesc, const_fdesc);

    // poke the "fd" field with the file descriptor
    field_fd = env->GetFieldID(class_fdesc, "fd", "I");
    if (field_fd == NULL) return NULL;
    env->SetIntField(ret, field_fd, fd);

    // and return it
    return ret;
}

int getFDFromFileDescriptor(JNIEnv *env, jobject fd) {
    jfieldID field_fd;
    jclass class_fdesc;
    int ret;

    class_fdesc = env->FindClass("java/io/FileDescriptor");
    if (class_fdesc == NULL) return -1;

    field_fd = env->GetFieldID(class_fdesc, "fd", "I");
    if (field_fd == NULL) return -1;

    ret = env->GetIntField(fd, field_fd);
    return ret;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_jasonernst_icmp_1linux_ICMPLinux_setsockoptInt(JNIEnv *env, jclass _ignore, jobject fileDescriptor, jint level, jint optname, jint optval) {
    int fd = getFDFromFileDescriptor(env, fileDescriptor);
    return setsockopt(fd, level, optname, &optval, sizeof(optval));
}

extern "C"
JNIEXPORT jint JNICALL Java_com_jasonernst_icmp_1linux_ICMPLinux_setsocketRecvTimeout(JNIEnv *env, jclass _ignore, jobject fileDescriptor, jlong sec, jlong usec) {
    struct timeval tv;
    tv.tv_sec = sec;
    tv.tv_usec = usec;
    int fd = getFDFromFileDescriptor(env, fileDescriptor);
    return setsockopt(fd, SOL_SOCKET, SO_RCVTIMEO, &tv, sizeof(tv));
}

extern "C"
JNIEXPORT jint JNICALL Java_com_jasonernst_icmp_1linux_ICMPLinux_sendTo(JNIEnv *env, jclass _ignore, jobject fileDescriptor, jbyteArray data, jint flags, jbyteArray address, jint port) {

    jbyte *data_ptr, *addr_ptr;
    jsize data_len, addr_len;
    data_ptr = env->GetByteArrayElements(data, NULL);
    data_len = env->GetArrayLength(data);
    addr_ptr = env->GetByteArrayElements(address, NULL);
    addr_len = env->GetArrayLength(address);

    int fd = getFDFromFileDescriptor(env, fileDescriptor);

    // determine if we have an ipv4 or ipv6 address by length
    int ret;
    if (addr_len == 4) {
        printf("IPv4 address\n");
        struct sockaddr_in addr;
        memset(&addr, 0, sizeof(addr));
        addr.sin_family = AF_INET;
        addr.sin_port = htons(port);
        memcpy(&addr.sin_addr, addr_ptr, addr_len);
        ret = sendto(fd, data_ptr, data_len, flags, (struct sockaddr *)&addr, sizeof(addr));
    } else if (addr_len == 16) {
        printf("IPv6 address\n");
        struct sockaddr_in6 addr;
        memset(&addr, 0, sizeof(addr));
        addr.sin6_family = AF_INET6;
        addr.sin6_port = htons(port);
        memcpy(&addr.sin6_addr, addr_ptr, addr_len);
        ret = sendto(fd, data_ptr, data_len, flags, (struct sockaddr *)&addr, sizeof(addr));
    } else {
        printf("Unknown address length\n");
    }

    if (ret < 0) {
        // sendTo returned an error. Throw an IOException with the error string
        char buf[1024];
        sprintf(buf, "sendTo: %s", strerror(errno));
        jclass class_ioex = env->FindClass("java/io/IOException");
        env->ThrowNew(class_ioex, buf);
    }

    env->ReleaseByteArrayElements(data, data_ptr, JNI_ABORT);
    env->ReleaseByteArrayElements(address, addr_ptr, JNI_ABORT);

    return ret;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_jasonernst_icmp_1linux_ICMPLinux_recvFrom(JNIEnv *env, jclass _ignore, jobject fileDescriptor, jbyteArray data, jint flags, jbyteArray address, jint port) {
    struct sockaddr_in addr;
    jbyte *data_ptr, *addr_ptr;
    jsize data_len, addr_len;
    int ret;

    data_ptr = env->GetByteArrayElements(data, NULL);
    data_len = env->GetArrayLength(data);
    addr_ptr = env->GetByteArrayElements(address, NULL);
    addr_len = env->GetArrayLength(address);

    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port = htons(port);
    int fd = getFDFromFileDescriptor(env, fileDescriptor);
    ret = recvfrom(fd, data_ptr, data_len, flags, (struct sockaddr *)&addr, (socklen_t *)&addr_len);

    if (ret < 0) {
        // recvFrom returned an error. Throw an IOException with the error string
        char buf[1024];
        sprintf(buf, "recvFrom: %s", strerror(errno));
        jclass class_ioex = env->FindClass("java/io/IOException");
        env->ThrowNew(class_ioex, buf);
    }

    env->ReleaseByteArrayElements(data, data_ptr, 0);
    env->ReleaseByteArrayElements(address, addr_ptr, 0);

    return ret;
}
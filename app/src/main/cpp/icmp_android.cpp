// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("icmp_android");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("icmp_android")
//      }
//    }
#include <cstdio>
#include <cstdlib>

#include <cerrno>
#include <unistd.h>
#include <linux/icmp.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <jni.h>
#include <android/log.h>
#include <cstring>

int ping(const char *a, const int count)
{
    struct sockaddr_in addr;
    const sockaddr_in * addr_ptr = &addr;
    struct icmphdr icmp_hdr;
    char packetdata[sizeof(icmp_hdr) + 5];

    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;

    if (inet_pton(AF_INET, a, &(addr.sin_addr)) < 0)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ICMP", "inet_pton errno %d %s\n", errno, strerror(errno));

        return EXIT_FAILURE;
    }

    int sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_ICMP);

    if(sock < 0)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ICMP", "socket errno %d %s\n", errno, strerror(errno));

        return EXIT_FAILURE;
    }

    memset(&icmp_hdr, 0, sizeof(icmp_hdr));
    icmp_hdr.type = ICMP_ECHO;
    icmp_hdr.un.echo.id = 1234;
    icmp_hdr.un.echo.sequence = 1;

    memcpy(packetdata, &icmp_hdr, sizeof(icmp_hdr));
    memcpy(packetdata + sizeof(icmp_hdr), "12345", 5);

    if(sendto(sock, packetdata, sizeof(packetdata), 0, reinterpret_cast<const sockaddr *>(addr_ptr), sizeof(addr)) < 0)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ICMP", "sendto errno %d %s\n", errno, strerror(errno));

        return EXIT_FAILURE;
    }

    fd_set read_set;
    socklen_t slen;
    int rc;
    struct timeval timeout = {3, 0};
    struct icmphdr rcv_hdr;

    memset(&read_set, 0, sizeof(read_set));
    FD_SET(sock, &read_set);

    rc = select(sock + 1, &read_set, NULL, NULL, &timeout);
    if (rc == 0)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ICMP", "no reply in 3 second\n");

        return EXIT_FAILURE;
    }
    else if (rc < 0)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ICMP", "select errno %d %s\n", errno, strerror(errno));
        return EXIT_FAILURE;
    }

    unsigned char data[2048];

    slen = 0;
    rc = recvfrom(sock, data, sizeof(data), 0, NULL, &slen);
    if (rc <= 0)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ICMP", "revcfrom errno %d %s\n", errno, strerror(errno));
        return EXIT_FAILURE;
    }
    else if (rc < sizeof(rcv_hdr))
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ICMP", "error got short ICMP packet, %d bytes\n", rc);

        return EXIT_FAILURE;
    }

    memcpy(&rcv_hdr, data, sizeof(rcv_hdr));
    if (rcv_hdr.type == ICMP_ECHOREPLY)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ICMP", "ICMP Reply, id=0x%x, sequence =  0x%x\n",
                            icmp_hdr.un.echo.id, icmp_hdr.un.echo.sequence);
    }
    else
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ICMP", "Got ICMP packet with type 0x%x ?!?\n", rcv_hdr.type);
    }

    __android_log_print(ANDROID_LOG_DEBUG, "ICMP", "ICMP ECHO succeed\n");
    return EXIT_SUCCESS;
}

extern "C" {
    JNIEXPORT jint JNICALL Java_com_github_compscidr_icmp_1android_MainActivity_00024Companion_ping(JNIEnv *env, jobject thiz, jstring ip, jint count) {
        const char *ip_str = env->GetStringUTFChars(ip, 0);
        int result = ping(ip_str, count);
        env->ReleaseStringUTFChars(ip, ip_str);
        return result;
    }
}
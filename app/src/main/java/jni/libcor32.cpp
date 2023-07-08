#include "com_android_ppp_data_libcor32.h"
#include "ancillary.h"
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <netdb.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <sys/time.h>
#include <netinet/tcp.h>
#include <error.h>
#include <sys/poll.h>
#include <sys/un.h>

JNIEXPORT void JNICALL Java_com_android_ppp_data_libcor32_closesocket(JNIEnv *env, jobject obj, jint sock)
{
    int fd = sock;
    close(fd);
}

JNIEXPORT jboolean JNICALL Java_com_android_ppp_data_libcor32_sendack(JNIEnv* env, jobject obj, jint connection, jint errcode)
{
    char err = errcode;
    int rc = send(connection, &err, 1, MSG_NOSIGNAL);

    // 关闭套接字句柄
    close(connection);
    return rc >= 0;
}

JNIEXPORT jint JNICALL Java_com_android_ppp_data_libcor32_create_1unix_1socket(JNIEnv *env, jobject obj, jstring path)
{
    //转换为 char*类型
    const char* unix_path = env->GetStringUTFChars(path , NULL);
    int sock = socket(AF_UNIX, SOCK_STREAM, 0);
    if (sock < 0) {
        return -1011;
    }

    unlink(unix_path);

    struct sockaddr_un addr;
    memset(&addr, 0, sizeof(addr));

    addr.sun_family = AF_UNIX;
    strncpy(addr.sun_path, unix_path, sizeof(addr.sun_path) - 1);

    if (bind(sock, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
        close(sock);
        return -1012;
    }

    if (listen(sock, 1000) < 0) {
        close(sock);
        return -1013;
    }

    return sock;
}

JNIEXPORT jintArray Java_com_android_ppp_data_libcor32_recvfd(JNIEnv *env, jobject obj, jint sock)
{
    int len = 3;
    jintArray results = env->NewIntArray(len);
    jint *elems = env->GetIntArrayElements(results, NULL);

    int server = sock;
    for (; ;) {
        struct sockaddr_un remoteEP;
        memset(&remoteEP, 0, sizeof(remoteEP));

        socklen_t size = sizeof(remoteEP);
        int connection = accept(server, (struct sockaddr*)&remoteEP, &size);
        if (connection < 0) {
            LOGE("ERROR : GetFiledescriptor connection");
            elems[0] = -1;
            elems[1] = connection;
            elems[2] = -10002;
            env->ReleaseIntArrayElements(results, elems, 0);
            return results;
        }

        int fd = -1;
        if (ancil_recv_fd(connection, &fd)) {
             LOGE("ERROR : ancil_recv_fd result fd is %d", fd);
             close(connection);
             continue;
        }

        if (fd == -1) {
            LOGE("ERROR : GetFD fd equals -1 ");
            close(connection);
            continue;
        }

        elems[0] = fd;
        elems[1] = connection;
        elems[2] = fd == -1 ? -10003 : 0;
        env->ReleaseIntArrayElements(results, elems, 0);
        return results;
    }

    elems[0] = -1;
    elems[1] = -1;
    elems[2] = -10001;
    env->ReleaseIntArrayElements(results, elems, 0);
    return results;
}
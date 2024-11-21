#ifndef SLFF4J_H
#define SLFF4J_H

#include <jni.h>
#include <string>
#include <assert.h>

/**
 * A JNI wrapper for the SLF4J logging library.
 *
 * @author Andrea Leofreddi
 * https://www.vleo.net/using-slf4j-from-c-jni/
 * see: https://github.com/BumpApp/bump_android/blob/ed4c3fded04d4fbf415e9052ffea998ffde8f881/native/src/main/cpp/icmp_common.cpp
 */

class Log {
public:
    template<typename... Ts>
    void info(const std::string &string, Ts &&... args);

    Log(JNIEnv *env, jclass clazz);

private:
    JNIEnv *env;
    const jobject logger;
    const jmethodID infoMethod;

    inline jobject &toJava(jobject &value);
    inline jstring toJava(const char *value);
    inline jstring toJava(const std::string &value);
    inline jobject toJava(int value);

    inline void toArgArray(JNIEnv *env, jobjectArray &array, int position);

    template<typename T, typename... Ts>
    inline void toArgArray(JNIEnv *env, jobjectArray &array, int position, T && current, Ts &&... args);
};

template<typename... Ts>
void Log::info(const std::string &format, Ts &&... args) {
    auto argArray = env->NewObjectArray(sizeof...(args), env->FindClass("java/lang/Object"), nullptr);
    toArgArray(env, argArray, 0, std::forward<Ts>(args)...);
    env->CallVoidMethod(logger, infoMethod, toJava(format), argArray);
}

void Log::toArgArray(JNIEnv *env, jobjectArray &array, int position) {
}

template<typename T, typename... Ts>
void Log::toArgArray(JNIEnv *env, jobjectArray &array, int position, T && current, Ts &&... args) {
    env->SetObjectArrayElement(array, position, toJava(std::forward<T>(current)));
    toArgArray(env, array, position + 1, std::forward<Ts>(args)...);
}

jobject &Log::toJava(jobject &value) {
    return value;
}

jstring Log::toJava(const char *value) {
    return env->NewStringUTF(value);
}

jstring Log::toJava(const std::string &value) {
    return env->NewStringUTF(value.c_str());
}

jobject Log::toJava(int value) {
    auto class_ = env->FindClass("java/lang/Integer");
    assert(class_);

    auto ctor = env->GetMethodID(class_, "<init>", "(I)V");
    assert(ctor);

    return env->NewObject(class_, ctor, value);
}
#endif
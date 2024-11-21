#include "slf4j_native.h"

/**
 * https://www.vleo.net/using-slf4j-from-c-jni/
 */

/**
 * Assuming that the class provided has a "logger" field, this method will return the logger object.
 * @param env the JNI environment
 * @param clazz the Java object that contains the logger field
 * @return the logger object
 */
static jobject getLogObject(JNIEnv *env, jclass clazz) {
    assert(clazz);

    auto class_ = env->GetObjectClass(clazz);
    assert(class_);

    auto logField = env->GetStaticFieldID(clazz, "logger", "Lorg/slf4j/Logger;");
    assert(logField);

    auto logObject = env->GetStaticObjectField(clazz, logField);
    assert(logObject);

    return logObject;
}

static jmethodID getLogMethod(JNIEnv *env, jobject log, std::string method) {
    auto methodId = env->GetMethodID(env->GetObjectClass(log), method.c_str(), "(Ljava/lang/String;[Ljava/lang/Object;)V");
    assert(methodId);

    return methodId;
}

Log::Log(JNIEnv *env, jclass clazz): env(env), logger(getLogObject(env, clazz)), infoMethod(getLogMethod(env, logger, "info")) {
}
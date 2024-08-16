# icmp-android 
A simple library to ping a host using ICMP on Android or Linux JVM.

## Usage

### Android
The android library depends on the android.system.Os.socket() method. The common library abstracts
this method + some other native methods so that the library can be used on both Android and Linux
JVM implementations.
```kotlin
implementation("com.jasonernst.icmp:icmp-android")
```

Using the Library:
```kotlin
import com.jasonernst.icmp.ICMPAndroid 
import java.net.InetAddress

// this will ping google.com with a dns resolution timeout of 500ms and an ICMP timeout of 1000ms
ICMPAndroid.ping("google.com", 500, 1000)

// this will ping the local host with a timeout of 1000ms. Note that the InetAddress object is used
// here, and that when we do this, there is no DNS request made because we already have the IP address
ICMPAndroid.ping(InetAddress.getLocalHost(), 1000)
```

### Linux JVM
Note the the Linux JVM implementation uses a .so file that is built using cmake. For unit tests,
the .so file is added to the lib path for the test task. For the actual library, the .so file is
included in the jar file. This means that the .so file will be extracted to /tmp on the filesystem
and read from there. This is not ideal, but it is a workaround for now. What should happen, is that
the .so is produced as a separate artifact and then included in the library as a dependency and the
location of the .so file is added to the java.library.path. This is a future improvement.
```kotlin
implementation("com.jasonernst.icmp:icmp-linux")
```

Using the library:
```kotlin
import java.net.InetAddress
import com.jasonernst.icmp.ICMPLinux

// this will ping google.com with a dns resolution timeout of 500ms and an ICMP timeout of 1000ms
ICMPLinux.ping("google.com", 500, 1000)

// this will ping the local host with a timeout of 1000ms. Note that the InetAddress object is used
// here, and that when we do this, there is no DNS request made because we already have the IP address
ICMPLinux.ping(InetAddress.getLocalHost(), 1000)
```

## Building locally:
If changes are made to icmp-common, you will need to publish it locally. 
This can be done by running the `publishToMavenLocal` task in the icmp-common module.

## Todo:
- [x] Structure into a library and an app that uses it
- [x] Release the library to maven central so it can be used
- [ ] Implement icmp v4
- [ ] Implement icmp v6
- [x] Implement tests for both Ipv4 and Ipv6
  - tests working on both JVM and Android Instrumented Tests 
- [ ] Hookup instrumented tests in CI/CD with actual phone on self-hosted runner
- [x] Investigate whether there is a similar function call for android.system.Os.socket() for 
      non-Android Kotlin. This will make it possible to run tests on a non-Android JVM, ie) the unit
      tests.
  - Implemented an abstraction that still uses cmake / native code for the non-android JVM. This
    also means this could be released as a cross-platform android / JVM library.

## Inspirations
- Originally motivated by https://github.com/kirillF/icmp-android but updated since it no longer
  worked
- Then found https://github.com/marsounjan/icmp4a so instead of using cmake and ndk, we can use
  kotlin directly with the android.system.Os.socket() method. Prior to finding this library, I
  didn't realize those API methods were available. Playing around with the demo app, I found that
  upon changing the host, it could lead to delays up upwards of one minute. I'm not sure if it's
  because of the underlying Flow API or something - it could be that there is some blocking until
  all of the attempts have failed for a particular host or something. I also would like to have
  tests for this library, so I'm going to try to implement that myself.
- Upon further investigation - it appears that it might be related to the DNS resolution. There is
  no separation of timeout for DNS resolution and the actual ping. I have implemented this library
  such that there are separate function calls which are used, and separate configurable timeouts for
  each.
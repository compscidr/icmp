# icmp-android 
A simple library to ping a host using ICMP on Android.

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
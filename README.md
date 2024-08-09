# icmp-android 
A simple library to ping a host using ICMP on Android.

## Todo:
- [x] Structure into a library and an app that uses it
- [x] Release the library to maven central so it can be used
- [ ] Implement icmp v4
- [ ] Implement icmp v6
- [ ] Implement tests for both Ipv4 and Ipv6

## Inspriations
- Originally motivated by https://github.com/kirillF/icmp-android but updated since it no longer
  worked
- Then found https://github.com/marsounjan/icmp4a so instead of using cmake and ndk, we can use
  kotlin directly with the android.system.Os.socket() method. Prior to finding this library, I
  didn't realize those API methods were available. Playing around with the demo app, I found that
  upon changing the host, it could lead to delays up upwards of one minute. I'm not sure if it's
  because of the underlying Flow API or something - it could be that there is some blocking until
  all of the attempts have failed for a particular host or something. I also would like to have
  tests for this library, so I'm going to try to implement that myself.
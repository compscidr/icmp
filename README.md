# icmp-android 
[![Maven Central](https://img.shields.io/maven-central/v/com.jasonernst.icmp/icmp.common?style=flat&logo=maven&label=maven-central&color=blue)](https://central.sonatype.com/artifact/com.jasonernst.icmp/icmp.common/overview)
[![codecov](https://codecov.io/gh/compscidr/icmp/graph/badge.svg?token=RybCFQyDaH)](https://codecov.io/gh/compscidr/icmp)

A simple library to ping a host using ICMP on Android or Linux JVM.

Currently, can:
- creates a non-privileged ICMPv4 or ICMPv6 socket see: https://keith.github.io/xcode-man-pages/icmp.4.html#Non-privileged_ICMP
- uses the android.system.Os.socket() method on Android and a JNI implementation for Linux JVM
- separate DNS resolution timeout and ICMP timeout
- ping a host using ICMPv4 or ICMPv6 depending on the host resolution, receive and parse the response
- can produce and parse the following ICMP packets:
  - echo request
  - echo reply
  - destination unreachable
  - time exceeded

See: https://www.rfc-editor.org/rfc/rfc792.html and https://datatracker.ietf.org/doc/html/rfc4443

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

On Linux, in order to make a userspace ping without root, it may be required to set a kernel flag
in order to use this and not get permission denied errors, see:
- https://opennms.discourse.group/t/how-to-allow-unprivileged-users-to-use-icmp-ping/1573.
- [iputils/iputils#105 (comment)](https://github.com/iputils/iputils/issues/105#issuecomment-431475908)

You may also find, that when you use GH actions runners, tests using this code fails, even if
the above fix for permission errors is done. This is because:

> GitHub hosts Linux and Windows runners on virtual machines in Microsoft Azure with the GitHub Actions runner application installed. The GitHub-hosted runner application is a fork of the Azure Pipelines Agent. Inbound ICMP packets are blocked for all Azure virtual machines, so ping or traceroute commands might not work.

https://docs.github.com/en/actions/using-github-hosted-runners/using-github-hosted-runners/about-github-hosted-runners#cloud-hosts-used-by-github-hosted-runners

There are a few workarounds for this. 1) You could use a self-hosted runner on your own network 
where ICMP is not blocked. 2) You could modify your tests to only ping to `127.0.0.1` and `::1`
within GH actions. 3) You could connect GH actions to tailscale or some other VPN solution so
that the pings go through the VPN interface.

## Building a library that may be used on both Android and Linux JVM:
```kotlin
api("com.jasonernst.icmp:icmp-common")
```

```kotlin
fun someFunction(icmp: ICMP) {
    icmp.ping("google.com", 500, 1000)

    // this will ping the local host with a timeout of 1000ms. Note that the InetAddress object is used
    // here, and that when we do this, there is no DNS request made because we already have the IP address
    icmp.ping(InetAddress.getLocalHost(), 1000)
}
```
And each consumer of the library can pass the appropriate ICMP implementation.

## Building locally:
If changes are made to icmp-common, you will need to publish it locally. 
This can be done by running the `publishToMavenLocal` task in the icmp-common module.

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

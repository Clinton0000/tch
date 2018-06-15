# Welcome to tcheumj

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/tcheum/tcheumj?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/tcheum/tcheumj.svg?branch=master)](https://travis-ci.org/tcheum/tcheumj)
[![Coverage Status](https://coveralls.io/repos/tcheum/tcheumj/badge.png?branch=master)](https://coveralls.io/r/tcheum/tcheumj?branch=master)


# About
tcheumJ is a pure-Java implementation of the tcheum protocol. For high-level information about tcheum and its goals, visit [tcheum.org](https://tcheum.org). The [tcheum white paper](https://github.com/tcheum/wiki/wiki/White-Paper) provides a complete conceptual overview, and the [yellow paper](http://gavwood.com/Paper.pdf) provides a formal definition of the protocol.

We keep tcheumJ as thin as possible. For [JSON-RPC](https://github.com/tcheum/wiki/wiki/JSON-RPC) support and other client features check [tcheum Harmony](https://github.com/tch-camp/tcheum-harmony).

# Running tcheumJ

##### Adding as a dependency to your Maven project: 

```
   <dependency>
     <groupId>org.tcheum</groupId>
     <artifactId>tcheumj-core</artifactId>
     <version>1.8.0-RELEASE</version>
   </dependency>
```

##### or your Gradle project: 

```
   repositories {
       mavenCentral()
       jcenter()
       maven { url "https://dl.bintray.com/tcheum/maven/" }
   }
   compile "org.tcheum:tcheumj-core:1.8.+"
```

As a starting point for your own project take a look at https://github.com/tch-camp/tcheumj.starter

##### Building an executable JAR
```
git clone https://github.com/tcheum/tcheumj
cd tcheumj
cp tcheumj-core/src/main/resources/tcheumj.conf tcheumj-core/src/main/resources/user.conf
vim tcheumj-core/src/main/resources/user.conf # adjust user.conf to your needs
./gradlew clean fatJar
java -jar tcheumj-core/build/libs/tcheumj-core-*-all.jar
```

##### Running from command line:
```
> git clone https://github.com/tcheum/tcheumj
> cd tcheumj
> ./gradlew run [-PmainClass=<sample class>]
```

##### Optional samples to try:
```
./gradlew run -PmainClass=org.tcheum.samples.BasicSample
./gradlew run -PmainClass=org.tcheum.samples.FollowAccount
./gradlew run -PmainClass=org.tcheum.samples.PendingStateSample
./gradlew run -PmainClass=org.tcheum.samples.PriceFeedSample
./gradlew run -PmainClass=org.tcheum.samples.PrivateMinerSample
./gradlew run -PmainClass=org.tcheum.samples.TestNetSample
./gradlew run -PmainClass=org.tcheum.samples.TransactionBomb
```

##### Importing project to IntelliJ IDEA: 
```
> git clone https://github.com/tcheum/tcheumj
> cd tcheumj
> gradlew build
```
  IDEA: 
* File -> New -> Project from existing sources…
* Select tcheumj/build.gradle
* Dialog “Import Project from gradle”: press “OK”
* After building run either `org.tcheum.Start`, one of `org.tcheum.samples.*` or create your own main. 

# Configuring tcheumJ

For reference on all existing options, their description and defaults you may refer to the default config `tcheumj.conf` (you may find it in either the library jar or in the source tree `tcheum-core/src/main/resources`) 
To override needed options you may use one of the following ways: 
* put your options to the `<working dir>/config/tcheumj.conf` file
* put `user.conf` to the root of your classpath (as a resource) 
* put your options to any file and supply it via `-Dtcheumj.conf.file=<your config>`
* programmatically by using `SystemProperties.CONFIG.override*()`
* programmatically using by overriding Spring `SystemProperties` bean 

Note that don’t need to put all the options to your custom config, just those you want to override. 

# Special thanks
YourKit for providing us with their nice profiler absolutely for free.

YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.

![YourKit Logo](https://www.yourkit.com/images/yklogo.png)

# Contact
Chat with us via [Gitter](https://gitter.im/tcheum/tcheumj)

# License
tcheumj is released under the [LGPL-V3 license](LICENSE).


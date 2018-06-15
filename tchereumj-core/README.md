
## tcheum-core

### Include tcheum-core in your project

#### For snapshot builds:

 - Add https://oss.jfrog.org/libs-snapshot/ as a repository to your build script
 - Add a dependency on `org.tcheum:tcheumj-core:${VERSION}`, where `${VERSION}` is of the form `0.8.1-SNAPSHOT`.

Example:

    <repository>
        <id>jfrog-snapshots</id>
        <name>oss.jfrog.org</name>
        <url>https://oss.jfrog.org/libs-snapshot/</url>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
    <!-- ... -->
    <dependency>
       <groupId>org.tcheum</groupId>
       <artifactId>tcheumj-core</artifactId>
       <version>0.8.1-SNAPSHOT</version>
    </dependency>

#### For release builds:

_There are no release builds at this time. Use snapshots in the meantime._


### Examples

See [tcheumj-studio](../tcheumj-studio).


### Build from source

#### Compile, test and package

Run `../gradlew build`.

 - find jar artifacts at `build/libs`
 - find unit test and code coverage reports at `build/reports`

#### Run an tcheum node

 - run `../gradlew run`, or
 - build a standalone executable jar with `../gradlew shadow` and execute the `-all` jar in `build/libs` using `java -jar [jarfile]`.

#### Import sources into IntelliJ IDEA

Use IDEA 14 or better and import project based on Gradle sources.

Note that in order to build the project without errors in IDEA, you will need to run `gradle antlr4` manually.

#### Install artifacts into your local `~/.m2` repository

Run `../gradlew install`.

#### Publish tcheumj-core builds

Simply push to master, and [the Travis CI build](https://travis-ci.org/tcheum/tcheumj) will take care of the rest. To publish manually instead, just run `../gradlew publish`. Where the artifacts are published depends on the value of the `version` property in the [root build.gradle file](../build.gradle). Snapshots (version values ending in `-SNAPSHOT`) will be published to [oss.jfrog.org](https://oss.jfrog.org/libs-snapshot/org/tcheum/), while releases will be published to [Bintray](https://bintray.com/tcheum/maven/org.tcheum/) (and subsequently to [JCenter](http://jcenter.bintray.com/org/tcheum/)). **You must be a member of the [tcheum Bintray organization](https://bintray.com/tcheum) and you must supply `bintrayUser` and `bintrayKey` properties to the Gradle build in order to authenticate against these repositories**. Configure these properties in your `$HOME/.gradle/gradle.properties` for greatest convenience and security.

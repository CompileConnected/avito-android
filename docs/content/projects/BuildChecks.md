# Build checks Gradle plugin

This plugin verifies [common build problems](#checks) with environment and project configuration.

## Getting started

Apply the plugin in the root build script:

```groovy
plugins {
    id("com.avito.android.buildchecks")
}
```

--8<--
plugins-setup.md
--8<--

=== "Kotlin"

    `build.gradle.kts`
    
    ```kotlin
    buildChecks {
        androidSdk {
            compileSdkVersion = 29
            revision = 5
        }
        javaVersion {
            version = JavaVersion.VERSION_1_8
        }
        uniqueRClasses {
            enabled = false
        }
    }
    ```

=== "Groovy"

    `build.gradle`
    
    ```groovy
    buildChecks {
        androidSdk {
            compileSdkVersion = 29
            revision = 5
        }
        javaVersion {
            version = JavaVersion.VERSION_1_8
        }
        uniqueRClasses {
            enabled = false
        }
    }
    ```

That's all for a configuration. Run it manually to verify that it works:

```text
./gradlew checkBuildEnvironment
```

The plugin will run it automatically on every build.

## Configuration

### Enable single check

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        enableByDefault = false
    
        androidSdk {
            compileSdkVersion = 29
            revision = 5
        }
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        enableByDefault = false
    
        androidSdk {
            compileSdkVersion = 29
            revision = 5
        }
    }
    ```

### Disable single check

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        androidSdk {
            enabled = false
        }
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        androidSdk {
            enabled = false
        }
    }
    ```

### Disable all checks

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        enableByDefault = false
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        enableByDefault = false
    }
    ```

### Disable plugin

To completely disable the plugin add a Gradle property:

```properties
avito.build-checks.enabled=false
```

## Checks

### Java version

The Java version can influence the output of the Java compiler. It leads to
Gradle [remote cache misses](https://guides.gradle.org/using-build-cache/#diagnosing_cache_miss)
due
to [Java version tracking](https://docs.gradle.org/nightly/userguide/common_caching_problems.html#java_version_tracking).\
This check forces the same major version for all builds.

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        javaVersion {
            version = JavaVersion.VERSION_1_8
        }   
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        javaVersion {
            version = JavaVersion.VERSION_1_8
        }   
    }
    ```

### Android SDK version

Android build tools uses android.jar (`$ANDROID_HOME/platforms/android-<compileSdkVersion>/android.jar`).\
The version can be specified only without a revision ([#117789774](https://issuetracker.google.com/issues/117789774)).
Different revisions lead to Gradle remote cache misses. This check forces the same revision:

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        androidSdk {
            compileSdkVersion = 29
            revision = 5
        }
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        androidSdk {
            compileSdkVersion = 29
            revision = 5
        }
    }
    ```

### macOS localhost lookup

On macOs `java.net.InetAddress#getLocalHost()` invocation can last up to 5 seconds instead of milliseconds
([thoeni.io/post/macos-sierra-java](https://thoeni.io/post/macos-sierra-java/)). Gradle has
a [workaround](https://github.com/gradle/gradle/pull/11134) but it works only inside Gradle's code.

To diagnose the problem manually use [thoeni/inetTester](https://github.com/thoeni/inetTester).

To fix the problem use this workaround:

1. Find your computer's name - [Find your computer's name and network address](https://support.apple.com/en-us/guide/mac-help/find-your-computers-name-and-network-address-mchlp1177/mac)
2. Add it to `/etc/hosts` config:

```text
127.0.0.1        localhost <your_host_name>.local
::1              localhost <your_host_name>.local
```

3. Reboot the computer
4. Check again by [thoeni/inetTester](https://github.com/thoeni/inetTester)

This check automatically detects the issue:

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        macOSLocalhost { }
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        macOSLocalhost { }
    }
    ```

### Dynamic dependency version

Dynamic versions, such as "2.+", and snapshot versions force Gradle to check them on a remote server. It slows down
a [configuration time](https://guides.gradle.org/performance/#minimize_dynamic_and_snapshot_versions)
and makes build [less reproducible](https://reproducible-builds.org/).

This check forbids dynamic dependency versions.

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        dynamicDependencies { }
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        dynamicDependencies { }
    }
    ```

### Unique R classes

If two Android modules use the same package, their R classes will be merged. While merging, it can unexpectedly override
resources ([#175316324](https://issuetracker.google.com/issues/175316324)). It happens even
with `android.namespacedRClass`.

To forbid merged R files use this check:

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    plugins {
        id("com.avito.android.buildchecks")
    }
    
    buildChecks {
        uniqueRClasses { } // enabled by default
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    plugins {
        id("com.avito.android.buildchecks")
    }
    
    buildChecks {
        uniqueRClasses { } // enabled by default
    }
    ```

Try also `android.uniquePackageNames` check. In AGP 4.1 it provides similar but less complete contract.

#### Configuration

You can suppress errors for a specific package name:

`build.gradle.kts`

```kotlin
buildChecks {
    uniqueRClasses {
        allowedNonUniquePackageNames.addAll(listOf(
            "androidx.test", // Default from ManifestMerger #151171905
            "androidx.test.espresso", // Won't fix: https://issuetracker.google.com/issues/176002058
            "androidx.navigation.ktx" // Fixed in 2.2.1: https://developer.android.com/jetpack/androidx/releases/navigation#2.2.1
        ))
    }
}
```

### Gradle daemon reusage

Gradle can run multiple daemons
for [many reasons](https://docs.gradle.org/5.0/userguide/gradle_daemon.html#sec:why_is_there_more_than_one_daemon_process_on_my_machine)
.

If you use `buildSrc` in the project with standalone Gradle wrapper, this check will verify common problems to reuse it.

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        gradleDaemon { }
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        gradleDaemon { }
    }
    ```

### Module types

This check force to
apply [`module-types`](https://github.com/avito-tech/avito-android/blob/develop/subprojects/gradle/module-types) Gradle
plugin in all modules. It prevents modules go to wrong configurations (android-test module as an app’s implementation
dependency for example).

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        moduleTypes { 
            enabled = true // disabled by default
        } 
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        moduleTypes { 
            enabled = true // disabled by default
        } 
    }
    ```

### Gradle properties

--8<--
avito-disclaimer.md
--8<--

This check detects if you
override [Gradle project property](https://docs.gradle.org/current/userguide/build_environment.html#sec:project_properties)
by command-line. It sends mismatches to statsd. This information helps to see frequently changed propeties that can lead
to remote cache misses.

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        gradleProperties { 
            enabled = true // disabled by default
        } 
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        gradleProperties { 
            enabled = true // disabled by default
        } 
    }
    ```

### Incremental KAPT

This check verifies that all KAPT annotation processors support incremental annotation processing if it
is [enabled](https://kotlinlang.org/docs/reference/kapt.html#incremental-annotation-processing-since-1330) (`kapt.incremental.apt=true`)
. Because if one of them does not support it then whole incremental annotation processing won't work at all.

Supported processors:

- [Room](https://developer.android.com/topic/libraries/architecture/room)

Incremental KAPT check has three modes:

- `"none"` -- check is disable
- `"warning"` -- prints warning in build log (default behaviour)
- `"fail"` -- fail whole build

=== "Kotlin"
    `build.gradle.kts`

    ```kotlin
    buildChecks {
        incrementalKapt {
            mode = "fail"
        }
    }
    ```

=== "Groovy"
    `build.gradle`

    ```groovy
    buildChecks {
        incrementalKapt {
            mode = "fail"
        }
    }
    ```

#### Room

Room supports incremental annotation processing if all of the following conditions are met:

- [`room.incremental`](https://developer.android.com/jetpack/androidx/releases/room#compiler-options) it set to `true`
  or `com.avito.android.room-config` Gradle-plugin is applied
- You use Java 11 and higher or use JDK embedded in Android Studio 3.5.0-beta02 and higher. For more info about these
  restrictions read the documentation for the method `methodParametersVisibleInClassFiles`
  in `RoomProcessor` [sources](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-master-dev/room/compiler/src/main/kotlin/androidx/room/RoomProcessor.kt)

If you use IntelliJ IDEA or build a project from the command line you have to set up `JAVA_HOME` environment variable to 
reference the path to embedded JDK in Android Studio. You can add to `~/.gradle/gradle.properties` line like this:

```properties
org.gradle.java.home=/Applications/Android Studio.app/Contents/jre/jdk/Contents/Home
```

This path can be retrieved from **Project Structure > SDK Location > JDK location** in 
Android Studio.

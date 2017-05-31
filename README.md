<p align="center">
  <img height="140px" width="400px" src="https://cloud.githubusercontent.com/assets/4109119/25454461/de81fad0-2aa2-11e7-831d-b1f3ea7f313a.png"/>
</p>

# WOLMO NETWORKING (ANDROID)

The NETWORKING module provides a separation from the network tier from the rest of the project while also providing offline support capabilities.

### Usage

Import the module as alibrary in your project using Gradle:

**root build.gradle**
```groovy
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
**your app module build.gradle**
```groovy
dependencies {
        compile 'com.github.Wolox:wolmo-networking-android:master-SNAPSHOT'
}
```
Note: The above line will download the latest version of the module, if you want to run an specific version replace `master-SNAPSHOT` with `1.0.0` or any other version. Avaiable versions can be found here: [Github releases](https://github.com/Wolox/wolmo-networking-android/releases)

### Features

* Perform REST API calls using a preconfigured Retrofit client
* HTTP requests logging in LogCat preconfigured
* `Time` classes handling preconfigured with Retrofit and custom serializers/deserializers
* Cache (offline) mechanisms base classes
* Repository pattern oriented architecture 

### Dependencies

1. [WOLMO CORE](https://github.com/Wolox/wolmo-core-android)
2. [Retrofit](https://github.com/square/retrofit)
3. [OkHTTP3](https://github.com/square/okhttp)
4. [Joda Time](http://www.joda.org/joda-time/)

## <a name="topic-contributing"></a> Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push your branch (`git push origin my-new-feature`)
5. Create a new Pull Request

## <a name="topic-about"></a> About

This project was written by [Wolox](http://www.wolox.com.ar) and is maintained by:
* [Juan Ignacio Molina](https://github.com/juanignaciomolina)
* [Emanuel Lamela](https://github.com/emalamela)

![Wolox](https://raw.githubusercontent.com/Wolox/press-kit/master/logos/logo_banner.png)

## <a name="topic-license"></a> License

**WOLMO NETWORKING** is available under the MIT [license](https://raw.githubusercontent.com/Wolox/wolmo-networking-android/master/LICENSE.md).

    Copyright (c) Wolox S.A

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.

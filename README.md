[![Build Status](https://app.bitrise.io/app/98b385dd0144faa0/status.svg?token=9gD8GNFS5DF6iIQ7eecIXA&branch=master)](https://app.bitrise.io/app/98b385dd0144faa0)
[![Release](https://jitpack.io/v/Wolox/wolmo-networking-android.svg)](https://jitpack.io/#Wolox/wolmo-networking-android)
[![Coverage Status](https://coveralls.io/repos/github/Wolox/wolmo-networking-android/badge.svg?branch=master)](https://coveralls.io/github/Wolox/wolmo-networking-android?branch=master)

<p align="center">
  <img height="140px" width="400px" src="https://cloud.githubusercontent.com/assets/4109119/25454461/de81fad0-2aa2-11e7-831d-b1f3ea7f313a.png"/>
</p>

# WOLMO NETWORKING (ANDROID)

The NETWORKING module provides a separation from the network tier from the rest of the project while also providing offline support capabilities.

## Features

* Fully compatible with Dagger and WOLMO Core v2.
* Perform REST API calls using a preconfigured Retrofit client
* HTTP requests logging in LogCat preconfigured
* `Time` classes handling preconfigured with Retrofit and custom serializers/deserializers
* Cache (offline) mechanisms base classes
* Repository pattern oriented architecture

## Installation

Import the module as a library in your project using Gradle:

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
Note: The above line will download the latest version of the module, if you want to run a specific version replace `master-SNAPSHOT` with `2.0.0` or any other version. Available versions can be found here: [Github releases](https://github.com/Wolox/wolmo-networking-android/releases)

## How to use

Applications that use `wolmo-network` don't need to extend `NetworkingApplication` anymore.
WOLMO Networking provides the modules to build and configure the dependencies of `Retrofit`, `Gson` and `OkHttp`, it also provides a default `NetworkingComponent` to simplify the creation and reduce configuration on common scenarios. For it to work you only need to provide it with the `baseUrl`, an optional list of interceptors to add to okHttp and an optional list of Gson type adapters.

This version of Networking adds the following Modules:

* **CachingModule**: Provides all the dependencies to cache request.
* **GsonModule**: Provides a Gson instance and all the dependencies to build it.
* **OkHttpClientModule**: Provides a instance of `OkHttpClient` and all the dependencies to build it.
* **NetworkingModule**:  Provides a instance of `Retrofit`.

There are two possible ways to use it:

### Add a component dependency to NetworkingComponent

You can use it this way when you don't need further customizations.

```java
@ApplicationScope
@Component(dependencies = {NetworkingComponent.class},
           modules = { AndroidSupportInjectionModule.class, DefaultModule.class,
                   ContextModule.class, AppModule.class })
public interface AppComponent extends AndroidInjector<BootstrapApplication> {

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<BootstrapApplication> {

        @BindsInstance
        public abstract Builder application(Application application);

        public abstract Builder networkingComponent(NetworkingComponent networkingComponent);

    }
}
```

In your application you need to build the `NetworkingComponent` like:

```java
public class BootstrapApplication extends WolmoApplication {

    @Override
    public void onInit() { ... }

    @Override
    protected AndroidInjector<BootstrapApplication> applicationInjector() {
        return DaggerAppComponent.builder().networkingComponent(buildDaggerNetworkingComponent())
            .application(this)
            .create(this);
    }

    private NetworkingComponent buildDaggerNetworkingComponent() {
        NetworkingComponent.Builder builder =
            DaggerNetworkingComponent.builder().baseUrl(Configuration.EXAMPLE_CONFIGURATION_KEY)
                .gsonNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);

        if (BuildConfig.DEBUG) {
            builder.okHttpInterceptors(
                buildHttpLoggingInterceptor(Level.BODY), new ChuckInterceptor(this));
        }

        return builder.build();
    }

    /**
     * Returns a {@link HttpLoggingInterceptor} with the level given by <b>level</b>.
     *
     * @param level - Logging level for the interceptor.
     * @return New instance of interceptor
     */
    private static HttpLoggingInterceptor buildHttpLoggingInterceptor(
          @NonNull HttpLoggingInterceptor.Level level) {
        HttpLoggingInterceptor loggerInterceptor = new HttpLoggingInterceptor();
        loggerInterceptor.setLevel(level);
        return loggerInterceptor;
    }
}
```

### Create a new NetworkComponent

If you need to satisfy another dependencies or you need to change those provided by WOLMO Networking you can create your own `CustomNetworkComponent` and add only the
wanted modules from WOLMO and your own. For example:

```java
@NetworkingScope
@Component(modules = { JacksonModule.class, OkHttpClientModule.class, NetworkingModule.class })
public interface MyCustomNetworkingComponent {

    RetrofitServices retrofitServices();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder baseUrl(String baseUrl);

        @BindsInstance
        Builder okHttpInterceptors(@Nullable Interceptor... interceptors);
	
	...

        MyCustomNetworkingComponent build();
    }
}
```

Or you can directly add the Modules to your `AppComponent` if you wish.

## Usages

An example instantiation and usage of the `Repository` class is the following.

Consider the case of an app that needs to load a set of `Task`s and wants to fetch them locally first (See `Repository.AccessPolicy` for other policies). To create the necessary `Repository` these few lines of code are needed:

```java
mTaskRepository = new Repository(cache, Repository.CACHE_FIRST);
```

The `Repository.AccessPolicy` can be omitted if you intend to use the configurable `Repository.DEFAULT_ACCESS_POLICY` as its access policy.

Note that the creation *requires* one thing: a `TaskCache`.

We won't dwell in the details of what a `TaskCache` should be. The advantage is that it can be **anything the users of the library consider a cache**. It can be implemented atop of memory, SQLite database, a faster web service, etc.

On the other hand, a `QueryStrategy` is a mechanism that defines what to do in case the `Repository` determines it should either `readLocalSource` or `consumeRemoteSource`.

```
public class TaskQueryStrategy extends Repository.QueryStrategy<List<Task>, TaskCache> {

    @Nullable
    @Override
    public List<Task> readLocalSource(@NonNull TaskCache cache) {
        // Read from cache
        // Important to notice that it should return null when there's a cache miss.
     }

     @Override
     public void consumeRemoteSource(@NonNull List<Task> tasks, @NonNull TaskCache cache) {
         // Save tasks to cache
     }
     
}
```

Finally, the last step is to `query` the newly created `Repository<Task, TaskCache>` to then act on the `Task`s information retrieved.

There are 2 flavors of `Repository#query`, each offering an overload for passing an `AccessPolicy` to use for that particular `query`.

* Executing the query logic immediately:

```
mTaskRepository.query(
    getService(TaskService.class).fetchTasks(),
    new TaskQueryStrategy() {
        // Implementation
    },
    new IRepositoryCallback<List<Task>> {
        // Callback Implementation
    });
```

Information retrieval is notified through `IRepositoryCallback#onSuccess(T)` and errors through `IRepositoryCallback#onError(Throwable)`. The user can then execute the required behaviour after said events. Whether it fetched the information from network, memory, disk or any other source is transparent.

* Getting a hold of a `Repository.Query` object:

```
Query<List<Task>> tasksQuery = mTaskRepository.query(
    getService(TaskService.class).fetchTasks(),
    new TaskQueryStrategy() {
        // Implementation
    });
```

The `Repository.Query` holds the logic of the query ready to be `Repository.Query#run`. This class implements `Runnable`, which allows integration with several other APIs.
 
This allows the users of the class to do:

```
tasksQuery
    .onSuccess((List<Task> tasks) -> {// Implement})
    .onError((Throwable error) -> {// Implement})
    .run();
```

The SAM interfaces for `Repository.Query#onSuccess` and `Repository.Query#onError` are `Consumer<T>` and `Consumer<Throwable>` respectively. 

Important note: We are using a custom `Customer` class. When the `Java 8` functional interfaces are supported everywhere officially, the implementation will use those.

The library also offers a tool to handle the 'dirty' cache common problem. Users can create `TimeResolveQueryStrategy` which already handles that case for them. Instances of this class are created with a delta time that is the time left for the local data to be considered 'clean'. When that amount of time has passed since the last remote data fetch, the `TimeResolveQueryStrategy` forces the remote query.

## Dependencies

1. [WOLMO CORE](https://github.com/Wolox/wolmo-core-android)
2. [Retrofit](https://github.com/square/retrofit)
3. [OkHTTP3](https://github.com/square/okhttp)
4. [Joda Time](http://www.joda.org/joda-time/)
5. [Dagger](https://google.github.io/dagger/android.html)

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

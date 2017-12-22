package ar.com.wolox.wolmo.networking.di.modules;

import ar.com.wolox.wolmo.networking.di.scopes.NetworkingScope;
import ar.com.wolox.wolmo.networking.optimizations.BaseCallCollapser;
import ar.com.wolox.wolmo.networking.optimizations.ICallCollapser;

import dagger.Module;
import dagger.Provides;

/**
 * Default module with caching dependencies
 */
@Module
public class CachingModule {

    @Provides
    @NetworkingScope
    static ICallCollapser provideBaseCallCollapser() {
        return new BaseCallCollapser();
    }
}

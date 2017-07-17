package ar.com.wolox.wolmo.networking.di.modules;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ar.com.wolox.wolmo.networking.retrofit.serializer.LocalDateSerializer;

import org.joda.time.LocalDate;

import dagger.Module;
import dagger.Provides;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class GsonModule {

    @Provides
    GsonConverterFactory provideGsonConverterFactory(Gson gson) {
        return GsonConverterFactory.create(gson);
    }

    @Provides
    Gson provideGson(GsonBuilder gsonBuilder) {
        return gsonBuilder.create();
    }

    @Provides
    GsonBuilder provideGsonBuilder() {
        return new com.google.gson.GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
    }
}

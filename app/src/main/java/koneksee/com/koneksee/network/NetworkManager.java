package koneksee.com.koneksee.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import koneksee.com.koneksee.BuildConfig;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by mexan on 1/5/16.
 */
public class NetworkManager {

    private NetworkService networkService;

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ssZ";

    public NetworkManager() {
        initializeNetwork();
    }

    public void initializeNetwork() {
        Gson gson = gsonHandler(new GsonBuilder().setPrettyPrinting())
            .setDateFormat(DATE_FORMAT)
            .create();

        Retrofit.Builder restAdapterBuilder = new Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create(gson));

        networkService = restAdapterBuilder.build().create(NetworkService.class);

    }

    protected GsonBuilder gsonHandler(GsonBuilder builder) {
        builder.registerTypeAdapterFactory(new ResponseTypeAdapterFactory());
        return builder;
    }

    private class ResponseTypeAdapterFactory implements TypeAdapterFactory {

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    delegate.write(out, value);
                }

                @Override
                public T read(JsonReader in) throws IOException {

                    JsonElement jsonElement = elementAdapter.read(in);
                    if (jsonElement.isJsonObject()) {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        if (jsonObject.has("data") && jsonObject.get("data").isJsonObject()) {
                            jsonElement = jsonObject.get("data");
                        } else if (jsonObject.has("data") && jsonObject.get("data").isJsonArray()) {
                            jsonElement = jsonObject.get("data").getAsJsonArray();
                        }
                    }
                    return delegate.fromJsonTree(jsonElement);
                }
            }.nullSafe();
        }
    }

}

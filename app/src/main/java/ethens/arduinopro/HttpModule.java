package ethens.arduinopro;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created by ethens on 12/12/17.
 */

public class HttpModule {
  private OkHttpClient getHttpClient() {

    OkHttpClient.Builder builder = new OkHttpClient.Builder().retryOnConnectionFailure(true);

    return builder.build();
  }

  public Retrofit provideRetrofit(String url) {
    Retrofit.Builder builder = new Retrofit.Builder();
    builder.client(getHttpClient()).baseUrl(url);
    return builder.build();
  }
}

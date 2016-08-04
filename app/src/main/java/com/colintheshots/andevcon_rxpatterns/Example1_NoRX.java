package com.colintheshots.andevcon_rxpatterns;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Iterables;
import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * An example of solving the same problem without RxJava, but with Retrofit.
 *
 * Created by colinlee on 8/3/16.
 */
public class Example1_NoRX extends Activity {

    /** The GitHub REST API Endpoint */
    public final static String GITHUB_BASE_URL = "https://api.github.com";

    /** Set this variable to your GitHub personal access token */
    /* public final static String GITHUB_PERSONAL_ACCESS_TOKEN = "XXX"; */

    private GitHubClient mGitHubClient;
    private ListView mListView;

    /**
     * Retrofit interface to GitHub API methods
     */
    public interface GitHubClient {
        @GET("/gists")
        Call<List<Gist>> gists();

        @GET("/gists/{id}")
        Call<GistDetail> gist(@Path("id") String id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example1);
        mListView = (ListView) findViewById(R.id.listView);
        createGithubClient();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Secrets.GITHUB_PERSONAL_ACCESS_TOKEN.equals("XXX")) {
            Toast.makeText(getApplicationContext(), "GitHub Personal Access Token is Unset!", Toast.LENGTH_LONG).show();
        }

        mGitHubClient.gists().enqueue(new Callback<List<Gist>>() {
            @Override
            public void onResponse(Call<List<Gist>> call, Response<List<Gist>> response) {
                String gistName = response.body().get(0).getId();
                mGitHubClient.gist(gistName).enqueue(new Callback<GistDetail>() {
                    @Override
                    public void onResponse(Call<GistDetail> call, Response<GistDetail> response) {
                        displayFileList(response.body());
                    }

                    @Override
                    public void onFailure(Call<GistDetail> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Gist>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void createGithubClient() {
        if (mGitHubClient == null) {

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(chain ->
                            chain.proceed(chain.request().newBuilder().addHeader("Authorization",
                                    "token " + Secrets.GITHUB_PERSONAL_ACCESS_TOKEN).build())).build();

            mGitHubClient = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(GITHUB_BASE_URL)
                    .build()
                    .create(GitHubClient.class);
        }
    }

    public void displayFileList(final GistDetail gistDetail) {
        if (gistDetail.getFiles().size()>0 && mListView!=null) {
            mListView.setAdapter(new GistAdapter(Example1_NoRX.this, gistDetail));
        }
    }

    private class GistAdapter extends BaseAdapter {

        private Context mContext;
        private List<Gist> mGists;
        private Map<String, GistFile> mGistFileMap;

        public GistAdapter(Context context, GistDetail gistDetail) {
            mContext = context;
            mGistFileMap = gistDetail.getFiles();
        }

        @Override
        public int getCount() {
            if (mGistFileMap==null) {
                return mGists.size();
            } else {
                return mGistFileMap.size();
            }
        }

        @Override
        public Object getItem(int i) {
            if (mGistFileMap==null) {
                return mGists.get(i);
            } else {
                String key = Iterables.get(mGistFileMap.keySet(), i);
                return mGistFileMap.get(key);
            }
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            GistHolder holder;
            if (view != null) {
                holder = (GistHolder) view.getTag();
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.row_gist, viewGroup, false);
                holder = new GistHolder(view);
                view.setTag(holder);
            }

            if (mGistFileMap == null) {
                Gist g = mGists.get(i);

                holder.title.setText(g.getHtml_url());
                holder.id.setText(g.getId());
            } else {
                GistFile gistFile = (GistFile) getItem(i);

                holder.title.setText(gistFile.getContent());
                holder.id.setText(gistFile.getContent());
            }

            return view;
        }

        private class GistHolder {

            TextView title;
            TextView id;

            GistHolder(View view) {
                title = (TextView) view.findViewById(R.id.gistTextView);
                id = (TextView) view.findViewById(R.id.hiddenIdTextView);
            }
        }
    }

    private class Gist {
        @Expose
        private String id;

        @Expose
        private String description;

        @Expose
        private String html_url;

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String getHtml_url() {
            return html_url;
        }
    }

    private class GistDetail {
        @Expose
        private Map<String, GistFile> files;

        public Map<String, GistFile> getFiles() {
            return files;
        }
    }

    private class GistFile {
        @Expose
        private String content;

        public String getContent() {
            return content;
        }
    }
}

package com.example.android.project7_newsappstage2;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity {

    /**
     * URL for news data from the dataset
     */
    public static final String NEWS_REQUEST_URL = "https://content.guardianapis.com/search?show-tags=" +
            "contributor&show-fields=thumbnail&api-key=c66eef35-0811-42e7-b42f-b07cdf5a60fe";


    /**
     * Constant value for the news loader ID.
     */
    private static final int NEWS_GETTER_ID = 1;

    /**
     * Adapter for the list of news
     */
    private NewsAdapter newsAdapter;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView noNewsStateTextView;


    /**
     * Get news from loader
     */
    private final LoaderManager.LoaderCallbacks<List<News>> newsGetter
            = new LoaderManager.LoaderCallbacks<List<News>>() {

        @Override
        public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(NewsActivity.this);

            String numberOfArticles = sharedPreferences.getString(getString(R.string.number_of_articles_key),
                    getString(R.string.number_of_articles_default));

            String orderBySection = sharedPreferences.getString(getString(R.string.settings_order_by_section_key),
                    getString(R.string.settings_order_by_section_default));

            Uri baseUri = Uri.parse(NEWS_REQUEST_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();

            uriBuilder.appendQueryParameter("page-size", numberOfArticles);
            uriBuilder.appendQueryParameter("section", orderBySection.toLowerCase());

            return new NewsLoader(NewsActivity.this, uriBuilder.toString());

        }

        @Override
        public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Set empty state text to display "There are currently no news."
            noNewsStateTextView.setText(R.string.no_news_found);

            // If there is a valid list of {@link News}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (news != null && !news.isEmpty()) {
                newsAdapter.addAll(news);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<News>> loader) {
            // Loader reset, so we can clear out our existing data.
            newsAdapter.clear();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_list);
        //Set context

        // Find a reference to the {@link ListView} in the layout
        ListView newsListView = findViewById(R.id.list);

        noNewsStateTextView = findViewById(R.id.no_news_view);
        newsListView.setEmptyView(noNewsStateTextView);

        // Create a new adapter that takes the list of earthquakes as input
        newsAdapter = new NewsAdapter(this, new ArrayList<News>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        newsListView.setAdapter(newsAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected news.
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current news that was clicked on
                News currentNews = newsAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri newsUri = Uri.parse(currentNews.getArticleUrl());

                // Create a new intent to view the news URI
                Intent goToWebIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                // Send the intent to launch a new activity
                startActivity(goToWebIntent);
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loaders. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(NEWS_GETTER_ID, null, newsGetter);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            noNewsStateTextView.setText(R.string.please_connect);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

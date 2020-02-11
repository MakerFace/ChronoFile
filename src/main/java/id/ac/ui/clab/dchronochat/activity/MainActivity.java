package id.ac.ui.clab.dchronochat.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.named_data.jndn.Name;
import net.named_data.jndn_xx.util.FaceUri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import id.ac.ui.clab.dchronochat.chat.ChatListFragment;
import id.ac.ui.clab.dchronochat.net.NfdcHelper;
import id.ac.ui.clab.dchronochat.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    /**
     * Reference to the most recent AsyncTask that was created for listing routes
     */
    private RouteCreateAsyncTask m_routeCreateAsyncTask;

    private SharedPreferences mSharedPrefs;
    private Button startChat;
    private Button manageResouces;
    private TextView editRouterIP; //edittext to inssert router IP
    private String mUserName;
    private String mScreenName;
    private String mHubPrefix;
    private String mChatRoom;
    private String mRouterIP;
    private static final String IPADDRESS_PATTERN =
            "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    /**
     * Progress bar spinner to display to user when destroying faces
     */
    private ProgressBar m_reloadingListProgressBar;
    public static String CONTEXT_CACHE_DIRECTORY;
    public static String CONTEXT_FILES_DIRECTORY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Bundle lSavedInstanceState = savedInstanceState;
        super.onCreate(lSavedInstanceState);
        setContentView(R.layout.activity_main_page);
        startChat = (Button) findViewById(R.id.startButton);
        manageResouces = (Button) findViewById(R.id.manageButton);
        editRouterIP = (TextView) findViewById(R.id.editRouterIP);

        // Get progress bar spinner view
        m_reloadingListProgressBar = (ProgressBar) findViewById(R.id.route_list_reloading_list_progress_bar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Setup Chat
        mSharedPrefs = getSharedPreferences(getString(R.string.sharedPrefs), MODE_PRIVATE);
        if (!mSharedPrefs.contains(getString(R.string.usernameShared)) || !mSharedPrefs.contains(getString(R.string.screennameShared))) {
            Intent toLogin = new Intent(this, LoginActivity.class);
            startActivity(toLogin);
            return;
        }

        mUserName = mSharedPrefs.getString(getString(R.string.usernameShared), "");
        mHubPrefix = mSharedPrefs.getString(getString(R.string.hubprefixShared), "");
        mScreenName = mSharedPrefs.getString(getString(R.string.screennameShared), "");
        editRouterIP.setText(mSharedPrefs.getString(getString(R.string.remoteIP), ""));
        mChatRoom = "TestRoom";

        startChat.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (lSavedInstanceState == null) {

                    mRouterIP = mSharedPrefs.getString(getString(R.string.remoteIP), "");
                    Log.i("MainActivity", "remote IP :" + mRouterIP);

                    if (!isIPValid(mRouterIP)) {
                        editRouterIP.setError("IP Address Invalid!");
                        return;
                    } else if (isIPValid(mRouterIP)) {
//                        String faceUri = "udp://" + mRouterIP;
                        String faceUri = "tcp://" + mRouterIP;
                        Name globalPrefix = new Name("/ndn/");
                        createRoute(globalPrefix, faceUri);
                        // During initial setup, plug in the details fragment.
                        ChatListFragment details = new ChatListFragment();
                        details.setArguments(getIntent().getExtras());
                        getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.drawer_layout,
                                        ChatListFragment.newInstance(
                                                mScreenName,
                                                mUserName,
                                                mHubPrefix,
                                                mChatRoom,
                                                mRouterIP),
                                        "chatItemList")
                                .commit();
                    }
                    if (TextUtils.isEmpty(mRouterIP)) {
                        editRouterIP.setError("Cannot be empty!");
                    }
                }

            }
        });

        manageResouces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ManageActivity.class);
                startActivity(intent);
            }
        });

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 2);
            }
        }

        CONTEXT_CACHE_DIRECTORY = getExternalCacheDir().getPath();
        CONTEXT_FILES_DIRECTORY = getFilesDir().getPath();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button_chat, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            MainActivity.this.startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            Intent i = new Intent(this, SettingsActivity.class);
            MainActivity.this.startActivity(i);
            return true;
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    static boolean isIPValid(String IP) {
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(IP);
        return matcher.find();
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private class RouteCreateAsyncTask extends AsyncTask<Void, Void, String> {
        public RouteCreateAsyncTask(Name prefix, String faceUri) {
            m_prefix = prefix;
            m_faceUri = faceUri;
        }

        @Override
        protected String
        doInBackground(Void... params) {
            NfdcHelper nfdcHelper = new NfdcHelper();
            try {
                int faceId = nfdcHelper.faceCreate(m_faceUri);
                nfdcHelper.ribRegisterPrefix(new Name(m_prefix), faceId, 10, true, false);
                nfdcHelper.shutdown();
                return "OK";
            } catch (FaceUri.CanonizeError e) {
                return "Error creating face (" + e.getMessage() + ")";
            } catch (FaceUri.Error e) {
                return "Error creating face (" + e.getMessage() + ")";
            } catch (Exception e) {
                return "Error communicating with NFD (" + e.getMessage() + ")";
            } finally {
                nfdcHelper.shutdown();
            }
        }

        @Override
        protected void
        onPreExecute() {
            // Display progress bar
            m_reloadingListProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void
        onPostExecute(String status) {
            // Display progress bar
            m_reloadingListProgressBar.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();

            //retrieveRouteList();
        }

        @Override
        protected void
        onCancelled() {
            // Remove progress bar
            m_reloadingListProgressBar.setVisibility(View.GONE);
        }

        ///////////////////////////////////////////////////////////////////////////

        private Name m_prefix;
        private String m_faceUri;
    }

    public void
    createRoute(Name prefix, String faceUri) {
        m_routeCreateAsyncTask = new RouteCreateAsyncTask(prefix, faceUri);
        m_routeCreateAsyncTask.execute();
    }

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
}

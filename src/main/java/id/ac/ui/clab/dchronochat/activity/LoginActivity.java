package id.ac.ui.clab.dchronochat.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import id.ac.ui.clab.dchronochat.R;

/**
 * Login screen, user enters the information of email(username) and screenName, as well as hubprefix
 */
public class LoginActivity extends AppCompatActivity  {

    // img0 references.
    private View mCreateRoomView;
    private AutoCompleteTextView mUserName;
    private EditText mScreenName;
    private EditText mHubPrefix;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mScreenName = (EditText) findViewById(R.id.editScreenName);
        mUserName = (AutoCompleteTextView) findViewById(R.id.editUserName);
        mHubPrefix = (EditText) findViewById(R.id.editHubPrefix);
        mHubPrefix.setText("ndn/ui/clab/");

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            String lastUsername = extras.getString("oldUsername", "");
            String lastScreenName = extras.getString("oldScreenName", "");
            String lastHubPrefix = extras.getString("oldHubPrefix", "");
            mUserName.setText(lastUsername);
            mScreenName.setText(lastScreenName);
            mHubPrefix.setText(lastHubPrefix);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Takes the username/screenName/hubPrefix from the EditText, check its validity and saves it if valid.
     *   Then, redirects to the MainActivity.
     * @param view Button clicked to trigger call to joinChat
     */
    public void joinChat(View view) {
        // Reset errors.
        mScreenName.setError(null);
        mUserName.setError(null);
        mHubPrefix.setError(null);

        // Store values at the time of the login attempt.
        String userName = mUserName.getText().toString();
        String screenName = mScreenName.getText().toString();
        String hubPrefix = mHubPrefix.getText().toString();

        // Check for a valid username
        if (TextUtils.isEmpty(userName) && (userName.length() > 20)) {
            mUserName.setError("ScreenName must be between 0-10 characters!");
            return;
        }
        else if (!isEmailValid(userName)) {
            mUserName.setError(getString(R.string.error_invalid_email));
            return;
        }
        // Check for a valid screenname
        if (!TextUtils.isEmpty(screenName) && (screenName.length() > 20)) {
            mScreenName.setError("ScreenName must be between 0-10 characters!");
            return;
        }

        SharedPreferences sp = this.getSharedPreferences(getString(R.string.sharedPrefs), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(getString(R.string.usernameShared), userName);
        edit.putString(getString(R.string.screennameShared), screenName);
        edit.putString(getString(R.string.hubprefixShared), hubPrefix);
        edit.apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    static boolean isEmailValid(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(email);
        return matcher.find();
    }


}


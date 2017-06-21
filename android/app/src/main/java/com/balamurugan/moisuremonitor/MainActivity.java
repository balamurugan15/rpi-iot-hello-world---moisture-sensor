package com.balamurugan.moisuremonitor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends ActionBarActivity {

    ProgressDialog mProgressDialog;
    private Toolbar toolbar;

    Context context = this;

    private ProgressDialog pDialog;

    boolean failed = false;

    // URL to get contacts JSON
    private static String url = "http://api.thingspeak.com/channels/77041/feed.json?result=10";

    // JSON Node names
    private static final String TAG_FEEDS = "feeds";
    private static final String TAG_ID = "entry_id";
 //   private static final String TAG_NAME = "name";
    private static final String TAG_FIELD1 = "field1";
    private static final String TAG_FIELD2 = "field2";
    private static final String TAG_CREATED = "created_at";
    private static final String TAG_UPDATED = "updated_at";
    private static final String TAG_CHANNEL = "channel";

    // contacts JSONArray
    JSONArray feeds = null;

    ListView lv;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> feedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        feedList = new ArrayList<HashMap<String, String>>();

        lv = (ListView) findViewById(R.id.list2);

        new GetFeeds().execute();


        setTitle("Moisture Monitor");

        final Button retryButton = new Button(this);
        retryButton.setText("Fetch data");
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // noInternet = expired = false;
               // SharedPreferences pref2 = getApplicationContext().getSharedPreferences("boolean", 0); // 0 - for private mode
               // SharedPreferences.Editor editor = pref2.edit();
               // editor.putBoolean("log", false);
               // editor.commit();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.putExtra("retry", true);
                startActivity(intent);
                finish();
            }
        });

        lv.addFooterView(retryButton);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class GetFeeds extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    feeds = jsonObj.getJSONArray(TAG_FEEDS);

                    // looping through All Contacts
                    for (int i = 0; i < feeds.length(); i++) {
                        JSONObject c = feeds.getJSONObject(i);

                        String id = "Id : " + c.getString(TAG_ID);
           //             String name = c.getString(TAG_NAME);
                        String field1 = "Value : " + c.getString(TAG_FIELD1);
                        String field2 = "Value : " + c.getString(TAG_FIELD2);
                        String created = "Timestamp : " + c.getString(TAG_CREATED);
                   //     String created = c.getString(TAG_GENDER);


                        // tmp hashmap for single contact
                        HashMap<String, String> contact = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        contact.put(TAG_ID, id);
                        contact.put(TAG_FIELD1, field1);
                        contact.put(TAG_CREATED, created);

                        // adding contact to contact list
                        feedList.add(contact);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
                failed = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, feedList,
                    R.layout.list_item, new String[] { TAG_ID, TAG_FIELD1, TAG_CREATED}, new int[] { R.id.id,
                    R.id.field1, R.id.created});

            lv.setAdapter(adapter);

            if(failed){
                Toast.makeText(context, "Couldn't retrieve data! Try again..", Toast.LENGTH_SHORT).show();
            }
        }

    }
}

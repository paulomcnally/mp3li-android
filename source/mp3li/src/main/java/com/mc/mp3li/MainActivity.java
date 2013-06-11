package com.mc.mp3li;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mc.mp3li.tools.Api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private Api api = null;
    private final ProgressDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initClass();
        initSearchAction();
    }

    private void initClass() {
        api = new Api();
    }

    private void initSearchAction() {
        final EditText search_text = (EditText) findViewById(R.id.editTextSearch);
        ImageButton search_button = (ImageButton) findViewById(R.id.imageButtonSearch);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncRequest().execute(search_text.getText().toString());
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private class AsyncRequest extends AsyncTask<String, String, JSONArray> {

        protected void onPreExecute() {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            // progressBarHome.setVisibility(View.VISIBLE);
        }

        protected JSONArray doInBackground(String... params) {
            JSONArray list = api.request(params[0]);
            return list;
        }

        protected void onProgressUpdate(String... message) {
            //
        }

        protected void onPostExecute(JSONArray list_song) {
            LinearLayout list_container = (LinearLayout) findViewById(R.id.linearLayoutList);
            list_container.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            int i = 0;
            if (list_song.length() > 0) {
                for (i = 0; i < list_song.length(); i++) {
                    final JSONObject song;
                    try {
                        song = list_song.getJSONObject(i);
                        try {
                            View view_list = inflater.inflate(R.layout.list, null);
                            TextView title = (TextView) view_list
                                    .findViewById(R.id.textViewTitle);
                            title.setText(song.getString("song"));

                            TextView artist = (TextView) view_list
                                    .findViewById(R.id.textViewArtist);
                            artist.setText(song.getString("artist") + " | " + song.getString("duration"));

                            final String link = song.getString("link");

                            view_list.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {

                                    showDialog();
                                    // Toast.makeText(getApplicationContext(), link, Toast.LENGTH_LONG).show();

                                }

                            });

                            list_container.addView(view_list);
                            i++;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private void showDialog() {
        //Log.i(TAG, "show Dialog ButtonClick");
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title));

        builder.setItems(
                R.array.dialog_array,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {

                    }
                }).setNegativeButton(getString(R.string.dialog_cancel),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {

                    }
                }
        );
        AlertDialog alert = builder.create();
        alert.show();
    }
}

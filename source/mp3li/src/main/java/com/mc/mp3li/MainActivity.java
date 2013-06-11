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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends Activity {

    private Api api = null;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initClass();
        initSearchAction();

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("A message");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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

                            final String song_title = song.getString("song");
                            final String song_link = song.getString("link");

                            view_list.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {

                                    showDialog(song_link, song_title);
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

    private void showDialog(String url, String title) {

        final String my_url = url;
        final String my_title = title;
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

                        switch (which) {
                            case 0:
                                DownloadFile downloadFile = new DownloadFile();
                                downloadFile.execute(my_url, my_title);
                                break;
                        }

                    }
                }).setNegativeButton(getString(R.string.dialog_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {

                    }
                }
        );
        AlertDialog alert = builder.create();
        alert.show();
    }

    // new DownloadFile().execute( String url, String song name )
    private class DownloadFile extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... sUrl) {
            try {
                URL url = new URL(sUrl[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                urlConnection.setDoOutput(true);
                urlConnection.connect();

                File SDCardRoot = new File("/sdcard/" + "result/");

                File file = new File(SDCardRoot, "filename.format");

                FileOutputStream fileOutput = new FileOutputStream(file);

                InputStream inputStream = urlConnection.getInputStream();

                //this is the total size of the file

                int totalSize = urlConnection.getContentLength();

                //variable to store total downloaded bytes

                int downloadedSize = 0;

                //create a buffer...

                byte[] buffer = new byte[1024];

                int bufferLength = 0; //used to store a temporary size of the buffer

                //now, read through the input buffer and write the contents to the file

                while ((bufferLength = inputStream.read(buffer)) > 0)

                {

                    //add the data in the buffer to the file in the file output stream (the file on the sd card

                    fileOutput.write(buffer, 0, bufferLength);

                    //add up the size so we know how much is downloaded

                    downloadedSize += bufferLength;

                    int progress = (int) (downloadedSize * 100 / totalSize);

                    //this is where you would do something to report the prgress, like this maybe

                    //updateProgress(downloadedSize, totalSize);
                    publishProgress(progress);

                }

                fileOutput.close();


            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mProgressDialog.setProgress(progress[0]);
        }

        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
        }
    }
}

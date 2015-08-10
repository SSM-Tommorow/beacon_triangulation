package com.example.hyeonseob.beacontriangulation;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DBManager {
    private static final String HOST = "211.189.127.101";
    private static final String DB_NAME = "fingerprint";
    private static final String COORDINATE_TABLE_NAME = "coordinate";
    private static final String FINGERPRINT_TABLE_NAME = "fingerprinting";
    private static final String BEACON_TABLE_NAME = "beacon";
    private TextView textView;

    public void getResult(TextView tv){
        textView = tv;
        new GettingPHP().execute("http://"+HOST+"/test.php");
    }

    class GettingPHP extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            StringBuilder jsonHtml = new StringBuilder();
            try {
                URL phpUrl = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection)phpUrl.openConnection();

                if ( conn != null ) {
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);

                    if ( conn.getResponseCode() == HttpURLConnection.HTTP_OK ) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        while ( true ) {
                            String line = br.readLine();
                            if ( line == null )
                                break;
                            jsonHtml.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            return jsonHtml.toString();
        }

        protected void onPostExecute(String str) {
            StringBuffer result = new StringBuffer();
            try {
                JSONObject jObject = new JSONObject(str);
                JSONArray results = jObject.getJSONArray("results");

                result.append("Status : ").append(jObject.get("status")).append('\n');
                result.append("Number of results : ").append(jObject.get("num_result")).append('\n');
                result.append("Results : ");

                for ( int i = 0; i < results.length(); ++i ) {
                    JSONObject temp = results.getJSONObject(i);
                    result.append("table : ").append(temp.get("Tables_in_fingerprint")).append("\n");
                    Log.i("result", i + ": " + temp.get("Tables_in_fingerprint"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            textView.setText(result.toString());
        }
    }
}
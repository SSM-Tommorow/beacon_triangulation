package com.example.hyeonseob.beacontriangulation;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DBManager {
    private static final String HOST = "211.189.127.101";
    private static final String DB_NAME = "fingerprint";
    private static final String COORDINATE_TABLE_NAME = "coordinate";
    private static final String FINGERPRINTING_TABLE_NAME = "fingerprinting";
    private static final String BEACON_TABLE_NAME = "beacon";

    private static final String INSERT_FINGERPRINT_PHP = "http://"+HOST+"/swmem2015_1/insert_fingerprint.php";

    private static final int CONN_TIMEOUT = 100000;

    private TextView textView;
    private int coordinateId;
    private int beaconMajor;


    public DBManager(){
    }

    public void setTextView(TextView tv){
        textView = tv;
    }

    public void insertFingerprint(double[] result, int coordinate_id, int beacon_major){
        Log.i("MAP","start inserting fingerprint");
        coordinateId = coordinate_id;
        beaconMajor = beacon_major;
        textView.setText("Inserting...");
        new InsertFingerprint().execute(result);
    }

    public void getResult(TextView tv){
        textView = tv;
        new GettingPHP().execute("http://"+HOST+"/test.php");
    }

    class InsertFingerprint extends AsyncTask<double[], Integer, String>{

        @Override
        protected String doInBackground(double[]... params) {
            JSONObject jsonObj = new JSONObject();
            JSONArray jsonArr = new JSONArray();
            URL phpUrl;
            HttpURLConnection conn;
            DataOutputStream outputStream;

            try{
                phpUrl = new URL(INSERT_FINGERPRINT_PHP);
                conn = (HttpURLConnection)phpUrl.openConnection();
                conn.setConnectTimeout(CONN_TIMEOUT);
                conn.setReadTimeout(CONN_TIMEOUT);
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                for(double a : params[0])
                    jsonArr.put(a);
                jsonObj.put("rssiList", jsonArr);
                jsonObj.put("windowSize", ConfidenceIntervalActivity.WINDOW_SIZE);
                jsonObj.put("coordID", coordinateId);
                jsonObj.put("beaconMajor", beaconMajor);

                Log.i("MAP", "JSON: " + jsonObj.toString());

                outputStream = new DataOutputStream(conn.getOutputStream());
                outputStream.writeBytes(jsonObj.toString());
                outputStream.flush();
                outputStream.close();

                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    StringBuilder jsonHtml = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    for(;;){
                        String line = br.readLine();
                        if(line == null) break;
                        jsonHtml.append(line + "\n");
                    }

                    Log.i("MAP",jsonHtml.toString());
                    jsonObj = new JSONObject(jsonHtml.toString());
                    if((Boolean)jsonObj.get("success"))
                        return "Complete!";
                    else
                        return "Insert Failed";
                }
            } catch (Exception e){
                Log.i("MAP",e.toString());
                e.printStackTrace();
                return "Connect Failed";
            }

            return "Error";
        }

        @Override
        protected void onPostExecute(String s) {
            textView.setText(s);
        }
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
                return null;
            }
            return jsonHtml.toString();
        }

        protected void onPostExecute(String str) {
            if(str == null)
                return;

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
                return;
            }

            textView.setText("Complete!");
        }
    }
}
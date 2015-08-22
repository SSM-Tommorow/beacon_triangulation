package com.example.hyeonseob.beacontriangulation.Class;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.hyeonseob.beacontriangulation.Activity.ConfidenceIntervalActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DBManager{
    private static final String HOST = "http://211.189.127.101/swmem2015_1/";
    private static final String DB_NAME = "fingerprint";
    private static final String COORDINATE_TABLE_NAME = "coordinate";
    private static final String FINGERPRINTING_TABLE_NAME = "fingerprinting";
    private static final String BEACON_TABLE_NAME = "beacon";

    private static final String INSERT_FINGERPRINT_PHP = HOST+"insert_fingerprint.php";
    private static final String GET_ESTIMATED_LOCATION_PHP = HOST+"get_estimated_location.php";

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
        Log.i("DB","start inserting fingerprint");
        coordinateId = coordinate_id;
        beaconMajor = beacon_major;
        textView.setText("Status: Inserting...");
        new InsertFingerprint().execute(result);
    }

    public void getEstimatedLocation(double[] result, OnTaskCompleted listener){
        Log.i("DB","start estimating location");
        new GetEstimatedLocation(listener).execute(result);
    }

    class GetEstimatedLocation extends AsyncTask<double[], Integer, JSONObject>{
        private OnTaskCompleted listener;
        public GetEstimatedLocation(OnTaskCompleted listener){
            this.listener = listener;
        }

        @Override
        protected JSONObject doInBackground(double[]...params) {
            JSONObject jsonObj = new JSONObject();
            JSONArray jsonArr = new JSONArray();
            URL phpUrl;
            HttpURLConnection conn;
            DataOutputStream outputStream;

            try{
                phpUrl = new URL(GET_ESTIMATED_LOCATION_PHP);
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
                    jsonArr.put((int)a);
                jsonObj.put("rssiList", jsonArr);
                Log.i("DB", "JSON: " + jsonObj.toString());

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

                    Log.i("DB",jsonHtml.toString());
                    jsonObj = new JSONObject(jsonHtml.toString());
                    if((Boolean)jsonObj.get("success"))
                        return jsonObj;
                    else
                        return new JSONObject();
                }
            } catch (Exception e){
                Log.i("MAP",e.toString());
                e.printStackTrace();
                return new JSONObject();
            }
            return new JSONObject();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObj) {
            textView.setText(jsonObj.toString());
            listener.onTaskCompleted(jsonObj);
        }
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


}
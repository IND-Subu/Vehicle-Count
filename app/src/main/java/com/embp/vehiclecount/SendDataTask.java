package com.embp.vehiclecount;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;


public class SendDataTask extends AsyncTask<Map<String, Object>, Void, String> {

    private static final String TAG = "SendDataTask";
    private final WeakReference<OnTaskCompleted> listenerReference;

    public interface OnTaskCompleted {
        void onTaskCompleted(String result);
    }

    public SendDataTask(OnTaskCompleted listener) {
        this.listenerReference = new WeakReference<>(listener);
    }

    @Override
    protected String doInBackground(Map<String, Object>... params) {
        if (params.length == 0 || params[0].isEmpty()) {
            return "Error: No parameters provided";
        }

        Map<String, Object> dataToSend = params[0];

        try {
            String serverUrl = "https://frand.000webhostapp.com/vehicles/vehicle_count_handler.php";

            // Implement the HTTP request logic
            URL url = new URL(serverUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> entry : dataToSend.entrySet()){
                if (postData.length() != 0){
                    postData.append('&');
                }
                postData.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
            }

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(postData.toString());
            wr.flush();
            wr.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK){
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null){
                    response.append(line);
                }
                reader.close();
                return response.toString();
            }
            else {
                return "Error code 62 : "+responseCode+" "+connection.getResponseMessage();
            }
            // ...

        } catch (Exception e) {
            return "Error 80 : " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("Tag: 86","Result: "+result);
        OnTaskCompleted listener = listenerReference.get();
        if (listener != null) {
            listener.onTaskCompleted(result);
        }
    }
}

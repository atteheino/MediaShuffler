package fi.atteheino.mediashuffler.app.utils;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Atte on 25.11.2014.
 */
public class Downloader {
    private static final String TAG = "Downloader";

    public static boolean downloadFile(String locationOfFile, String path, String filename) {
        try {
            URL url = new URL(locationOfFile);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            //urlConnection.setDoOutput(true);

            //connect
            urlConnection.connect();


            //create a new file, to save the downloaded file
            File file = new File(path, filename);

            FileOutputStream fileOutput = new FileOutputStream(file);

            //Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            //close the output stream when complete //
            fileOutput.close();


        } catch (final MalformedURLException e) {
            Log.e(TAG, "Error : MalformedURLException " + e);
            e.printStackTrace();
            return false;
        } catch (final IOException e) {
            Log.e(TAG, "Error : IOException " + e);
            e.printStackTrace();
            return false;
        } catch (final Exception e) {
            Log.e(TAG, "Error : Please check your internet connection " + e);
            return false;
        }

        return true;
    }
}

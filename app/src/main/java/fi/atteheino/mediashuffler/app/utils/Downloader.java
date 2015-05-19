/*
 * Copyright 2015 Atte Heino
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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

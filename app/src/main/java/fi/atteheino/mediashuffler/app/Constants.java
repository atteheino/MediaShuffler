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
package fi.atteheino.mediashuffler.app;

/**
 * Created by Atte on 18.12.2014.
 */
public final class Constants {


    // Defines a custom Intent action
    public static final String BROADCAST_ACTION =
            "fi.atteheino.mediashuffler.app.BROADCAST";

    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS =
            "fi.atteheino.mediashuffler.app.STATUS";

    //Defines shared preferences file
    public static final String SHARED_PREFERENCES_FILE =
            "fi.atteheino.mediashuffler.app.sharedpreferences";

    // Placeholders for shared preferences
    public static final String MEDIASERVER_SET = "mediaserver_set";
    public static final String MEDIASERVER_NAME = "mediaserver_name";
    public static final String MEDIASERVER_UDN = "mediaserver_UDN";
    public static final String SOURCE_SET = "source_set";
    public static final String SOURCE_FOLDER_NAME = "source_folder_name";
    public static final String SOURCE_FOLDER_ID = "source_folder_id";
    public static final String TARGET_SET = "target_set";
    public static final String TARGET_FOLDER = "target_folder";




}

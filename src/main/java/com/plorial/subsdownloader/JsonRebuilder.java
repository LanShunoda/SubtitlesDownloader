package com.plorial.subsdownloader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.*;

/**
 * Created by plorial on 10/13/16.
 */
public class JsonRebuilder {
    public static void main(String[] args) {
        try {
            JSONTokener tokener = new JSONTokener(new FileReader("/home/plorial/Documents/Exoro_backup/exoro-player-export.json"));
            JSONObject series = new JSONObject(tokener).getJSONObject("Series");
            Set<String> seriesArray = series.keySet();
            Map<String,JSONObject> resultSeries = new TreeMap<>();
            for (String seriesName : seriesArray) {
                JSONObject serial = series.getJSONObject(seriesName);
                Set<String> seasons = serial.keySet();
                Map<String,JSONObject> resultSeasons = new TreeMap<>();
                for (String season : seasons) {
                    JSONObject seasonJson = serial.getJSONObject(season);
                   Set<String> episodes =  seasonJson.keySet();
                    Map<String,JSONObject> resultEpisodes = new TreeMap<>();
                    for (String episode : episodes) {
                        if(episode.equals("zzz_urls") | episode.equals("zzz_video_urls")){

                        }else {
                            String subUrl = seasonJson.getString(episode);
                            System.out.println(subUrl);
                            Map<String, String> map = new TreeMap<>();
                            map.put("srt", subUrl);
                            map.put("id", "");
                            JSONObject episodJson = new JSONObject(map);
                            resultEpisodes.put(episode,episodJson);
                            System.out.println(episodJson);
                        }
                    }
                    resultSeasons.put(season,new JSONObject(resultEpisodes));
                }
                resultSeries.put(seriesName, new JSONObject(resultSeasons));
            }
            TreeMap<String, JSONObject> r = new TreeMap<>();
            r.put("Series", new JSONObject(resultSeries));
            JSONObject result = new JSONObject(r);
            File resultFile = new File("/home/plorial/Documents/Exoro_backup/exoro_rebuild.json");
            Writer writer = null;
            if(!resultFile.exists()){
                resultFile.createNewFile();
            }
            try {
                writer = new BufferedWriter(new FileWriter(resultFile));
                writer.write(result.toString());
                writer.flush();
            }finally {
                writer.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

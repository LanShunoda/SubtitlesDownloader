package com.plorial.subsdownloader;

import com.github.wtekiela.opensub4j.api.OpenSubtitles;
import com.github.wtekiela.opensub4j.impl.OpenSubtitlesImpl;
import com.github.wtekiela.opensub4j.response.SubtitleFile;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Created by plorial on 6/30/16.
 */
public class Main {

    private static final String[] LOGIN = {"plorial","lanshunoda", "shport93", "lan_yandex", "lan_yahoo"};
    private static final String[] PASS = {"qazwsx","v9akueyp", "qazwsx","qazwsx","qazwsx" };
    private static final String USER_AGENT = "plorial";
    private static final String URL = "http://api.opensubtitles.org/xml-rpc";

    public static final String BUCKET = "exoro-player.appspot.com";

    private static int download_counter = 0;
    private static int login_counter = 0;
    private static OpenSubtitles subtitles;

    private static Storage storage;
    private static DatabaseReference dbRef;

    public static void main(String[] args) throws MalformedURLException, InterruptedException {
        URL url = new URL(URL);

        subtitles = new OpenSubtitlesImpl(url);

        FirebaseOptions options = null;
        try {
            options = new FirebaseOptions.Builder()
                    .setServiceAccount(new FileInputStream("/home/plorial/Documents/Exoro Player-96d81f15e21c.json"))
                    .setDatabaseUrl("https://exoro-player.firebaseio.com/")
                    .build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FirebaseApp.initializeApp(options);

        dbRef = FirebaseDatabase
                .getInstance()
                .getReference(
//                        "Series FS"
                        "Films"
                );
        try {
            storage = StorageFactory.getService();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        Map<Integer,  Integer> seasons= new TreeMap<>();
//        seasons.put(1,22);
//        seasons.put(2,22);
//        seasons.put(3,20);
//        seasons.put(4,24);
//        seasons.put(5,24);
//        seasons.put(6,24);
//        seasons.put(7,24);
//        seasons.put(8,24);
//        seasons.put(9,24);
//        seasons.put(10,23);
//        seasons.put(11,22);
//        seasons.put(12,21);
//        seasons.put(13,22);
//        seasons.put(14,22);
//        seasons.put(15,22);
//        seasons.put(16,21);
//        seasons.put(17,22);
//        seasons.put(18,22);
//        seasons.put(19,20);
//        seasons.put(20,21);
//        seasons.put(21,23);
//        seasons.put(22,22);
//        seasons.put(23,22);
//        seasons.put(24,22);
//        seasons.put(25,22);
//        seasons.put(26,22);
//        seasons.put(27,22);
//        seasons.put(28,2);

        try {
            subtitles.login(
//                    LOGIN[login_counter],PASS[login_counter],
                    "eng",USER_AGENT);
//            downloadSerialFS("Fear the Walking Dead",seasons);
//            downloadSerialEX("How I Met Your Mother",seasons);
            downloadFilm("I Robot","0343818w");
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
    }

    private static void downloadSerialEX(String serial, Map<Integer, Integer> seasons) throws XmlRpcException {
        TreeMap<String,JSONObject> resultSeries = new TreeMap<>();
        TreeMap<String,JSONObject> resultSeasons = new TreeMap<>();
        for(Map.Entry<Integer,Integer> entry : seasons.entrySet()) {
            int episodes = entry.getValue();
            TreeMap<String,JSONObject> resultEpisodes = new TreeMap<>();
            for(
                    int j = 1; j < episodes+1; j++
//                    int j = episodes; j > 16; j--
                    ) {

                ArrayList<String> unzipedFiles = new ArrayList<>();
                for (int i = 0; i < LANGUAGES.values().length; i++) {
                    List<SubtitleInfo> subs = null;
                    try {
                        subs = subtitles.searchSubtitles(LANGUAGES.values()[i].toString(), serial, entry.getKey().toString(), String.valueOf(j));
                    } catch (XmlRpcException e) {
                        e.printStackTrace();
                    }
                    if (subs.size() > 0) {
                        System.out.println(serial + " Season " + entry.getKey() + " Episode " + j +" lang " + subs.get(0).getLanguage());
//                        String downloadedZip = Downloader.downloadFromUrl(subs.get(0).getZipDownloadLink(), subs.get(0).getFileName());
                        List<SubtitleFile> subtitleFile = subtitles.downloadSubtitles(subs.get(0).getSubtitleFileId());
                        if(!subtitleFile.isEmpty()) {
                            String subFile = subtitleFile.get(0).getContentAsString(subs.get(0).getSubEncoding());
                            String filePath = Downloader.writeStringToFile(subFile, "/home/plorial/Documents/Exoro/" + serial + "/Season " +  entry.getKey() + "/Episode " + j, subs.get(0).getLanguage() + "." + subs.get(0).getFormat());
                            unzipedFiles.add(filePath);
                            download_counter++;
                            System.out.println("downloaded " + download_counter + " (login " + login_counter + ")");
                            if(download_counter == 200 ){
                                login_counter++;
                                if(login_counter == 5){return;}
                                subtitles.logout();
                                subtitles.login(LOGIN[login_counter],PASS[login_counter],"eng",USER_AGENT);
                                download_counter = 0;
                            }
                        } else {
                            System.err.println("failed to load");
                        }
//                        unzipedFiles.add(Downloader.unzipFile(downloadedZip, "/home/plorial/Documents/Exoro/" + serial + "/Season " +  entry.getKey() + "/Episode " + j, subs.get(0).getLanguage() + "." + subs.get(0).getFormat()));
                    }
                }
                String zipFile = Downloader.zipFiles("/home/plorial/Documents/Exoro/"+ serial + "/Season " +  entry.getKey() + "/Episode " + j + "/" + serial + "_s_" +  entry.getKey() + "_e_" + j + ".zip", unzipedFiles.toArray(new String[unzipedFiles.size()]));
                File zip = new File(zipFile);
                try {
                    String link = upload(new FileInputStream(zip),zip.length(), "Series/" + serial + "/Season "+  entry.getKey() + "/" + zip.getName(),BUCKET );
                    System.out.println(link);
                    String serial_name = serial.replace(".", "")/*.replace("'","")*/;
//                    dbRef.child(serial_name + "/Season " +  entry.getKey() + "/Episode " + j).setValue(link);
                    TreeMap<String, String> map = new TreeMap<>();
                    map.put("srt", link);
                    map.put("id", "");
                    JSONObject episodJson = new JSONObject(map);
                    resultEpisodes.put("Episode " + j,episodJson);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            resultSeasons.put("Season " +  entry.getKey(),new JSONObject(resultEpisodes));
        }
        resultSeries.put("" + serial, new JSONObject(resultSeasons));
        File resultFile = new File("/home/plorial/Documents/Exoro_backup/exoro_" + serial + "_ex.json");
        Writer writer = null;
        try {
        if(!resultFile.exists()){
                resultFile.createNewFile();
        }
        try {
            writer = new BufferedWriter(new FileWriter(resultFile));
            writer.write(resultSeries.toString());
            writer.flush();
        }finally {
            writer.close();
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void downloadFilm(String filmName, String imdbid){
        ArrayList<String> unzipedFiles = new ArrayList<>();
        for (int i = 0; i < LANGUAGES.values().length; i++) {
            List<SubtitleInfo> subs = null;
            try {
                subs = subtitles.searchSubtitles(LANGUAGES.values()[i].toString(), imdbid);
            } catch (XmlRpcException e) {
                e.printStackTrace();
            }
            if (subs.size() > 0) {
                System.out.println(" lang " + subs.get(0).getLanguage() + " size " + subs.size());
//                        String downloadedZip = Downloader.downloadFromUrl(subs.get(0).getZipDownloadLink(), subs.get(0).getFileName());
                List<SubtitleFile> subtitleFile = null;
                try {
                    for(int k = 0; k < subs.size(); k ++) {
                        System.out.println(k);
                        subtitleFile = subtitles.downloadSubtitles(subs.get(k).getSubtitleFileId());
                        if(!subtitleFile.isEmpty())
                            break;
                    }
                } catch (XmlRpcException e) {
                    e.printStackTrace();
                }
                if(subtitleFile != null && !subtitleFile.isEmpty()) {
                    String subFile = subtitleFile.get(0).getContentAsString(subs.get(0).getSubEncoding());
                    String filePath = Downloader.writeStringToFile(subFile, "/home/plorial/Documents/Exoro/Films/" + filmName, subs.get(0).getLanguage() + "." + subs.get(0).getFormat());
                    unzipedFiles.add(filePath);
                    download_counter++;
                    System.out.println("downloaded " + download_counter + " (login " + login_counter + ")");
                    if(download_counter == 200 ){
                        login_counter++;
                        if(login_counter == 5){return;}
                        try {
                            subtitles.logout();
                            subtitles.login(LOGIN[login_counter],PASS[login_counter],"eng",USER_AGENT);
                        } catch (XmlRpcException e) {
                            e.printStackTrace();
                        }
                        download_counter = 0;
                    }
                } else {
                    System.err.println("failed to load");
                }
//                        unzipedFiles.add(Downloader.unzipFile(downloadedZip, "/home/plorial/Documents/Exoro/" + serial + "/Season " +  entry.getKey() + "/Episode " + j, subs.get(0).getLanguage() + "." + subs.get(0).getFormat()));
            }
        }
        String zipFile = Downloader.zipFiles("/home/plorial/Documents/Exoro/Films/" + filmName + ".zip", unzipedFiles.toArray(new String[unzipedFiles.size()]));
        File zip = new File(zipFile);
        try {
            String link = upload(new FileInputStream(zip),zip.length(), "Films/" + filmName + "/" + zip.getName(),BUCKET );
            System.out.println(link);
            String film_name = filmName.replace(".", "")/*.replace("'","")*/;
            dbRef.child(film_name + "/srt").setValue(link);
            dbRef.child(film_name + "/id").setValue("");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void downloadSerialFS(String serial, Map<Integer, Integer> seasons) throws XmlRpcException {
        for(Map.Entry<Integer,Integer> entry : seasons.entrySet()) {
            int episodes = entry.getValue();
            for(
                    int j = 1; j < episodes+1; j++
//                    int j = episodes; j > 9; j--
                    ) {
                ArrayList<String> unzipedFiles = new ArrayList<>();
                for (int i = 0; i < LANGUAGES.values().length; i++) {
                    List<SubtitleInfo> subs = null;
                    try {
                        subs = subtitles.searchSubtitles(LANGUAGES.values()[i].toString(), serial, entry.getKey().toString(), String.valueOf(j));
                    } catch (XmlRpcException e) {
                        e.printStackTrace();
                    }
                    if (subs.size() > 0) {
                        System.out.println(serial + " Season " + entry.getKey() + " Episode " + j +" lang " + subs.get(0).getLanguage());
//                        String downloadedZip = Downloader.downloadFromUrl(subs.get(0).getZipDownloadLink(), subs.get(0).getFileName());
                        List<SubtitleFile> subtitleFile = subtitles.downloadSubtitles(subs.get(0).getSubtitleFileId());
                        if(!subtitleFile.isEmpty()) {
                            String subFile = subtitleFile.get(0).getContentAsString(subs.get(0).getSubEncoding());
                            String filePath = Downloader.writeStringToFile(subFile, "/home/plorial/Documents/Exoro/" + serial + "/Season " +  entry.getKey() + "/Episode " + j, subs.get(0).getLanguage() + "." + subs.get(0).getFormat());
                            unzipedFiles.add(filePath);
                            download_counter++;
                            System.out.println("downloaded " + download_counter + " (login " + login_counter + ")");
                            if(download_counter == 200 ){
                                login_counter++;
                                if(login_counter == 5){return;}
                                subtitles.logout();
                                subtitles.login(LOGIN[login_counter],PASS[login_counter],"eng",USER_AGENT);
                                download_counter = 0;
                            }
                        } else {
                            System.err.println("failed to load");
                        }
//                        unzipedFiles.add(Downloader.unzipFile(downloadedZip, "/home/plorial/Documents/Exoro/" + serial + "/Season " +  entry.getKey() + "/Episode " + j, subs.get(0).getLanguage() + "." + subs.get(0).getFormat()));
                    }
                }
                String zipFile = Downloader.zipFiles("/home/plorial/Documents/Exoro/"+ serial + "/Season " +  entry.getKey() + "/Episode " + j + "/" + serial + "_s_" +  entry.getKey() + "_e_" + j + ".zip", unzipedFiles.toArray(new String[unzipedFiles.size()]));
                File zip = new File(zipFile);
                try {
                    String link = upload(new FileInputStream(zip),zip.length(), "Series/" + serial + "/Season "+  entry.getKey() + "/" + zip.getName(),BUCKET );
                    System.out.println(link);
                    String serial_name = serial.replace(".", "")/*.replace("'","")*/;
                    dbRef.child(serial_name + "/Season " +  entry.getKey() + "/Episode " + j).setValue(link);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String upload(FileInputStream stream, long byteCount, String name, String bucket){
        InputStream inputStream = stream;
        try {
            InputStreamContent mediaContent = new InputStreamContent("application/octet-stream", inputStream);

            mediaContent.setLength(byteCount);

            Storage.Objects.Insert insertObject = storage.objects().insert(bucket, null, mediaContent);

            insertObject.setName(name);
            insertObject.setPredefinedAcl("publicRead");

            if (mediaContent.getLength() > 0 && mediaContent.getLength() <= 2 * 1000 * 1000 /* 2MB */) {
                insertObject.getMediaHttpUploader().setDirectUploadEnabled(true);
            }
            return insertObject.execute().getMediaLink();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

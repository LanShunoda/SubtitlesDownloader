package com.plorial.subsdownloader;

import com.github.wtekiela.opensub4j.api.OpenSubtitles;
import com.github.wtekiela.opensub4j.impl.OpenSubtitlesImpl;
import com.github.wtekiela.opensub4j.response.SubtitleFile;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import org.apache.xmlrpc.XmlRpcException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.*;

import static java.lang.Thread.sleep;

/**
 * Created by plorial on 6/30/16.
 */
public class Main {

    private static final String LOGIN = "lanshunoda";
    private static final transient String PASS = "v9akueyp";
    private static final String USER_AGENT = "OSTestUserAgent";
    private static final String URL = "http://api.opensubtitles.org/xml-rpc";

    public static final String BUCKET = "exoro-player.appspot.com";

    private static Storage storage;
    private static DatabaseReference dbRef;

    public static void main(String[] args) throws MalformedURLException, XmlRpcException, InterruptedException {
        URL url = new URL(URL);
        OpenSubtitles subtitles = new OpenSubtitlesImpl(url);
        subtitles.login(LOGIN,PASS,"eng",USER_AGENT);

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
                .getReference("Series");

        try {
            storage = StorageFactory.getService();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        Map<Integer,  Integer> seasons= new HashMap<>();
        seasons.put(1,10);
        seasons.put(2,10);
        seasons.put(3,10);
        seasons.put(4,10);
        seasons.put(5,10);
        seasons.put(6,10);

        downloadSerial("Game of Thrones",seasons,subtitles);
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

    private static void downloadSerial(String serial, Map<Integer, Integer> seasons, OpenSubtitles subtitles) throws XmlRpcException {
        for(Map.Entry<Integer,Integer> entry : seasons.entrySet()) {
            int episodes = entry.getValue();
            for(int j = 1; j < episodes+1; j++) {
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
                        SubtitleFile subtitleFile = subtitles.downloadSubtitles(subs.get(0).getSubtitleFileId()).get(0);
                        String subFile = subtitleFile.getContentAsString(subs.get(0).getSubEncoding());
//                        unzipedFiles.add(Downloader.unzipFile(downloadedZip, "/home/plorial/Documents/Exoro/" + serial + "/Season " +  entry.getKey() + "/Episode " + j, subs.get(0).getLanguage() + "." + subs.get(0).getFormat()));
                        unzipedFiles.add(Downloader.writeStringToFile(subFile, "/home/plorial/Documents/Exoro/" + serial + "/Season " +  entry.getKey() + "/Episode " + j, subs.get(0).getLanguage() + "." + subs.get(0).getFormat()));
                    }
                }
                String zipFile = Downloader.zipFiles("/home/plorial/Documents/Exoro/"+ serial + "/Season " +  entry.getKey() + "/Episode " + j + "/" + serial + "_s_" +  entry.getKey() + "_e_" + j + ".zip", unzipedFiles.toArray(new String[unzipedFiles.size()]));
                File zip = new File(zipFile);
                try {
                    String link = upload(new FileInputStream(zip),zip.length(), "Series/" + serial + "/Season "+  entry.getKey() + "/" + zip.getName(),BUCKET );
                    System.out.println(link);
                    dbRef.child(serial + "/Season " +  entry.getKey() + "/Episode " + j).setValue(link);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package com.plorial.subsdownloader;

import com.github.wtekiela.opensub4j.api.OpenSubtitles;
import com.github.wtekiela.opensub4j.impl.OpenSubtitlesImpl;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import org.apache.xmlrpc.XmlRpcException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * Created by plorial on 6/30/16.
 */
public class Main {

    private static final String LOGIN = "lanshunoda";
    private static final String PASS = "v9akueyp";
    private static final String USER_AGENT = "OSTestUserAgent";
    private static final String URL = "http://api.opensubtitles.org/xml-rpc";

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

        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("Series");

        try {
            Bucket list = getBucket("exoro-player.appspot.com");

            System.out.println(list.toString());
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

//        downloadSerial("Game of Thrones",seasons,subtitles);

    }

    public static List<StorageObject> listBucket(String bucketName)
            throws IOException, GeneralSecurityException {
        Storage client = StorageFactory.getService();
        Storage.Objects.List listRequest = client.objects().list(bucketName);

        List<StorageObject> results = new ArrayList<StorageObject>();
        Objects objects;

        // Iterate through each page of results, and add them to our results list.
        do {
            objects = listRequest.execute();
            // Add the items in this page of results to the list we'll return.
            results.addAll(objects.getItems());

            // Get the next page, in the next iteration of this loop.
            listRequest.setPageToken(objects.getNextPageToken());
        } while (null != objects.getNextPageToken());

        return results;
    }

    public static Bucket getBucket(String bucketName) throws IOException, GeneralSecurityException {
        Storage client = StorageFactory.getService();

        Storage.Buckets.Get bucketRequest = client.buckets().get(bucketName);
        // Fetch the full set of the bucket's properties (e.g. include the ACLs in the response)
        bucketRequest.setProjection("full");
        return bucketRequest.execute();
    }

    private static void downloadSerial(String serial, Map<Integer, Integer> seasons, OpenSubtitles subtitles){
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
                        System.out.println("serial " + serial + " Season " + entry.getKey() + " Episode " + j +" lang " + subs.get(0).getLanguage());
                        String downloadedZip = Downloader.downloadFromUrl(subs.get(0).getZipDownloadLink(), subs.get(0).getFileName());
                        unzipedFiles.add(Downloader.unzipFile(downloadedZip, "/home/plorial/Documents/Exoro/" + serial + "/Season " +  entry.getKey() + "/Episode " + j, subs.get(0).getLanguage() + "." + subs.get(0).getFormat()));
                    }
                }
                Downloader.zipFiles("/home/plorial/Documents/Exoro/"+ serial + "/Season " +  entry.getKey() + "/Episode " + j + "/" + serial + "_s_" +  entry.getKey() + "_e_" + j + ".zip", unzipedFiles.toArray(new String[unzipedFiles.size()]));
            }
        }
    }
}

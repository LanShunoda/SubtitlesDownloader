package com.plorial.subsdownloader;

import com.github.wtekiela.opensub4j.api.OpenSubtitles;
import com.github.wtekiela.opensub4j.impl.OpenSubtitlesImpl;
import com.github.wtekiela.opensub4j.response.ServerInfo;
import com.github.wtekiela.opensub4j.response.SubtitleFile;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;
import org.apache.xmlrpc.XmlRpcException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by plorial on 6/30/16.
 */
public class Main {

    private static final String LOGIN = "lanshunoda";
    private static final String PASS = "v9akueyp";
    private static final String USER_AGENT = "OSTestUserAgent";
    private static final String URL = "http://api.opensubtitles.org/xml-rpc";

    public static void main(String[] args) throws MalformedURLException, XmlRpcException {
        URL url = new URL(URL);
        OpenSubtitles subtitles = new OpenSubtitlesImpl(url);
        subtitles.login(LOGIN,PASS,"eng",USER_AGENT);

        Map<Integer,  Integer> seasons= new HashMap<>();
        seasons.put(1,10);
        seasons.put(2,10);
        seasons.put(3,10);
        seasons.put(4,10);
        seasons.put(5,10);
        seasons.put(6,10);

        downloadSerial("Game of Thrones",seasons,subtitles);
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
                        unzipedFiles.add(Downloader.unzipFile(downloadedZip, "/home/plorial/Documents/Exoro/" + serial + "/season " +  entry.getKey() + "/episode " + j, subs.get(0).getLanguage() + "." + subs.get(0).getFormat()));
                    }
                }
                Downloader.zipFiles("/home/plorial/Documents/Exoro/"+ serial + "/Season " +  entry.getKey() + "/Episode " + j + "/" + serial + "_s_" +  entry.getKey() + "_e_" + j + ".zip", unzipedFiles.toArray(new String[unzipedFiles.size()]));
            }
        }
    }
}

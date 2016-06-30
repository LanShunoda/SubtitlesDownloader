package com.plorial.subsdownloader;

import com.github.wtekiela.opensub4j.api.OpenSubtitles;
import com.github.wtekiela.opensub4j.impl.OpenSubtitlesImpl;
import com.github.wtekiela.opensub4j.response.ServerInfo;
import com.github.wtekiela.opensub4j.response.SubtitleFile;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;
import org.apache.xmlrpc.XmlRpcException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        ArrayList<String> unzipedFiles = new ArrayList<>();

        for (int i =0 ; i < LANGUAGES.values().length; i++){
            List<SubtitleInfo> subs = subtitles.searchSubtitles(LANGUAGES.values()[i].toString(), "Game of Thrones", "1", "1");
            System.out.println(LANGUAGES.values()[i].toString());
            if(subs.size() > 0) {
                System.out.println(subs.get(0).getLanguage());
                String downloadedZip = Downloader.downloadFromUrl(subs.get(0).getZipDownloadLink(),subs.get(0).getFileName());
                unzipedFiles.add(Downloader.unzipFile(downloadedZip,"/home/plorial/Documents/Exoro/Game of Thrones/season 1",subs.get(0).getLanguage() + "." + subs.get(0).getFormat()));

            }
        }
        Downloader.zipFiles("/home/plorial/Documents/Exoro/Game of Thrones/season 1/Got_s1e1.zip", unzipedFiles.toArray(new String[unzipedFiles.size()]));
    }
}

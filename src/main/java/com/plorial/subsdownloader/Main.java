package com.plorial.subsdownloader;

import com.github.wtekiela.opensub4j.api.OpenSubtitles;
import com.github.wtekiela.opensub4j.impl.OpenSubtitlesImpl;
import com.github.wtekiela.opensub4j.response.ServerInfo;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;
import org.apache.xmlrpc.XmlRpcException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Created by plorial on 6/30/16.
 */
public class Main {
    public static void main(String[] args) throws MalformedURLException, XmlRpcException {
        URL url = new URL("http://api.opensubtitles.org/xml-rpc");
        OpenSubtitles subtitles = new OpenSubtitlesImpl(url);
        subtitles.login("lanshunoda","v9akueyp","eng","OSTestUserAgent");
        ServerInfo info = subtitles.serverInfo();
        System.out.println(info);
        List<SubtitleInfo> subs = subtitles.searchSubtitles("eng", "Game of Thrones", "1", "1");
        System.out.println(subs.get(0).getDownloadLink());

    }
}

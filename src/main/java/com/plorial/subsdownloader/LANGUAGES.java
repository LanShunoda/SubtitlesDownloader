package com.plorial.subsdownloader;

/**
 * Created by plorial on 6/30/16.
 */
public enum LANGUAGES {
    EN("eng"),
    RU("rus"),
    ES("spa"),
    IT("ita"),
    PT("por"),
    FR("fre"),
    DE("ger"),
    PL("pol"),
    TR("tur");

    private String language;

    private LANGUAGES(final String language){
        this.language = language;
    }

    @Override
    public String toString() {
        return language;
    }
}

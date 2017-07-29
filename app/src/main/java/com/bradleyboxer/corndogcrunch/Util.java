package com.bradleyboxer.corndogcrunch;

/**
 * Created by Bradley on 7/28/2017.
 */

public class Util {
    public static String getCommand(String data) {
        if(data.startsWith("/")) {
            String basecommand = null;

            for(int i=0;i<data.length();i++) {
                if(Character.isWhitespace(data.charAt(i))) {
                    basecommand = data.substring(1, i);
                }
            }
            if(basecommand==null) {
                basecommand = data.substring(1, data.length());
            }

            return basecommand;
        } else return data;
    }

    public static String getSubcommand(String data) {
        if(data.startsWith("/")) {
            String subcommand = null;

            for(int i=0;i<data.length();i++) {
                if(Character.isWhitespace(data.charAt(i))) {
                    subcommand = data.substring(i+1, data.length());
                    return subcommand;
                }
            }
        }
        return null;
    }
}

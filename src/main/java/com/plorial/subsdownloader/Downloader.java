package com.plorial.subsdownloader;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by plorial on 6/30/16.
 */
public class Downloader {

    public static String downloadFromUrl(String url, String name){
        InputStream inputStream = null;
        OutputStream output = null;
        File file = null;
        try {
            file = File.createTempFile(name,".zip");
            file.deleteOnExit();

            URL uri = new URL(url);
            URLConnection connection = uri.openConnection();

            inputStream = connection.getInputStream();
            byte[] buffer = new byte[4096];
            int n = - 1;
            output = new FileOutputStream( file );
            while ( (n = inputStream.read(buffer)) != -1)
            {
                output.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file.getAbsolutePath();
    }

    public static String unzipFile(String zipFile, String outputFolder, String fileName){

        byte[] buffer = new byte[1024];

        ZipInputStream zis = null;
        FileOutputStream fos = null;

        try{
            File folder = new File(outputFolder);

            if(!folder.exists()){
                folder.mkdir();
            }
            zis = new ZipInputStream(new FileInputStream(zipFile));

            ZipEntry ze = zis.getNextEntry();

//            while(ze!=null){

                File newFile = new File(outputFolder, fileName);

                new File(newFile.getParent()).mkdirs();

                fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
            return newFile.getAbsolutePath();
//                ze = zis.getNextEntry();
//            }
        }catch(IOException ex){
            ex.printStackTrace();
        }finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (zis != null) {
                    zis.closeEntry();
                    zis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return zipFile;
    }

    public static void zipFiles(String zipName, String[] srcFiles){
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        FileInputStream fis = null;
        try {
            // create byte buffer
            byte[] buffer = new byte[1024];


            fos = new FileOutputStream(zipName);


            zos = new ZipOutputStream(fos);

            for (int i=0; i < srcFiles.length; i++) {

                File srcFile = new File(srcFiles[i]);


                fis = new FileInputStream(srcFile);

                // begin writing a new ZIP entry, positions the stream to the start of the entry data
                zos.putNextEntry(new ZipEntry(srcFile.getName()));

                int length;

                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();

                // close the InputStream
                fis.close();
            }
            // close the ZipOutputStream
            zos.close();
        }
        catch (IOException ioe) {
            System.out.println("Error creating zip file: " + ioe);
        }/*finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (zos != null) {
                    zos.close();
                }
                if (fis != null ) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }
}

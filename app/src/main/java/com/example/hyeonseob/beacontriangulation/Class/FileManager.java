package com.example.hyeonseob.beacontriangulation.Class;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class FileManager {

    private static final String PATH = Environment.getExternalStorageDirectory()+"/Beacon_Triangulation/";
    private String FILE_NAME;
    private int[][][] mInitialData;
    private File file;

    public FileManager(String deviceString){
        mInitialData = new int[32][4][15];
        FILE_NAME = "fingerprint_" + deviceString + ".txt";
        makeFile();
    }

    private void makeFile(){
        File dir = new File(PATH);
        if (!dir.exists())
        {
            dir.mkdirs();
            Log.i("FILE", "dir not exists");
        }else{
            Log.i("FILE", "dir exists" );
        }

        boolean isSuccess = false;
        if(dir.isDirectory()){
            file = new File(PATH+FILE_NAME);
            if(file!=null && !file.exists()){
                Log.i("FILE", "file not exists");
                try {
                    isSuccess = file.createNewFile();
                    writeFile(mInitialData);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    Log.i("FILE", "isSuccess: "+isSuccess);
                }
            }else{
                Log.i("FILE", "file exists" );
            }
        }
    }

    public int[][][] readFile(){
        int[][][] fingerprint = new int[32][4][15];
        int count = 0, i;
        String line, list[];
        if(file!=null && file.exists()){
            try {
                FileReader reader = new FileReader(file);
                BufferedReader buffReader = new BufferedReader(reader);

                while((line = buffReader.readLine()) != null){
                    list = line.split("\\s",16);
                    for(i=0; i<15; i++)
                        fingerprint[count/4][count%4][i] = Integer.parseInt(list[i]);
                    Log.i("FILE","read: "+fingerprint[count/4][count%4][0]+"...");
                    count++;
                }

                buffReader.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fingerprint;
    }

    public void writeFile(int[][][] fingerprint){
        FileOutputStream fos;
        StringBuffer strbuff = new StringBuffer();
        int i,j,k;
        for(i=0; i<32; i++) {
            for(j=0; j<4; j++) {
                for(k=0; k<15; k++) {
                    strbuff.append(fingerprint[i][j][k]);
                    strbuff.append(" ");
                }
                strbuff.append("\n");
            }
        }

        if(file!=null && file.exists() && fingerprint!=null){
            try {
                fos = new FileOutputStream(file);
                try {
                    fos.write(strbuff.toString().getBytes());
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Log.i("FILE","wrilte success!: "+strbuff.toString());
        }else{
            Log.i("FILE","write failed");
        }
    }
}

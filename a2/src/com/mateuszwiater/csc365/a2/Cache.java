package com.mateuszwiater.csc365.a2;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Mateusz on 4/8/2015.
 */
public class Cache {
    private HashMap<String, String> cache = new HashMap<String, String>();;

    private RandomAccessFile persistence;

    private String name;

    private long lastModified;

    public Cache(String name) throws IOException {
        this.name = name;
        // Check if the cache exists
        if(new File(name).exists()) {
            // Cache exists, load it
            persistence = new RandomAccessFile(this.name, "rw");
            load();
        } else {
            // Create the RandomAccessFile
            persistence = new RandomAccessFile(this.name, "rw");
        }
    }

    public void save() throws IOException {
        // Seek to the beginning
        persistence.seek(0);
        // Write the lastModified value
        persistence.writeLong(System.currentTimeMillis());
        // Save the rest of the elements
        for(String key : cache.keySet()) {
            // Write the length of the key
            persistence.writeInt(key.getBytes("UTF-8").length);
            // Write the actual key
            persistence.write(key.getBytes("UTF-8"));
            // Write the length of the value
            persistence.writeInt(cache.get(key).getBytes("UTF-8").length);
            // Write the actual value
            persistence.write(cache.get(key).getBytes("UTF-8"));
        }
        // Write the terminating value
        persistence.writeInt(-1);
    }

    private void load() throws IOException {
        // Seek to the beginning
        persistence.seek(0);
        // Read the last modified time
        lastModified = persistence.readLong();
        // Loop to get the rest of the hash elements
        while(true) {
            String key,value;
            byte[] tmpBytes;
            // Read how big the next element will be
            int readSize = persistence.readInt();
            // Check if this is the end of the hashMap
            if(readSize == -1) {
                // End of file reached
                break;
            }
            tmpBytes = new byte[readSize];
            // Read in the key
            persistence.read(tmpBytes);
            key = new String(tmpBytes, "UTF-8");
            // Read the value size
            readSize = persistence.readInt();
            // Read the value
            tmpBytes = new byte[readSize];
            persistence.read(tmpBytes);
            value = new String(tmpBytes, "UTF-8");
            // Add to the cache
            cache.put(key, value);
        }
    }

    public String get(String url) {
        return cache.get(url);
    }

    public void put(String url, String file) {
        cache.put(url, file);
    }

    public long getLastModified() {
        return lastModified;
    }

    public Set<String> getUrls() {
        return cache.keySet();
    }

    public int getSize() {
        return cache.size();
    }




}

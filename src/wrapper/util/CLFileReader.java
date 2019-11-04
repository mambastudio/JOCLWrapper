/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class CLFileReader 
{
    public static String readFile(String directory, String file)
    {
        Path path = FileSystems.getDefault().getPath(directory, file);
        byte[] byteArray = null;
        try {
            byteArray = Files.readAllBytes(path);
        } catch (IOException ex) {
            Logger.getLogger(CLFileReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new String(byteArray);
    }
    
    public static String[] readFile(String directory, String... files)
    {
        String[] sources = new String[files.length];        
        for(int i = 0; i<files.length; i++)
        {
            Path path = FileSystems.getDefault().getPath(directory, files[i]);
            byte[] byteArray = null;
            try {
                byteArray = Files.readAllBytes(path);
            } catch (IOException ex) {
                Logger.getLogger(CLFileReader.class.getName()).log(Level.SEVERE, null, ex);
            }
            sources[i] = new String(byteArray);
        }
        return sources;
    }
    
    public static String readFile(Class<?> clazz, String file)
    {
        InputStream inputStream = clazz.getResourceAsStream(file);
        return readFromInputStream(inputStream);
    }
    
    public static String[] readFile(Class<?> clazz, String... files)
    {
        String[] sources = new String[files.length];
        for(int i = 0; i<files.length; i++)
            sources[i] = readFile(clazz, files[i]);
        return sources;
    }
    
    private static String readFromInputStream(InputStream inputStream)
    {
        StringBuilder resultStringBuilder = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(CLFileReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultStringBuilder.toString();
    }
}

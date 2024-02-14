package com.eidiko.mongo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class MongoDBBackupRestore {

    public static void main(String[] args) {
    	Properties props = new Properties();
		try (InputStream inputStream = MongoDBBackupRestoreAWS.class.getResourceAsStream("/config.properties")) {
		    props.load(inputStream);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		String sHost = props.getProperty("sHost");
		String tHost = props.getProperty("tHost");
		Integer sPort = Integer.parseInt(props.getProperty("sPort"));
		Integer tPort = Integer.parseInt(props.getProperty("tPort"));
		String sDatabase = props.getProperty("sDatabase");
		String tDatabase = props.getProperty("tDatabase");

        
        // Backup from source database
        backupDatabase(sHost, sPort, sDatabase);
        
        // Restore to target database
        restoreDatabase(tHost, tPort, tDatabase,sDatabase);
    }

    private static void backupDatabase(String host, int port, String database) {
        String command = "mongodump" +
                " --host " + host +
                " --port " + port +
                " --db " + database +
                " --out C:\\Users\\tonda\\Downloads\\mongodb-tools";       
        executeCommand(command);
    }

    private static void restoreDatabase(String host, int port, String database,String srcDatabase) {
        String command = "mongorestore" +
                " --host " + host +
                " --port " + port +
                " --db " + database +
                " C:\\Users\\tonda\\Downloads\\mongodb-tools\\" + srcDatabase;
        
        executeCommand(command);
    }

    private static void executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Command executed successfully: " + command);
            } else {
                System.err.println("Error executing command: " + command);
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                try {
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
        } catch (Exception e) {
            System.err.println("Error executing command Catch: " + command);
            e.printStackTrace();
        }
    }
}

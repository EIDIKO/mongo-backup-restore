package com.eidiko.mongo;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MongoDBBackupRestoreAWS {
	 private static final Logger logger = LogManager.getLogger(MongoDBBackupRestoreAWS.class);


	public static void main(String[] args) {

		try {
			Properties props = loadProperties("config.properties");

			String s3BucketName = props.getProperty("mywso2bucket");
			String s3Key = props.getProperty("s3Key");
			String accessKey = props.getProperty("accessKey");
			String secretKey = props.getProperty("secretKey");
			String region = props.getProperty("region");
			String s = props.getProperty("srcFilePath");
			String t = props.getProperty("targetFilePath");
			String sHost = props.getProperty("sHost");
			String tHost = props.getProperty("tHost");
			Integer sPort = Integer.parseInt(props.getProperty("sPort"));
			Integer tPort = Integer.parseInt(props.getProperty("tPort"));
			String sDatabase = props.getProperty("sDatabase");
			String tDatabase = props.getProperty("tDatabase");
			String backupFolder = props.getProperty("backupFolder");
			
			logger.info("Backing up Database:: " + sDatabase);

			String backupCommand = "mongodump" + " --host " + sHost + " --port " + sPort + " --db " + sDatabase
					+ " --out " +backupFolder;
			executeCommand(backupCommand);
			logger.info("Uploading Backup file to ASW S3 Bucket:: " + s3BucketName);
			backupDatabase(s3BucketName, s3Key, accessKey, secretKey, region, s, t);
			logger.info("Downloading Backup file to ASW S3 Bucket:: " + s3BucketName);
			logger.info("Restoring Database to :: " + tDatabase);
			String restoreCommand = "mongorestore" + " --host " + tHost + " --port " + tPort + " --db " + tDatabase
					+ " " +backupFolder + sDatabase;
			executeCommand(restoreCommand);
			logger.info("Done!!");
			logger.info("Success!!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void backupDatabase(String s3BucketName, String s3Key, String accessKey, String secretKey,
			String region, String s, String t) {
		
		uploadToS3(s3BucketName, s3Key, s, region, accessKey, secretKey);
		downloadFromS3(s3BucketName, s3Key, t, region, accessKey, secretKey);
	}

	private static void uploadToS3(String bucketName, String key, String filePath, String region, String accessKey,
			String secretKey) {
		AmazonS3 s3Client = getS3Client(accessKey, secretKey, region);
		File file = new File(filePath);
		s3Client.putObject(new PutObjectRequest(bucketName, file.getName(), file));
		logger.info("File uploaded to S3: " + file.getName());
	}

	private static void downloadFromS3(String bucketName, String key, String localFilePath, String region,
			String accessKey, String secretKey) {
		AmazonS3 s3Client = getS3Client(accessKey, secretKey, region);
		S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, key));
		try (InputStream inputStream = object.getObjectContent();
				OutputStream outputStream = new FileOutputStream(new File(localFilePath))) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			logger.info("File downloaded from S3: " + localFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static AmazonS3 getS3Client(String accessKey, String secretKey, String region) {
		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region) // Specify the AWS
																									// region here
				.build();

		return s3Client;
	}

	private static Properties loadProperties(String fileName) throws IOException {
		Properties props = new Properties();

		try (InputStream inputStream = MongoDBBackupRestoreAWS.class.getResourceAsStream("/" + fileName)) {
			props.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}

	private static void executeCommand(String command) {
		try {
			Process process = Runtime.getRuntime().exec(command);
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				logger.info("Command Executed Successfully:: " + command);
			} else {
				System.err.println("Error Executing Command: " + command);
				BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				String line;
				try {
					while ((line = errorReader.readLine()) != null) {
						logger.error(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		} catch (Exception e) {
			logger.error("Error executing command in Catch: " + command);
			e.printStackTrace();
		}
	}
}

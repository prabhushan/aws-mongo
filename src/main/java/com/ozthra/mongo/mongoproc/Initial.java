package com.ozthra.mongo.mongoproc;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDocument;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

//@SpringBootApplication
public class Initial {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String... args) throws ExecuteException, IOException {
		// ApplicationContext ctx = SpringApplication.run(Intial.class, args);
		Initial initial = new Initial();
		if (StringUtils.equalsIgnoreCase("start", args[0]))
			initial.startMongo();
		else if (StringUtils.equalsIgnoreCase("stop", args[0]))
			initial.stopMongo();

	}

	private void startMongo() throws ExecuteException, IOException {
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		Runnable runnable = new Runnable() {
			public void run() {
				String line = "/Users/prabhu/Desktop/prabhu/software/mongodb-osx-x86_64-3.0.7/bin/mongod --replSet \"rs0\"";
				// mongod --replSet "rs0"
				CommandLine cmdLine = CommandLine.parse(line);
				DefaultExecutor executor = new DefaultExecutor();
				try {
					int exitValue = executor.execute(cmdLine);
				} catch (ExecuteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		executorService.submit(runnable);
		executorService.shutdown();
		System.out.println("started");

	}

	public void stopMongo() {
		MongoClient client = new MongoClient(new ServerAddress("ec2-54-201-72-231.us-west-2.compute.amazonaws.com"));
		System.out.println(client.getConnectPoint());
		BsonDocument command = new BsonDocument();
		final MongoDatabase database = client.getDatabase("admin");

		Document buildInfo = database.runCommand(new Document("shutdown", 1));
		System.out.println(buildInfo);
	}

}

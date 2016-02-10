package com.ozthra.mongo.mongoproc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.annotation.JsonAppend.Prop;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.ozthra.mongo.model.ReplicaConfig;
import com.ozthra.mongo.model.ReplicaNode;

public class CloudConnect {

	private static String CREATE_REPLICA_DIR = "sudo mkdir -p ";
	private static String REPLICA_FOLDER_PATH = "/data/mongosvr/rs-";
	private static String MONGO_DB_FOLDER_PATH = "/data/db";
	private static String CREATE_REPLICA_SHELL = "sudo mongod --replSet repset --fork --logpath /data/mongosvr/log";
	private static String CREATE_REPLICA_CONFIG = "mongo --port <<PORT>> --eval \"JSON.stringify(db.adminCommand({\"replSetInitiate\" : <<CONFIG>>}))\"";
	private static String PORT = " --port ";
	private static int InitialPortNumber = 27091;
	private static String[] arrStringSource = new String[] { "<<PORT>>", "<<CONFIG>>" };

	private JSch jsch;
	private String serverName;
	static Gson gson = new GsonBuilder().create();
	static Properties prop = new Properties();

	public CloudConnect(JSch jsch2, String serverName) {
		this.setJsch(jsch2);
		this.setServerName(serverName);
	}

	public static void main(String[] args) throws NumberFormatException, Exception {

		JSch jsch = new JSch();
		JSch.setConfig("StrictHostKeyChecking", "no");

		prop.load(new FileInputStream("./resources/config.properties"));
		jsch.addIdentity(prop.getProperty("PEM_FILE"));
		String command = null;
		// run stuff
		if (args != null && args.length >= 1) {
			CloudConnect cloudConnect = new CloudConnect(jsch, prop.getProperty("CLOUD_NAME"));

			if (args[0].equalsIgnoreCase("start")) {
				cloudConnect.startMongo();
			} else if (args[0].equalsIgnoreCase("stop")) {
				cloudConnect.stopMongo();
			} else if (args[0].equalsIgnoreCase("replica")) {
				cloudConnect.replicateMongo(Integer.parseInt(args[1]));
			} else if (args[0].equalsIgnoreCase("general")) {
				cloudConnect.printGeneralInfo();
			} else if (args[0].equalsIgnoreCase("kill")) {
				cloudConnect.findThemKillThem();
			} else {
				System.out.println("Invalid arguments passed!!");
				System.exit(0);
			}
		} else {
			System.out.println("at least 2 Arguments required for Mongo AWS!!");
			System.exit(0);
		}

	}

	private void startMongo() {

		try {
			executeCommands("whoami;hostname;sudo mongod --dbpath " + MONGO_DB_FOLDER_PATH, this.getJsch(),
					this.getServerName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void stopMongo() {

		try {
			executeCommands("whoami;hostname;sudo mongod --dbpath " + MONGO_DB_FOLDER_PATH + " --shutdown",
					this.getJsch(), this.getServerName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void replicateMongo(int numberOfReplica) throws Exception {
		StringBuilder replicaSetBuilder = null;

		ReplicaConfig replicaConfig = new ReplicaConfig();
		replicaConfig.set_id("repset");
		String cloudName = prop.getProperty("CLOUD_NAME");
		for (int i = 0; i < numberOfReplica; i++) {
			replicaSetBuilder = new StringBuilder(CREATE_REPLICA_DIR).append(REPLICA_FOLDER_PATH).append(i).append(";")
					.append(CREATE_REPLICA_SHELL).append(PORT).append(InitialPortNumber + i).append(" --dbpath ")
					.append(REPLICA_FOLDER_PATH).append(i).append(";");
			executeCommands(replicaSetBuilder.toString(), this.getJsch(), this.getServerName());
			replicaConfig.AddReplicateNode(new ReplicaNode(Integer.toString(i), cloudName+":"+Integer.toString(InitialPortNumber + i)));
		}
		String config;
		System.out.println(config = gson.toJson(replicaConfig));
		String command = StringUtils.replaceEach(CREATE_REPLICA_CONFIG, arrStringSource,
				new String[] { Integer.toString(InitialPortNumber), config });
		System.out.println(command);
		executeCommands(command, this.getJsch(), this.getServerName());

	}

	private void printGeneralInfo() {
		try {
			executeCommands("whoami;hostname", this.getJsch(), this.getServerName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void printInput(Channel channel) throws IOException {

		// start reading the input from the executed commands on the shell
		byte[] tmp = new byte[1024];
		InputStream input = channel.getInputStream();
		while (true) {
			while (input.available() > 0) {
				int i = input.read(tmp, 0, 1024);
				if (i < 0)
					break;
				System.out.println((new String(tmp, 0, i)));
			}
			if (channel.isClosed()) {
				System.out.println("exit-status: " + channel.getExitStatus());
				break;
			}
			// Thread.sleep(1000);
		}
	}

	// private static void buildReplicaConfig() {
	// ReplicaConfig replicaConfig = new ReplicaConfig();
	// replicaConfig.set_id("repset");
	// replicaConfig.AddReplicateNode(new ReplicaNode("localhost", "1"));
	// replicaConfig.AddReplicateNode(new ReplicaNode("localhost2", "2"));
	// System.out.println(gson.toJson(replicaConfig));
	// }

	private void findThemKillThem() {
		try {
			executeCommands("whoami;hostname;sudo killall mongod", this.getJsch(), this.getServerName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void executeCommands(String command, JSch jsch, String serverName) throws Exception {
		Session session = jsch.getSession("ubuntu", serverName, 22);
		session.connect(5000);

		Channel channel = session.openChannel("exec");
		// Channel channel=session.openChannel("shell");
		((ChannelExec) channel).setCommand(command);

		// channel.setInputStream(System.in);
		// channel.setOutputStream(System.out);

		channel.connect();

		printInput(channel);

		channel.disconnect();
		session.disconnect();
	}

	public JSch getJsch() {
		return jsch;
	}

	public void setJsch(JSch jsch) {
		this.jsch = jsch;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

}

package com.ozthra.mongo.model;

public class ReplicaNode {

	private String _id;
	private String host;

	public ReplicaNode(String _id, String host) {
		this._id = _id;
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

}

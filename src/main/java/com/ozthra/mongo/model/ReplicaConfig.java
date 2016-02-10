package com.ozthra.mongo.model;

import java.util.ArrayList;
import java.util.List;

public class ReplicaConfig {
	private String _id;
	private List<ReplicaNode> members;

	public List<ReplicaNode> getMembers() {
		return members;
	}

	public void AddReplicateNode(ReplicaNode node) {
		if (members == null) {
			members = new ArrayList<>();

		}
		members.add(node);
	}

	public void setMembers(List<ReplicaNode> members) {
		this.members = members;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}
}

// cfg="{
// _id: 'set',
// members: [
// {_id: 1, host: 'localhost:27091'},
// {_id: 2, host: 'localhost:27092'},
// {_id: 3, host: 'localhost:27093'}
// ]
// }"
package org.apgrp10.gwent.server;

import org.apgrp10.gwent.model.FriendshipRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FriendshipManager {
	private static final ArrayList<FriendshipRequest> requests = new ArrayList<>();

	private FriendshipManager() {}

	public static synchronized List<FriendshipRequest> getIncomingRequests(long userId) {
		return requests.stream().filter(r -> r.to() == userId).collect(Collectors.toList());
	}

	public static synchronized List<FriendshipRequest> getOutgoingRequests(long userId) {
		return requests.stream().filter(r -> r.from() == userId).collect(Collectors.toList());
	}

	public static synchronized boolean haveRequest(long from, long to) {
		return requests.stream().anyMatch(r -> r.from() == from && r.to() == to);
	}

	public static synchronized void addRequest(long from, long to) throws Exception {
		if(UserManager.getInstance().haveFriendShip(from, to))
			throw new IllegalArgumentException("Friendship Exists between Users " + from + " and " + to);
		if(haveRequest(from, to) || haveRequest(to, from))
			throw new IllegalArgumentException("Friendship Request Exists between Users " + from + " and " + to);
		requests.add(FriendshipRequest.of(from, to));
	}

	public static synchronized void acceptRequest(long from, long to) throws Exception {
		for (FriendshipRequest request : new ArrayList<>(requests)) {
			if (request.from() == from && request.to() == to) {
				requests.remove(request);
				requests.add(request.accept());
				UserManager.getInstance().addFriendShip(from, to);
				return;
			}
		}
	}

	public static synchronized void rejectRequest(long from, long to) {
		for (FriendshipRequest request : requests) {
			if (request.from() == from && request.to() == to) {
				requests.remove(request);
				return;
			}
		}
	}
}

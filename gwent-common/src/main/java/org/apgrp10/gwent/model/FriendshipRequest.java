package org.apgrp10.gwent.model;

public record FriendshipRequest(long from, long to, long time, RequestState state) {
	public enum RequestState {PENDING, ACCEPTED, REJECTED}

	public static FriendshipRequest of(long from, long to) {
		return new FriendshipRequest(from, to, System.currentTimeMillis(), RequestState.PENDING);
	}

	public FriendshipRequest accept() {
		return new FriendshipRequest(from, to, System.currentTimeMillis(), RequestState.ACCEPTED);
	}

	public FriendshipRequest reject() {
		return new FriendshipRequest(from, to, System.currentTimeMillis(), RequestState.REJECTED);
	}
}

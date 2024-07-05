package org.apgrp10.gwent.utils;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;

import java.awt.*;
import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ANSI {
	public static final ANSI RST = new ANSI("\u001B[0m");
	public static final ANSI BOLD = new ANSI("\u001B[1m");
	public static final ANSI DIM = new ANSI("\u001B[2m");
	public static final ANSI NORMAL = new ANSI("\u001B[22m");
	public static final ANSI UNDERLINE = new ANSI("\u001B[4m");
	public static final ANSI NO_UNDERLINE = new ANSI("\u001B[24m");
	public static final ANSI STRIKE = new ANSI("\u001B[9m");
	public static final ANSI NO_STRIKE = new ANSI("\u001B[29m");
	public static final ANSI BLINK = new ANSI("\u001B[5m");
	public static final ANSI NO_BLINK = new ANSI("\u001B[25m");
	public static final ANSI INVERT = new ANSI("\u001B[7m");
	public static final ANSI DEF_FG = new ANSI("\u001B[39m");
	public static final ANSI WHITE = new ANSI("\u001B[37m");
	public static final ANSI BLACK = new ANSI("\u001B[30m");
	public static final ANSI RED = new ANSI("\u001B[31m");
	public static final ANSI GREEN = new ANSI("\u001B[32m");
	public static final ANSI BLUE = new ANSI("\u001B[34m");
	public static final ANSI YELLOW = new ANSI("\u001B[33m");
	public static final ANSI CYAN = new ANSI("\u001B[36m");
	public static final ANSI MAGENTA = new ANSI("\u001B[35m");
	public static final ANSI LRED = new ANSI("\u001B[91m");
	public static final ANSI LGREEN = new ANSI("\u001B[92m");
	public static final ANSI LBLUE = new ANSI("\u001B[94m");
	public static final ANSI LYELLOW = new ANSI("\u001B[93m");
	public static final ANSI LCYAN = new ANSI("\u001B[96m");
	public static final ANSI LMAGENTA = new ANSI("\u001B[95m");

	private final String ansi;

	private ANSI(String ansi) {
		this.ansi = ansi;
	}

	public static ANSI fg(Color color) {
		return new ANSI("\u001B[38;2;" + color.getRed() + ";" + color.getGreen() + ";" + color.getBlue() + "m");
	}

	public static ANSI bg(Color color) {
		return new ANSI("\u001B[48;2;" + color.getRed() + ";" + color.getGreen() + ";" + color.getBlue() + "m");
	}

	public synchronized static void log(long time, PrintStream stream, String logMsg, StackTraceElement... stackTraceElements) {
		for (StackTraceElement element : stackTraceElements)
			log(time, stream,
					"In Thread \"" + Thread.currentThread().getName() + "\" at " +
					element + ":");
		String timeStr = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
		stream.println(timeStr + " -> " + logMsg + ANSI.RST);
	}

	public synchronized static void log(long time, PrintStream stream, String logMsg, ANSI color, StackTraceElement... stackTraceElements) {
		if (!stream.equals(System.out) && !stream.equals(System.err)) {
			log(time, stream, logMsg, stackTraceElements);
			return;
		}
		for (StackTraceElement element : stackTraceElements)
			log(time, stream,
					"In Thread \"" + Thread.currentThread().getName() + "\" at " +
					element + ":",
					ANSI.fg(Color.GRAY));
		String timeStr = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
		stream.println(ANSI.fg(Color.gray) + timeStr + " -> " + ANSI.RST + color + logMsg + ANSI.RST);
	}

	public synchronized static void logError(PrintStream stream, String errorMessage, Throwable... optional) {
		long time = System.currentTimeMillis();
		boolean emptyMsg = errorMessage == null || errorMessage.isEmpty();
		if (!emptyMsg) log(time, stream, errorMessage, ANSI.LRED.bd(), Thread.currentThread().getStackTrace()[2]);
		if (optional.length == 1) {
			if (emptyMsg) log(time, stream,
					optional[0].getClass().getName() + " : " + optional[0].getMessage(),
					ANSI.RED.bd(),
					Thread.currentThread().getStackTrace()[2]);
			else
				log(time, stream,
						optional[0].getClass().getName() + " : " + optional[0].getMessage(),
						ANSI.RED.bd());
			for (StackTraceElement element : optional[0].getStackTrace())
				log(time, stream, element.toString(), ANSI.RED);
		}
	}

	public synchronized static Response createErrorResponse(Request req, String errorMessage, Throwable e) {
		return req.response(Response.INTERNAL_SERVER_ERROR,
				MGson.makeJsonObject("message", errorMessage, "error", e.getMessage(),
						"stackTrace", Arrays.stream(e.getStackTrace()).map(Object::toString).collect(Collectors.toList())));
	}

	public synchronized static void printErrorResponse(String errorMessage, Response res) {
		log(errorMessage, LRED.bd(), false);
		if (res.getStatus() != Response.INTERNAL_SERVER_ERROR)
			return;
		JsonObject errObj = res.getBody();
		String message = errObj.get("message").getAsString();
		String error = errObj.get("error").getAsString();
		List<String> stackTrace = MGson.get(false, false)
				.fromJson(errObj.get("stackTrace"), TypeToken.getParameterized(List.class, String.class).getType());
		log(message, ANSI.RED, false);
		log(error, ANSI.RED, false);
		stackTrace.forEach(e -> log(System.currentTimeMillis(), System.err, e, ANSI.RED));
	}

	public synchronized static void log(String message) {
		log(System.currentTimeMillis(), System.err, message, ANSI.DEF_FG);
	}

	public synchronized static void log(String message, ANSI color, boolean printLineNumber) {
		if (printLineNumber)
			log(System.currentTimeMillis(), System.err, message, color, Thread.currentThread().getStackTrace()[2]);
		else
			log(System.currentTimeMillis(), System.err, message, color);
	}

	public ANSI bd() {
		return append(BOLD);
	}

	public ANSI setBg(Color color) {
		return append(bg(color));
	}

	public ANSI append(ANSI toBeAppended) {
		return new ANSI(this.ansi + toBeAppended.ansi);
	}

	@Override
	public String toString() {
		return this.ansi;
	}
}

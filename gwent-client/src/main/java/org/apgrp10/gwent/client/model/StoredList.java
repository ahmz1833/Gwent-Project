package org.apgrp10.gwent.client.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import org.apgrp10.gwent.client.Gwent;
import org.apgrp10.gwent.utils.ANSI;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.*;

// A util only for offline client-local use (phase 2)
public class StoredList<E> extends ArrayList<E> implements List<E>, InvocationHandler
{
	private static final Set<StoredList<?>> allStoredLists = new HashSet<>();
	private final File file;

	private final Class<E> type;
	private final TimerTask task;
	private long lastRefresh = 0;
	
	private StoredList(List<E> list, File file, Class<E> type)
	{
		super(list);
		this.file = file;
		this.type = type;
		allStoredLists.add(this);
		task = new TimerTask()
		{
			@Override
			public void run()
			{
				refreshFromFile();
				save();
			}
		};
		new Timer("StoredListTimer-" + file.getName(), true).schedule(task, 0, 10000);
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized <E> List<E> of(String filepath, Class<E> type)
	{
		File file = new File(Gwent.APP_DATA, filepath);
		ArrayList<E> list = parseFile(file, type);
		StoredList<E> storedList = new StoredList<>(list, file, type);
		return (List<E>) Proxy.newProxyInstance(
				StoredList.class.getClassLoader(),
				new Class<?>[]{List.class},
				storedList
		);
	}
	
	public static synchronized void saveToFile(ArrayList<?> list, File file)
	{
		try {
			if (file.createNewFile())
				System.out.println(ANSI.LYELLOW.bd() + file.getName() + " file created." + ANSI.RST);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(list);
		try {
			if (Files.readString(file.toPath()).equals(json)) return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (PrintWriter pw = new PrintWriter(file)) {
			pw.write(json);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized  <E> ArrayList<E> parseFile(File file, Class<E> eClass)
	{
		ArrayList<E> arr = new ArrayList<>();
		Gson gson = new Gson();
		try {
			JsonArray a = gson.fromJson(new FileReader(file), JsonArray.class);
			if (a == null) return arr;
			a.forEach(e -> {
				try {
					JsonReader reader = new JsonReader(new StringReader(e.toString()));
					reader.setLenient(true);
					E obj = gson.fromJson(reader, eClass);
					arr.add(obj);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});
		} catch (FileNotFoundException e) {
			saveToFile(arr, file);
		}
		return arr;
	}
	
	public static synchronized void saveAll()
	{
		allStoredLists.forEach(StoredList::save);
	}
	
	public static synchronized void refreshAll()
	{
		allStoredLists.forEach(StoredList::refreshFromFile);
	}
	
	@Override
	public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		Object result;
		String methodName = method.getName();
		if (methodName.matches("get|indexOf|lastIndexOf|contains|containsAll|isEmpty|iterator|listIterator|subList|toArray|spliterator|stream|parallelStream"))
			refreshFromFile();
		result = method.invoke(this, args);
		save();
		return result;
	}
	
	private synchronized void save()
	{
		saveToFile(this, file);
		lastRefresh = System.currentTimeMillis();
	}
	
	private synchronized void refreshFromFile()
	{
		try {
			if (Files.getLastModifiedTime(file.toPath()).toMillis() <= lastRefresh) return;
			clear();
			addAll(parseFile(file, type));
			lastRefresh = System.currentTimeMillis();
		} catch (IOException ignored) {}
	}
	
	private synchronized void delete()
	{
		allStoredLists.remove(this);
		task.cancel();
		if (file.delete())
			System.out.println(ANSI.LRED.bd() + file.getName() + " file deleted." + ANSI.RST);
	}
}

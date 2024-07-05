package org.apgrp10.gwent.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DatabaseTable {
	protected final Statement stmt;
	private final String tableName;
	private final Supplier<Long> idGenerator;

	protected DatabaseTable(String dbPath, String tableName, Supplier<Long> idGenerator, DBColumn... columns) throws Exception {
		this.tableName = tableName;
		this.idGenerator = idGenerator;
		// if the database does not exist, it will be created
		if (new File(dbPath).getParentFile() != null)
			new File(dbPath).getParentFile().mkdirs();
		if (!new File(dbPath).exists())
			new File(dbPath).createNewFile();
		String url = "jdbc:sqlite:" + dbPath;
		Connection conn = DriverManager.getConnection(url);
		stmt = conn.createStatement();
		assert stmt.execute("CREATE TABLE IF NOT EXISTS " + tableName + " (id BIGINT PRIMARY KEY, " + getColumnsSyntax(columns) + ")");
	}

	@SuppressWarnings("unchecked")
	protected static <T> T getValue(ResultSet table, DBColumn col) throws Exception {
		return (T) table.getObject(col.name(), col.typeclass());
	}

	protected <T> T getValue(long id, DBColumn col) throws Exception {
		return getValue(getRow("WHERE id = " + id), col);
	}

	@SafeVarargs
	protected final long insert(Map.Entry<DBColumn, Object>... data) throws Exception {
		String[] keys = Arrays.stream(data).map(Map.Entry::getKey).map(DBColumn::name).toArray(String[]::new);
		String[] values = Arrays.stream(data).map(entry -> entry.getKey().valueToString(entry.getValue())).toArray(String[]::new);

		// if idGenerator is null, then the id is auto-incremented
		if (idGenerator == null)
			assert stmt.execute("INSERT INTO " + tableName + " (" + Arrays.stream(keys).collect(Collectors.joining(", ")) +
			                    ") VALUES (" + Arrays.stream(values).collect(Collectors.joining(", ")) + ")");
		else
			assert stmt.execute("INSERT INTO " + tableName + " (id, " + Arrays.stream(keys).collect(Collectors.joining(", ")) +
			                    ") VALUES (" + genId() + ", " + Arrays.stream(values).collect(Collectors.joining(", ")) + ")");

		return getId("ORDER BY ROWID DESC LIMIT 1");
	}


	public List<Long> getAllIds() {
		try {
			ResultSet table = getRow("");
			List<Long> ids = new ArrayList<>();
			while (table.next())
				ids.add(table.getLong("id"));
			return ids;
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	public boolean isIdTaken(long id) {
		try {
			return stmt.executeQuery("SELECT * FROM " + tableName + " WHERE id = " + id).next();
		} catch (Exception e) {
			return false;
		}
	}

	private long genId() {
		long id;
		do id = idGenerator.get(); while (isIdTaken(id));
		return id;
	}

	protected ResultSet getRow(String condition) throws Exception {
		return stmt.executeQuery("SELECT * FROM " + tableName + " " + condition);
	}

	public long getId(String condition) {
		try {
			ResultSet table = getRow(condition);
			return table.next() ? table.getLong("id") : -1;
		} catch (Exception e) {
			return -1;
		}
	}

	protected void updateInfo(String condition, DBColumn column, Object newData) throws Exception {
		String command = "UPDATE " + tableName + " SET " + column.name() + " = " + column.valueToString(newData) + " WHERE " + condition;
		assert stmt.execute(command);
	}

	protected void updateInfo(long id, DBColumn column, Object newData) throws Exception {
		updateInfo("id = " + id, column, newData);
	}

	@SafeVarargs
	protected final void update(long id, Map.Entry<DBColumn, Object>... data) throws Exception {
		for (Map.Entry<DBColumn, Object> entry : data)
			updateInfo(id, entry.getKey(), entry.getValue());
	}

	private String getColumnsSyntax(DBColumn[] columns) {
		StringBuilder syntax = new StringBuilder();
		for (DBColumn column : columns)
			syntax.append(column.name()).append(" ").append(column.type()).append(",");
		return syntax.substring(0, syntax.length() - 1);
	}

	public interface DBColumn {
		String name();

		String type();

		default Class<?> typeclass() {
			return switch (type()) {
				case "TEXT" -> String.class;
				case "INTEGER" -> Integer.class;
				case "BIGINT" -> Long.class;
				case "BIT" -> Boolean.class;
				default -> throw new IllegalStateException("Unexpected value: " + type());
			};
		}

		default String valueToString(Object value) {
			return switch (type()) {
				case "TEXT" -> "'" + value + "'";
				case "INTEGER", "BIGINT" -> value.toString();
				case "BIT" -> (boolean) value ? "1" : "0";
				default -> throw new IllegalStateException("Unexpected value: " + type());
			};
		}
	}
}


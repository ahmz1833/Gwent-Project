package org.apgrp10.gwent.server.DataBase;

import java.sql.*;

public class DataBaseController {
	//TODO change path to correct db file path
	final String path = "GwentDatabase.db";
	final Statement stmt;

	void executeCommand(String command) {
		try {
			stmt.execute(command);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	DataBaseController(String createSyntax){
		try {
			String url = "jdbc:sqlite:" + path;
			Connection conn = DriverManager.getConnection(url);
			stmt = conn.createStatement();
			stmt.execute(createSyntax);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	ResultSet getRow(String tableName, String condition) {
		try {
			return stmt.executeQuery("SELECT * FROM " + tableName + " WHERE " + condition);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	String getValue(ResultSet table, String value) {
		try {
			return table.getString(value);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	void updateInfo(String tableName, String condition, String newData, String columnName) {
		String command = "UPDATE " + tableName + " SET " + columnName + " = '" + newData + "' WHERE " + condition;
		executeCommand(command);
	}
}


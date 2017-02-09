package br.com.triadworks.tx.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import br.com.triadworks.tx.spi.TransactionCallback;

@FunctionalInterface
public interface JdbcTransactionCallback<R> extends TransactionCallback<Connection, R> {

	@Override
	default R execute(Connection conn) {
		try {
			return transact(conn);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public R transact(Connection connection) throws SQLException;
	
}

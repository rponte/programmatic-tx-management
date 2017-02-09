package br.com.triadworks.tx.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import br.com.triadworks.tx.spi.TransactionVoidCallback;

@FunctionalInterface
public interface JdbcTransactionVoidCallback extends TransactionVoidCallback<Connection> {

	@Override
	default void execute(Connection conn) {
		try {
			transact(conn);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void transact(Connection connection) throws SQLException;
	
}

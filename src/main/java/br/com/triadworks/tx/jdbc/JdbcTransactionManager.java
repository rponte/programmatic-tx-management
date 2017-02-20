package br.com.triadworks.tx.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import br.com.triadworks.tx.support.DataAccessException;
import br.com.triadworks.tx.support.TransactionCallback;
import br.com.triadworks.tx.support.TransactionManager;
import br.com.triadworks.tx.support.TransactionVoidCallback;

public class JdbcTransactionManager implements TransactionManager<Connection> {
	
	private final DataSource dataSource;
	
	public JdbcTransactionManager(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public <R> R doInTransactionWithReturn(TransactionCallback<Connection, R> callback) throws DataAccessException {
		
		Connection connection = null; 
		try {
			
			connection = dataSource.getConnection();

			connection.setAutoCommit(false); // inicia transação
			R result = callback.execute(connection);
			connection.commit(); // comita transação
			
			return result;
			
		} catch (Exception e) {
			if (connection != null) {
				try {
					connection.rollback(); // rollback da transação
				} catch (SQLException e1) {
					e.printStackTrace();
				}
			}
			throw new DataAccessException(e);
		} finally {
			if (connection != null) {
				try { connection.close(); } catch (SQLException e) {}
			}
		}
	}

	@Override
	public void doInTransaction(TransactionVoidCallback<Connection> callback) throws DataAccessException {
		doInTransactionWithReturn(new TransactionCallback<Connection, Void>() {
			@Override
			public Void execute(Connection conn) {
				callback.execute(conn);
				return null;
			}
		});		
	}

}

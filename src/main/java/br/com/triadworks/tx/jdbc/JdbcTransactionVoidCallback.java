package br.com.triadworks.tx.jdbc;

import java.sql.Connection;

import br.com.triadworks.tx.spi.TransactionVoidCallback;

@FunctionalInterface
public interface JdbcTransactionVoidCallback extends TransactionVoidCallback<Connection> {

}

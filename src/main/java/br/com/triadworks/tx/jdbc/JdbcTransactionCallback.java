package br.com.triadworks.tx.jdbc;

import java.sql.Connection;

import br.com.triadworks.tx.spi.TransactionCallback;

@FunctionalInterface
public interface JdbcTransactionCallback<R> extends TransactionCallback<Connection, R> {

}

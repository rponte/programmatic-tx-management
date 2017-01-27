package br.com.triadworks.tx.spi;

public interface TransactionManagerI {

	/**
	 * Executa uma Unidade de Trabalho dentro de um contexto transacional e
	 * retorna o resultado da operação.
	 */
	<T> T doInTransactionWithReturn(TransactionCallback<T> callback) throws DataAccessException;

	/**
	 * Executa uma Unidade de Trabalho dentro de um contexto transacional.
	 */
	void doInTransaction(TransactionVoidCallback callback) throws DataAccessException;

}
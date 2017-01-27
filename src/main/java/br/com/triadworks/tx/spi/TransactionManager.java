package br.com.triadworks.tx.spi;

public interface TransactionManager {

	/**
	 * Executa uma Unidade de Trabalho dentro de um contexto transacional e
	 * retorna o resultado da operação.
	 */
	public <T> T doInTransactionWithReturn(TransactionCallback<T> callback) throws DataAccessException;

	/**
	 * Executa uma Unidade de Trabalho dentro de um contexto transacional.
	 */
	public void doInTransaction(TransactionVoidCallback callback) throws DataAccessException;

}
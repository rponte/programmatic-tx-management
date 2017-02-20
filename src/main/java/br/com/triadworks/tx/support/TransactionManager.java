package br.com.triadworks.tx.support;

public interface TransactionManager<T> {

	/**
	 * Executa uma Unidade de Trabalho dentro de um contexto transacional e
	 * retorna o resultado da operação.
	 */
	public <R> R doInTransactionWithReturn(TransactionCallback<T, R> callback) throws DataAccessException;

	/**
	 * Executa uma Unidade de Trabalho dentro de um contexto transacional.
	 */
	public void doInTransaction(TransactionVoidCallback<T> callback) throws DataAccessException;

}
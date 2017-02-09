package br.com.triadworks.tx.jdbc;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

import br.com.triadworks.sample.model.Produto;
import br.com.triadworks.tx.jpa.JpaTransactionManagerTest;
import br.com.triadworks.tx.spi.DataAccessException;
import br.com.triadworks.tx.spi.TransactionManager;

public class JdbcTransactionManagerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(JpaTransactionManagerTest.class);

	private EntityManagerFactory entityManagerFactory;
	private TransactionManager<Connection> txManager;
	
	@Before
	public void setUp() {
		// cria schema
		entityManagerFactory = Persistence.createEntityManagerFactory("sample-pu");
		
		DataSource dataSource = newDataSource();
		txManager = new JdbcTransactionManager(dataSource);
	}
	
	private DataSource newDataSource() {
		HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl("jdbc:hsqldb:mem:sample.db");
		ds.setUsername("sa");
		ds.setPassword("");
		return ds;
	}

	@After
	public void cleanUp() {
		// dropa schema
		entityManagerFactory.close();
	}
	
	@Test
	public void deveInserirNovoProdutoNoBancoDeDados() {
		
		Produto ipad = new Produto("iPad Retina Display");
		
		txManager.doInTransaction(new JdbcTransactionVoidCallback() {
			@Override
			public void transact(Connection connection) throws SQLException {
				String sql = "insert into Produto(id, nome)"
						   + " values((call next value for hibernate_sequence), ?)";
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				stmt.execute();
			}
		});
		
		Produto produto = txManager.doInTransactionWithReturn(new JdbcTransactionCallback<Produto>() {
			@Override
			public Produto transact(Connection connection) throws SQLException {
				
				String sql = "select * from Produto";
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					Produto p = new Produto();
					p.setId(rs.getInt("id"));
					p.setNome(rs.getString("nome"));
					
					return p;
				}
				
				return null;
			}
		});
		
		assertEquals("produto id", ipad.getId(), produto.getId());
		assertEquals("produto nome", ipad.getNome(), produto.getNome());
	}
	
	@Test
	public void deveInserirNovoProdutoNoBancoDeDados_comJava8() {
		
		Produto ipad = new Produto("iPad Retina Display");
		
		txManager.doInTransaction((connection) -> { });
		
		Produto produto = txManager
				.doInTransactionWithReturn((connection) -> {
					return null;
				});
		
		assertEquals("produto id", ipad.getId(), produto.getId());
		assertEquals("produto nome", ipad.getNome(), produto.getNome());
	}
	
	@Test
	public void naoDeveInserirProdutosEmCasoDeErroDentroDaTransacao() {
		
		try {
			txManager.doInTransaction(new JdbcTransactionVoidCallback() {
				@Override
				public void transact(Connection connection) throws SQLException {
					
					throw new IllegalStateException("Erro qualquer");
				}
			});
		} catch(DataAccessException e) {
			LOGGER.error("Erro ao tentar gravar varios produtos na mesma transação: ", e);
		}
		
		Long total = txManager.doInTransactionWithReturn(new JdbcTransactionCallback<Long>() {
			@Override
			public Long transact(Connection connection) {
				Long count = 0L;
				return count;
			}
		});
		
		assertEquals("total de produtos gravados", 0, total.longValue());
	}
	
	@Test
	public void naoDeveInserirProdutosEmCasoDeErroDentroDaTransacao_comJava8() {
		
		try {
			txManager.doInTransaction((connection) ->  {
				
				throw new IllegalStateException("Erro qualquer");
			});
		} catch(DataAccessException e) {
			LOGGER.error("Erro ao tentar gravar varios produtos na mesma transação: ", e);
		}
		
		Long total = txManager.doInTransactionWithReturn((em) -> {
				Long count = 0L;
				return count;
		});
		
		assertEquals("total de produtos gravados", 0, total.longValue());
	}

}

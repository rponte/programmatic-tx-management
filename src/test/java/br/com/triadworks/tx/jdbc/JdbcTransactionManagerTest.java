package br.com.triadworks.tx.jdbc;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.jpa.boot.spi.Bootstrap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

import br.com.triadworks.sample.model.Produto;
import br.com.triadworks.tx.jpa.JpaTransactionManagerTest;
import br.com.triadworks.tx.support.DataAccessException;
import br.com.triadworks.tx.support.TransactionManager;

public class JdbcTransactionManagerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(JpaTransactionManagerTest.class);

	private EntityManagerFactory entityManagerFactory;
	private TransactionManager<Connection> txManager;
	
	@Before
	public void setUp() {
		// cria datasource
		DataSource dataSource = newDataSource();
		txManager = new JdbcTransactionManager(dataSource);
		
		// cria schema
		URL xml = this.getClass().getResource("/META-INF/persistence.xml");
		String persistenceUnitName = "sample-pu";
		entityManagerFactory = Bootstrap
				.getEntityManagerFactoryBuilder(xml, persistenceUnitName, new HashMap<>())
				.withDataSource(dataSource)
				.build();
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
				// carrega sequence
				PreparedStatement stmt = connection.prepareStatement("call next value for hibernate_sequence");
				ResultSet rs = stmt.executeQuery(); rs.next();
				ipad.setId(rs.getInt(1));
				
				// insere registro
				stmt = connection.prepareStatement("insert into Produto(id, nome) values(?, ?)");
				stmt.setInt(1, ipad.getId());
				stmt.setString(2, ipad.getNome());
				
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
		
		txManager.doInTransaction((JdbcTransactionVoidCallback)(connection) -> { 
			// carrega sequence
			PreparedStatement stmt = connection.prepareStatement("call next value for hibernate_sequence");
			ResultSet rs = stmt.executeQuery(); rs.next();
			ipad.setId(rs.getInt(1));
			
			// insere registro
			stmt = connection.prepareStatement("insert into Produto(id, nome) values(?, ?)");
			stmt.setInt(1, ipad.getId());
			stmt.setString(2, ipad.getNome());
			
			stmt.execute();
		});
		
		Produto produto = txManager
				.doInTransactionWithReturn((JdbcTransactionCallback<Produto>)(connection) -> {
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
			txManager.doInTransaction((JdbcTransactionVoidCallback)(connection) ->  {
				
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

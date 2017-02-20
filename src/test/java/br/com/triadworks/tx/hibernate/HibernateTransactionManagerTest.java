package br.com.triadworks.tx.hibernate;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.triadworks.sample.model.Produto;
import br.com.triadworks.tx.support.DataAccessException;
import br.com.triadworks.tx.support.TransactionManager;

public class HibernateTransactionManagerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateTransactionManagerTest.class);

	private TransactionManager<Session> txManager;
	private SessionFactory sessionFactory;
	
	@Before
	public void setUp() {
		sessionFactory = newSessionFactory();
		txManager = new HibernateTransactionManager(sessionFactory);
	}
	
	private SessionFactory newSessionFactory() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("sample-pu");
		return emf.unwrap(SessionFactory.class);
	}

	@After
	public void cleanUp() {
		sessionFactory.close();
	}
	
	
	@Test
	public void deveInserirNovoProdutoNoBancoDeDados() {
		
		Produto ipad = new Produto("iPad Retina Display");
		
		txManager.doInTransaction(new HibernateTransactionVoidCallback() {
			@Override
			public void execute(Session session) {
				session.save(ipad);
			}
		});
		
		Produto produto = txManager.doInTransactionWithReturn(new HibernateTransactionCallback<Produto>() {
			@Override
			public Produto execute(Session session) {
				return session.get(Produto.class, ipad.getId());
			}
		});
		
		assertEquals("produto id", ipad.getId(), produto.getId());
		assertEquals("produto nome", ipad.getNome(), produto.getNome());
	}
	
	@Test
	public void deveInserirNovoProdutoNoBancoDeDados_comJava8() {
		
		Produto ipad = new Produto("iPad Retina Display");
		
		txManager.doInTransaction((em) -> em.persist(ipad));
		
		Produto produto = txManager
				.doInTransactionWithReturn((em) -> em.get(Produto.class, ipad.getId()));
		
		assertEquals("produto id", ipad.getId(), produto.getId());
		assertEquals("produto nome", ipad.getNome(), produto.getNome());
	}
	
	@Test
	public void naoDeveInserirProdutosEmCasoDeErroDentroDaTransacao() {
		
		try {
			txManager.doInTransaction(new HibernateTransactionVoidCallback() {
				@Override
				public void execute(Session session) {
					session.save(new Produto("produto #1"));
					session.save(new Produto("produto #2"));
					session.save(new Produto("produto #3"));
					session.save(new Produto("produto #4"));
					
					throw new IllegalStateException("Erro qualquer");
				}
			});
		} catch(DataAccessException e) {
			LOGGER.error("Erro ao tentar gravar varios produtos na mesma transação: ", e);
		}
		
		Long total = txManager.doInTransactionWithReturn(new HibernateTransactionCallback<Long>() {
			@Override
			public Long execute(Session session) {
				Long count = session
					.createQuery("select count(p) from Produto p", Long.class)
					.getSingleResult();
				return count;
			}
		});
		
		assertEquals("total de produtos gravados", 0, total.longValue());
	}
	
	@Test
	public void naoDeveInserirProdutosEmCasoDeErroDentroDaTransacao_comJava8() {
		
		try {
			txManager.doInTransaction((em) ->  {
				em.persist(new Produto("produto #1"));
				em.persist(new Produto("produto #2"));
				em.persist(new Produto("produto #3"));
				em.persist(new Produto("produto #4"));
				
				throw new IllegalStateException("Erro qualquer");
			});
		} catch(DataAccessException e) {
			LOGGER.error("Erro ao tentar gravar varios produtos na mesma transação: ", e);
		}
		
		Long total = txManager.doInTransactionWithReturn((em) -> {
				Long count = em.createQuery("select count(p) from Produto p", Long.class)
					.getSingleResult();
				return count;
		});
		
		assertEquals("total de produtos gravados", 0, total.longValue());
	}

}

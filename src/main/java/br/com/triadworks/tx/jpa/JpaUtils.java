package br.com.triadworks.tx.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JpaUtils {

	private final static EntityManagerFactory factory 
						= Persistence.createEntityManagerFactory("sample-pu");
	
	public EntityManager getEntityManager() {
		return factory.createEntityManager();
	}
	
	public void close(EntityManager entityManager) {
		if (entityManager != null) {
			entityManager.close();
		}
	}
}

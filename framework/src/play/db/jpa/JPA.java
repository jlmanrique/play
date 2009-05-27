package play.db.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * JPA Support
 */
public class JPA {

    public static EntityManagerFactory entityManagerFactory = null;
    public static ThreadLocal<JPA> local = new ThreadLocal<JPA>();
    public EntityManager entityManager;
    boolean readonly = true;

    static JPA get() {
        if (local.get() == null) {
            throw new IllegalStateException("The JPA context is not initialized.");
        }
        return local.get();
    }

    static void clearContext() {
        local.remove();
    }

    static void createContext(EntityManager entityManager, boolean readonly) {
        if (local.get() != null) {
            local.remove();
        }
        JPA context = new JPA();
        context.entityManager = entityManager;
        context.readonly = readonly;
        local.set(context);
    }
    
    // ~~~~~~~~~~~
    
    /*
     * Retrieve the current entityManager
     */ 
    public static EntityManager getEntityManager() {
        return get().entityManager;
    }
    
    /*
     * Tell to JPA do not commit the current transaction
     */ 
    public static void abort() {
        getEntityManager().getTransaction().setRollbackOnly();
    }

    /**
     * @return true if an entityManagerFactory has started
     */
    public static boolean isEnabled() {
        return entityManagerFactory != null;
    }
    
    public static int execute(String query) {
        return getEntityManager().createQuery(query).executeUpdate();
    }

    /*
     * Build a new entityManager.
     * (In most case you want to use the local entityManager with getEntityManager)
     */ 
    public static EntityManager newEntityManager() {
        return entityManagerFactory.createEntityManager();
    }
}

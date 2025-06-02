package com.keelean.accountmanager.repo;

import com.keelean.accountmanager.constants.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class EntitySessionManager {

    @Autowired
    @Qualifier(AppConstants.HIBERNATE_SESSION_FACTORY)
    private SessionFactory sessionFactory;

    public <T> T saveOrUpdateCommit(T entity){
        Session session = null;
        Transaction transaction = null;

        try {
            session = this.sessionFactory.openSession();
            transaction = session.beginTransaction();
            session.saveOrUpdate(entity);
            session.flush();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        finally {
            if(transaction != null){
                transaction.commit();
                session.close();
            }
        }
        return entity;
    }
}

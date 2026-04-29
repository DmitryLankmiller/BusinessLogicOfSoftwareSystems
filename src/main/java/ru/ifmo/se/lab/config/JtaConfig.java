package ru.ifmo.se.lab.config;

import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

@Configuration
@EnableTransactionManagement
public class JtaConfig {

    @Bean
    public JndiObjectFactoryBean userTransaction() {
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setJndiName("java:comp/UserTransaction");
        bean.setProxyInterface(UserTransaction.class);
        return bean;
    }

    @Bean
    public JndiObjectFactoryBean transactionManagerLookup() {
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setJndiName("java:jboss/TransactionManager");
        bean.setProxyInterface(TransactionManager.class);
        return bean;
    }

    @Bean
    public PlatformTransactionManager transactionManager(
            UserTransaction userTransaction,
            TransactionManager transactionManagerLookup) {
        return new JtaTransactionManager(userTransaction, transactionManagerLookup);
    }
}
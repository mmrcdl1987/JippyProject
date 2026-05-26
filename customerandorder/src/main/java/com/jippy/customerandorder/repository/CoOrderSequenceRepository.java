package com.jippy.customerandorder.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
@Slf4j
public class CoOrderSequenceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // SELECT FOR UPDATE
    public Object[] getSequenceForUpdate() {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT last_date, current_seq " +
                            "FROM jippy_customer_and_order.order_sequence_manager " +
                            "WHERE sequence_name = 'jippy_seq' FOR UPDATE"
            );
            return (Object[]) query.getSingleResult();

        } catch (NoResultException ex) {
            return null;
        }
    }

    // INSERT INITIAL ROW
    public void insertInitial(LocalDate today) {

        entityManager.createNativeQuery(
                        "INSERT INTO jippy_customer_and_order.order_sequence_manager " +
                                "(sequence_name, last_date, current_seq) " +
                                "VALUES ('jippy_seq', :today, 0)"
                )
                .setParameter("today", today)
                .executeUpdate();

        log.info("Inserted initial sequence row");
    }

    // UPDATE SEQUENCE
    public void updateSequence(LocalDate today, Long seq) {

        entityManager.createNativeQuery(
                        "UPDATE jippy_customer_and_order.order_sequence_manager " +
                                "SET last_date = :today, current_seq = :seq " +
                                "WHERE sequence_name = 'jippy_seq'"
                )
                .setParameter("today", today)
                .setParameter("seq", seq)
                .executeUpdate();

        log.info("Sequence updated | seq={}", seq);
    }
}
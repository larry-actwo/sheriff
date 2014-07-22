package net.bitcoinguard.sheriff.core.services;

import net.bitcoinguard.sheriff.core.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Jiri on 9. 7. 2014.
 */
@Repository
public interface TransactionsRepository extends JpaRepository<Transaction, Long>{
}

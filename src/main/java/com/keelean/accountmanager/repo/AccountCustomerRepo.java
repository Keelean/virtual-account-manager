package com.keelean.accountmanager.repo;

import com.keelean.accountmanager.entity.AccountCustomer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountCustomerRepo extends JpaRepository<AccountCustomer, Long> {

    List<AccountCustomer> findByReferenceIdOrAccountId(String referenceId, String accountId);

    List<AccountCustomer> findByReferenceIdInOrAccountIdIn(Collection<String> referenceIds, Collection<String> accountIds);

    @Query(value = "SELECT * FROM virtual_account_customer WHERE reference_id = :referenceId OR account_id = :accountId AND meta->>'mode' = :mode", nativeQuery = true)
    List<AccountCustomer> findByReferenceIdOrAccountIdAndMode(@Param("referenceId") String referenceId, @Param("accountId") String accountId, @Param("mode") String mode);

    Page<AccountCustomer> findByPartnerId(String partnerId, Pageable pageable);

    default AccountCustomer getVirtualAccountFromList(String accountOrReferenceId) {
        List<AccountCustomer> vcList = findByReferenceIdOrAccountId(accountOrReferenceId, accountOrReferenceId);
        AccountCustomer vc;

        if (vcList.isEmpty()) {
            vc = null;
        } else {
            //return expired dynamic account else return null
            Optional<AccountCustomer> optionalVirtualAccountCustomer = vcList.stream().filter(v -> validateDynamicAccount(v)).findFirst();
            vc = optionalVirtualAccountCustomer.orElseGet(() -> null);
        }
        return vc;

    }

    private boolean validateDynamicAccount(AccountCustomer virtualAccountCustomer) {
        // Pre-created accounts have no expiry until they are completed
        return virtualAccountCustomer.getExpiryDate() == null || LocalDateTime.now().compareTo(virtualAccountCustomer.getExpiryDate()) < 0;
    }

}

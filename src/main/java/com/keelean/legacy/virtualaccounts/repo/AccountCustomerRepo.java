package com.keelean.legacy.customeraccounts.repo;

import com.keelean.legacy.customeraccounts.entity.VirtualAccountCustomer;
import com.keelean.legacy.customeraccounts.entity.VirtualAccountPartnerConfig;
import com.keelean.legacy.customeraccounts.enums.VirtualAccountMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VirtualAccountCustomerRepo extends JpaRepository<VirtualAccountCustomer, Long> {
    List<VirtualAccountCustomer> findByReferenceIdOrAccountId(String referenceId, String accountId);
    @Query(value = "SELECT * FROM virtual_account_customer WHERE reference_id = :referenceId OR account_id = :accountId AND meta->>'mode' = :mode", nativeQuery = true)
    List<VirtualAccountCustomer> findByReferenceIdOrAccountIdAndMode(@Param("referenceId") String referenceId, @Param("accountId") String accountId, @Param("mode") String mode);
    Page<VirtualAccountCustomer> findByPartnerId(String partnerId, Pageable pageable);

    default VirtualAccountCustomer getVirtualAccountFromList(String accountOrReferenceId){

        List<VirtualAccountCustomer> vcList = findByReferenceIdOrAccountId(accountOrReferenceId, accountOrReferenceId);

        System.out.println("vcList:{}"+ vcList);

        VirtualAccountCustomer vc;

        if(vcList.isEmpty()){
            vc = null;
        }else {
            //return expired dynamic account else return null
            Optional<VirtualAccountCustomer> optionalVirtualAccountCustomer = vcList.stream().filter(v -> validateDynamicAccount(v)).findFirst();
            vc = optionalVirtualAccountCustomer.orElseGet(()-> null);
        }
        return vc;

    }

    private boolean validateDynamicAccount(VirtualAccountCustomer virtualAccountCustomer){
        return LocalDateTime.now().compareTo(virtualAccountCustomer.getExpiryDate()) < 0;
    }
}

package com.keelean.legacy.customeraccounts.aspect;

import com.keelean.legacy.customeraccounts.entity.VirtualAccountActivity;
import com.keelean.legacy.customeraccounts.repo.VirtualAccountActivityRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@AllArgsConstructor
@Slf4j
public class VirtualAccountLookupAspect {

    private final VirtualAccountActivityRepo virtualAccountActivityRepo;

    @AfterReturning("@annotation(activityLog)")
    void logLookup(JoinPoint jp, ActivityLog activityLog){

        if(jp.getArgs().length > 0){
            if(jp.getArgs()[0] instanceof String){
                String accountId = (String) jp.getArgs()[0];
                log.info("Log account Id::[{}]", accountId);
                VirtualAccountActivity accountActivity = VirtualAccountActivity.builder()
                        .accountId(accountId)
                        .build();
                if(virtualAccountActivityRepo.findByAccountId(accountId).isPresent()){
                    return;
                }
                virtualAccountActivityRepo.save(accountActivity);
            }
        }


    }
}

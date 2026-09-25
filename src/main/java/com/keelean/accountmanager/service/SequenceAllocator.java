package com.keelean.accountmanager.service;

import com.keelean.accountmanager.entity.AccountPool;
import com.keelean.accountmanager.entity.PartnerAccountConfig;
import com.keelean.accountmanager.exception.ErrorCodes;
import com.keelean.accountmanager.exception.RestServiceException;
import com.keelean.accountmanager.repo.AccountPoolRepo;
import com.keelean.accountmanager.repo.PartnerAccountConfigRepo;
import com.keelean.accountmanager.utils.AppUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Hands out account IDs from a partner's sequence. The sequence row (the partner config for a dedicated
 * pool, the account pool for a shared one) is locked for update, so concurrent reservations queue instead
 * of failing the optimistic lock, and a bulk request takes all its IDs in one update.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SequenceAllocator {

    private final PartnerAccountConfigRepo configRepo;
    private final AccountPoolRepo poolRepo;

    // REQUIRES_NEW commits the reservation straight away, so the row lock is held only for this call
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public List<String> reserveAccountIds(PartnerAccountConfig config, int count) {
        if (config.isSharedPool()) {
            AccountPool pool = poolRepo.findLockedByPrefixSeries(Integer.valueOf(config.getPrefix()))
                    .orElseThrow(() -> new IllegalArgumentException("Account prefix series does not exist!"));
            int first = pool.reserveSequences(count);
            log.info("Reserved {} sequences from {} in shared pool {}", count, first, pool.getPrefixSeries());
            return accountIds(String.valueOf(pool.getStartPrefix()), pool.getCapacity().getReusableDigits(), first, count);
        }

        PartnerAccountConfig lockedConfig = configRepo.findLockedById(config.getId())
                .orElseThrow(() -> new RestServiceException(ErrorCodes.PARTNER_CONFIG_DOES_NOT_EXIST.getCode(), config.getPartnerId()));
        int first = lockedConfig.reserveSequences(count);
        log.info("Reserved {} sequences from {} for partner {}", count, first, lockedConfig.getPartnerId());
        return accountIds(lockedConfig.getPrefix(), lockedConfig.getCapacity().getReusableDigits(), first, count);
    }

    private static List<String> accountIds(String prefix, int reusableDigits, int firstSequence, int count) {
        List<String> accountIds = new ArrayList<>(count);
        for (int sequence = firstSequence; sequence < firstSequence + count; sequence++) {
            accountIds.add(prefix + AppUtils.formatEndSequence(reusableDigits, sequence));
        }
        return accountIds;
    }
}

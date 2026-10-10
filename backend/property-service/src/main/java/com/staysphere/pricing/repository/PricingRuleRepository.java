package com.staysphere.pricing.repository;

import com.staysphere.pricing.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PricingRuleRepository
        extends JpaRepository<PricingRule, String> {

    Optional<PricingRule> findByPropertyIdAndStatus(
            String propertyId, PricingRule.RuleStatus status);

    boolean existsByPropertyId(String propertyId);
}
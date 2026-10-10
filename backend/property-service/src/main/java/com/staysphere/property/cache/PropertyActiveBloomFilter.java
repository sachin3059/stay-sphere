package com.staysphere.property.cache;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.staysphere.property.entity.Property;
import com.staysphere.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Probabilistic set of active property IDs — blocks cache penetration on junk UUIDs
 * without a DB round-trip when the ID is definitely not published.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PropertyActiveBloomFilter {

    private static final double FALSE_POSITIVE_RATE = 0.01;

    private final PropertyRepository propertyRepository;
    private volatile BloomFilter<CharSequence> filter = emptyFilter();

    @EventListener(ApplicationReadyEvent.class)
    public void warmOnStartup() {
        rebuild();
    }

    public boolean mightBeActive(String propertyId) {
        if (propertyId == null || propertyId.isBlank()) {
            return false;
        }
        return filter.mightContain(propertyId);
    }

    public void onPropertyActivated(String propertyId) {
        filter.put(propertyId);
    }

    /** Required when a listing is unlisted — standard Bloom filters do not support delete. */
    public void rebuild() {
        List<String> ids = propertyRepository.findAllActiveIds();
        int expected = Math.max(ids.size() * 2, 1_000);
        BloomFilter<CharSequence> next = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                expected,
                FALSE_POSITIVE_RATE);
        for (String id : ids) {
            next.put(id);
        }
        this.filter = next;
        log.info("Property Bloom filter rebuilt — {} active listings indexed", ids.size());
    }

    private static BloomFilter<CharSequence> emptyFilter() {
        return BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                1_000,
                FALSE_POSITIVE_RATE);
    }
}

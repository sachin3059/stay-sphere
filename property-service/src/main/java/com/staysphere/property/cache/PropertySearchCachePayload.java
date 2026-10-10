package com.staysphere.property.cache;

import com.staysphere.property.dto.PropertyPageResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertySearchCachePayload {
    private PropertyPageResponse page;
}

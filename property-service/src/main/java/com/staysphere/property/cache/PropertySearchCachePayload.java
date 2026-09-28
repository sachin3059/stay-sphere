package com.staysphere.property.cache;

import com.staysphere.property.dto.PropertyResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertySearchCachePayload {
    private List<PropertyResponse> items;
}

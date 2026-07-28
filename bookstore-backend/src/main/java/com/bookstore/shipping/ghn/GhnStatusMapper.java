package com.bookstore.shipping.ghn;

import com.bookstore.shipping.entity.ShipmentStatus;

import java.util.Map;

public final class GhnStatusMapper {

    private static final Map<String, ShipmentStatus> MAPPING = Map.ofEntries(
            Map.entry("ready_to_pick", ShipmentStatus.PACKING),
            Map.entry("picking", ShipmentStatus.PACKING),
            Map.entry("money_collect_picking", ShipmentStatus.PACKING),
            Map.entry("storing", ShipmentStatus.PACKING),
            Map.entry("picked", ShipmentStatus.SHIPPED),
            Map.entry("transporting", ShipmentStatus.IN_TRANSIT),
            Map.entry("sorting", ShipmentStatus.IN_TRANSIT),
            Map.entry("delivering", ShipmentStatus.IN_TRANSIT),
            Map.entry("money_collect_delivering", ShipmentStatus.IN_TRANSIT),
            Map.entry("delivered", ShipmentStatus.DELIVERED),
            Map.entry("waiting_to_return", ShipmentStatus.RETURNED),
            Map.entry("return", ShipmentStatus.RETURNED),
            Map.entry("return_transporting", ShipmentStatus.RETURNED),
            Map.entry("return_sorting", ShipmentStatus.RETURNED),
            Map.entry("returning", ShipmentStatus.RETURNED),
            Map.entry("returned", ShipmentStatus.RETURNED),
            Map.entry("delivery_fail", ShipmentStatus.FAILED),
            Map.entry("return_fail", ShipmentStatus.FAILED),
            Map.entry("exception", ShipmentStatus.FAILED),
            Map.entry("damage", ShipmentStatus.FAILED),
            Map.entry("lost", ShipmentStatus.FAILED),
            Map.entry("cancel", ShipmentStatus.FAILED)
    );

    private GhnStatusMapper() {
    }

    public static ShipmentStatus map(String ghnStatus) {
        return MAPPING.get(ghnStatus);
    }
}
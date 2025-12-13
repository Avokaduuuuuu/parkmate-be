package com.parkmate.parking_lot;

public interface PlatformLotProjection {
    Long getTotal();
    Long getActiveTotal();
    Long getPendingTotal();
    Long getUnderMaintenanceTotal();
    Long getPreparingTotal();
}

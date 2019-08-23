package com.devteam.fantasy.util;

import java.time.LocalDate;
import java.time.ZoneId;

public class TimeDate {

    private static TimeDate single_instance = null;

    private ZoneId z;

    private TimeDate() {
        this.z= ZoneId.of( "America/Tegucigalpa");
    }

    // static method to create instance of Singleton class
    public static TimeDate getInstance()
    {
        if (single_instance == null)
            single_instance = new TimeDate();

        return single_instance;
    }

    public ZoneId getZ() {
        return z;
    }
}

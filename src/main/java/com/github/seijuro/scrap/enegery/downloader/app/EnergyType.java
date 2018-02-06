package com.github.seijuro.scrap.enegery.downloader.app;

import lombok.Getter;
import lombok.NonNull;

import java.util.Objects;

public enum EnergyType {
    ELECTROCITY("전기", "elec"),
    GAS("가스", "gas");

    /**
     * Instance Properties
     */
    @NonNull
    @Getter
    private final String name;
    @NonNull @Getter
    private final String shortName;

    /**
     * C'tor
     *
     * @param $name
     * @param $shortName
     */
    EnergyType(String $name, String $shortName) {
        this.name = $name;
        this.shortName = $shortName;
    }

    public static EnergyType toEnergyTypeIfPossible(String text) {
        if (Objects.nonNull(text)) {
            if (text.contains(EnergyType.ELECTROCITY.getName())) {
                return EnergyType.ELECTROCITY;
            }

            if (text.contains(EnergyType.GAS.getName())) {
                return EnergyType.GAS;
            }
        }

        return null;
    }
}

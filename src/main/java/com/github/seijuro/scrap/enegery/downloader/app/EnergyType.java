package com.github.seijuro.scrap.enegery.downloader.app;

import lombok.Getter;
import lombok.NonNull;

import java.util.Objects;

public enum EnergyType {
    ELECTROCITY("전기"),
    GAS("가스");

    /**
     * Instance Properties
     */
    @NonNull
    @Getter
    private final String name;

    /**
     * C'tor
     *
     * @param $name
     */
    EnergyType(String $name) {
        this.name = $name;
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

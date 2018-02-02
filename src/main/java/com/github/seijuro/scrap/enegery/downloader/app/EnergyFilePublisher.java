package com.github.seijuro.scrap.enegery.downloader.app;

import com.github.seijuro.scrap.enegery.downloader.util.Observer;
import com.github.seijuro.scrap.enegery.downloader.util.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnergyFilePublisher implements Publisher<EnergyFileEvent> {
    private static Logger LOG = LoggerFactory.getLogger(EnergyFilePublisher.class);

    /**
     * Singleton Instance.
     */
    private static EnergyFilePublisher gInstance = null;

    /**
     * Singleton Method
     *
     * @return
     */
    synchronized public static EnergyFilePublisher getInstance() {
        if (gInstance == null) {
            gInstance = new EnergyFilePublisher();
        }

        return gInstance;
    }

    /**
     * Instance Properties
     */
    private final List<Observer<EnergyFileEvent>> observers;

    /**
     * C'tor
     */
    protected EnergyFilePublisher() {
        this.observers = new ArrayList<>();
    }


    @Override
    public void add(Observer observer) {
        if (Objects.nonNull(observer)) {
            this.observers.add(observer);
        }
    }

    @Override
    public void delete(Observer $observer) {
        if (Objects.nonNull($observer)) {
            this.observers.remove($observer);
        }
    }

    @Override
    public void notifyObservers() {
        for (Observer<EnergyFileEvent> observer : this.observers) {
            observer.update();
        }
    }

    @Override
    public void notifyObservers(EnergyFileEvent notification) {
        for (Observer<EnergyFileEvent> observer : this.observers) {
            observer.update(notification);
        }
    }
}

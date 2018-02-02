package com.github.seijuro.scrap.enegery.downloader.util;

/**
 * Publisher interface
 *
 * @param <T>
 */
public interface Publisher<T extends Notification> {
    public void add(Observer observer);
    public void delete(Observer $observer);
    public void notifyObservers();
    public void notifyObservers(T notification);
}

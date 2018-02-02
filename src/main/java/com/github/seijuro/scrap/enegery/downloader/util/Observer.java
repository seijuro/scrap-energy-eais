package com.github.seijuro.scrap.enegery.downloader.util;

/**
 * Observer interface.
 *
 * @param <T>
 */
public interface Observer<T extends Notification> {
    public abstract void update();
    public abstract void update(T notification);
}

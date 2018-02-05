package com.github.seijuro.scrap.enegery.downloader.app;

import com.github.seijuro.scrap.enegery.downloader.util.Observer;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

public class EnergyFileEventSubscriber implements Observer<EnergyFileEvent> {
    /**
     * Class Instance(s)
     */
    private static Logger LOG = LoggerFactory.getLogger(EnergyFileEventSubscriber.class);
    private static final String Tag = "[SUBSCRIBER][ENERGY/FILE]";

    private static final long DefaultThreadSleepMillis = 10L * DateUtils.MILLIS_PER_SECOND;

    /**
     * Instance Properties
     */
    protected Queue<EnergyFileEvent> eventQueue = new LinkedList<>();

    @Override
    public void update() {
        //
        LOG.debug("[SUBSCRIBER][ENERGY/FILE] update ... ");
    }

    @Override
    public void update(EnergyFileEvent event) {
        synchronized (this.eventQueue) {
            this.eventQueue.offer(event);

            //  Log
            LOG.debug("{} update(event) ... event : {} (queue.size : {})", Tag, event.toString(), this.eventQueue.size());
        }
    }

    protected int countEvents() {
        synchronized (this.eventQueue) {
            return this.eventQueue.size();
        }
    }

    protected EnergyFileEvent pollEvent() {
        synchronized (this.eventQueue) {
            return this.eventQueue.poll();
        }
    }
}

package com.ea.orbit.actors.cluster;

import com.ea.orbit.actors.providers.ILifetimeProvider;
import com.ea.orbit.actors.runtime.Execution;
import com.ea.orbit.actors.runtime.OrbitActor;
import com.ea.orbit.concurrent.ConcurrentHashSet;
import com.ea.orbit.concurrent.Task;
import com.google.common.base.Preconditions;
import org.jgroups.util.ConcurrentLinkedBlockingQueue;

import java.time.Clock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ReservationProvider implements  IReservationProvider, ILifetimeProvider {


    public static enum DefaultPolicy
    {
        ALLOW,
        DENY
    }

    private static class ActivationReservation
    {
        public final String interfaceClassName;
        public final Long expiresAt;

        public ActivationReservation(String interfaceClassName, Long expiresAt) {
            this.interfaceClassName = interfaceClassName;
            this.expiresAt = expiresAt;
        }
    }

    private final Timer timer = new Timer("Reservation Provider");
    private final DefaultPolicy policy;
    private final Clock clock; private final Map<String, Long> reservationLimits;
    private final Map<String, Set<ActivationReservation>> reservations;
    private final Map<String, AtomicLong> activations;
    private final Long reservationDuration;

    private final Object lock = new Object();

    private final TimerTask task = new TimerTask()
    {
        @Override
        public void run()
        {
            tick();
        }
    };



    public ReservationProvider(Clock clock, DefaultPolicy policy, Map<String, Long> reservationLimits, long reservationDuration)
    {
        Preconditions.checkArgument(reservationDuration > 0, "Reservation duration cannot be <= 0");

        this.policy = policy;
        this.clock = clock;
        this.reservationLimits = reservationLimits;
        this.reservationDuration = reservationDuration;

        this.reservations = new HashMap<>();
        this.activations = new HashMap<>();
    }

    @Override
    public Task<Boolean> tryReservation(String interfaceClassName)
    {

        System.out.println(interfaceClassName);

        synchronized (this.lock) {
            if (!reservationLimits.containsKey(interfaceClassName))
            {
                return Task.fromValue(policy.equals(DefaultPolicy.ALLOW));
            }

            long limit = reservationLimits.get(interfaceClassName);


            activations.putIfAbsent(interfaceClassName, new AtomicLong(0));
            reservations.putIfAbsent(interfaceClassName, new HashSet<>());

            long active = activations.get(interfaceClassName).get();
            long reserved = reservations.get(interfaceClassName).size();

            if(active + reserved >= limit)
            {
                return Task.fromValue(false);
            }

            ActivationReservation reservation = new ActivationReservation(interfaceClassName, clock.millis() + reservationDuration);

            reservations.get(interfaceClassName).add(reservation);

            return Task.fromValue(true);
        }
    }


    @Override
    public Task<?> start()
    {
        timer.schedule(task, new Date(clock.millis() + reservationDuration), reservationDuration);
        return Task.done();
    }

    @Override
    public Task<?> stop()
    {
        task.cancel();
        return Task.done();

    }

    private void tick()
    {
        final long ms = clock.millis();
        synchronized (this.lock)
        {
            reservations.values().stream().forEach(resSet -> resSet.removeIf(res -> res.expiresAt >= ms));
        }
    }

    @Override
    public Task<?> postActivation(OrbitActor<?> actor)
    {
        //TODO: This would be much easier if we were able to get the interface class name here.
        for(Class<?> clazz : actor.getClass().getInterfaces())
        {
            if(reservationLimits.containsKey(clazz.getName()))
            {
                synchronized (this.lock)
                {
                    activations.putIfAbsent(clazz.getName(), new AtomicLong(0));
                    activations.get(clazz.getName()).incrementAndGet();
                }
            }
        }

        return Task.done();
    }

    @Override
    public Task<?> postDeactivation(OrbitActor<?> actor)
    {
        for(Class<?> clazz : actor.getClass().getInterfaces())
        {
            if(activations.containsKey(clazz.getName()))
            {
                synchronized(this.lock)
                {
                    activations.get(clazz.getName()).decrementAndGet();
                }
            }
        }
        return Task.done();
    }

}


package com.ea.orbit.actors.cluster;


import com.ea.orbit.actors.IActorObserver;
import com.ea.orbit.actors.providers.IOrbitProvider;
import com.ea.orbit.concurrent.Task;

public interface IReservationProvider extends IOrbitProvider, IActorObserver {
    public Task<Boolean> tryReservation(String interfaceClassName);
}

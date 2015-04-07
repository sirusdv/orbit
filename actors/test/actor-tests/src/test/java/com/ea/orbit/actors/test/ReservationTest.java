package com.ea.orbit.actors.test;

import com.ea.orbit.actors.IActor;
import com.ea.orbit.actors.OrbitStage;
import com.ea.orbit.actors.cluster.ReservationProvider;
import com.ea.orbit.actors.providers.IOrbitProvider;
import com.ea.orbit.actors.runtime.Execution;
import com.ea.orbit.actors.runtime.IReminderController;
import com.ea.orbit.actors.test.actors.ISomeActor;
import com.ea.orbit.actors.test.actors.SomeActor;
import com.ea.orbit.actors.test.actors.SomePlayer;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ReservationTest extends ActorBaseTest {



public OrbitStage createStage(IOrbitProvider provider) throws ExecutionException, InterruptedException
    {
        OrbitStage stage = new OrbitStage();
        stage.addProvider(provider);
        stage.setMode(OrbitStage.StageMode.HOST);
        stage.setExecutionPool(commonPool);
        stage.setMessagingPool(commonPool);
        stage.addProvider(new FakeStorageProvider(fakeDatabase));
        stage.setClock(clock);
        stage.setClusterName(clusterName);
        stage.setClusterPeer(new FakeClusterPeer());
        stage.start().join();
        stage.bind();
        IActor.getReference(IReminderController.class, "0").ensureStart();
        return stage;
    }



    @Test
    public void testReservationLimitSimple() throws NoSuchFieldException, IllegalAccessException, ExecutionException, InterruptedException
    {

        ReservationProvider provider = new ReservationProvider(clock, ReservationProvider.DefaultPolicy.ALLOW, new HashMap<>(),1000);
        OrbitStage stage = createStage(provider);
        ISomeActor actor = IActor.getReference(ISomeActor.class, "0");
        assertEquals(actor.sayHello("bla").get(), "bla");
    }

     @Test
    public void testReservationLimitSimpleLimit() throws NoSuchFieldException, IllegalAccessException, ExecutionException, InterruptedException
    {
       Map<String, Long> restrictions = new HashMap<>();
       restrictions.put(ISomeActor.class.getName(), 1L);

        ReservationProvider provider = new ReservationProvider(clock, ReservationProvider.DefaultPolicy.DENY, restrictions,1000);
        OrbitStage stage = createStage(provider);
        ISomeActor actor = IActor.getReference(ISomeActor.class, "0");
        assertEquals(actor.sayHello("bla").get(), "bla");
    }
}

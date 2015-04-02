package com.ea.orbit.actors.test;

import com.ea.orbit.actors.OrbitStage;
import com.ea.orbit.actors.runtime.Execution;
import com.ea.orbit.actors.test.actors.ISomeActor;
import com.ea.orbit.actors.test.actors.SomeActor;
import com.ea.orbit.actors.test.actors.SomePlayer;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * Created by sirus on 4/1/15.
 */
public class ReservationTest extends ActorBaseTest {


    private void setField(Object target, String name, Object value) throws IllegalAccessException, NoSuchFieldException
    {
        final Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    public OrbitStage createStage(Execution exec, Class<?>... classes) throws ExecutionException, InterruptedException, NoSuchFieldException, IllegalAccessException
    {
        OrbitStage stage = new OrbitStage();
        setField(stage, "execution", exec);
        exec.setAutoDiscovery(false);
        exec.addActorClasses(Arrays.asList(classes));
        stage.setMode(OrbitStage.StageMode.HOST);
        stage.setExecutionPool(commonPool);
        stage.setMessagingPool(commonPool);
        stage.addProvider(new FakeStorageProvider(fakeDatabase));
        stage.setClock(clock);
        stage.setClusterName(clusterName);
        stage.setClusterPeer(new FakeClusterPeer());
        stage.start().get();
        return stage;
    }


    @Test
    public void testReservationLimitSimple() throws NoSuchFieldException, IllegalAccessException, ExecutionException, InterruptedException
    {
        Execution exec = new Execution();
        exec.getActorActivationLimits().put(ISomeActor.class.getCanonicalName(), 2L);

        OrbitStage stage = createStage(exec, SomeActor.class);


        assertEquals(stage.getReference(ISomeActor.class, "0").sayHello("hi").get(), "bla");
        assertEquals(stage.getReference(ISomeActor.class, "1").sayHello("hi").get(), "bla");
    }

    @Test
    public void testReservationLimit() throws NoSuchFieldException, IllegalAccessException, ExecutionException, InterruptedException
    {
        Execution exec = new Execution();
        exec.getActorActivationLimits().put(ISomeActor.class.getCanonicalName(), 2L);

        OrbitStage stage = createStage(exec, SomeActor.class);


        assertEquals(stage.getReference(ISomeActor.class, "0").sayHello("hi").get(), "bla");
        assertEquals(stage.getReference(ISomeActor.class, "1").sayHello("hi").get(), "bla");
        assertEquals(stage.getReference(ISomeActor.class, "2").sayHello("hi").get(), "bla");

    }

}

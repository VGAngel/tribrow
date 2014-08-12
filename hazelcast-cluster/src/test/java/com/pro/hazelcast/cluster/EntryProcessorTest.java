package com.pro.hazelcast.cluster;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: vgangel
 * Date: 8/21/13
 * Time: 4:13 PM
 */
public class EntryProcessorTest {

    @Test
    public void testMapEntryProcessor() throws InterruptedException {
        Config cfg = new Config();
        cfg.getMapConfig("default").setInMemoryFormat(MapConfig.InMemoryFormat.OBJECT);
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(cfg);
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(cfg);
        IMap<Integer, Integer> map = instance1.getMap("testMapEntryProcessor");
        map.put(1, 1);
        EntryProcessor entryProcessor = new IncrementorEntryProcessor();
        map.executeOnKey(1, entryProcessor);
        assertEquals(map.get(1), (Object) 2);
        instance1.getLifecycleService().shutdown();
        instance2.getLifecycleService().shutdown();
    }

//
//    @Test
//    public void testMapEntryProcessorAllKeys() throws InterruptedException {
//        StaticNodeFactory nodeFactory = new StaticNodeFactory(2);
//        Config cfg = new Config();
//        cfg.getMapConfig("default").setInMemoryFormat(MapConfig.InMemoryFormat.OBJECT);
//        HazelcastInstance instance1 = nodeFactory.newHazelcastInstance(cfg);
//        HazelcastInstance instance2 = nodeFactory.newHazelcastInstance(cfg);
//        IMap<Integer, Integer> map = instance1.getMap("testMapEntryProcessorAllKeys");
//        int size = 100;
//        for (int i = 0; i < size; i++) {
//            map.put(i, i);
//        }
//        EntryProcessor entryProcessor = new IncrementorEntryProcessor();
//        Map<Integer, Object> res = map.executeOnAllKeys(entryProcessor);
//        for (int i = 0; i < size; i++) {
//            assertEquals(map.get(i), (Object) (i+1));
//        }
//        for (int i = 0; i < size; i++) {
//            assertEquals(map.get(i)+1, res.get(i));
//        }
//        instance1.getLifecycleService().shutdown();
//        instance2.getLifecycleService().shutdown();
//    }

    static class IncrementorEntryProcessor implements EntryProcessor, EntryBackupProcessor, Serializable {
        public Object process(Map.Entry entry) {
            Integer value = (Integer) entry.getValue();
            entry.setValue(value + 1);
            return value + 1;
        }

        public EntryBackupProcessor getBackupProcessor() {
            return IncrementorEntryProcessor.this;
        }

        public void processBackup(Map.Entry entry) {
            entry.setValue((Integer) entry.getValue() + 1);
        }
    }

}

/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.collection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @ali 1/2/13
 */
public class CollectionPartitionContainer {

    final CollectionService service;

    final ConcurrentMap<CollectionProxyId, CollectionContainer> containerMap = new ConcurrentHashMap<CollectionProxyId, CollectionContainer>(1000);

    public CollectionPartitionContainer(CollectionService service) {
        this.service = service;
    }

    public CollectionContainer getOrCreateCollectionContainer(CollectionProxyId proxyId) {
        CollectionContainer collectionContainer = containerMap.get(proxyId);
        if (collectionContainer == null) {
            collectionContainer = new CollectionContainer(proxyId, service);
            CollectionContainer current = containerMap.putIfAbsent(proxyId, collectionContainer);
            collectionContainer = current == null ? collectionContainer : current;
        }
        return collectionContainer;
    }

    public ConcurrentMap<CollectionProxyId, CollectionContainer> getContainerMap() {
        return containerMap; //TODO for testing only
    }

    public int getMaxBackupCount() {
        int max = 0;
        for (CollectionContainer container : containerMap.values()) {
            int c = container.config.getTotalBackupCount();
            max = Math.max(max, c);
        }
        return max;
    }

    void destroyCollection(CollectionProxyId collectionProxyId) {
        final CollectionContainer container = containerMap.remove(collectionProxyId);
        if (container != null) {
            container.clear();
        }
    }

    void destroy() {
        for (CollectionContainer container : containerMap.values()) {
            container.clear();
        }
        containerMap.clear();
    }

}
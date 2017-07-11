/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.classloader;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link BeanInfo} cache that stores introspection results by bean class and stop class. This goes
 * beyond the caching provided by the JRE, which only caches the results of
 * {@link Introspector#getBeanInfo(Class)}, but not the results of
 * {@link Introspector#getBeanInfo(Class, Class)} (with non null stop class).
 * <p>
 * To avoid class loader leaks, this class should not be used as a singleton if the introspected
 * classes are loaded from class loaders that are children of the class loader which holds the
 * reference to the cache. In such scenarios, use {@link #getCachedBeanInfo(Class, Class)}.
 */
public final class BeanInfoCache {
    private static final class CacheKey {
        private final Class<?> beanClass;
        private final Class<?> stopClass;

        CacheKey(Class<?> beanClass, Class<?> stopClass) {
            this.beanClass = beanClass;
            this.stopClass = stopClass;
        }

        @Override
        public int hashCode() {
            return 31*beanClass.hashCode() + (stopClass == null ? 0 : stopClass.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CacheKey) {
                CacheKey other = (CacheKey)obj;
                return beanClass == other.beanClass && stopClass == other.stopClass;
            } else {
                return false;
            }
        }
    }

    private static final Log log = LogFactory.getLog(BeanInfoCache.class);

    /**
     * Local cache instance for Javabeans that are loaded from the same class loader as this class.
     */
    private static final BeanInfoCache localCache = new BeanInfoCache();

    private final Map<CacheKey,BeanInfo> cache = new ConcurrentHashMap<CacheKey,BeanInfo>();

    /**
     * Introspect on a Java bean and return a cached {@link BeanInfo} object.
     *
     * @param beanClass
     *            The bean class to be analyzed.
     * @param stopClass
     *            The base class at which to stop the analysis; may be <code>null</code>.
     * @return A {@link BeanInfo} object describing the target bean. 
     * @exception IntrospectionException
     *                if an exception occurs during introspection.
     * @see Introspector#getBeanInfo(Class, Class)
     */
    public BeanInfo getBeanInfo(Class<?> beanClass, Class<?> stopClass) throws IntrospectionException {
        CacheKey key = new CacheKey(beanClass, stopClass);
        BeanInfo beanInfo = cache.get(key);
        if (beanInfo == null) {
            beanInfo = Introspector.getBeanInfo(beanClass, stopClass);
            cache.put(key, beanInfo);
        }
        return beanInfo;
    }

    /**
     * Locate an appropriate {@link BeanInfoCache} and return a cached {@link BeanInfo} object.
     * This method ensures that caching the {@link BeanInfo} object will not result in a class
     * loader leak.
     *
     * @param beanClass
     *            The bean class to be analyzed.
     * @param stopClass
     *            The base class at which to stop the analysis; may be <code>null</code>.
     * @return A {@link BeanInfo} object describing the target bean. 
     * @exception IntrospectionException
     *                if an exception occurs during introspection.
     */
    public static BeanInfo getCachedBeanInfo(Class<?> beanClass, Class<?> stopClass) throws IntrospectionException {
        ClassLoader classLoader = beanClass.getClassLoader();
        BeanInfoCache cache;
        if (classLoader instanceof BeanInfoCachingClassLoader) {
            cache = ((BeanInfoCachingClassLoader)classLoader).getBeanInfoCache();
        } else {
            // resObject from Admin Services have the bean.getClassLoader() is
            // "org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader"
            cache = localCache;
        }
        return cache.getBeanInfo(beanClass, stopClass);
    }
}
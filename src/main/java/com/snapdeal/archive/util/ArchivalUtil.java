/**
 *  Copyright 2016 Jasper Infotech (P) Limited . All Rights Reserved.
 *  JASPER INFOTECH PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */  
package com.snapdeal.archive.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;

/**
 *  
 *  @version     1.0, 10-Mar-2016
 *  @author vishesh
 */
public class ArchivalUtil {
    
    /**
     * Returns a map where key is string and value is object. Caution should be made to use this method iff the property is String 
     * @param objects
     * @param propertyName
     * @return
     */
    public static <K,T> Map<K, T> getPropertyObjectMap(List<T> objects, String propertyName, Class<K> propertyClass){
        Map<K,T> propertyObjectMap = new HashMap<>();
        for(T t : objects){
            Object value = null;            
            try {
                Field field = t.getClass().getDeclaredField(propertyName);
                field.setAccessible(true);
                value = field.get(t);
               // value = PropertyUtils.getProperty(t, propertyName);
                K k = (K) propertyClass.cast(value);
                propertyObjectMap.put(k, t);
            } catch (IllegalAccessException | NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }
        return propertyObjectMap;
    }
    
    /**
     * Returns a map where key is of type K and value is object. 
     * @param objects
     * @param propertyName
     * @return
     */
    public static <K,T> Map<K, T> getPropertyObjectMap(Set<T> objects, String propertyName, Class<K> propertyClass){
        Map<K,T> propertyObjectMap = new HashMap<>();
        for(T t : objects){
            Object value = null;
            try {
                value = PropertyUtils.getProperty(t, propertyName);
                K k = (K) propertyClass.cast(value);
                propertyObjectMap.put(k, t);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return propertyObjectMap;
    }
    
    /**
     * Returns the set of property with name <code>propertyName</code>, of type  <code>propertyClass</code> from the list of object
     * @param objects
     * @param propertyName
     * @param propertyClass
     * @return Set<K>, where K is of class <code>propertyClass</code>, 
     * @return empty set if either list is empty or if these exception occurs IllegalAccessException,InvocationTargetException,NoSuchMethodException
     */
    public static <K,T> Set<K> getPropertySet(Set<T> objects, String propertyName, Class<K> propertyClass){
        Map<K,T> map = getPropertyObjectMap(objects, propertyName,propertyClass);
        return map.keySet();
    }
    
    /**
     * Returns the set of property with name <code>propertyName</code>, of type  <code>propertyClass</code> from the list of object
     * @param objects
     * @param propertyName
     * @param propertyClass
     * @return Set<K>, where K is of class <code>propertyClass</code>, 
     * @return empty set if either list is empty or if these exception occurs IllegalAccessException,InvocationTargetException,NoSuchMethodException
     */
    public static <K,T> Set<K> getPropertySet(List<T> objects, String propertyName, Class<K> propertyClass){
        Map<K,T> map = getPropertyObjectMap(objects, propertyName,propertyClass);
        return map.keySet();
    }
    
    public static Set<Object> getPropertySetForListOfMap(List<Map<String,Object>> result, String propertyName){
        Set<Object> set = new HashSet<>();
        for(Map<String,Object> map : result){
            Object obj = map.get(propertyName);
            set.add(obj);
        }
        return set;
    }

}

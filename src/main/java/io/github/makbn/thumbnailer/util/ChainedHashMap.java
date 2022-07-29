/*
 * regain/Thumbnailer - A file search engine providing plenty of formats (Plugin)
 * Copyright (C) 2011  Come_IN Computerclubs (University of Siegen)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Come_IN-Team <come_in-team@listserv.uni-siegen.de>
 */
package io.github.makbn.thumbnailer.util;


import java.util.*;


public class ChainedHashMap<K, V> implements Map<K, V>, Iterable<Map.Entry<K, V>> {

    private static final int DEFAULT_HASHTABLE_SIZE = 20;

    private static final int DEFAULT_LIST_SIZE = 10;
    private final int listSize;

    private final HashMap<K, List<V>> hashtable;
    private int size;

    public ChainedHashMap() {
        this(DEFAULT_HASHTABLE_SIZE);
    }

    public ChainedHashMap(int hashtableSize) {
        this(hashtableSize, DEFAULT_LIST_SIZE);
    }

    public ChainedHashMap(int hashtableSize, int chainSize) {
        hashtable = new HashMap<>(hashtableSize);
        listSize = chainSize;

        size = 0;
    }

    public int size() {
        return size;
    }


    public boolean isEmpty() {
        return size == 0;
    }


    public boolean containsKey(Object key) {
        return hashtable.containsKey(key);
    }

    public boolean containsValue(Object value) {
        if (isEmpty())
            return false;

        Collection<List<V>> elements = hashtable.values();

        for (List<V> list : elements) {
            if (list.contains(value))
                return true;
        }
        return false;
    }

    /**
     * Get first of the linked objects by this key.
     */
    public V get(Object key) {
        List<V> list = hashtable.get(key);
        if (list == null)
            return null;
        else
            return list.get(0);
    }

    /**
     * Get all objects linked by this key
     * as an Iterable usable an foreach loop.
     *
     * @param key
     * @return Iterable
     * @throws NullPointerException (if key null)
     */
    public Iterable<V> getIterable(Object key) {
        final List<V> list = hashtable.get(key);

        if (list == null) {
            // Empty Iterator
            return new Iterable<V>() {
                public Iterator<V> iterator() {
                    return new Iterator<V>() {
                        public boolean hasNext() {
                            return false;
                        }

                        public V next() {
                            throw new NoSuchElementException("Empty");
                        }

                        @Override
                        public void remove() {
                            //do nothing
                        }

                    };
                }
            };
        } else {
            return new Iterable<V>() {
                public Iterator<V> iterator() {
                    return list.iterator();
                }
            };
        }
    }

    public List<V> getList(Object key) {
        List<V> list = hashtable.get(key);
        if (list == null)
            list = new ArrayList<V>();
        return list;
    }

    /**
     * Iterate over all elements in the table.
     * Note that this currently copies them into a collection,
     * so concurrent modification will not be taken into account
     * (there will be no ConcurrentModificationException, either).
     */

    public Iterator<Entry<K, V>> iterator() {
        if (size == 0) {
            return new Iterator<Entry<K, V>>() {
                public boolean hasNext() {
                    return false;
                }

                public Entry<K, V> next() {
                    throw new NoSuchElementException("Empty");
                }

                public void remove() {
                }
            };
        } else {
            Collection<Entry<K, V>> entries = new ArrayList<Entry<K, V>>();
            for (K key : hashtable.keySet()) {
                List<V> values = hashtable.get(key);
                for (V value : values)
                    entries.add(new AbstractMap.SimpleEntry<K, V>(key, value));
            }
            return entries.iterator();
        }
    }


    /**
     * Add this Value at the end of this key.
     *
     * @return As the value is never replaced, this will always return null.
     */
    public V put(K key, V value) {
        boolean success;

        List<V> list = hashtable.get(key);
        if (list == null) {
            list = new ArrayList<V>(listSize);
            success = list.add(value);
            hashtable.put(key, list);
        } else {
            success = list.add(value);
        }

        if (success)
            size++;

        return null;
    }


    /**
     * Remove all objects linked to this key.
     *
     * @param key Key
     * @return First of linked objects (or null).
     */
    public V remove(Object key) {
        List<V> list = hashtable.remove(key);
        if (list == null)
            return null;
        else {
            V element = list.get(0);
            size -= list.size();
            return element;
        }
    }

    public boolean remove(Object key, Object value) {
        List<V> list = hashtable.get(key);

        if (list == null)
            return false;

        boolean removed = list.remove(value);
        if (removed) {
            if (list.isEmpty())
                hashtable.remove(key);
            size--;
        }
        return removed;
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        hashtable.clear();
        size = 0;
    }


    public Set<K> keySet() {
        return hashtable.keySet();
    }

    public Collection<V> values() {
        List<V> newList = new ArrayList<V>();

        if (isEmpty())
            return newList;

        Collection<List<V>> values = hashtable.values();

        for (List<V> list : values)
            newList.addAll(list);

        return newList;
    }

    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException("entrySet is not implemented, as identical entries are allowed (conflict with Set contract). Instead, use .iterator() to iterate through all entries.");
    }

    public String toString() {
        StringBuffer str = new StringBuffer(200);

        for (K key : hashtable.keySet()) {
            str.append(key).append(":\n");

            List<V> values = hashtable.get(key);
            for (V value : values)
                str.append("\t").append(value).append("\n");
            str.append("\n");
        }

        return str.toString();
    }


}

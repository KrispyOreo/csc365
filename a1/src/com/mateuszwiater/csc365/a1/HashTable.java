package com.mateuszwiater.csc365.a1;

import java.util.ArrayList;

/**
 * Created by Mateusz on 2/14/2015.
 */
public class HashTable {

    private double LOADFACTOR;

    private int INITIALSIZE;

    private int elements;

    private HashElement[] hashTable;

    public HashTable() {
        // Set the loadFactor
        LOADFACTOR = 0.75;
        // Set the initialSize
        INITIALSIZE = 1;
        // Set the number of elements in the table
        elements = 0;
        // Create the hash array
        hashTable = new HashElement[INITIALSIZE];
    }

    public String[] getKeys() {
        // Create the arrayList
        ArrayList<String> keys = new ArrayList<String>();
        // Store the next element for traversing
        HashElement next;

        for(HashElement element : hashTable) {
            next = element;
            while(true) {
                // Check if next is null
                if(next == null) {
                    // Element does not exist, break
                    break;
                } else {
                    // Add the key
                    keys.add(element.getKey());
                    // Move on to the next element
                    next = next.getNext();
                }
            }
        }
        // Return the keys
        return keys.toArray(new String[keys.size()]);
    }

    public void put(String key, int value) {
        // Hold the calculated index
        int index;
        // Set the calculated index
        index = Math.abs(key.hashCode()) % hashTable.length;
        // Hold the next element
        HashElement next;

        // Check if the position is empty
        if(hashTable[index] == null) {
            // Put the element there
            hashTable[index] = new HashElement(key, value);
        } else {
            // Set the next element
            next = hashTable[index];
            // Loop through the linked list
            while(true) {
                // Check if next is null
                if(key.equals(next.getKey())) {
                    // Same key, increment the value
                    next.setValue(next.getValue() + value);
                    return;
                } else if (next.getNext() == null) {
                    // Put the new element there
                    next.setNext(new HashElement(key, value));
                    // Increment the elements
                    elements++;
                    // Check if rehashing is needed
                    if(elements >= (hashTable.length * LOADFACTOR)) {
                        // Rehash
                        rehash();
                    }
                    return;
                } else {
                    // Move on to the next element
                    next = next.getNext();
                }
            }
        }
    }

    public int get(String key) {
        // Hold the calculated index
        int index;
        // Set the calculated index
        index = Math.abs(key.hashCode()) % hashTable.length;

        // Hold the next element
        HashElement next;

        // Check if the position is empty
        if(hashTable[index] == null) {
            // Element does not exist, return 0
            return 0;
        } else {
            // Set the next element
            next = hashTable[index];
            // Loop through the linked list
            while(true) {
                // Check if next is null
                if(next == null) {
                    // Element does not exist, return 0
                    return 0;
                } else if (key.equals(next.getKey())) {
                    // Same key, element exists
                    return next.getValue();
                } else {
                    // Move on to the next element
                    next = next.getNext();
                }
            }
        }
    }

    private void rehash() {
        // Reset the elements count
        elements = 0;
        // Store the current table
        HashElement[] tmp = hashTable;
        // Store the next element for traversing
        HashElement next;
        // Create a new hashTable
        hashTable = new HashElement[hashTable.length * 2];

        for(HashElement element : tmp) {
            next = element;
            while(true) {
                // Check if next is null
                if(next == null) {
                    // Element does not exist, break
                    break;
                } else {
                    // Rehash element
                    this.put(next.getKey(), next.getValue());
                    // Move on to the next element
                    next = next.getNext();
                }
            }
        }
    }

    private class HashElement {

        // String to store the key
        private String key;

        // Int to store the word count
        private int value;

        // HashElement to store the next element
        private HashElement next;

        public HashElement(String key, int value) {
            // Set the key
            this.key = key;
            // Set the value
            this.value = value;
            // Set the next element
            this.next = null;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public HashElement getNext() {
            return this.next;
        }

        public void setNext(HashElement next) {
            this.next = next;
        }
    }
}

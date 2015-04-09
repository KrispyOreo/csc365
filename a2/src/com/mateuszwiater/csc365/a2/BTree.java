package com.mateuszwiater.csc365.a2;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mateusz on 4/7/2015.
 */
public class BTree {
    // RandomAccessFile to store the BTree
    RandomAccessFile persistence;

    // Root Node pointer
    Node root = null;

    // The degree of the bTree
    int BTREE_DEGREE = 24;

    // Size limit of strings
    int STRING_SIZE_LIMIT = 100;

    // Number of Keys
    int numberOfKeys = BTREE_DEGREE - 1;

    // Number of Frequency's
    int numberOfFrequencys = BTREE_DEGREE - 1;

    // Number of Pointers
    int numberOfPointers = BTREE_DEGREE;

    // Offset of the url
    int offset = 0;

    // Next free write position
    int nextFreeWritePosition = 0;

    // Size of the node
    int nodeSize = (Long.BYTES * (numberOfPointers + 2)) + (Integer.BYTES * (numberOfFrequencys * 2)) + (STRING_SIZE_LIMIT * numberOfKeys);

    public BTree(String name, String url) throws IOException {
        //System.out.println("Creating BTree...");
        // Check if the file exists
        File file = new File(name);
        // Calculate the offset
        offset = url.getBytes("UTF-8").length;
        if(file.exists()) {
            // File exists
            // Load the root node
            persistence = new RandomAccessFile(name, "rw");
            root = new Node(offset);
        } else {
            // File does not exist
            // Create the bTree RandomAccessFile
            persistence = new RandomAccessFile(name, "rw");
            // Write the url to the file
            persistence.write(url.getBytes("UTF-8"));
            // Set the nextFreeWritePosition
            nextFreeWritePosition = offset;
            // Create a new root node
            root = new Node(-1, offset);
            root.save();
        }
    }

    public void add(String key, int value) throws IOException {
        // Keep track of the current node
        Node next = root;
        while(true) {
            // Loop thorough the nodeObjects array
            for(int i = 0; i < next.getNodeObjects().length; i++) {
                // Don't check pointers
                if(!((i % 2) == 0)) {
                    // Check if object is null or if key is less than the current key
                    if((next.getNodeObjects()[i] == null) || key.compareToIgnoreCase(next.getNodeObjects()[i].getKey()) < 0) {
                        // Check if this is a leaf node
                        if(next.getNodeObjects()[i - 1] == null) {
                            // At a leaf node, place the nodeObject in there
                            Node n = new Node(0, 0);
                            n.setNodeObject(1, new NodeObject(key, value));
                            merge(next, n);
                            return;
                        } else {
                            // Not a leaf node, go down a level
                            next = next.getNodeObjects()[i - 1].getNode();
                            i = 0;
                        }
                    } else {
                        if(key.compareToIgnoreCase(next.getNodeObjects()[i].getKey()) == 0) {
                            // Key is the same, set the new value
                            next.getNodeObjects()[i].setValue(value);
                            next.save();
                            return;
                        }
                    }
                }
            }
        }
    }

    public int get(String key) throws IOException {
        // Keep track of the current node
        Node next = root;
        while(true) {
            // Loop thorough the nodeObjects array
            for(int i = 0; i < next.getNodeObjects().length; i++) {
                // Don't check pointers
                if(!((i % 2) == 0)) {
                    // Check if object is null or if key is greater than the current key
                    if((next.getNodeObjects()[i] == null) || key.compareToIgnoreCase(next.getNodeObjects()[i].getKey()) < 0) {
                        // Check if this is a leaf node
                        if(next.getNodeObjects()[i - 1] == null) {
                            // At a leaf node, key does not exist return -1
                            return -1;
                        } else {
                            // Not a leaf node, go down a level
                            next = next.getNodeObjects()[i - 1].getNode();
                            i = 0;
                        }
                    } else {
                        if(key.compareToIgnoreCase(next.getNodeObjects()[i].getKey()) == 0) {
                            // Key is the same, return the value
                            return next.getNodeObjects()[i].getValue();
                        }
                    }
                }
            }
        }
    }

    public HashMap<String, Integer> getBulkFrequencys(HashMap<String, Integer> input) throws IOException {
        // Store the next node
        Node next = root;
        // Store the key/frequency pairs
        HashMap<String, Integer> output = new HashMap<String, Integer>();
        // Store the pointers not yet accessed
        ArrayList<NodeObject> pointers = new ArrayList<NodeObject>();
        while(true) {
            // Loop through the node
            for(int i = 0; i < next.getNodeObjects().length - 2; i++) {
                // Check if a pointer or element
                if((i % 2) == 0) {
                    // This is a pointer
                    // Add it to the pointers ArrayList
                    if(next.getNodeObjects()[i] != null) {
                        pointers.add(next.getNodeObjects()[i]);
                    }
                } else {
                    // This is a key
                    // check if the input array has this key
                    if((next.getNodeObjects()[i] != null) && (input.containsKey(next.getNodeObjects()[i].getKey()))) {
                        // It contains the key
                        output.put(next.getNodeObjects()[i].getKey(), next.getNodeObjects()[i].getValue());
                    }
                }
            }
            // Set the next node
            if(pointers.size() != 0) {
                next = new Node(pointers.get(0).getPointer());
                // Remove the just used pointer
                pointers.remove(0);
            } else {
                // There sre no more pointers
                break;
            }
        }
        // return the new HashMap
        return output;
    }

    private Node split(Node node) throws IOException {
        Node leftNode;
        Node middleNode;
        Node rightNode;

        long middleNodeParent        = 0;
        long leftNodeSavePosition    = this.getNextFreeWritePosition();
        long middleNodeSavePosition  = 0;
        long rightNodeSavePosition   = this.getNextFreeWritePosition();

        int nodeArraySize = node.getNodeObjects().length;
        int splitIndex;

        // Check if the node is a root node
        if(node.getParentPointer() == -1) {
            // Set the middle node parent pointer to -1, since the middle node is the new root
            middleNodeParent = -1;
            // Set the middle node pointer to the start the memory allocated for it
            middleNodeSavePosition = node.getSelfPointer();
        } else {
            // Set the middle node pointer to the nodes parent since it will get merged into there
            middleNodeSavePosition = node.getParentPointer();
        }

        // Calculate the index at which to split the Node
        splitIndex = (BTREE_DEGREE % 2) == 0 ? BTREE_DEGREE - 1 : BTREE_DEGREE;

        // Create the left and right Nodes
        leftNode = new Node(middleNodeSavePosition, leftNodeSavePosition);
        rightNode = new Node(middleNodeSavePosition, rightNodeSavePosition);

        // Populate the leftNode
        for(int i = 0; i < splitIndex; i++) {
            leftNode.setNodeObject(i, node.getNodeObjects()[i]);
        }

        // Fix the leftNode's children's parent pointers
        for(int i = 0; i < leftNode.getNodeObjects().length; i++) {
            // Look at pointers
            if(((i % 2) == 0)) {
                if(leftNode.getNodeObjects()[i] != null) {
                    Node n = leftNode.getNodeObjects()[i].getNode();
                    n.setParentPointer(leftNodeSavePosition);
                    n.save();
                } else {
                    break;
                }
            }
        }

        // Populate the rightNode
        for(int i = (splitIndex + 1); i < nodeArraySize; i++) {
            rightNode.setNodeObject((i - (splitIndex + 1)), node.getNodeObjects()[i]);
        }

        // Fix the rightNode's children's parent pointers
        for(int i = 0; i < rightNode.getNodeObjects().length; i++) {
            // Look at pointers
            if(((i % 2) == 0)) {
                if(rightNode.getNodeObjects()[i] != null) {
                    Node n = rightNode.getNodeObjects()[i].getNode();
                    n.setParentPointer(rightNodeSavePosition);
                    n.save();
                } else {
                    break;
                }
            }
        }

        // Create the middleNode
        middleNode = new Node(middleNodeParent, middleNodeSavePosition);
        middleNode.setNodeObject(0, new NodeObject(leftNodeSavePosition));
        middleNode.setNodeObject(1, new NodeObject(node.getNodeObjects()[splitIndex].getKey(), node.getNodeObjects()[splitIndex].getValue()));
        middleNode.setNodeObject(2, new NodeObject(rightNodeSavePosition));

        // Save the nodes
        leftNode.save();
        rightNode.save();

        // Return the middleNode
        return middleNode;
    }

    private void merge(Node mainNode, Node nodeToBeAdded) throws IOException {
        Node nextMainNode = mainNode;
        Node nextNodeToBeAdded = nodeToBeAdded;

        // Loop to handle the domino effect of merging
        while(true) {
            // Loop to find where the nextNodeToBeAdded belongs
            for(int i = 0; i < nextMainNode.getNodeObjects().length; i++) {
                // Look at only keys
                if(!((i % 2) == 0)) {
                    // Check if nextNodeToBeAdded belongs here
                    if(nextMainNode.getNodeObjects()[i] == null || (nextNodeToBeAdded.getNodeObjects()[1].getKey().compareToIgnoreCase(nextMainNode.getNodeObjects()[i].getKey()) < 0)) {
                        // Set a bunch of initial values for the array push
                        NodeObject tmpKey = nextMainNode.getNodeObjects()[i];
                        NodeObject tmpPointer = nextMainNode.getNodeObjects()[i + 1];
                        nextMainNode.setNodeObject(i - 1, nextNodeToBeAdded.getNodeObjects()[0]);
                        nextMainNode.setNodeObject(i, nextNodeToBeAdded.getNodeObjects()[1]);
                        nextMainNode.setNodeObject(i + 1, nextNodeToBeAdded.getNodeObjects()[2]);
                        // Push the objects to the right of where the nextNodeToBeAdded was placed
                        for(int j = i + 2; j < nextMainNode.getNodeObjects().length; j++) {
                            NodeObject tmp = nextMainNode.getNodeObjects()[j];
                            nextMainNode.setNodeObject(j,tmpKey);
                            j++;
                            tmpKey = tmp;
                            tmp = nextMainNode.getNodeObjects()[j];
                            nextMainNode.setNodeObject(j,tmpPointer);
                            tmpPointer = tmp;
                        }
                        // NextNodeToBeAdded was placed, break
                        break;
                    }
                }
            }

            // Check if this node has to be split
            if(nextMainNode.haveToSplit()) {
                // Node has to be split
                // Check if current node is the root node
                if(nextMainNode.getParentPointer() == -1) {
                    // Current node is the root node, set the new root node
                    root = split(nextMainNode);
                    // Save the new root node
                    root.save();
                    // Break, new node does not need to be split
                    break;
                } else {
                    // Current node is not root
                    // Split the current node
                    nextNodeToBeAdded = split(nextMainNode);
                    // Get the next main node
                    nextMainNode = new Node(nextMainNode.getParentPointer());
                }
            } else {
                if(nextMainNode.getNodeObjects()[nextMainNode.getNodeObjects().length-2] != null || nextMainNode.getNodeObjects()[nextMainNode.getNodeObjects().length-1] != null) {
                    //System.out.println("Pointer: " + nextMainNode.getNodeObjects()[nextMainNode.getNodeObjects().length - 1].getPointer());
                    System.out.print("Problem: ");
                    System.out.println(nextMainNode.printNode());
                    throw new RuntimeException();
                }
                if(nextMainNode.getParentPointer() == -1) {
                    // Current node is the root node, set the new root node
                    root = nextMainNode;
                    // Save the new root node
                    root.save();
                    // Break, new node does not need to be split
                    break;
                } else {
                    nextMainNode.save();
                    break;
                }
            }
        }
    }

    private long getNextFreeWritePosition() {
        nextFreeWritePosition += nodeSize;
        return nextFreeWritePosition;
    }

    public void close() throws IOException {
        persistence.close();
    }

    // Container for each BTree element
    private class Node {
        // Pointer to the parent Node
        private long parentPointer;
        // Pointer to itself
        private long selfPointer;

        // Array of NodeObjects
        NodeObject[] nodeObjects = new NodeObject[BTREE_DEGREE + numberOfKeys + 2];

        // Create a new Node
        public Node(long parentPointer, long selfPointer) {
            this.setParentPointer(parentPointer);
            this.selfPointer = selfPointer;
        }

        // Load a Node from the RAF
        public Node(long selfPointer) throws IOException {
            this.selfPointer = selfPointer;
            load();
        }

        public String printNode() {
            String node = "";
            for(int i = 0; i < nodeObjects.length; i++) {
                node += "|";
                //System.out.print("|");
                if((i % 2) == 0) {
                    if(nodeObjects[i] == null) {
                        node += "n|";
                        //System.out.print("n|");
                    } else {
                        node += "P|";
                        //System.out.print("P|");
                    }

                } else {
                    if(nodeObjects[i] == null) {
                        node += "n|";
                        //System.out.print("n|");
                    } else {
                        node += nodeObjects[i].getKey() + "|";
                        //System.out.print(nodeObjects[i].getKey() + "|");
                    }

                }
            }
            return node;
        }

        public void setNodeObject(int index, NodeObject nodeObject) {
            nodeObjects[index] = nodeObject;
        }

        // Loads the Node from the RAF
        private void load() throws IOException {
            // Seek to where the Node is stored
            persistence.seek(selfPointer);
            // Set the selfPointer
            selfPointer = persistence.readLong();
            // Set the parentPointer
            parentPointer = persistence.readLong();
            // Load the node pointers and keys
            for(byte i = 0; i < (nodeObjects.length - 2); i++ ) {
                if((i % 2) == 0) {
                    // This is a pointer
                    // Check if the pointer is null
                    long tmpPointer = persistence.readLong();
                    if(tmpPointer != -1) {
                        // Create a new Pointer
                        nodeObjects[i] = new NodeObject(tmpPointer);
                    } else {
                        // Set the pointer to null
                        nodeObjects[i] = null;
                    }
                } else {
                    // This is a key
                    // Check if the key exists
                    int keySize = persistence.readInt();
                    if(keySize == -1) {
                        // Set the key to null
                        nodeObjects[i] = null;
                        byte[] keyBytes = new byte[STRING_SIZE_LIMIT];
                        persistence.read(keyBytes);
                        persistence.readInt();
                    } else {
                        byte[] keyBytes = new byte[keySize];
                        persistence.read(keyBytes);
                        String key = new String(keyBytes, "UTF-8");
                        keyBytes = new byte[STRING_SIZE_LIMIT - keySize];
                        persistence.read(keyBytes);
                        int value = persistence.readInt();
                        nodeObjects[i] = new NodeObject(key, value);
                    }
                }
            }
        }

        // Write the Node to the RAF
        public void save() throws IOException {
            // Seek to where the node is stored
            persistence.seek(selfPointer);
            // Write the self pointer
            persistence.writeLong(selfPointer);
            // Write the parent pointer
            persistence.writeLong(parentPointer);
            // Write the node pointers and keys
            for(byte i = 0; i < (nodeObjects.length - 2); i++) {
                if((i % 2) == 0) {
                    // This is a pointer
                    // Check if the pointer is null
                    if(nodeObjects[i] != null) {
                        // Write the pointer
                        persistence.writeLong(nodeObjects[i].getPointer());
                    } else {
                        // Write the null pointer
                        persistence.writeLong(-1);
                    }
                } else {
                    // This is a key
                    // Check if the key exists
                    if(nodeObjects[i] != null) {
                        byte[] tmpByte = nodeObjects[i].getKey().getBytes("UTF-8");
                        byte[] dummyData = new byte[STRING_SIZE_LIMIT - tmpByte.length];
                        //System.out.println("KEY: " + nodeObjects[i].getKey() +  " SIZE TMP: " + tmpByte.length + " SIZ DUMMY: " + dummyData.length);
                        // Write the size of the actual data
                        persistence.writeInt(tmpByte.length);
                        // Write the key bytes
                        persistence.write(tmpByte);
                        // Write the dummyData
                        persistence.write(dummyData);
                        // Write the value
                        persistence.writeInt(nodeObjects[i].getValue());
                    } else {
                        // Write a null key and value
                        persistence.writeInt(-1);
                        byte[] keyBytes = new byte[STRING_SIZE_LIMIT];
                        persistence.write(keyBytes);
                        persistence.writeInt(-1);
                    }
                }
            }
        }

        public boolean haveToSplit() {
            return this.nodeObjects[nodeObjects.length - 2] != null;
        }

        public long getSelfPointer() {
            return selfPointer;
        }

        public void setParentPointer(long parentPointer) {
            this.parentPointer = parentPointer;
        }

        public long getParentPointer() {
            return parentPointer;
        }

        public NodeObject[] getNodeObjects() {
            return nodeObjects;
        }
    }

    // Wrapper for each element within the Node
    private class NodeObject {
        // The word
        String key;
        // The frequency
        int value;
        // The pointer
        long pointer;

        public NodeObject(String key, int value) {
            this.key   = key;
            this.value = value;
        }

        public NodeObject(long pointer) {
            this.pointer = pointer;
        }

        public Node getNode() throws IOException {
            return new Node(pointer);
        }

        public String getKey() {
            return this.key;
        }

        public int getValue() {
            return this.value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public long getPointer() {
            return pointer;
        }
    }
}
package org.example;

import java.util.ArrayList;

public class Tree <Key extends Comparable<Key>, Value, M>{
    static int degree = 4;//maximum degree = every Node has at most degree Keys
    protected Node<Key, Value, M> root;
    protected Monoid<M, Key> monoid;
    /**
     * Constructor
     */
    public Tree(Monoid<M, Key> monoid){
        this.monoid = monoid;
        this.root = new LeafNode<Key, Value, M>(monoid);
    }

    /**
     * This method starts at the root of the B+ tree and traverses down the
     * tree via key comparisons to the corresponding leaf node that should hold the 'key'.
     * @return LeafNode that should contain the key we searched for
     */
    public LeafNode<Key, Value, M> shouldContainKey(Key key){
        if (this.root == null){ return null; }
        Node<Key, Value, M> node = this.root;
        while (node.getNodeType() == NodeType.InnerNode) {
            //iterate over the children that contain range including key
            node = ((InnerNode<Key, Value, M>) node).getChild(node.searchInNode(key));
        }

        return (LeafNode<Key, Value, M>)node;
    }

    /**
     * Searches for the InnerNode containing the key
     * @return: InnerNode containing key or null, if key does not exist in InnerNodes
     */
    public InnerNode<Key, Value, M> searchInnerKeyNode(Key key){
        if (this.root == null){ return null; }
        Node<Key, Value, M> node = this.root;
        while (node.getNodeType() != NodeType.LeafNode) {
            //iterate over the children that contain range including key
            if (node.keys.contains(key)){
                return (InnerNode<Key, Value, M>)node;
            }
            node = ((InnerNode<Key, Value, M>) node).getChild(node.searchInNode(key));
        }
        return null;
    }

    /**
     * @return value associated with the specified key => key-value pair in LeafNode
     */
    public Value searchValue(Key key){
        LeafNode<Key, Value, M> leaf = shouldContainKey(key);
        int index = leaf.searchInNode(key);
        if (index == -1){
            return null;
        }
        return leaf.getValue(index);
    }

    /**
     * inserts a key-value pair into the tree and checks that every node does not have more
     * than their maximum allowed keys
     */
    public void insert(Key key, Value value){
        LeafNode<Key, Value, M> leaf = shouldContainKey(key);//return leaf that should contain our key
        if (leaf == null){
            this.root = new LeafNode<Key, Value, M>(this.monoid, key, value);
            return;
        }
        //search index of the first keys-entry that is bigger than key
        int i = leaf.searchNextBest(key);
        if (i < leaf.keys.size() && leaf.keys.get(i).equals(key)){//key already exists
            leaf.setValue(i, value);//replace the old value with the new one
            return;
        }

        leaf.addKeyValueIndex(i, key, value);

        if (leaf.getSizeKeys() > degree){//we have Overflow
            Node<Key, Value, M> node = leaf.overflow();
            if (node != null){ //was the last node with an overflow the original root?
                this.root = node;
            }
        }
        //update the parent labels
        leaf.calculateLabel();
        leaf.recalculateParentLabels();
    }

    /**
     * Deletes a key-value pair and checks, that every node has at least the minimum number of nodes
     */
    public void delete(Key key){
        LeafNode<Key, Value, M> leaf = shouldContainKey(key);
        if (leaf == null){ return; }

        int index = leaf.searchInNode(key);
        if (index < 0){
            return;//key does not exist
        }
        InnerNode<Key, Value, M> tmp = null;
        if (leaf.keys.get(0) == key){
            //if the deleted key is the first Key of a leaf node, then that Key can also be found in an inner node
            tmp = searchInnerKeyNode(key);
        }
        leaf.removeKeyIndex(index);
        leaf.removeValueIndex(index);
        if (leaf.checkUnderflow()){
            Node<Key, Value, M> node = leaf.underflow(tmp, key);
            if (node != null) {
                this.root = node;
            }
            //leaf.calculateLabel(); //is being done in the Inner/Leaf-Node functions itself
            leaf.recalculateParentLabels();
            return;
        } else {//if we do not have an underflow, but the deleted Key is in parent
            if (tmp != null && tmp == leaf.parent){
                tmp.keys.set(tmp.keys.indexOf(key), leaf.keys.get(0));
            }
        }
        //Case X: if a higher Node than parent contained the key, then change that key to the new key
        if (tmp != leaf.parent && tmp != null){
            tmp.keys.set(tmp.keys.indexOf(key), leaf.keys.get(0));
        }
        leaf.calculateLabel();
        leaf.recalculateParentLabels();
    }

    /**
     * computes the fingerprint of the range in between x and y (including x and y) according to the Monoid used
     * @param y     end point of range (y > x) (exclusive)
     * @param node  the node that contains or whose subtree contains the beginning of our range = x
     * @param indexNode the index at which Key x is written/saved in the given node
     * @return a triplet consisting of: the monoid of the subrange the method was called on, the LeafNode and index that contain the first key >= y
     */
    public Pair<M, Pair<Node<Key, Value, M>, Integer>> computeFingerprint (Key y, Node<Key, Value, M> node, int indexNode){
        M acc = this.monoid.identity();
        while (node != null){
            if (this.monoid.compare(node.label, y) < 0) {
                //aggregateUP: aggregates the hash-values of all upward Nodes that are in the range [x, y]
                //             This means that this function only looks at parents of our node and only goes upwards in the tree
                //runtime optimisation: check if the Node is in range from index = 0 to index = node.keys.size() -> then add the whole label of the node
                //                      (can only actually occur with the first LeafNode we look at)
                Pair<M, Integer> pair = node.aggregate(indexNode, y, node.keys.size() + 1, true); //+1 so that we also add the last child of InnerNodes
                acc = this.monoid.combine(acc, pair.getFirst());

                if (node.parent == null) {
                    return new Pair<>(acc, Pair.createPair(null, 0));
                } else {
                    indexNode = node.indexInParent + 1;
                    node = node.parent;
                }
            } else {
                //runtime optimisation: it's possible that aggUP already added everything needed (different from line 147) -> aggDOWN unnecessary
                //                      that is the case when node.keys.get(indexNode-1) > y (-> we are not at the end of the node, but we are at the end of range)
                //                      -> the Key that marks the first element of the next child to look at is outside our range
                //aggregateDOWN: aggregates the hash-values of all downward Nodes that are in the range [x, y]
                //               This means that this function only looks at the children of our node and only goes downwards in the tree
                Pair<M, Integer> pair = node.aggregate(indexNode, y, node.keys.size(), false);
                indexNode = pair.getSecond();
                acc = this.monoid.combine(acc, pair.getFirst());
                //runtime optimisation: we can stop aggDOWN earlier if (node.keys.get(index) > y and node.getChildren(index).greatestElement < y)
                //                      then we simply need to add the whole left child and return

                if (node.getNodeType() == NodeType.LeafNode){
                    break;
                } else {
                    node = ((InnerNode<Key, Value, M>) node).getChild(indexNode);
                    indexNode = 0;
                }
            }
        }
        if (node != null && indexNode == node.keys.size()){
            node = node.getRightSibling();
            indexNode = 0;
        }
        return new Pair<>(acc, Pair.createPair(node, indexNode));
    }

    /* FOR TESTING */
    public Node<Key, Value, M> getRoot() {
        return root;
    }

    /**
     * collects all Keys between x and y (including x and y)
     * THIS FUNCTION WORKS THE SAME AS "computeFingerprint"
     * @param x starting point of range
     * @param y end point of range (y > x)
     * @return ordered ArrayList of Keys from x to y
     */
    public ArrayList<Key> aggregateRange(Key x, Key y){
        return null;
    }
}

package org.example;

import java.util.*;

abstract class Node <Key extends Comparable<Key>, Value, M>{
    //any Node has: keys (more than one), parent pointer, (Sibling pointer?)
    protected ArrayList<Key> keys;
    protected M label;
    protected Monoid<M, Key> monoid;
    //protected CountingMonoid<Value> monoid2 = new CountingMonoid<>();
    protected InnerNode<Key, Value, M> parent;
    protected int indexInParent;//corresponds to the children index

    protected Node(Monoid<M, Key> monoid) {
        this.keys = new ArrayList<Key>();//maximum 2*degree-1 Keys in every InnerNode
        this.monoid = monoid;//every node then calls "this.monoid.combine()"
        this.parent = null;
        indexInParent = 0;
    }


    public Key getKey(int index) {
        return this.keys.get(index);
    }
    public void setKey(int index, Key key) {
        this.keys.set(index, key);//overwrites
    }
    public void addKeyIndex(int index, Key key) {
        this.keys.add(index, key);//insert another element at index
    }
    public void addKey(Key key) {
        this.keys.add(key);//insert another element at the end
    }
    public void removeKeyIndex(int index) {
        this.keys.remove(index);//remove element at index
    }

    public int getSizeKeys(){
        return this.keys.size();
    }

    public InnerNode<Key, Value, M> getParent() {
        return parent;
    }
    public void setParent(InnerNode<Key, Value, M> parent) {
        this.parent = parent;
    }

    /**
     * returns Type of Node
     */
    public abstract NodeType getNodeType();
    public abstract Node<Key, Value, M> getLeftSibling();
    public abstract Node<Key, Value, M> getRightSibling();

    /**
     * has individual descriptions
     */
    public abstract void calculateLabel();

    /**
     * recalculates all parent labels of the specified node
     */
    public void recalculateParentLabels(){
        InnerNode<Key, Value, M> tmp = this.parent;
        while (tmp != null){
            tmp.calculateLabel();
            tmp = tmp.parent;
        }
    }

    /** Search for the specified key in the certain node
     * and returns index of the value or the child associated with the key
     * exception: LeafNode does not contain key -> return -1
     */
    public abstract int searchInNode(Key key);

    /* FOR INSERT */
    /**
     * Search the correct index for insertion
     * @return index of the first keys-entry that is bigger than key (if the key does not exist) or the exact key
     */
    public int searchNextBest(Key key){
        int i = 0;
        for (i = 0; i < this.keys.size(); i++){ //iterate over all keys
            int compare = key.compareTo(this.keys.get(i));//-1 if key > keys[i], 0 if =, 1 if <
            if (compare == 0){
                return i;//entry already exists
            } else if (compare < 0) {
                return i;
            }
        }
        return i;
    }

    /**
     * Handles the case, in which the number of entries in a node (after insertion) is above
     * the number of permitted entries (=Overflow). In this case, the Node will get split in
     * two and then checks if the parent has an overflow.
     * @return root node, if the last node that had an overflow was the original root, otherwise null
     */
    public Node<Key, Value, M> overflow(){
        int midIndex = (this.keys.size()/2);//cuts the decimal part off -> round down
        Key upKey = this.keys.get(midIndex);

        Node<Key, Value, M> newNode = this.split(midIndex);

        //setParents
        if (this.parent == null) {//we split the root node
            this.parent = new InnerNode<Key, Value, M>(this.monoid);//make new root
        }
        newNode.parent = this.parent;

        //push the upKey into the parent node and set index of childNode
        this.parent.insertNode(upKey, this, newNode);
        if (this.parent.keys.size() > Tree.degree){//we have Overflow
             return this.parent.overflow();
        } else {
            return this.parent.parent == null ? this.parent : null;//x?y:z = if x then y else z
        }
        //return this.parent.parent;
    }

    /**
     * Splits a Node into 2 Nodes without setting parents
     * @return the new Node, which was inserted as the right sibling e.g. Node split into Node and newNode => return newNode
     */
    public abstract Node<Key, Value, M> split(int midIndex);

    /**
     * Adds the key and the child reference in the parent (at suitable index). Is called on the parent node of node and newNode.
     * @param upKey the new Key that has to be inserted in the parent node
     * @param node the node that was split
     * @param newNode the newNode, which is the result of the split operation
     */
    public abstract void insertNode(Key upKey, Node<Key, Value, M> node, Node<Key, Value, M> newNode);


    /*FOR DELETE*/
    /**
     * checks, that the node has at least degree/2 number of entries
     * exception is the root node
     * @return false if node has degree/2 or more entries otherwise true
     */
    public boolean checkUnderflow(){
        if (this.parent == null){
            return false;//root node cannot have underflow
        }
        return (this.keys.size() < (Tree.degree/2));
    }

    /**
     * In case of an underflow, either borrow a node from a sibling (if possible) or fuse two sibling Nodes
     * Attention "Case X": it can happen, that the deleted Key is a Key of an InnerNode on a higher floor than the parent Node,
     *                      in this case we need to manually find that InnerNode and change the original key to the new key
     * @param innerNode: the InnerNode that contains the key, necessary for case X
     * @param key: necessary for case X
     * @return a node, if the root has to be changed, otherwise null
     */
    public Node<Key, Value, M> underflow(InnerNode<Key, Value, M> innerNode, Key key){
        //1: try to borrow a key from sibling
        Node<Key, Value, M> leftSibling = this.getLeftSibling();
        //has a left sibling and left sibling has more keys than minimum amount
        if (leftSibling != null && leftSibling.parent == this.parent && leftSibling.keys.size() > Tree.degree/2) {
            this.transferLeftSibling(leftSibling);
            //Case X: cannot happen here because our node has a left sibling -> only the parent has
            //        a key reference, no other inner nodes
            return null;
        }

        Node<Key, Value, M> rightSibling = this.getRightSibling();
        //has a right sibling and right sibling has more keys than minimum amount
        if (rightSibling != null && rightSibling.parent == this.parent && rightSibling.keys.size() > Tree.degree/2) {
            this.transferRightSibling(rightSibling);
            //Case X: if a higher Node than parent contained the deleted key, then change that key to the new key
            if (this.keys.size() == 1 && innerNode != this.parent && innerNode != null){
                //if the borrowed key is now the first key and innerNode is neither null nor the parent node then
                innerNode.keys.set(innerNode.keys.indexOf(key), this.keys.get(0));
            }
            return null;
        }

        Node<Key, Value, M> parentTMP = this.parent;
        Node<Key, Value, M> node;
        //2: Can't borrow a key from any sibling => fuse with sibling
        if (leftSibling != null && leftSibling.parent == this.parent) {
            leftSibling.fuseSiblings(this);
            node = leftSibling;
        } else { //B+-tree node has at least 2 children -> one of them has the same parent
            this.fuseSiblings(rightSibling);
            node = this;
        }
        //Case X: if a higher Node than parent contained the reference key, then change that key to the new key
        if (innerNode != node.parent && innerNode != null){
            innerNode.keys.set(innerNode.keys.indexOf(key), node.keys.get(0));
        }

        //Check on underflow and check if it's the root and if necessary change root node
        if (parentTMP.keys.size() < (Tree.degree/2)){//we have underflow
            if (parentTMP.parent == null){//our parent is root node
                if (parentTMP.keys.size() == 0){//root now has 0 entries and one child => change root node to child
                    node.parent = null;
                    return node;
                } else {
                    return null;//root has entries
                }
            }
            return this.parent.underflow(null, key);//our node.parent is not root
            //Can Case X Problem occur twice? No, because InnerNodes are not represented in InnerNodes
            // -> only when changing LeafNodes can this problem occur, and we only change LeafNodes in the first iteration
        }
        return null;//no underflow
    }

    /* FOR DELETE */
    /**
     * transfer the left child of our sibling node to our current node, adjust the label of all nodes
     * and adjust the references contained in the parent
     */
    public abstract void transferLeftSibling(Node<Key, Value, M> lender);
    /**
     * transfer the right child of our sibling node to our current node, adjust the label of all nodes
     * and adjust the references contained in the parent
     */
    public abstract void transferRightSibling(Node<Key, Value, M> lender);

    /**
     * fuses two sibling Nodes and removes the corresponding key + childPointer from parent node
     */
    public abstract void fuseSiblings(Node<Key, Value, M> rightSibling);

    /* FOR COMPUTE FINGERPRINT */

    /**
     * This function aggregates the relevant keys of the node (= "this") starting from indexNode up to size
     * @param indexNode first index to look at
     * @return a pair of (monoid, index) - the monoid stores the accumulated hash value; index = the last index we looked at
     */
    public abstract Pair<M, Integer> aggregate(int indexNode, Key y, int size, boolean upDown);

    /* FOR TESTING AND VISUALISATION */

    /**
     * Returns an inorder String of the entire Tree (including inner Nodes)
     * if "nullElement" is before a Key, then that Key belongs to an InnerNode; otherwise LeafNode
     * @param nullElement goes before InnerNodes
     * @param keys saves the ordered List of all keys in the tree
     */
    public abstract void inorder(Key nullElement, ArrayList<Key> keys);

    /**
     * Prints the tree, with every floor of nodes having one line and a node is depicted by [the keys contained in this node]
     */
    public abstract void printTree();

    /**
     * Represents the entire tree with an Array List of Array Lists
     * structure saved in list: [[rootNodeKeys], [Layer1], [Layer2], ...]
     * [Layer] structure:  [firstNodeKeys, 0, secondNodeKeys, 0, thirdNodeKeys, ...]
     */
    public abstract void arrayTree(Key nullElement, ArrayList<ArrayList<Key>> list);
}

package org.example;

import java.util.ArrayList;

public class InnerNode <Key extends Comparable<Key>, Value, M> extends Node<Key, Value, M>{
    private ArrayList<Node<Key, Value, M>> children;

    public InnerNode(Monoid<M, Key> monoid) {
        super(monoid);
        this.label = this.monoid.identity();
        this.children = new ArrayList<Node<Key, Value, M>>();
    }

    public void addChildIndex(int index, Node<Key, Value, M> child){
        this.children.add(index, child);
    }
    public Node<Key, Value, M> getChild(int index){
        return this.children.get(index);
    }
    public void removeChildIndex(int index) {
        this.children.remove(index);
    }

    public int getSizeChildren(){
        return this.children.size();
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.InnerNode;
    }

    @Override
    public InnerNode<Key, Value, M> getLeftSibling() {
        if (this.parent == null){
            return null;
        }
        if (this.indexInParent > 0){//otherwise "this" is at index 0
            return (InnerNode<Key, Value, M>) this.parent.children.get(this.indexInParent-1);
        }
        int down = 1;
        Node<Key, Value, M> tmp = this.children.get(0);
        while (tmp.getNodeType() != NodeType.LeafNode){
            tmp = ((InnerNode<Key, Value, M>)tmp).children.get(0);
            down++;
        }
        if (tmp.getLeftSibling() == null){
            return null;
        }
        tmp = tmp.getLeftSibling();//tmp here LeafNode
        while (down > 0){
            tmp = tmp.parent;
            down--;
        }
        return (InnerNode<Key, Value, M>) tmp;
    }

    @Override
    public InnerNode<Key, Value, M> getRightSibling() {
        if (this.parent == null){
            return null;
        }
        if (this.indexInParent < this.parent.keys.size()){
            return (InnerNode<Key, Value, M>) this.parent.children.get(this.indexInParent+1);
        }
        int down = 1;
        Node<Key, Value, M> tmp = this.children.get(this.children.size()-1);
        while (tmp.getNodeType() != NodeType.LeafNode){
            tmp = ((InnerNode<Key, Value, M>)tmp).children.get(((InnerNode<Key, Value, M>)tmp).children.size()-1);
            down++;
        }
        if (tmp.getRightSibling() == null){
            return null;
        }
        tmp = tmp.getRightSibling();//tmp here LeafNode
        while (down > 0){
            tmp = tmp.parent;
            down--;
        }
        return (InnerNode<Key, Value, M>) tmp;
    }

    /**
     * Calculates the label of the node => combines the labels of all children in order from left to right
     */
    @Override
    public void calculateLabel() {
        this.label = this.monoid.identity();
        for (Node<Key, Value, M> child : this.children) {
            this.label = this.monoid.combine(this.label, child.label);
        }
    }

    /**
     * Returns the index of the child pointer that holds the range, that the key is part of
     */
    @Override
    public int searchInNode(Key key) {
        int i = 0;
        for (i = 0; i < this.keys.size(); i++){ //iterate over all keys
            int compare = key.compareTo(this.keys.get(i));//-1 if key < keys[i], 0 if =, 1 if >
            if (compare == 0){
                return i + 1; //we need the second child of found key (structure: child1-Key-child2)
            } else if (compare < 0) {
                return i;//first child of found key
            }
        }
        return i;//return last child of node
    }

    @Override
    public Node<Key, Value, M> split(int midIndex){
        InnerNode<Key, Value, M> newNode = new InnerNode<Key, Value, M>(this.monoid);

        int size = this.keys.size();
        //relocate Keys
        for (int i = size-1; i > midIndex; i--){
            newNode.keys.add(0, this.keys.get(i));
            this.keys.remove(i);
        }
        //relocate children
        for (int i = size; i > midIndex; i--) {
            newNode.children.add(0, this.getChild(i));
            newNode.children.get(0).parent = newNode;
            this.children.remove(i);
        }
        this.keys.remove(midIndex);
        //recalculate labels
        this.calculateLabel();
        newNode.calculateLabel();

        return newNode;
    }

    @Override
    public void insertNode(Key upKey, Node<Key, Value, M> node, Node<Key, Value, M> newNode) {
        int index = this.searchNextBest(upKey);
        if (this.getSizeKeys() > index && this.getKey(index) == upKey){//it is impossible that the upKey already exists in InnerNode
            throw new UnsupportedOperationException("insertNode-InnerNode Error: This is impossible");
        }
        this.keys.add(index, upKey);
        if (this.children.size() == 0){
            this.children.add(index, node);//in case the parent node is new => empty
        }
        this.children.add(index+1, newNode);
        for (int i = index; i < this.children.size(); i++){//change the all children indices starting from node
            this.children.get(i).indexInParent = i;
        }
        //newNode.indexInParent = index+1;
        if (newNode.getNodeType() == NodeType.InnerNode){
            for (int i = 0; i <= newNode.keys.size(); i++){
                ((InnerNode<Key, Value, M>) newNode).children.get(i).indexInParent = i;
            }
        }
    }

    @Override
    public void transferLeftSibling(Node<Key, Value, M> lender) {
        InnerNode<Key, Value, M> lenderTMP = (InnerNode<Key, Value, M>) lender;
        this.keys.add(0, this.parent.keys.get(this.indexInParent-1));//since we transfer the left sibling, "this.indexInParent" is always > 0
        this.children.add(0, lenderTMP.children.get(lenderTMP.children.size()-1));//add in front the last sibling child
        this.children.get(0).parent = this;//update parent of new child
        lenderTMP.children.remove(lenderTMP.children.size()-1);
        this.parent.keys.set(this.indexInParent-1, lender.keys.get(lender.keys.size()-1));
        lender.keys.remove(lender.keys.size()-1);
        //update the index of all childrenNodes of "this" node (because we added children in front)
        for (int i = 0; i < this.children.size(); i++){
            this.children.get(i).indexInParent = i;
        }
        this.calculateLabel();
        lender.calculateLabel();
    }

    @Override
    public void transferRightSibling(Node<Key, Value, M> lender) {
        int sizeBorrower = this.keys.size();
        InnerNode<Key, Value, M> lenderTMP = (InnerNode<Key, Value, M>) lender;
        this.keys.add(this.parent.keys.get(this.indexInParent));//sink the key of the parent down and append to node
        this.children.add(lenderTMP.children.get(0));//append first child of sibling
        this.children.get(this.children.size()-1).parent = this;//update parent of new child
        lenderTMP.children.remove(0);//remove that child in right sibling
        /*if (sizeBorrower == 0 && this.indexInParent != 0){
            this.parent.keys.set(this.indexInParent-1, this.keys.get(0));//update the key in parent of necessary (if "this" was empty) - this is done in the line below right??
        }*/
        lender.parent.keys.set(lender.indexInParent-1, lender.keys.get(0));//change the key symbolizing right sibling in parent to "new" first key
        lender.keys.remove(0);//remove the key that is now in parent

        this.children.get(this.children.size()-1).indexInParent = this.children.size()-1;//update the index of the new childNode
        for (int i = 0; i < lenderTMP.children.size(); i++){//update the index of all children of lender
            lenderTMP.children.get(i).indexInParent = lenderTMP.children.get(i).indexInParent - 1;
        }
        this.calculateLabel();
        lender.calculateLabel();
    }

    @Override
    public void fuseSiblings(Node<Key, Value, M> rightSibling) {
        InnerNode<Key, Value, M> rightSTMP = (InnerNode<Key, Value, M>) rightSibling;
        //get the correct index of the Key that symbolises the right node in parent
        int rIndexKeyInParent = rightSibling.indexInParent == 0? rightSibling.indexInParent : rightSibling.indexInParent-1;
        int index = this.keys.size();
        this.keys.add(this.parent.keys.get(rIndexKeyInParent));
        this.keys.addAll(rightSibling.keys);
        this.children.addAll(rightSTMP.children);
        //update parent pointers and indices of those children
        for (int i = index; i < this.children.size(); i++){
            this.children.get(i).parent = this;
            this.children.get(i).indexInParent = i;
        }
        this.parent.keys.remove(rIndexKeyInParent);
        this.parent.children.remove(rightSibling.indexInParent);

        //update the index of all following sibling nodes
        for (int i = rIndexKeyInParent; i < this.parent.children.size(); i++){
            this.parent.children.get(i).indexInParent = i;
        }

        this.calculateLabel();
    }

    /* FOR COMPUTE FINGERPRINT */

    @Override
    public Pair<M, Integer> aggregate(int indexNode, Key y, int size, boolean upFunction){
        M acc = this.monoid.identity();
        while (indexNode < size && (upFunction || this.keys.get(indexNode).compareTo(y) < 0)){//1 if x > y, 0 if =, -1 if <
            acc = this.monoid.combine(acc, this.children.get(indexNode).label);
            indexNode++;
        }
        return new Pair<>(acc, indexNode);
    }


    /* FOR TESTING AND VISUALISATION */
    @Override
    public void inorder(Key nullElement, ArrayList<Key> sortedKeys){
        //if (this.children != null){
            for (int i = 0; i < this.children.size(); i++){
                this.children.get(i).inorder(nullElement, sortedKeys);
                if (i < this.keys.size()){
                    sortedKeys.add(nullElement);
                    sortedKeys.add(this.keys.get(i));
                }
            }
        //}
    }

    @Override
    public void printTree() {
            System.out.print(this.keys);
            Node<Key, Value, M> tmp = this.getRightSibling();
            while (tmp != null){
                System.out.print("   -   ");
                System.out.print(tmp.keys);
                tmp = tmp.getRightSibling();
            }
            System.out.println(" ");
            this.children.get(0).printTree();
    }
    @Override
    public void arrayTree(Key nullElement, ArrayList<ArrayList<Key>> list) {
        ArrayList<Key> list2 = new ArrayList<>(this.keys);
        Node<Key, Value, M> tmp = this.getRightSibling();
        while (tmp != null){
            list2.add(nullElement);
            list2.addAll(tmp.keys);
            tmp = tmp.getRightSibling();
        }
        list.add(list2);
        this.children.get(0).arrayTree(nullElement, list);
    }
}

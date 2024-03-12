import java.util.ArrayList;

public class LeafNode <Key extends Comparable<Key>, Value, M> extends Node<Key, Value, M>{
    private ArrayList<Value> values;
    private LeafNode<Key, Value, M> leftSibling;
    private LeafNode<Key, Value, M> rightSibling;

    public LeafNode(Monoid<M, Key> monoid) {
        super(monoid);
        this.label = this.monoid.identity();
        this.values = new ArrayList<Value>();
        this.leftSibling = null;
        this.rightSibling = null;
    }
    public LeafNode(Monoid<M, Key> monoid, Key key, Value value) {
        super(monoid);
        this.label = this.monoid.mapIntoMonoid(key);
        this.values = new ArrayList<Value>();
        this.keys.add(key);
        this.values.add(value);
        this.leftSibling = null;
        this.rightSibling = null;
    }

    public Value getValue(int index){ return this.values.get(index); }
    public void setValue(int index, Value value){ this.values.set(index, value); }
    public void removeValueIndex(int index) {
        this.values.remove(index);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.LeafNode;
    }
    @Override
    public LeafNode<Key, Value, M> getLeftSibling(){
        return leftSibling;
    }
    @Override
    public LeafNode<Key, Value, M> getRightSibling() { return rightSibling; }
    public void addKeyValueIndex(int index, Key key, Value value){
        this.keys.add(index, key);
        this.values.add(index, value);
    }

    /**
     * Calculates the label of the node => maps every value to sth. (according to given monoid)
     * and then combines them (according to given monoid) in order from left to right
     */
    @Override
    public void calculateLabel() {
        this.label = this.monoid.identity();
        for (Key key : this.keys) {
            this.label = this.monoid.combine(this.label, this.monoid.mapIntoMonoid(key));
        }
    }

    /**
     * Returns the index of the entry that has exactly the same key as (parameter) key
     * @return index
     */
    @Override
    public int searchInNode(Key key) {
        for (int i = 0; i < this.keys.size(); i++){ //iterate over all keys
            int compare = key.compareTo(this.keys.get(i));
            if (compare == 0){
                return i;
            }
        }
        return -1;
    }

    /* Following for insert Operation */
    @Override
    public Node<Key, Value, M> split(int midIndex){
        LeafNode<Key, Value, M> newNode = new LeafNode<Key, Value, M>(this.monoid);

        int size = this.getSizeKeys();
        for (int i = size-1; i >= midIndex; i--){
            newNode.addKeyValueIndex(0, this.keys.get(i), this.values.get(i));
            this.keys.remove(i);
            this.values.remove(i);
        }
        newNode.rightSibling = this.rightSibling;
        newNode.leftSibling = this;
        if (this.rightSibling != null){
            this.rightSibling.leftSibling = newNode;
        }
        this.rightSibling = newNode;
        //recalculate the labels
        this.calculateLabel();
        newNode.calculateLabel();

        return newNode;
    }

    @Override
    public void insertNode(Key upKey, Node<Key, Value, M> node, Node<Key, Value, M> newNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void transferLeftSibling(Node<Key, Value, M> lender) {
        LeafNode<Key, Value, M> lenderTMP = (LeafNode<Key, Value, M>) lender;
        this.keys.add(0, lender.keys.get(lender.keys.size()-1));
        this.values.add(0, lenderTMP.values.get(lenderTMP.values.size()-1));
        this.calculateLabel();

        lender.keys.remove(lender.keys.size()-1);
        lenderTMP.values.remove(lenderTMP.values.size()-1);
        lenderTMP.calculateLabel();

        this.parent.keys.set(this.indexInParent-1, this.keys.get(0));
    }

    @Override
    public void transferRightSibling(Node<Key, Value, M> lender) {
        int sizeBorrower = this.keys.size();
        LeafNode<Key, Value, M> lenderTMP = (LeafNode<Key, Value, M>) lender;
        this.keys.add(lender.keys.get(0));
        this.values.add(lenderTMP.values.get(0));
        this.calculateLabel();

        lender.keys.remove(0);
        lenderTMP.values.remove(0);
        lenderTMP.calculateLabel();

        if (sizeBorrower == 0 && this.indexInParent != 0){
            //if borrower was empty (then the borrowed key is now the first key in the node)
            // and not the first node in parent, then change key in parent
            this.parent.keys.set(this.indexInParent-1, this.keys.get(0));
        }//lender.parent == this.parent (see: Node line 158)
        this.parent.keys.set(lender.indexInParent-1, lender.keys.get(0));//change the key symbolizing right sibling in parent to "new" first key
    }

    @Override
    public void fuseSiblings(Node<Key, Value, M> rightSibling) {
        LeafNode<Key, Value, M> rightSiblingTMP = (LeafNode<Key, Value, M>) rightSibling;
        this.keys.addAll(rightSibling.keys);
        this.values.addAll(rightSiblingTMP.values);
        this.rightSibling = rightSiblingTMP.rightSibling;//remove right sibling from sibling list
        if (rightSiblingTMP.rightSibling != null){
            rightSiblingTMP.rightSibling.leftSibling = this;
        }

        //get the correct index of the Key that symbolises the right node in parent
        int rIndexKeyInParent = rightSibling.indexInParent == 0? rightSibling.indexInParent : rightSibling.indexInParent-1;
        this.parent.keys.remove(rIndexKeyInParent);
        this.parent.removeChildIndex(rightSibling.indexInParent);

        //update index in parent of all siblings with same parent
        for (int i = rIndexKeyInParent; i < this.parent.getSizeChildren(); i++){
            this.parent.getChild(i).indexInParent = i;
        }

        this.calculateLabel();
    }

    /* FOR COMPUTE FINGERPRINT */

    @Override
    public Pair<M, Integer> aggregate(int indexNode, Key y, int size, boolean upDown){
        /*//optimizes runtime
        if (up && indexNode == 0) {
            acc = this.monoid.combine(acc, this.label);
            return new Pair<>(acc, indexNode);
        } */
        M acc = this.monoid.identity();
        while (indexNode < this.keys.size() && this.keys.get(indexNode).compareTo(y) < 0) {
            acc = this.monoid.combine(acc, this.monoid.mapIntoMonoid(this.keys.get(indexNode)));
            indexNode++;
        }
        return new Pair<>(acc, indexNode);
    }

    /*FOR TESTING*/
    public int getSizeValues(){
        return this.values.size();
    }

    /* FOR TESTING AND VISUALISATION */
    public void inorder(Key nullElement, ArrayList<Key> sortedKeys){
        sortedKeys.addAll(this.keys);
    }

    @Override
    public void printTree() {
        System.out.print(this.keys);
        LeafNode<Key, Value, M> tmp = this.rightSibling;
        while (tmp != null){
            System.out.print("   -   ");
            System.out.print(tmp.keys);
            tmp = tmp.rightSibling;
        }
    }

    @Override
    public void arrayTree(Key nullElement, ArrayList<ArrayList<Key>> list) {
        ArrayList<Key> list2 = new ArrayList<>(this.keys);
        LeafNode<Key, Value, M> tmp = this.rightSibling;
        while (tmp != null){
            list2.add(nullElement);
            list2.addAll(tmp.keys);
            tmp = tmp.rightSibling;
        }
        list.add(list2);
    }
}

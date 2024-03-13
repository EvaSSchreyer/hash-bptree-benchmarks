package org.example;

import java.util.ArrayList;
//import java.io.File;

public class Main {
    public static void main(String[] args){
        //Monoid<Integer, Integer> monoid = new CountingMonoid<>();
        //Tree<Integer, Integer, Integer> tree = new Tree<Integer, Integer, Integer>(monoid);
        Monoid<ExampleMonoid<Integer>, Integer> monoid = new ExampleMonoid<>(0, 0, null);
        Tree<Integer, Integer, ExampleMonoid<Integer>> tree = new Tree<Integer, Integer, ExampleMonoid<Integer>>(monoid);
        tree.insert(1,11);
        tree.insert(2,22);
        tree.insert(3,33);//expected structure:      (3      5)
        tree.insert(4,44);//                     (2)    (4)     (6, 7)
        tree.insert(5,55);//                  (1) (2) (3) (4) (5) (6) (7,8)
        tree.insert(6,66);
        tree.insert(7,77);
        tree.insert(8,88);
        ArrayList<Integer> list = new ArrayList<>();
        tree.getRoot().inorder(0, list);
        System.out.println(list + "\n");

        ArrayList<ArrayList<Integer>> list2 = new ArrayList<>();
        tree.getRoot().arrayTree(0, list2);
        System.out.println(list2 + "\n");

        tree.root.printTree();
        System.out.println();

        LeafNode<Integer, Integer, ExampleMonoid<Integer>> startLeaf = tree.shouldContainKey(2);
        System.out.println("This is the hash value of subrange x = 2 to y = 7: " + tree.computeFingerprint(7, startLeaf, 0).getFirst().count);
    }
}

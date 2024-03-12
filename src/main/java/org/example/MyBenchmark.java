/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.example;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
//@State(Scope.Benchmark)
@Fork(value = 60, warmups = 1, jvmArgs = {"-Xms2G", "-Xmx2G"}) //doesn't function with @Warmup and @Measurement it seems, but makes it so setup is run anew
//                      for each fork and each fork runs n number of iterations + but only warmup forks function -> discards results
//                      normal forks ("value")  (remove the "(Level.Iteration)" from @Setup
@Warmup(iterations = 3)
@Measurement(iterations = 5)
public class MyBenchmark {
    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(MyBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

    @State(Scope.Thread)
    public static class Params100 {//this is the same as with the @Setup and others
        private Monoid<ExampleMonoid<Integer>, Integer> monoid = new ExampleMonoid<>(0,0, null);
        private Tree<Integer, Integer, ExampleMonoid<Integer>> tree = new Tree<>(monoid);
        private int firstKey;
        private int secondKey;
        private LeafNode<Integer, Integer, ExampleMonoid<Integer>> leafFirstKey;
        private int indexFirstKey;

        @Setup//(Level.Iteration) //"Level.Iteration" == The method is called once for each iteration of the benchmark. => makes it so that each iteration runs the setup anew
        public void setup() {
            Random ran = new Random();
            tree.insert(ran.nextInt(10000), ran.nextInt(100));
            while (tree.root.label.count < 100){
                tree.insert(ran.nextInt(10000), ran.nextInt(100));
            }

            firstKey = ran.nextInt(10000);
            secondKey = ran.nextInt((10000 - firstKey)) + firstKey;

            leafFirstKey = tree.shouldContainKey(firstKey);
            indexFirstKey = leafFirstKey.searchNextBest(firstKey);

            Pair<ExampleMonoid<Integer>, Pair<Node<Integer, Integer, ExampleMonoid<Integer>>, Integer>> pair = tree.computeFingerprint(secondKey, leafFirstKey, indexFirstKey);
            while (pair.getFirst().count < 10 || pair.getFirst().count > 15){
                firstKey = ran.nextInt(10000);
                secondKey = ran.nextInt((10000 - firstKey)) + firstKey;
                leafFirstKey = tree.shouldContainKey(firstKey);
                indexFirstKey = leafFirstKey.searchNextBest(firstKey);

                pair = tree.computeFingerprint(secondKey, leafFirstKey, indexFirstKey);
            }

            System.out.println("\n100-Param 1: " + firstKey + " Param 2: " + secondKey);
            //leafThirdKey = tree.shouldContainKey(thirdKey);
            //indexThirdKey = leafFirstKey.searchNextBest(thirdKey);
        }
    }

    @State(Scope.Thread)
    public static class Params1000 {//this is the same as with the @Setup and others
        private Monoid<ExampleMonoid<Integer>, Integer> monoid = new ExampleMonoid<>(0,0, null);
        private Tree<Integer, Integer, ExampleMonoid<Integer>> tree = new Tree<>(monoid);
        private int firstKey;
        private int secondKey;
        private LeafNode<Integer, Integer, ExampleMonoid<Integer>> leafFirstKey;
        private int indexFirstKey;

        @Setup//(Level.Iteration) //"Level.Iteration" == The method is called once for each iteration of the benchmark. => makes it so that each iteration runs the setup anew
        public void setup() {
            Random ran = new Random();
            tree.insert(ran.nextInt(10000), ran.nextInt(100));
            while (tree.root.label.count < 1000){
                tree.insert(ran.nextInt(10000), ran.nextInt(100));
            }

            firstKey = ran.nextInt(10000);
            secondKey = ran.nextInt((10000 - firstKey)) + firstKey;

            leafFirstKey = tree.shouldContainKey(firstKey);
            indexFirstKey = leafFirstKey.searchNextBest(firstKey);

            Pair<ExampleMonoid<Integer>, Pair<Node<Integer, Integer, ExampleMonoid<Integer>>, Integer>> pair = tree.computeFingerprint(secondKey, leafFirstKey, indexFirstKey);
            while (pair.getFirst().count < 100 || pair.getFirst().count > 150){
                firstKey = ran.nextInt(10000);
                secondKey = ran.nextInt((10000 - firstKey)) + firstKey;
                leafFirstKey = tree.shouldContainKey(firstKey);
                indexFirstKey = leafFirstKey.searchNextBest(firstKey);

                pair = tree.computeFingerprint(secondKey, leafFirstKey, indexFirstKey);
            }

            System.out.println("\n1000-Param 1: " + firstKey + " Param 2: " + secondKey);
            //leafThirdKey = tree.shouldContainKey(thirdKey);
            //indexThirdKey = leafFirstKey.searchNextBest(thirdKey);
        }
    }

    @State(Scope.Thread)
    public static class Params10000 {//this is the same as with the @Setup and others
        private Monoid<ExampleMonoid<Integer>, Integer> monoid = new ExampleMonoid<>(0,0, null);
        private Tree<Integer, Integer, ExampleMonoid<Integer>> tree = new Tree<>(monoid);
        private int firstKey;
        private int secondKey;
        private LeafNode<Integer, Integer, ExampleMonoid<Integer>> leafFirstKey;
        private int indexFirstKey;

        @Setup//(Level.Iteration) //"Level.Iteration" == The method is called once for each iteration of the benchmark. => makes it so that each iteration runs the setup anew
        public void setup() {
            Random ran = new Random();
            tree.insert(ran.nextInt(100000), ran.nextInt(1000));
            while (tree.root.label.count < 10000){
                tree.insert(ran.nextInt(100000), ran.nextInt(1000));
            }

            firstKey = ran.nextInt(100000);
            secondKey = ran.nextInt((100000 - firstKey)) + firstKey;

            leafFirstKey = tree.shouldContainKey(firstKey);
            indexFirstKey = leafFirstKey.searchNextBest(firstKey);

            Pair<ExampleMonoid<Integer>, Pair<Node<Integer, Integer, ExampleMonoid<Integer>>, Integer>> pair = tree.computeFingerprint(secondKey, leafFirstKey, indexFirstKey);
            while (pair.getFirst().count < 1000 || pair.getFirst().count > 1500){
                firstKey = ran.nextInt(100000);
                secondKey = ran.nextInt((100000 - firstKey)) + firstKey;
                leafFirstKey = tree.shouldContainKey(firstKey);
                indexFirstKey = leafFirstKey.searchNextBest(firstKey);

                pair = tree.computeFingerprint(secondKey, leafFirstKey, indexFirstKey);
            }

            System.out.println("\n10000-Param 1: " + firstKey + " Param 2: " + secondKey);
            //leafThirdKey = tree.shouldContainKey(thirdKey);
            //indexThirdKey = leafFirstKey.searchNextBest(thirdKey);
        }
    }

    @Benchmark
    public Pair<ExampleMonoid<Integer>, Pair<Node<Integer, Integer, ExampleMonoid<Integer>>, Integer>> computeFinger100(Params100 param){
        //need to return see "JMHSample_10_DeadCode.java"
        //return tree.get(param).computeFingerprint(secondArrayKey.get(param), leafFirstKey.get(param), indexFirstKey.get(param));
        return param.tree.computeFingerprint(param.secondKey, param.leafFirstKey, param.indexFirstKey);
    }

    @Benchmark
    public Pair<ExampleMonoid<Integer>, Pair<Node<Integer, Integer, ExampleMonoid<Integer>>, Integer>> computeFinger1000(Params1000 param){
        //need to return see "JMHSample_10_DeadCode.java"
        //return tree.get(param).computeFingerprint(secondArrayKey.get(param), leafFirstKey.get(param), indexFirstKey.get(param));
        return param.tree.computeFingerprint(param.secondKey, param.leafFirstKey, param.indexFirstKey);
    }

    @Benchmark
    public Pair<ExampleMonoid<Integer>, Pair<Node<Integer, Integer, ExampleMonoid<Integer>>, Integer>> computeFinger10000(Params10000 param){
        //need to return see "JMHSample_10_DeadCode.java"
        //return tree.get(param).computeFingerprint(secondArrayKey.get(param), leafFirstKey.get(param), indexFirstKey.get(param));
        return param.tree.computeFingerprint(param.secondKey, param.leafFirstKey, param.indexFirstKey);
    }
}

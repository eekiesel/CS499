package com.example.weighttracking;

import java.util.ArrayList;

public class WeightHeapUtil {

    // Custom MaxHeap implementation to track the highest weight
    public static class MaxHeap {
        private final ArrayList<Double> heap = new ArrayList<>();

        // Inserts a value into the max heap
        public void insert(double val) {
            // Add to the end
            heap.add(val);
            // Restore heap property
            heapifyUp(heap.size() - 1);
        }

        // Returns the maximum value without removing it
        public Double peek() {
            // Root is the max
            return heap.isEmpty() ? null : heap.get(0);
        }

        // Restores max-heap property by bubbling up the inserted value
        private void heapifyUp(int index) {
            while (index > 0) {
                int parentIndex = (index - 1) / 2;
                if (heap.get(index) > heap.get(parentIndex)) {
                    swap(index, parentIndex);
                    index = parentIndex;
                } else {
                    break;
                }
            }
        }

        // Swaps two values in the heap
        private void swap(int i, int j) {
            double temp = heap.get(i);
            heap.set(i, heap.get(j));
            heap.set(j, temp);
        }
    }

    // Custom MinHeap implementation to track the lowest weight
    public static class MinHeap {
        private final ArrayList<Double> heap = new ArrayList<>();

        // Inserts a value into the min heap
        public void insert(double val) {
            // Add to the end
            heap.add(val);
            // Restore heap property
            heapifyUp(heap.size() - 1);
        }

        // Returns the minimum value without removing it
        public Double peek() {
            // Root is the min
            return heap.isEmpty() ? null : heap.get(0);
        }

        // Restores min-heap property by bubbling up the inserted value
        private void heapifyUp(int index) {
            while (index > 0) {
                int parentIndex = (index - 1) / 2;
                if (heap.get(index) < heap.get(parentIndex)) {
                    swap(index, parentIndex);
                    index = parentIndex;
                } else {
                    break;
                }
            }
        }

        // Swaps two values in the heap
        private void swap(int i, int j) {
            double temp = heap.get(i);
            heap.set(i, heap.get(j));
            heap.set(j, temp);
        }
    }

}

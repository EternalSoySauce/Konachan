package com.ess.konachan.global;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class EightFigurePuzzle {

    private Node node;
    private int column;
    private int[][] aimState;

    public int[][] rebuild(int column) {
        this.column = column;
        initAimState();

        int count = column * column;
        Random random = new Random();
        ArrayList<Integer> numericList = new ArrayList<>();
        boolean canSolved = false;
        while (true) {
            while (!canSolved) {
                numericList.clear();
                for (int i = 0; i < count; i++) {
                    int num = random.nextInt(count);
                    while (numericList.contains(num)) {
                        num = random.nextInt(count);
                    }
                    numericList.add(num);
                }

                int parity = 0;
                for (int i = 1; i < count; i++) {
                    int j = 0;
                    Iterator<Integer> iterator = numericList.iterator();
                    while (iterator.hasNext() && j < i) {
                        int num = iterator.next();
                        if (num > numericList.get(i) && num != 0 && numericList.get(i) != 0) {
                            parity++;
                        }
                        j++;
                    }
                }

                // 有解条件：
                // column为奇数，则需初始与目标状态的逆序数奇偶性相同
                // column为偶数，则需初始与目标状态的逆序数奇偶性相同且空格位置为偶数，或逆序数奇偶性不同且空格位置为奇数
                // 目标状态：1234...0（逆序数为偶数）
                if (column % 2 == 1) {
                    canSolved = parity % 2 == 0;
                } else {
                    canSolved = (parity % 2 == 0 && (numericList.indexOf(0) + 1) % 2 == 0)
                            || (parity % 2 == 1 && (numericList.indexOf(0) + 1) % 2 == 1);
                }
            }

            node = new Node(numericList);
            if (!isCompleted(node.state)) {
                return node.state;
            }
        }
    }

    private void initAimState() {
        aimState = new int[column][column];
        for (int row = 0; row < column; row++) {
            for (int col = 0; col < column; col++) {
                int num = (row == column - 1 && col == column - 1) ? 0 : (row * column + col + 1);
                aimState[row][col] = num;
            }
        }
    }

    public boolean moveToNewState(int[][] oldState, int[] touchPos) {
        int[] blank = node.getPosition(0);
        if (blank != null) {
            int touchX = touchPos[0];
            int touchY = touchPos[1];
            int blankX = blank[0];
            int blankY = blank[1];
            int diffX = Math.abs(touchX - blankX);
            int diffY = Math.abs(touchY - blankY);
            if (diffX == 1 && diffY == 0 || diffX == 0 && diffY == 1) {
                int temp = oldState[touchX][touchY];
                oldState[touchX][touchY] = oldState[blankX][blankY];
                oldState[blankX][blankY] = temp;
                return true;
            }
        }
        return false;
    }

    public boolean isCompleted(@NonNull int[][] state) {
        for (int row = 0; row < column; row++) {
            for (int col = 0; col < column; col++) {
                if (state[row][col] != aimState[row][col]) {
                    return false;
                }
            }
        }
        return true;
    }

    private class Node {

        private int columnCount;
        private int[][] state;

        private Node(ArrayList<Integer> numericList) {
            columnCount = (int) Math.sqrt(numericList.size());
            state = new int[columnCount][columnCount];
            setState(numericList);
        }

        private void setState(ArrayList<Integer> numericList) {
            for (int row = 0; row < columnCount; row++) {
                for (int col = 0; col < columnCount; col++) {
                    int index = row * columnCount + col;
                    state[row][col] = numericList.get(index);
                }
            }
        }

        private int[] getPosition(int num) {
            for (int row = 0; row < columnCount; row++) {
                for (int col = 0; col < columnCount; col++) {
                    if (state[row][col] == num) {
                        return new int[]{row, col};
                    }
                }
            }
            return null;
        }
    }
}

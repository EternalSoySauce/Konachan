package com.ess.anime.wallpaper.model.helper;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import androidx.annotation.NonNull;

public class XPuzzleHelper implements Parcelable {

    private Node node;
    private int column;
    private int[][] aimState;

    public XPuzzleHelper() {
    }

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

    protected XPuzzleHelper(Parcel in) {
        node = in.readParcelable(Node.class.getClassLoader());
        column = in.readInt();
        aimState = (int[][]) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(node, flags);
        dest.writeInt(column);
        dest.writeSerializable(aimState);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<XPuzzleHelper> CREATOR = new Creator<XPuzzleHelper>() {
        @Override
        public XPuzzleHelper createFromParcel(Parcel in) {
            return new XPuzzleHelper(in);
        }

        @Override
        public XPuzzleHelper[] newArray(int size) {
            return new XPuzzleHelper[size];
        }
    };

    private static class Node implements Parcelable {

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

        protected Node(Parcel in) {
            columnCount = in.readInt();
            state = (int[][]) in.readSerializable();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(columnCount);
            dest.writeSerializable(state);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Node> CREATOR = new Creator<Node>() {
            @Override
            public Node createFromParcel(Parcel in) {
                return new Node(in);
            }

            @Override
            public Node[] newArray(int size) {
                return new Node[size];
            }
        };
    }

}

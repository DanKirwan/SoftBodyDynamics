package render.shader;

import java.util.Arrays;
import java.util.TreeSet;

class LineNumberMap {

    private Entry[] entries;

    private LineNumberMap(Entry[] entries) {
        this.entries = entries;
    }

    private Entry getEntry(int globalLine) {
        int index = Arrays.binarySearch(entries, new Entry(0, globalLine, null));
        if (index > 0)
            return entries[index];
        else if (index == -1)
            return entries[0];
        else
            return entries[-index - 2];
    }

    public String getActualFileName(int globalLine) {
        return getEntry(globalLine).fileName;
    }

    public int getActualLineNumber(int globalLine) {
        Entry entry = getEntry(globalLine);
        return globalLine - entry.globalLine + entry.localLine;
    }

    public void dump() {
        for (Entry entry : entries) {
            System.out.println(entry.globalLine + " -> " + entry.fileName + ":" + entry.localLine);
        }
    }

    public static class Builder {
        private TreeSet<Entry> entries = new TreeSet<>();

        public void addEntry(int localLine, int globalLine, String fileName) {
            Entry entry = new Entry(localLine, globalLine, fileName);
            entries.remove(entry);
            entries.add(entry);
        }

        public LineNumberMap build() {
            return new LineNumberMap(entries.toArray(new Entry[0]));
        }
    }

    private static class Entry implements Comparable<Entry> {
        private int localLine;
        private int globalLine;
        private String fileName;

        public Entry(int localLine, int globalLine, String fileName) {
            this.localLine = localLine;
            this.globalLine = globalLine;
            this.fileName = fileName;
        }

        @Override
        public int compareTo(Entry other) {
            return Integer.compare(globalLine, other.globalLine);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            return globalLine == entry.globalLine;
        }

        @Override
        public int hashCode() {
            return globalLine;
        }
    }

}

package astechzgo.luminescent.sound;

public class Source {

    private final int bufferPointer, sourcePointer;

    public Source(int bufferPointer, int sourcePointer) {
        this.bufferPointer = bufferPointer;
        this.sourcePointer = sourcePointer;
    }

    public int getBufferPointer() {
        return bufferPointer;
    }

    public int getSourcePointer() {
        return sourcePointer;
    }
}

public class BarSet {
    public float len;
    public int num;

    public BarSet(float length, int number) {
        this.len = length;
        this.num = number;
    }

    public String toString() {
        return this.len + "x" + this.num;
    }
}

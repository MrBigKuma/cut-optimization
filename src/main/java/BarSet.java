/**
 * Object to hold bar len and its number
 */
public class BarSet {
    public Double len;
    public Integer num;

    public BarSet(Double length, Integer number) {
        this.len = length;
        this.num = number;
    }

    public String toString() {
        return this.len + "x" + this.num;
    }
}

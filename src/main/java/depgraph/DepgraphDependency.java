package depgraph;

public class DepgraphDependency {
    private String from;
    private String to;
    private int numericFrom;
    private int numericTo;
    private String resolution;

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getNumericFrom() {
        return numericFrom;
    }

    public int getNumericTo() {
        return numericTo;
    }

    public String getResolution() {
        return resolution;
    }

    @Override
    public String toString() {
        return "depgraph.DepgraphDependency{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", numericFrom=" + numericFrom +
                ", numericTo=" + numericTo +
                ", resolution='" + resolution + '\'' +
                '}';
    }
}

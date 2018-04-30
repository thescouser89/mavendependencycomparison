
public class Main {

    public static void main(String[] args) throws Exception {
        DependencyComparison comparison = new DependencyComparison("https://github.com/rest-assured/rest-assured",
                "rest-assured-3.0.1", "rest-assured-3.1.0");

        comparison.getNewDepsOnly();
        comparison.getOldDepsOnly();
        comparison.getUnchangedDeps();
        comparison.getUpdatedDeps();
    }
}

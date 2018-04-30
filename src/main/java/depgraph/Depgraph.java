package depgraph;

import java.util.List;

public class Depgraph {
    private String graphName;
    private List<DepgraphArtifact> artifacts;
    private List<DepgraphDependency> dependencies;

    public String getGraphName() {
        return graphName;
    }

    public List<DepgraphArtifact> getArtifacts() {
        return artifacts;
    }

    public List<DepgraphDependency> getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return "depgraph.Depgraph{" +
                "graphName='" + graphName + '\'' +
                ", artifacts=" + artifacts +
                ", dependencies=" + dependencies +
                '}';
    }
}

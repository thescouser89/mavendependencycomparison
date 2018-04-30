package depgraph;

import java.util.List;

public class DepgraphArtifact {

    private String id;
    private int numericId;
    private String groupId;
    private String artifactId;
    private String version;
    private List<String> scopes;
    private List<String> types;

    public String getId() {
        return id;
    }

    public int getNumericId() {
        return numericId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public List<String> getTypes() {
        return types;
    }

    public String getGav() {
        return groupId + ":" + artifactId + ":" + version + ":" + scopes;
    }

    @Override
    public String toString() {
        return "depgraph.DepgraphArtifact{" +
                "id='" + id + '\'' +
                ", numericId=" + numericId +
                ", groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", scopes=" + scopes +
                ", types=" + types +
                '}';
    }
}

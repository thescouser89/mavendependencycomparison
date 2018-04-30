import java.util.Objects;

public class GAVDependency {
    private String groupId;
    private String artifactId;
    private String version;
    private String from;

    public GAVDependency(String groupId, String artifactId, String version, String from) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.from = from;
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

    public String getFrom() {
        return from;
    }

    public String getGAV() {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GAVDependency gav = (GAVDependency) o;
        return Objects.equals(groupId, gav.groupId) &&
                Objects.equals(artifactId, gav.artifactId) &&
                Objects.equals(version, gav.version);
    }

    @Override
    public int hashCode() {

        return Objects.hash(groupId, artifactId, version);
    }
}

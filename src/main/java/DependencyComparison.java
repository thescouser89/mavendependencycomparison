import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import depgraph.Depgraph;
import depgraph.DepgraphArtifact;
import depgraph.DepgraphDependency;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.model.Model;
import com.google.common.collect.Sets;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyComparison {

    private String gitUrl;
    private String oldRef;
    private String newRef;

    private Map<String, GAVDependency> gaOldMap;
    private Map<String, GAVDependency> gaNewMap;

    public DependencyComparison(String gitUrl, String oldRef, String newRef) throws Exception {
        this.gitUrl = gitUrl;
        this.oldRef = oldRef;
        this.newRef = newRef;

        analyze();
    }

    public void getUpdatedDeps() {

        for (String ga : Sets.intersection(gaOldMap.keySet(), gaNewMap.keySet())) {
            if (!gaOldMap.get(ga).getVersion().equals(gaNewMap.get(ga).getVersion())) {
                System.out.println("---- " + ga + " ----");
                System.out.println("Old: " + gaOldMap.get(ga).getVersion());
                System.out.println("New: " + gaNewMap.get(ga).getVersion());
            }
        }
    }

    public void getUnchangedDeps() {

        System.out.println("--- Unchanged GA ---");
        for (String ga : Sets.intersection(gaOldMap.keySet(), gaNewMap.keySet())) {
            if (gaOldMap.get(ga).getVersion().equals(gaNewMap.get(ga).getVersion())) {
                System.out.println(gaOldMap.get(ga).getGAV() + " :: " + gaOldMap.get(ga).getFrom());
            }
        }
    }

    public void getOldDepsOnly() {

        System.out.println("--- Old GA not in new GA ---");
        for (String ga : Sets.difference(gaOldMap.keySet(), gaNewMap.keySet())) {
            System.out.println(ga + ":" + gaOldMap.get(ga).getVersion() + " :: " + gaOldMap.get(ga).getFrom());
        }
    }

    public void getNewDepsOnly() {

        System.out.println("--- New GA not in old GA ---");
        for (String ga : Sets.difference(gaOldMap.keySet(), gaNewMap.keySet())) {
            System.out.println(ga + ":" + gaNewMap.get(ga).getVersion() + " :: " + gaNewMap.get(ga).getFrom());
        }
    }

    private void analyze() throws Exception {

        File file = Files.createTempDir();

        String pathFilename = file.getAbsolutePath();
        String repository = pathFilename + "/repository";

        Git git = clone(this.gitUrl, repository);

        checkout(git, this.oldRef);
        getDependencies(repository);

        String groupId;

        groupId = getGroupId(repository);
        Set<GAVDependency> oldGAVs = parseJson(repository, groupId);

        checkout(git, this.newRef);
        getDependencies(repository);

        groupId = getGroupId(repository);

        Set<GAVDependency> newGAVs = parseJson(repository, groupId);

        gaOldMap = generateGaMap(oldGAVs);
        gaNewMap = generateGaMap(newGAVs);
    }

    /**
     * @param gitUrl    Git URL to clone
     * @param directory directory where to clone the repository. Must not exist in the filesystem
     *                  <p>
     *                  TODO: replace println with logger
     */
    private Git clone(String gitUrl, String directory) throws Exception {

        System.out.println("Cloning repository");

        Git git = Git.cloneRepository()
                .setURI(gitUrl)
                .setDirectory(new File(directory))
                .call();

        System.out.println("Cloning done!");

        return git;
    }

    private void checkout(Git git, String ref) throws Exception {

        if (ref != null) {
            System.out.println("Checking out ref");
            git.checkout().setName(ref).call();
            System.out.println("Checking out ref done!");
        }
    }

    private void getDependencies(String directory) throws Exception {

        MavenCli cli = new MavenCli();

        List<String> args = new ArrayList<String>();

        args.add("mvn");
        args.add("-DgraphFormat=json");
        args.add("-DskipTests=true");
        args.add("compile");
        args.add("com.github.ferstl:depgraph-maven-plugin:3.0.1:aggregate");
        args.add("-f");
        args.add(directory + "/pom.xml");

        ProcessBuilder pb = new ProcessBuilder(args);
        Process p = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String readLine;

        // Print output
        while ((readLine = reader.readLine()) != null) {
            System.out.println(readLine);
        }

        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Program failed with exit status: " + exitCode);
        }

    }

    private String getGroupId(String directory) throws Exception {

        Model model = null;
        FileReader reader = null;
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();

        try {

            reader = new FileReader(new File(directory + "/pom.xml"));
            model = mavenReader.read(reader);
            model.setPomFile(new File(directory + "/pom.xml"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        MavenProject project = new MavenProject(model);
        return project.getGroupId();
    }

    private Set<GAVDependency> parseJson(String directory, String groupId) throws Exception {

        String path = directory + "/target/dependency-graph.json";

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Depgraph depgraph = mapper.readValue(new File(path), Depgraph.class);

        Map<String, DepgraphArtifact> artifactMap = new HashMap<String, DepgraphArtifact>();

        for (DepgraphArtifact artifact : depgraph.getArtifacts()) {
            artifactMap.put(artifact.getId(), artifact);
        }

        Set<GAVDependency> dependencies = new HashSet<GAVDependency>();

        for (DepgraphDependency dependency : depgraph.getDependencies()) {

            DepgraphArtifact to = artifactMap.get(dependency.getTo());
            DepgraphArtifact from = artifactMap.get(dependency.getFrom());

            if (to.getGroupId().equals(groupId)) {

                // do nothing

            } else if (!to.getScopes().contains("test") && !from.getScopes().contains("test")) {

                // lets ignore target dependencies

                dependencies.add(new GAVDependency(to.getGroupId(), to.getArtifactId(), to.getVersion(),
                        artifactMap.get(dependency.getFrom()).getGav()));

            }
        }

        return dependencies;
    }

    private static Map<String, GAVDependency> generateGaMap(Set<GAVDependency> gavs) {

        Map<String, GAVDependency> gaMap = new HashMap<String, GAVDependency>();

        for (GAVDependency gav : gavs) {
            gaMap.put(gav.getGroupId() + ":" + gav.getArtifactId(), gav);
        }

        return gaMap;
    }

}

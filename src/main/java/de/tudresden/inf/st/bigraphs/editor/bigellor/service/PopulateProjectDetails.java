package de.tudresden.inf.st.bigraphs.editor.bigellor.service;

import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.DomainUtils;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.Project;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import static de.tudresden.inf.st.bigraphs.editor.bigellor.service.PopulateProjectDetails.CurrentDirectoryFlag.*;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SIBLINGS;

public class PopulateProjectDetails extends SimpleFileVisitor<Path> {
    Project project;

    enum CurrentDirectoryFlag {
        PROCESSING_AGENTS, PROCESSING_RULES, PROCESSING_SIGNATURE, UNSPECIFIED
    }

    CurrentDirectoryFlag processingDirectory = UNSPECIFIED;

    public PopulateProjectDetails(String projectName) {
        this(new Project(projectName));
    }

    public PopulateProjectDetails(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        System.out.format("PRE Directory: %s%n", dir);
        String directoryName = dir.toFile().getName();
        if(directoryName.equalsIgnoreCase(String.valueOf(project.getProjectId()))) {
            project.setCreatedDate(new Date(attrs.creationTime().toMillis()));
            project.setModifiedDate(new Date(attrs.lastModifiedTime().toMillis()));
        }
        if (directoryName.equalsIgnoreCase(ProjectFileLocationService.RESOURCES_DIR_AGENTS)) {
            processingDirectory = PROCESSING_AGENTS;
        }
        if (directoryName.equalsIgnoreCase(ProjectFileLocationService.RESOURCES_DIR_RULES)) {
            processingDirectory = PROCESSING_RULES;

        }
//        if (directoryName.equalsIgnoreCase(ProjectFileLocationService.RESOURCES_FILE_PROJECTNAME)) {
//            processingDirectory = UNSPECIFIED;
//        }
        return CONTINUE;
    }

    /**
     * List details about the file
     *
     * @param file a reference to the file
     * @param attr the file's basic attributes
     * @return result
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
//        if (attr.isSymbolicLink()) {
//            System.out.format("Symbolic link: %s ", file);
//        } else if (attr.isRegularFile()) {
//            System.out.format("Regular file: %s ", file);
//        } else {
//            System.out.format("Other: %s ", file);
//        }
//        System.out.println("(" + attr.size() + "bytes)");
        switch (processingDirectory) {
            case PROCESSING_AGENTS:
                System.out.println("Procc agents"); //TODO create modelentities
                break;
            case PROCESSING_RULES:
                System.out.println("Procc rules");
                break;
            case UNSPECIFIED:
                if (attr.isRegularFile() &&
                        file.toFile().getName().equalsIgnoreCase(ProjectFileLocationService.RESOURCES_FILE_PROJECTNAME)) {
                    String projectName = DomainUtils.readProjectFile(file.toFile().getAbsolutePath());
                    if (!projectName.isBlank() || !projectName.isEmpty()) {
                        project.setName(projectName);
                    }
                }
                break;

            default:
                return CONTINUE;
        }
        return CONTINUE;
    }

    // Print each directory visited.
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        System.out.format("POST Directory: %s%n", dir);
        processingDirectory = UNSPECIFIED;
        return CONTINUE;
    }

    //    If there is a problem reading the file, the user should be informed, otherwise while not override this method,
//    an IOException will be thrown without the possibility to handle the case.
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.err.println(exc);
        return CONTINUE;
    }
}
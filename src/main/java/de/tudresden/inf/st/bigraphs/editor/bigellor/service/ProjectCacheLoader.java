package de.tudresden.inf.st.bigraphs.editor.bigellor.service;

import com.google.common.cache.CacheLoader;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.NewProjectDTO;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.Project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//TODO eviction by folderlistener
public class ProjectCacheLoader extends CacheLoader<String, Project> {

    ProjectFileLocationService pService;

    public ProjectCacheLoader(ProjectFileLocationService pService) {
        this.pService = pService;
    }

    @Override
    public Project load(String projectName) throws Exception {
        Path startingDir = Paths.get(pService.getProjectFolder(projectName));
        PopulateProjectDetails pf = new PopulateProjectDetails(projectName);
        Files.walkFileTree(startingDir, pf);

        Project project = pf.getProject();

        if (!pService.projectExistsInRepository(projectName)) {
            // Create and update respective DTO
            NewProjectDTO newProjectDTO = new NewProjectDTO();
//            newProjectDTO.setNewProjectId(projectId);
            newProjectDTO.setProjectName(project.getName());
            newProjectDTO.setCreatedDate(project.getCreatedDate());
            newProjectDTO.setProjectStatus(NewProjectDTO.Status.SAVED);
            if (project.getBigraphs().size() > 0)
                newProjectDTO.setModelStorageEntityId(project.getBigraphs().get(0).getModelStorageId());
            newProjectDTO = pService.save(newProjectDTO);
            project.setProjectId(newProjectDTO.getNewProjectId());
        }
        return project;
    }
}

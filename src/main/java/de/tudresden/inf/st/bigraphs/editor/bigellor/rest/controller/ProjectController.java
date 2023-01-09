package de.tudresden.inf.st.bigraphs.editor.bigellor.rest.controller;

import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.ModelEntity;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.NewProjectDTO;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.SignatureEntity;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.ModelStorageRepository;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.conversion.cytoscape.BBigraph2CytoscapeJSON;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.conversion.cytoscape.CytoscapeJSON2BBigraph;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.AbstractController;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.response.ModelUploadedResponse;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.response.ProjectSavedResponse;
import de.tudresden.inf.st.bigraphs.editor.bigellor.service.BigraphModelFileStorageService;
import de.tudresden.inf.st.bigraphs.editor.bigellor.service.DownloadUtils;
import de.tudresden.inf.st.bigraphs.editor.bigellor.service.ProjectFileLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static de.tudresden.inf.st.bigraphs.editor.bigellor.service.ProjectFileLocationService.RESOURCES_DIR_AGENTS;

//TODO newbigraph: navbar etc., set current modelentity

/**
 * Rest controller for managing all project-related tasks on the UI:
 * loading and storing projects and corresponding models.
 */
//@Controller
@RestController
public class ProjectController extends AbstractController {

    public static final String URL_DOWNLOAD_BIGRAPH = "/projects/{id}/download/bigraph";

    @Deprecated
    @Autowired
    BigraphModelFileStorageService bigraphModelFileStorageService;

    @Deprecated
    @Autowired
    ModelStorageRepository modelStorageRepository;

    @ModelAttribute("availableControls")
    public List<String> populateAvailableControlLabels(NewProjectDTO newProjectDTO) {
        try {
            SignatureEntity currentSignatureEntity = signatureService.findById(newProjectDTO.getSigId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid signature id:" + newProjectDTO.getSigId()));
            return currentSignatureEntity.getControlEntityList().stream().map(x -> x.getCtrlLbl()).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }


    /**
     * Utilizes the Post-Redirect-Get Pattern.
     *
     * @param selectedSigIds
     * @param newProjectDTO
     * @param bindingResult
     * @param redirectAttributes
     * @param modelAndView
     * @param model
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/projects/new", method = {RequestMethod.POST})
    public ModelAndView createNewProject(
            @RequestParam(value = "optionSig", required = false) long[] selectedSigIds,
            @ModelAttribute(name = "newProjectDTO") @Valid NewProjectDTO newProjectDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            ModelAndView modelAndView,
            Model model,
            ModelMap modelMap
    ) {
        // On errors ...
        if (bindingResult.hasErrors() || selectedSigIds == null) {
            if (selectedSigIds == null) {
                modelAndView.addObject("sigNotSelectedError", "No signature selected!");
            }
            setDefaultModelViewObjects(modelAndView);
            modelAndView.addObject("newProjectDTO", newProjectDTO);
            modelAndView.addObject("content", "index");
            modelAndView.getModel().put("showProjectForm", true);
            return modelAndView;
        }

        // Sucess ...
        RedirectView redirectView = new RedirectView("/projects/edit", true);
        redirectView.setStatusCode(HttpStatus.SEE_OTHER);
        modelAndView.setView(redirectView);

        if (selectedSigIds.length != 0) {
            newProjectDTO.setSigId(selectedSigIds[0]);
        }

        try {
            projectFileLocationService.prepareNewProjectEntry(newProjectDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        setDefaultModelViewObjects(modelAndView);
//        modelAndView.getModel().put("content", "new-project-diagram");
//        modelAndView.addObject("content", "new-project-diagram");
        redirectAttributes.addAttribute("id", newProjectDTO.getNewProjectId());
        redirectAttributes.addFlashAttribute("id", newProjectDTO.getNewProjectId());
//        redirectView.setExposeModelAttributes(false);
        return modelAndView;
    }

    /**
     * Main entry point when working with a project to edit a bigraph model.
     * <p>
     * A ModelEntity has to be set in NewProjectDTO
     * The bigraph model is then loaded, converted as JSON, and passed to the thymeleaf template via an attribute to be
     * loaded by cytoscape.
     */
    @RequestMapping(value = "/projects/edit", method = {RequestMethod.GET})
    public ModelAndView createNewProjectGet(ModelAndView modelAndView,
                                            @RequestParam(name = "id") long newProjId,
                                            @ModelAttribute(name = "specialContent") String specialContent) {
        setDefaultModelViewObjects(modelAndView);
//        long id = model.asMap().get("id") != null ? (long) model.asMap().get("id") : newProjId;
        long id = modelAndView.getModelMap().get("id") != null ? (long) modelAndView.getModelMap().get("id") : newProjId;

        ProjectFileLocationService.LoadedModelResult result = null;
        try {
            result = projectFileLocationService.loadModelById(
                    newProjId,
                    -1
            );
            PureBigraph loadedBigraph = result.getBigraph(); // Can only be a bigraph in this state

            BBigraph2CytoscapeJSON ecore2JSON = new BBigraph2CytoscapeJSON();
            String json = ecore2JSON.parseBigraph(loadedBigraph);
            if (json != null && !json.isEmpty()) {
                BBigraph2CytoscapeJSON.prettyFormat(json);
                modelAndView.getModel().put("currentElementsBigraph", json.replaceAll("\\\\", "").replaceAll("[\\r\\n]+", ""));
            }

            modelAndView.getModel().put("newProjectDTO", result.getNewProjectDTO());
            modelAndView.getModel().put("content", "new-project-diagram");
            modelAndView.getModel().put("currentSignatureEntity", result.getSignatureEntity());
        } catch (Exception e) {
            e.printStackTrace();
//                throw new RuntimeException(e);
            modelAndView.getModel().put("currentElementsBigraph", "undefined");
        }

        if (result != null) {
            // Find all related model storage entities that is referenced to this project id
            // and filtered by the type of the model: here we want only bigraphs
            // Each entry of the ModelEntity list is initialized with the correct download path
            List<ModelEntity> storedModelEntities = modelStorageRepository.findModelEntitiesByProjectIdAndModelType(id, ModelEntity.ModelType.BIGRAPH);
            for (ModelEntity each : storedModelEntities) {
                Path path = bigraphModelFileStorageService.load(
                        projectFileLocationService.getProjectFolder(result.getNewProjectDTO().getProjectName()),
                        each.getFileName()
                );
                String downloadURL = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path(URL_DOWNLOAD_BIGRAPH.replace("{id}", "" + newProjId))
                        .queryParam("filename", path.getFileName().toString())
                        .toUriString();
                each.setDownloadUrl(downloadURL);
            }
            modelAndView.getModel().put("files", storedModelEntities);
        }

        return modelAndView;
    }

    /**
     * Stores the current bigraph that is edited to the filesystem and updates all required repositories.
     *
     * @param newProjId        the project id
     * @param bigraphJsonModel the bigraph model as GraphML from Cytoscape.js
     * @return if save operation is successful, {@link ProjectSavedResponse} is returned,
     * otherwise an "unprocessableEntity" response entity
     */
    @RequestMapping(value = "/projects/save/{id}",
            method = {RequestMethod.POST},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<Object> saveProject(@PathVariable(name = "id") long newProjId, @RequestBody String bigraphJsonModel) {
//        System.out.println(newProjId);
        System.out.println("bigraphJsonModel= " + bigraphJsonModel);
        try {
            CytoscapeJSON2BBigraph converter = new CytoscapeJSON2BBigraph(bigraphJsonModel);
            PureBigraph bigraph = converter.convert();
//            BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);

            String filename = projectFileLocationService.update(newProjId, bigraph);
            return ResponseEntity.ok(new ProjectSavedResponse(newProjId, filename));
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }

    //TODO move to MMController
    @PostMapping("/upload/model/bigraph/{id}")//TODO change url as below
    public ModelUploadedResponse uploadFile(@RequestParam("file") MultipartFile file,
                                            @PathVariable(name = "id") long newProjId) {
        String fileName = projectFileLocationService.uploadBigraphModel(newProjId, file);

        String uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(URL_DOWNLOAD_BIGRAPH)
                .queryParam("filename", fileName)
                .toUriString();
        return new ModelUploadedResponse(fileName, uri, file.getContentType(), file.getSize());
    }

    //    @GetMapping(URL_DOWNLOAD_BIGRAPH)
    @GetMapping("/projects/{id}/download/bigraph")
    public ResponseEntity<Resource> downloadFile(@RequestParam("filename") String fileName,
                                                 @PathVariable(name = "id") long newProjId,
                                                 HttpServletRequest request) {
        Resource resource;
        if (fileName != null && !fileName.isEmpty() && projectFileLocationService.getById(newProjId).isPresent()) {
            try {
                resource = DownloadUtils.loadFileAsResource(
                        Paths.get(projectFileLocationService.getProjectFolder(projectFileLocationService.getById(newProjId).get().getProjectName()), RESOURCES_DIR_AGENTS).toAbsolutePath().toString(),
                        fileName);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.notFound().build();
            }

            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                //logger.info("Could not determine file type.");
            } finally {
                // Fallback
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

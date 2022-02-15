package de.tudresden.inf.st.bigraphs.editor.bigellor.rest.controller;

import de.tudresden.inf.st.bigraphs.core.BigraphFileModelManagement;
import de.tudresden.inf.st.bigraphs.core.BigraphFileModelManagement.*;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.*;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.conversion.cytoscape.BBigraph2CytoscapeJSON;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.conversion.cytoscape.CytoscapeJSON2BBigraph;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.AbstractController;
import de.tudresden.inf.st.bigraphs.editor.bigellor.BigraphModelFileStorageService;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.ModelStorageRepository;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.response.ModelUploadedResponse;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.response.ProjectSavedResponse;
import de.tudresden.inf.st.spring.data.cdo.CdoTemplate;
import nu.xom.ParsingException;
import org.eclipse.emf.ecore.EPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.createOrGetBigraphMetaModel;
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureBuilder;

@Controller
@RestController
public class NewProjectController extends AbstractController {

    public static final String URL_DOWNLOAD_BIGRAPH = "/projects/download/bigraph";

    @Autowired
    CdoTemplate cdoTemplate;

    @Autowired
    BigraphModelFileStorageService bigraphModelFileStorageService;

    @Autowired
    ModelStorageRepository modelStorageRepository;

    @ModelAttribute("availableControls")
    public List<String> populateAvailableControlLabels(NewProjectDTO newProjectDTO) {
        try {
            SignatureEntity currentSignatureEntity = signatureEntityRepository.findById(newProjectDTO.getSigId())
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

        // Create empty bigraph model and store it first
        SignatureEntity currentSignatureEntity = signatureEntityRepository.findById(newProjectDTO.getSigId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid signature id:" + newProjectDTO.getSigId()));
        DefaultDynamicSignature dynamicSignature = SignatureEntity.convert(currentSignatureEntity);
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(dynamicSignature);
        builder.createRoot().addSite();
        ModelStorageEntity modelStorageEntity = new ModelStorageEntity();
        modelStorageEntity.setModelType(ModelStorageEntity.ModelType.BIGRAPH);
        modelStorageRepository.save(modelStorageEntity); // save it now to generate an ID
        // Save the stub bigraph model and attach it the the freshly created project
        PureBigraph bigraph = builder.createBigraph();
        String filename = bigraphModelFileStorageService.storeModel(bigraph, newProjectDTO.getProjectName() + "_agent_" +
                modelStorageEntity.getModelStorageId() + ".xmi");
        modelStorageEntity.setFileName(filename);
        newProjectDTO.setCreatedDate(new Date());
        newProjectDTO.setModelStorageEntityId(modelStorageEntity.getModelStorageId());
        newProjectDTORepository.save(newProjectDTO);
        modelStorageEntity.setProjectId(newProjectDTO.getNewProjectId());
        modelStorageRepository.save(modelStorageEntity);

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
     */
    @RequestMapping(value = "/projects/edit", method = {RequestMethod.GET})
    public ModelAndView createNewProjectGet(ModelAndView modelAndView,
                                            @RequestParam(name = "id") long newProjId,
                                            @ModelAttribute(name = "specialContent") String specialContent,
                                            Model model) {
        setDefaultModelViewObjects(modelAndView);
        long id = model.asMap().get("id") != null ? (long) model.asMap().get("id") : newProjId;

        NewProjectDTO newProjectDTO = newProjectDTORepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid new project id:" + id));

        SignatureEntity currentSignatureEntity = signatureEntityRepository.findById(newProjectDTO.getSigId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid signature id:" + newProjectDTO.getSigId()));
        modelAndView.getModel().put("newProjectDTO", newProjectDTO);
        modelAndView.getModel().put("content", "new-project-diagram");
        modelAndView.getModel().put("currentSignatureEntity", currentSignatureEntity);

        // Check if ModelStorageEntity is set in newProjectDTO
        // If yes then load this bigraph model, convert it as JSON, and pass it to the template via an attribute to be
        // loaded by cytoscape
        if (newProjectDTO.getModelStorageEntityId() > 0L) {
            ModelStorageEntity byId = modelStorageRepository.findById(newProjectDTO.getModelStorageEntityId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid model storage entity id id:" + newProjectDTO.getModelStorageEntityId()));
            Path pathLoaded = bigraphModelFileStorageService.load(byId.getFileName());
            DefaultDynamicSignature sig = SignatureEntity.convert(currentSignatureEntity);
            EPackage orGetBigraphMetaModel = createOrGetBigraphMetaModel(sig);
            PureBigraphBuilder<DefaultDynamicSignature> defaultDynamicSignaturePureBigraphBuilder =
                    PureBigraphBuilder.create(sig, orGetBigraphMetaModel, pathLoaded.toString());
            PureBigraph loadedBigraph = defaultDynamicSignaturePureBigraphBuilder.createBigraph();
            BBigraph2CytoscapeJSON ecore2JSON = new BBigraph2CytoscapeJSON();
            try {
                BigraphFileModelManagement.Store.exportAsInstanceModel(loadedBigraph, System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String json = ecore2JSON.parseBigraph(loadedBigraph);
            if (json != null && !json.isEmpty()) {
                BBigraph2CytoscapeJSON.prettyFormat(json);
                modelAndView.getModel().put("currentElementsBigraph", json.replaceAll("\\\\", "").replaceAll("[\\r\\n]+", ""));
            }
        } else {
            modelAndView.getModel().put("currentElementsBigraph", "undefined");
        }

        // Find all related model storage entities that is referenced to this project id
        // and filtered by the type of the model: here we want only bigraphs
        // Each entry of the ModelStorageEntity list is initialized with the correct download path
        List<ModelStorageEntity> storedModelEntities = modelStorageRepository.findModelStorageEntitiesByProjectIdAndModelType(id, ModelStorageEntity.ModelType.BIGRAPH);
        for (ModelStorageEntity each : storedModelEntities) {
            Path path = bigraphModelFileStorageService.load(each.getFileName());
            String downloadURL = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(URL_DOWNLOAD_BIGRAPH)
                    .queryParam("filename", path.getFileName().toString())
                    .toUriString();
            each.setDownloadUrl(downloadURL);
        }
        modelAndView.getModel().put("files", storedModelEntities);


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
            BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);
            NewProjectDTO newProjectDTO = newProjectDTORepository.findById(newProjId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid project id:" + newProjId));
            ModelStorageEntity modelStorageEntity = modelStorageRepository.findById(newProjectDTO.getModelStorageEntityId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid model storage entity id:" + newProjectDTO.getModelStorageEntityId()));
            String filename = bigraphModelFileStorageService.storeModel(bigraph, modelStorageEntity.getFileName());
            return ResponseEntity.ok(new ProjectSavedResponse(newProjectDTO.getNewProjectId(), filename));
        } catch (ParsingException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }


    }

    @PostMapping("/upload/model/bigraph/{id}")
    public ModelUploadedResponse uploadFile(@RequestParam("file") MultipartFile file,
                                            @PathVariable("id") long newProjId) {


        String fileName = bigraphModelFileStorageService.storeModel(file);
        String uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(URL_DOWNLOAD_BIGRAPH)
                .queryParam("filename", fileName)
                .toUriString();
        ModelStorageEntity modelStorageEntity = new ModelStorageEntity();
        modelStorageEntity.setProjectId(newProjId);
        modelStorageEntity.setFileName(fileName);
        modelStorageEntity.setModelType(ModelStorageEntity.ModelType.BIGRAPH);
        modelStorageEntity.setUploadFolder(bigraphModelFileStorageService.bigraphModelStorageLocation.toString());
        modelStorageRepository.save(modelStorageEntity);

        //TODO get project and renew the current modelStorageId
        // here...modelStorageEntityId
        NewProjectDTO newProjectDTO = newProjectDTORepository.findById(newProjId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid new project id:" + newProjId));
        newProjectDTO.setModelStorageEntityId(modelStorageEntity.getModelStorageId());
        newProjectDTORepository.save(newProjectDTO);
        return new ModelUploadedResponse(fileName, uri, file.getContentType(), file.getSize());
    }

    @GetMapping(URL_DOWNLOAD_BIGRAPH)
    public ResponseEntity<Resource> downloadFile(@RequestParam("filename") String fileName,
                                                 HttpServletRequest request) {
        Resource resource = null;
        if (fileName != null && !fileName.isEmpty()) {
            try {
                resource = bigraphModelFileStorageService.loadFileAsResource(fileName);
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

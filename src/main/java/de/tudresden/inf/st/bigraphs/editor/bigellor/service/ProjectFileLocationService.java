package de.tudresden.inf.st.bigraphs.editor.bigellor.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.*;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.ModelStorageRepository;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.NewProjectDTORepository;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

/**
 * A service facade for project-relevant activities.
 * Encapsulates access to various repositories needed to access project and model information.
 * <p>
 * This service class is used by REST controllers and other external business processes that
 * shall not have access to the internal details how projects and models are managed.
 *
 * @author Dominik Grzelak
 */
@Service
public class ProjectFileLocationService {

    //TODO read from config file
    public static final String RESOURCES_DIR_PROJECT = Paths.get("data/projects/").toAbsolutePath().toString();
    public static final String RESOURCES_DIR_AGENTS = "agents";
    public static final String RESOURCES_DIR_RULES = "rules";

    public static final String RESOURCES_DIR_SIGNATURES = "signature";
    public static final String RESOURCES_FILE_PROJECTNAME = "name";

    @Autowired
    BigraphModelFileStorageService modelStorageService;

    @Autowired
    SignatureFileStorageService signatureFileStorageService;
    @Autowired
    ModelStorageRepository modelStorageRepository;
    @Autowired
    NewProjectDTORepository newProjectDTORepository;

    ProjectCacheLoader projectCacheLoader = new ProjectCacheLoader(this);

    LoadingCache<String, Project> projectCache;
    //TODO use cache: read only invalidated project files

    /**
     * important for existing id population
     *
     * @throws IOException
     */
    public void initProjects() throws IOException {
        initCache();
        Files.createDirectories(Paths.get(RESOURCES_DIR_PROJECT));
        Files.list(Paths.get(RESOURCES_DIR_PROJECT)).forEach(x -> {
            if (x.toFile().isDirectory()) {
                String projectName = x.toFile().getName();
                try {
                    Project unchecked = this.projectCache.getUnchecked(projectName);
                    System.out.println("Project loaded: " + unchecked.getProjectId());
//                    projectCacheLoader.load(projectId);
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                    //TODO check what error happend: sigNotfound: give warning and omit this project
                    e.printStackTrace();
                }
            }
        });
    }

    private void initCache() {
        if (this.projectCache == null) {
            this.projectCache = CacheBuilder.newBuilder().build(projectCacheLoader);
        }
    }

    public boolean projectDTOExistsInRepository(long projectId) {
        return newProjectDTORepository.findById(projectId).isPresent();
    }

    public boolean projectDTOExistsInRepository(String projectName) {
        return newProjectDTORepository.findByProjectName(projectName).isPresent();
    }

    protected String prepareNewProjectFolder(String projectName) throws Exception {
        Path newFile = Paths.get(RESOURCES_DIR_PROJECT, projectName);
        Files.createDirectories(newFile.getParent());
        Files.createDirectories(Paths.get(newFile.toString(), RESOURCES_DIR_AGENTS));
        Files.createDirectories(Paths.get(newFile.toString(), RESOURCES_DIR_RULES));
        Files.createDirectories(Paths.get(newFile.toString(), RESOURCES_DIR_SIGNATURES));
        File file = Paths.get(newFile.toString(), RESOURCES_FILE_PROJECTNAME).toFile();
        if (!file.exists()) {
            if (file.createNewFile()) {
                DomainUtils.writeProjectFile(file.getAbsolutePath(), projectName);
            }
        }
        return newFile.toAbsolutePath().toString();
    }

    public Iterable<Project> findAllProjects() {
        return projectCache.asMap().values();
    }

    public void prepareNewProjectEntry(final NewProjectDTO newProjectDTO) throws Exception {
        String projectLocation = prepareNewProjectFolder(newProjectDTO.getProjectName());
        newProjectDTO.setCreatedDate(new Date());

        // Signature
        SignatureEntity currentSignatureEntity = signatureFileStorageService.findById(newProjectDTO.getSigId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid signature id:" + newProjectDTO.getSigId()));
//        signatureFileStorageService.storeModel(
//                SignatureEntity.convert(currentSignatureEntity),
//                Paths.get(RESOURCES_DIR, RESOURCES_DIR_SIGNATURES),
//                currentSignatureEntity.getName()
//        );
        File sigFile = Paths.get(projectLocation, RESOURCES_DIR_SIGNATURES, currentSignatureEntity.getName()).toFile();
        if (!sigFile.exists()) {
            if (sigFile.createNewFile()) {
                DomainUtils.writeProjectFile(sigFile.getAbsolutePath(), currentSignatureEntity.getName());
            }
        }
        DefaultDynamicSignature dynamicSignature = SignatureEntity.convert(currentSignatureEntity);

        // Bigraph
        // Create empty bigraph model and store it first
        // Save the stub bigraph model and attach it the the freshly created project
        ModelEntity modelEntity = new ModelEntity();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(dynamicSignature);
        builder.createRoot().addSite();
        PureBigraph bigraph = builder.createBigraph();
        String filename = modelStorageService.storeModel(bigraph,
                Paths.get(projectLocation, RESOURCES_DIR_AGENTS),
                modelEntity.getModelStorageId() + ".xmi");
        modelEntity.setModelType(ModelEntity.ModelType.BIGRAPH);
        modelEntity = modelStorageRepository.save(modelEntity); // save it now to generate an ID
        modelEntity.setFileName(filename);
        modelEntity.setProjectId(newProjectDTO.getNewProjectId());
        modelEntity.setUploadFolder(Paths.get(projectLocation, RESOURCES_DIR_AGENTS).toString());
        modelStorageRepository.save(modelEntity);

        // Project DTO
        // Update projectDTO in repository
        newProjectDTO.setModelStorageEntityId(modelEntity.getModelStorageId());
        save(newProjectDTO);
    }

    public String getProjectFolder(String projectName) {
        Path newFile = Paths.get(RESOURCES_DIR_PROJECT, projectName);
        return newFile.toAbsolutePath().toString();
    }

    public Optional<NewProjectDTO> getById(long projectId) {
        return newProjectDTORepository.findById(projectId);
    }

    public LoadedModelResult loadBigraphModelbyFilename(long projectId, String bigraphModelFilename) throws Exception {
        NewProjectDTO newProjectDTO = newProjectDTORepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid new project id:" + projectId));
        SignatureEntity signatureEntity = signatureFileStorageService.findById(newProjectDTO.getSigId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid signature id:" + newProjectDTO.getSigId()));

        Resource resource = DownloadUtils.loadFileAsResource(
                Paths.get(getProjectFolder(newProjectDTO.getProjectName()), ProjectFileLocationService.RESOURCES_DIR_AGENTS).toString(),
                bigraphModelFilename);

        DefaultDynamicSignature sig = SignatureEntity.convert(signatureEntity);
        EPackage orGetBigraphMetaModel = createOrGetBigraphMetaModel(sig);
        PureBigraphBuilder<DefaultDynamicSignature> defaultDynamicSignaturePureBigraphBuilder = PureBigraphBuilder.create(sig, orGetBigraphMetaModel, resource.getFile().getAbsolutePath());
        PureBigraph demoBigraph = defaultDynamicSignaturePureBigraphBuilder.createBigraph();
        return LoadedModelResult.create().setSignatureEntity(signatureEntity).setBigraph(demoBigraph);
    }

    public LoadedModelResult loadRuleModelbyFilename(long projectId, String bigraphModelFilename) throws Exception {
        NewProjectDTO newProjectDTO = newProjectDTORepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid new project id:" + projectId));
        SignatureEntity signatureEntity = signatureFileStorageService.findById(newProjectDTO.getSigId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid signature id:" + newProjectDTO.getSigId()));
        //TODO
        return LoadedModelResult.create().setSignatureEntity(signatureEntity); //.setBigraph(demoBigraph);
    }

    /**
     * Loads an agent or rule specified by the modelId of an existing project.
     *
     * @param projectId the project id
     * @param modelId   use -1 to get the "current" assigned model of a project
     * @return
     * @throws Exception
     */
    public LoadedModelResult loadModelById(long projectId, long modelId) throws Exception {
        NewProjectDTO newProjectDTO = newProjectDTORepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid new project id:" + projectId));

        // load signature
        SignatureEntity currentSignatureEntity = signatureFileStorageService.findById(newProjectDTO.getSigId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid signature id:" + newProjectDTO.getSigId()));

        // load agents and rules if available
        modelId = modelId == -1 ? newProjectDTO.getModelStorageEntityId() : modelId;
        long finalModelId = modelId;
        ModelEntity byId = modelStorageRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid model storage entity id id:" + finalModelId));
        //TODO Check if agent or rule
        Path pathLoaded = modelStorageService.load(
                Paths.get(getProjectFolder(newProjectDTO.getProjectName()), RESOURCES_DIR_AGENTS).toString(),
                byId.getFileName()
        );
        DefaultDynamicSignature sig = SignatureEntity.convert(currentSignatureEntity);
        EPackage orGetBigraphMetaModel = createOrGetBigraphMetaModel(sig);
        PureBigraphBuilder<DefaultDynamicSignature> defaultDynamicSignaturePureBigraphBuilder =
                PureBigraphBuilder.create(sig, orGetBigraphMetaModel, pathLoaded.toString());
        PureBigraph loadedBigraph = defaultDynamicSignaturePureBigraphBuilder.createBigraph();

        return LoadedModelResult.create()
                .setBigraph(loadedBigraph)
                .setSignatureEntity(currentSignatureEntity)
                .setNewProjectDTO(newProjectDTO);
//        return loadedBigraph;
    }

    public String uploadBigraphModel(long newProjId, MultipartFile file) {
        NewProjectDTO newProjectDTO = newProjectDTORepository.findById(newProjId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid new project id:" + newProjId));

        String fileName = DownloadUtils.storeModel(
                Paths.get(getProjectFolder(newProjectDTO.getProjectName()), RESOURCES_DIR_AGENTS).toString(),
                file
        );
        ModelEntity modelEntity = new ModelEntity();
        modelEntity.setProjectId(newProjId);
        modelEntity.setFileName(fileName);
        modelEntity.setModelType(ModelEntity.ModelType.BIGRAPH);
        modelEntity.setUploadFolder(
                Paths.get(getProjectFolder(newProjectDTO.getProjectName()), RESOURCES_DIR_AGENTS).toString()
        );
        modelEntity = modelStorageRepository.save(modelEntity);
        newProjectDTO.setModelStorageEntityId(modelEntity.getModelStorageId());
        newProjectDTORepository.save(newProjectDTO);

        return fileName;
    }

    public NewProjectDTO save(NewProjectDTO projectDTO) {
        return newProjectDTORepository.save(projectDTO);
    }

    /**
     * Project must exist already in the repository.
     * Gets current model entity of the project and stores it to the filesystem
     *
     * @param newProjId project id
     * @param bigraph   the bigraph model
     * @return the filename of the bigraph model stored to the filesystem
     * @throws Exception e.g., project id does not exist
     */
    public String update(long newProjId, PureBigraph bigraph) throws Exception {
        NewProjectDTO newProjectDTO = newProjectDTORepository.findById(newProjId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id:" + newProjId));
        String location = prepareNewProjectFolder(newProjectDTO.getProjectName());
        ModelEntity modelEntity = modelStorageService.findById(newProjectDTO.getModelStorageEntityId()).get();
        String filename = modelStorageService.storeModel(
                bigraph,
                Paths.get(location, RESOURCES_DIR_AGENTS),
                modelEntity.getFileName()
        );

        return filename;
    }

    //TODO
    public String update(long newProjId, ReactionRule<PureBigraph> bigraph) throws Exception {
        return null;
    }

    public static class LoadedModelResult {
        NewProjectDTO newProjectDTO;
        SignatureEntity signatureEntity;
        PureBigraph bigraph;
        ReactionRule<PureBigraph> reactionRule;

        public static LoadedModelResult create() {
            return new LoadedModelResult();
        }

        public NewProjectDTO getNewProjectDTO() {
            return newProjectDTO;
        }

        public LoadedModelResult setNewProjectDTO(NewProjectDTO newProjectDTO) {
            this.newProjectDTO = newProjectDTO;
            return this;
        }

        public SignatureEntity getSignatureEntity() {
            return signatureEntity;
        }

        public LoadedModelResult setSignatureEntity(SignatureEntity signatureEntity) {
            this.signatureEntity = signatureEntity;
            return this;
        }

        public PureBigraph getBigraph() {
            return bigraph;
        }

        public LoadedModelResult setBigraph(PureBigraph bigraph) {
            this.bigraph = bigraph;
            return this;
        }

        public ReactionRule<PureBigraph> getReactionRule() {
            return reactionRule;
        }

        public LoadedModelResult setReactionRule(ReactionRule<PureBigraph> reactionRule) {
            this.reactionRule = reactionRule;
            return this;
        }
    }
}

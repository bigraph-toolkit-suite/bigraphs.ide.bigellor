package de.tudresden.inf.st.bigraphs.editor.bigellor.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.*;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.ModelStorageRepository;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.NewProjectDTORepository;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.SignatureEntityRepository;
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
import java.util.Optional;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.createOrGetBigraphMetaModel;
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureBuilder;

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
    public static final String RESOURCES_DIR = Paths.get("data/projects/").toAbsolutePath().toString();
    public static final String RESOURCES_DIR_AGENTS = "agents";
    public static final String RESOURCES_DIR_RULES = "rules";
    public static final String RESOURCES_FILE_PROJECTNAME = "name";

    @Autowired
    protected SignatureEntityRepository signatureEntityRepository;

    @Autowired
    BigraphModelFileStorageService modelStorageService;
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
        Files.list(Paths.get(RESOURCES_DIR)).forEach(x -> {
            if (x.toFile().isDirectory()) {
                String projectName = x.toFile().getName();
                try {
                    Project unchecked = this.projectCache.getUnchecked(projectName);
                    System.out.println("Project loaded: " + unchecked.getProjectId());
//                    projectCacheLoader.load(projectId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void initCache() {
        if (this.projectCache == null) {
            this.projectCache = CacheBuilder.newBuilder().build(projectCacheLoader);
        }
    }

    public boolean projectExistsInRepository(long projectId) {
        return newProjectDTORepository.findById(projectId).isPresent();
    }

    public boolean projectExistsInRepository(String projectName) {
        return newProjectDTORepository.findByProjectName(projectName).isPresent();
    }

    protected String prepareNewProjectFolder(String projectName) throws Exception {
        Path newFile = Paths.get(RESOURCES_DIR, projectName);
        Files.createDirectories(newFile.getParent());
        Files.createDirectories(Paths.get(newFile.toString(), RESOURCES_DIR_AGENTS));
        Files.createDirectories(Paths.get(newFile.toString(), RESOURCES_DIR_RULES));
        File file = Paths.get(newFile.toString(), RESOURCES_FILE_PROJECTNAME).toFile();
        if (!file.exists()) {
            if (file.createNewFile()) {
                DomainUtils.writeProjectFile(file.getAbsolutePath(), projectName);
            }
        }
        return newFile.toAbsolutePath().toString();
    }


    public void prepareNewProjectEntry(final NewProjectDTO newProjectDTO) throws Exception {
        // Create empty bigraph model and store it first
        SignatureEntity currentSignatureEntity = signatureEntityRepository.findById(newProjectDTO.getSigId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid signature id:" + newProjectDTO.getSigId()));
        DefaultDynamicSignature dynamicSignature = SignatureEntity.convert(currentSignatureEntity);
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(dynamicSignature);
        builder.createRoot().addSite();
        ModelEntity modelEntity = new ModelEntity();
        modelEntity.setModelType(ModelEntity.ModelType.BIGRAPH);
        modelEntity = modelStorageRepository.save(modelEntity); // save it now to generate an ID
        // Save the stub bigraph model and attach it the the freshly created project
        newProjectDTO.setCreatedDate(new Date());
        newProjectDTO.setModelStorageEntityId(modelEntity.getModelStorageId());
        save(newProjectDTO);
        String projectLocation = prepareNewProjectFolder(newProjectDTO.getProjectName());

        PureBigraph bigraph = builder.createBigraph();
        String filename = modelStorageService.storeModel(bigraph,
                Paths.get(projectLocation, RESOURCES_DIR_AGENTS),
                "agent_" + modelEntity.getModelStorageId() + ".xmi");
        modelEntity.setFileName(filename);
        modelEntity.setProjectId(newProjectDTO.getNewProjectId());
        modelEntity.setUploadFolder(Paths.get(projectLocation, RESOURCES_DIR_AGENTS).toString());
        modelStorageRepository.save(modelEntity);
    }

    public String getProjectFolder(String projectName) {
        Path newFile = Paths.get(RESOURCES_DIR, projectName);
        return newFile.toAbsolutePath().toString();
    }

    public Optional<NewProjectDTO> getById(long projectId) {
        return newProjectDTORepository.findById(projectId);
    }

    public LoadedModelResult loadBigraphModelbyFilename(long projectId, String bigraphModelFilename) throws Exception {
        NewProjectDTO newProjectDTO = newProjectDTORepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid new project id:" + projectId));
        SignatureEntity signatureEntity = signatureEntityRepository.findById(newProjectDTO.getSigId())
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

    /**
     * Loads an agent or rule specified by the modelId of an existing project.
     *
     * @param projectId the project id
     * @param modelId   use -1 to get the "current" assigned model of a project
     * @return
     * @throws Exception
     */
    public LoadedModelResult loadModel(long projectId, long modelId) throws Exception {
        NewProjectDTO newProjectDTO = newProjectDTORepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid new project id:" + projectId));
        // load signature
        SignatureEntity currentSignatureEntity = signatureEntityRepository.findById(newProjectDTO.getSigId())
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
//        if (projectDTO.getNewProjectId() < 0) {
//            projectDTO.setNewProjectId(newProjectDTORepository.count() + 1);
//        }
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
        ModelEntity modelEntity = modelStorageService.load(newProjectDTO.getModelStorageEntityId());
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

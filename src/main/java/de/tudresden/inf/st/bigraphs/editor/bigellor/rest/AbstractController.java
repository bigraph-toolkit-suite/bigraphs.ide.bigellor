package de.tudresden.inf.st.bigraphs.editor.bigellor.rest;

import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.DomainUtils;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.NewProjectDTO;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.NewProjectDTORepository;
import de.tudresden.inf.st.bigraphs.editor.bigellor.service.CDOServerService;
import de.tudresden.inf.st.bigraphs.editor.bigellor.service.ProjectFileLocationService;
import de.tudresden.inf.st.bigraphs.editor.bigellor.service.SignatureFileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractController {

    @Autowired
    protected CDOServerService cdoServerService;
    @Autowired
    protected SignatureFileStorageService signatureService;
    @Autowired
    protected ProjectFileLocationService projectFileLocationService;

    @Autowired
    @Deprecated
    protected NewProjectDTORepository newProjectDTORepository;
    @LocalServerPort
    private String port;
    protected void setDefaultModelViewContent(ModelAndView modelAndView) {
        modelAndView.addObject("content", "index");
    }
    protected void setDefaultModelViewObjects(ModelAndView modelAndView) {
        NewProjectDTO newProjectDTO = new NewProjectDTO();
        if (newProjectDTO.getProjectName() == null || newProjectDTO.getProjectName().isEmpty()) {
            newProjectDTO.setProjectName(DomainUtils.createPlaceholderName());
        }
        modelAndView.addObject("signatures", signatureService.findAll());
        modelAndView.addObject("newProjectDTO", newProjectDTO);
        modelAndView.setViewName("base");
    }

    /**
     * {@link AbstractController#setDefaultModelViewObjects(ModelAndView)}
     * @param req
     * @return
     */
    @ModelAttribute(name = "auxiliaryTemplateContentTop")
    protected String injectAuxiliaryTemplateContentTop(final HttpServletRequest req) {
        if (req.getRequestURL().toString().contains("/projects/edit")) //TODO <- this construct should be improved! Key-Value-Map
            return "new-project-diagram // new-project-diagram-script-block-top";
        return null;
    }

    @ModelAttribute(name = "auxiliaryTemplateContentBottom")
    protected String injectAuxiliaryTemplateContentBottom(final HttpServletRequest req) {
        if (req.getRequestURL().toString().contains("/projects/edit")) //TODO <- this construct should be improved!
            return "new-project-diagram // new-project-diagram-script-bottom";
        return null;
    }

    /**
     * {@link AbstractController#addContent(ModelAndView, String)}
     * @param modelAndView
     * @param content
     */
    public void addContent(ModelAndView modelAndView, String content) {
        modelAndView.addObject("content", content);
    }

    @ModelAttribute(name = "BigellorPort")
    public String bigellorServerPort() {
        return port;
    }

    @ModelAttribute(name = "BigellorHostIp")
    public String bigellorServerIp() {
        try {
            return InetAddress.getLoopbackAddress().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return "localhost";
        }
    }


    /**
     * All navbar items in {@code nav-header.html}.
     */
    static Map<String, Boolean> allNavItems = new LinkedHashMap<>();

    static {
        //TODO: das automatisieren und nav-header.html parsen... vlt über thymeleaf helper mgl? oder XOM?
        allNavItems.put("dropdown-item-load-demo-graph", false);
        allNavItems.put("dropdown-item-export-current-graph", false);
        allNavItems.put("dropdown-item-save-current-bigraph", false);
        allNavItems.put("navbarDropdown-edit-fitview", false);
        allNavItems.put("navbarDropdown-edit-only-centerview", false);
    }

    /**
     * Provides a simple mechanism for the navbar to specify for each page which options shall be enabled/disabled
     *
     * @param req
     * @return
     */
    @ModelAttribute(name = "navItems")
    protected Map<String, Boolean> navItems(final HttpServletRequest req) {
        Map<String, Boolean> tmp = new LinkedHashMap<>(allNavItems);


        if (req.getRequestURL().toString().contains("/projects/edit")) { //TODO <- this construct should be improved!
            tmp.replace("dropdown-item-load-demo-graph", true);
            tmp.replace("dropdown-item-save-current-bigraph", true);
            tmp.replace("dropdown-item-export-current-graph", true);
            tmp.replace("navbarDropdown-edit-fitview", true);
            tmp.replace("navbarDropdown-edit-only-centerview", true);
        }

        return tmp;
    }
}

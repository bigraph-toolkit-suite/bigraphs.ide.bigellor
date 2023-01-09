package de.tudresden.inf.st.bigraphs.editor.bigellor.rest.controller;

import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.AbstractController;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.ControlEntity;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.SignatureEntity;
import de.tudresden.inf.st.bigraphs.editor.bigellor.service.SignatureFileStorageService;
import org.json.JSONArray;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// https://stingh711.github.io/dynamic-forms-with-springmvc-and-thymeleaf.html
//@RestController
//@Controller
@RestController
//@Validated
@RequestMapping("/signatures")
public class SignatureController extends AbstractController {

    @ModelAttribute("allTypes")
    public List<ControlStatus> populateControlStatusTypes() {
        return Arrays.asList(ControlStatus.values());
    }


    @RequestMapping(value = "add", method = RequestMethod.GET)
    public ModelAndView addSignature(final SignatureEntity signatureEntity,
                                     ModelAndView modelAndView,
                                     HttpServletRequest request) {
//        modelAndView.addObject("signatureEntity", signatureEntity);
//        if (signatureEntity.getName() == null || signatureEntity.getName().isEmpty())
//        signatureEntity.setName("");
//        } else {
//            SignatureEntity sigEntity = (SignatureEntity) session.getAttribute("sigEntity");
//            sigEntity.getControlEntityList().add(new ControlEntity());
//        }
//        modelAndView.setViewName("add-signature");
//        modelAndView.getModel().put("content", "add-signature"); // would be visible in the URL when redicrecting
//        modelAndView.setViewName("base");
//        modelAndView.setViewName("redirect:/index");
//        modelAndView.setViewName("forward:/index");
//        RedirectView redirectView = new RedirectView("/index", true);
//        Map<String, Object> flashAttributes = new LinkedHashMap<>();
//        flashAttributes.put("content", "add-signature");
//        flashAttributes.put("signatureEntity", signatureEntity);
//        redir.addFlashAttribute("flashAttributesMap", flashAttributes); // not visible in the URL
//        redir.addAttribute("contentAttribute", "add-signature");
        super.setDefaultModelViewObjects(modelAndView);
//        modelAndView.getModel().put("content", "add-signature");
        super.addContent(modelAndView, "add-signature");
        modelAndView.getModel().put("signatureEntity", signatureEntity);

        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) {
            modelAndView.addObject("errorMsg", inputFlashMap.get("errorMsg"));
            if (inputFlashMap.get("errorMsg") != null) {
                modelAndView.addObject("signatureEntity", inputFlashMap.get("signatureEntity"));
            }
        }
//        modelAndView.setViewName("base");
        return modelAndView;
    }

    @RequestMapping(value = "add", params = {"add-control-field"})
    public ModelAndView addSignatureAddControl(SignatureEntity signatureEntity,
                                               BindingResult result,
                                               ModelAndView modelAndView,
                                               HttpSession session) {
//        SignatureEntity sigEntity = (SignatureEntity) session.getAttribute("sigEntity");
        modelAndView.addObject("signatureEntity", signatureEntity);
        signatureEntity.getControlEntityList().add(new ControlEntity());
//        modelAndView.setViewName("add-signature");
        setDefaultModelViewObjects(modelAndView);
        modelAndView.getModel().put("content", "add-signature");
//        modelAndView.getModel().put("showManageSignatureForm", true);
        modelAndView.setViewName("base");
        return modelAndView;
    }

    @RequestMapping(value = "/add", params = {"remove-control"})
    public ModelAndView addSignatureRemoveControl(
            final SignatureEntity signatureEntity,
            final BindingResult bindingResult,
            ModelAndView modelAndView,
//            Model model,
            final HttpServletRequest req) {
        final Integer rowId = Integer.valueOf(req.getParameter("remove-control"));
        signatureEntity.getControlEntityList().remove(rowId.intValue());
        setDefaultModelViewObjects(modelAndView);
        modelAndView.getModel().put("content", "add-signature");
        modelAndView.getModel().put("signatureEntity", signatureEntity);
//        modelAndView.getModel().put("showManageSignatureForm", true);
//        modelAndView.setViewName("base");
        return modelAndView;
    }


    /**
     * Is called from add-signature.html and served by {@link SignatureController#editSignature(long, ModelAndView, RedirectAttributes)}.
     * Model is used to pass data between controllers and views
     *
     * @param modelAndView
     * @param signatureEntity
     * @param result
     * @return
     */
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public ModelAndView addSignatureStoreSignatureEntity(ModelAndView modelAndView,
                                                         @Valid @ModelAttribute(name = "signatureEntity") SignatureEntity signatureEntity,
                                                         BindingResult result,
                                                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {//TODO
            redirectAttributes.addFlashAttribute("errorMsg", result.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("signatureEntity", signatureEntity);
            modelAndView.setView(new RedirectView("/signatures/add-errors", true));
//            modelAndView.setViewName("forward:/signatures/add-errors");
            return modelAndView; //"base"; // the template
        }

        signatureService.storeModel(
                signatureEntity,
                Paths.get(SignatureFileStorageService.RESOURCES_DIR_SIGNATURES),
                signatureEntity.getName()
        );
        signatureService.save(signatureEntity);
        modelAndView.setView(new RedirectView("/index"));
        return modelAndView;
    }

    @RequestMapping(value = "add-errors", method = RequestMethod.GET)
    public ModelAndView addSignatureHasErrors(ModelAndView modelAndView,
                                              HttpServletRequest request,
                                              RedirectAttributes redirectAttributes) {
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) {
            redirectAttributes.addFlashAttribute("errorMsg", inputFlashMap.get("errorMsg"));
            redirectAttributes.addFlashAttribute("signatureEntity", inputFlashMap.get("signatureEntity"));
        }

//        setDefaultModelViewObjects(modelAndView);
//        super.addContent(modelAndView, "add-signature");
        modelAndView.setView(new RedirectView("/signatures/add", true));
//        modelAndView.setViewName("forward:/signatures/add");
//        modelAndView.getModel().put("signatureEntity", signatureEntity);;
//        modelAndView.getModel().put("errorMsg", redirectAttributes.getFlashAttributes().get("errorMsg"));
//        return new ModelAndView("", new ModelMap());
        return modelAndView;
    }

    @RequestMapping("update/{id}")
    public ModelAndView updateSignature(@PathVariable("id") long id,
                                        ModelAndView modelAndView,
                                        HttpServletRequest request) {
        setDefaultModelViewObjects(modelAndView);
        modelAndView.addObject("content", "update-signature");

        // Loading existing signature
        SignatureEntity signatureEntity;
        try {
            signatureEntity = signatureService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid sig Id:" + id));
            modelAndView.addObject("signatureEntity", signatureEntity);
        } catch (IllegalArgumentException e) {
            modelAndView.addObject("errorMsg2", e.getMessage());
        }

        // Checking attributes in request
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) {
            modelAndView.addObject("errorMsg", inputFlashMap.get("errorMsg"));
            modelAndView.addObject("successMsg", inputFlashMap.get("successMsg"));
            if (inputFlashMap.get("signatureEntity") != null) {
                modelAndView.addObject("signatureEntity", inputFlashMap.get("signatureEntity"));
            }
        }

        return modelAndView;
    }


    @PostMapping("update/{id}")
    public ModelAndView updateSignature(@PathVariable("id") long id,
                                        @Valid @ModelAttribute(name = "signatureEntity") SignatureEntity signatureEntity,
                                        BindingResult result,
                                        ModelAndView modelAndView,
                                        HttpServletRequest r) {
        if (result.hasErrors()) {
//            modelAndView.addObject("signatureEntity", signatureEntity);
            r.setAttribute("signatureEntity", signatureEntity);
            r.setAttribute("errorMsg", result.getAllErrors().get(0).getDefaultMessage());
            modelAndView.setViewName("forward:/signatures/update/" + id + "/error");
            return modelAndView;
        }

        modelAndView.setViewName("forward:/signatures/update/" + id + "/success");
        return modelAndView;
    }

    @PostMapping("update/{id}/success")
    public ModelAndView updateSignatureSuccess(
            @Valid @ModelAttribute(name = "signatureEntity") SignatureEntity signatureEntity,
            RedirectAttributes ra) {
        //TODO a savecallback hook would be better for repo-save action
        signatureService.storeModel(
                signatureEntity,
                Paths.get(SignatureFileStorageService.RESOURCES_DIR_SIGNATURES),
                signatureEntity.getName()
        );
        signatureService.save(signatureEntity);

        ra.addFlashAttribute("successMsg", "Signature updated successfully.");
        return new ModelAndView(new RedirectView("/signatures/update/" + signatureEntity.getId(), true));
    }

    @PostMapping("update/{id}/error")
    public ModelAndView updateSignatureError(
            @ModelAttribute(name = "signatureEntity") SignatureEntity signatureEntity,
            HttpServletRequest request,
            RedirectAttributes ra) {
        ra.addFlashAttribute("errorMsg", request.getAttribute("errorMsg"));
        ra.addFlashAttribute("signatureEntity", signatureEntity);
        return new ModelAndView(new RedirectView("/signatures/update/" + signatureEntity.getId(), true));
    }

    @RequestMapping(value = "update/{id}", params = {"add-control-field"})
    public ModelAndView updateSignatureAddControl(@PathVariable("id") long id,
                                                  SignatureEntity signatureEntity,
                                                  ModelAndView modelAndView,
                                                  RedirectAttributes redir,
                                                  final HttpServletRequest req
    ) {
        setDefaultModelViewObjects(modelAndView);
        modelAndView.getModel().put("content", "update-signature");
        modelAndView.addObject("signatureEntity", signatureEntity);

        signatureEntity.getControlEntityList().add(new ControlEntity());

        return modelAndView;
    }

    @RequestMapping(value = "update/{id}", params = {"remove-control"})
    public ModelAndView updateSignatureRemoveControl(
            @PathVariable("id") long id,
            final SignatureEntity signatureEntity, final BindingResult bindingResult,
            ModelAndView modelAndView,
//            Model model,
            final HttpServletRequest req) {
        setDefaultModelViewObjects(modelAndView);
        modelAndView.getModel().put("content", "update-signature");
        modelAndView.addObject("signatureEntity", signatureEntity);

        final Integer rowId = Integer.valueOf(req.getParameter("remove-control"));
        signatureEntity.getControlEntityList().remove(rowId.intValue());

        return modelAndView;
    }

    @RequestMapping("delete/{id}")
    public ModelAndView deleteSignature(@PathVariable("id") long id, Model model, ModelAndView modelAndView) {
        SignatureEntity signatureEntity = signatureService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid signature Id:" + id));
        signatureService.delete(signatureEntity);
        signatureService.deleteModel(signatureEntity);
//        return "redirect:/index";
//        modelAndView.setView(new RedirectView("/index"));
        setDefaultModelViewObjects(modelAndView);
        modelAndView.addObject("content", "index");
        modelAndView.getModel().put("showManageSignatureForm", true);
        return modelAndView;
    }

    @RequestMapping(value = "convert/{id}/json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getSignatureAsJson(@PathVariable("id") long id) {
        try {
            SignatureEntity currentSignatureEntity = signatureService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid signature id:" + id));
            JSONArray array = new JSONArray(currentSignatureEntity.getControlEntityList());
            return array.toString();
        } catch (IllegalArgumentException e) {
            return new JSONArray().toString();
        }
    }
}

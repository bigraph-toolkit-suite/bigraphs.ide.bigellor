package de.tudresden.inf.st.bigraphs.editor.bigellor.rest.controller;

import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.AbstractController;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.ControlEntity;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.SignatureEntity;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.SignatureEntityRepository;
import de.tudresden.inf.st.bigraphs.editor.bigellor.service.SignatureFileStorageService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

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


    @RequestMapping(value = "add")
    public ModelAndView addSignature(final SignatureEntity signatureEntity,
                                     ModelAndView modelAndView,
                                     RedirectAttributes redir,
                                     HttpSession session) {
//        modelAndView.addObject("signatureEntity", signatureEntity);
        signatureEntity.setName("test");
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
        setDefaultModelViewObjects(modelAndView);
        modelAndView.getModel().put("content", "add-signature");
        modelAndView.getModel().put("signatureEntity", signatureEntity);
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
            final SignatureEntity signatureEntity, final BindingResult bindingResult,
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

    //Model is used to pass data between controllers and views
//    @PostMapping("/storesignature")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public ModelAndView addSignatureStoreSignatureEntity(ModelAndView modelAndView,
                                                         @Valid @ModelAttribute(name = "signatureEntity") SignatureEntity signatureEntity, BindingResult result,
                                                         Model model, HttpSession session) {
        if (result.hasErrors()) {//TODO
//            model.addAttribute("content", "add-signature");
//            modelAndView.setViewName("base");
            modelAndView.setView(new RedirectView("index")); //TODO: same page without redirect
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


    @RequestMapping("edit/{id}")
    public ModelAndView editSignature(@PathVariable("id") long id,
                                      ModelAndView modelAndView,
//                                                Model model,
                                      RedirectAttributes redir) {
        SignatureEntity signatureEntity = signatureService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sig Id:" + id));
        setDefaultModelViewObjects(modelAndView);
//        RedirectView redirectView = new RedirectView("/index", true);
//        Map<String, Object> flashAttributes = new LinkedHashMap<>();
        modelAndView.addObject("content", "edit-signature");
        modelAndView.addObject("signatureEntity", signatureEntity);
//        redir.addFlashAttribute("flashAttributesMap", flashAttributes); // not visible in the URL
//        redir.addAttribute("contentAttribute", "add-signature");
//        modelAndView.setView(redirectView);
        return modelAndView;
    }


    @PostMapping("update/{id}")
    public ModelAndView updateSignature(@PathVariable("id") long id,
                                        @Valid @ModelAttribute(name = "signatureEntity") SignatureEntity signatureEntity, BindingResult result,
                                        ModelAndView modelAndView,
                                        Model model) {
        if (result.hasErrors()) {//TODO
            signatureEntity.setId(id);
//            modelAndView.setView(new RedirectView("/index"));
            setDefaultModelViewObjects(modelAndView);
            model.addAttribute("content", "edit-signature");
            modelAndView.addObject("signatureEntity", signatureEntity);
//            modelAndView.addObject("content", "index");
//            modelAndView.getModel().put("showManageSignatureForm", true);
//            setDefaultModelViewObjects(modelAndView);
            return modelAndView;
        }

        //TODO a savecallback hook would be better for repo-save action
        signatureService.storeModel(
                signatureEntity,
                Paths.get(SignatureFileStorageService.RESOURCES_DIR_SIGNATURES),
                signatureEntity.getName()
        );
        signatureService.save(signatureEntity);
//        modelAndView.setView(new RedirectView("/index"));
        setDefaultModelViewObjects(modelAndView);
        modelAndView.addObject("content", "index");
        modelAndView.getModel().put("showManageSignatureForm", true);
//        return "redirect:/index";
        return modelAndView;
    }

    @RequestMapping(value = "update/{id}", params = {"add-control-field"})
    public ModelAndView updateSignatureAddControl(@PathVariable("id") long id,
                                                  SignatureEntity signatureEntity,
                                                  ModelAndView modelAndView,
                                                  RedirectAttributes redir,
                                                  final HttpServletRequest req
    ) {
        setDefaultModelViewObjects(modelAndView);
        modelAndView.getModel().put("content", "edit-signature");
        modelAndView.addObject("signatureEntity", signatureEntity);

        signatureEntity.getControlEntityList().add(new ControlEntity());

        return modelAndView;
    }

    @RequestMapping(value = "update/{id}", params = {"remove-control"})
    public ModelAndView UpdateSignatureRemoveControl(
            @PathVariable("id") long id,
            final SignatureEntity signatureEntity, final BindingResult bindingResult,
            ModelAndView modelAndView,
//            Model model,
            final HttpServletRequest req) {
        setDefaultModelViewObjects(modelAndView);
        modelAndView.getModel().put("content", "edit-signature");
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

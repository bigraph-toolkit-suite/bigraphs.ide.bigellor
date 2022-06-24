package de.tudresden.inf.st.bigraphs.editor.bigellor.rest.controller;

import de.tudresden.inf.st.bigraphs.core.BigraphFileModelManagement;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.AbstractController;
import de.tudresden.inf.st.bigraphs.editor.bigellor.BigraphModelFileStorageService;
import de.tudresden.inf.st.bigraphs.editor.bigellor.domain.SignatureEntity;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.SignatureEntityRepository;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.conversion.cytoscape.BBigraph2CytoscapeJSON;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.conversion.cytoscape.CytoscapeJSON2BBigraph;
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
import org.springframework.web.bind.annotation.*;

import java.io.FileOutputStream;
import java.io.IOException;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;

@Controller
@RestController
public class DemoController extends AbstractController {

    @Autowired
    CdoTemplate cdoTemplate;
    @Autowired
    private SignatureEntityRepository signatureEntityRepository;
    @Autowired
    BigraphModelFileStorageService bigraphModelFileStorageService;

    //TODO add requestparam sig id
    //TODO change URL demobigraph
    @RequestMapping(value = "/demobigraph", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getDemoBigraph(@RequestParam(required = false, defaultValue = "", value = "filename") String filename) {
        try {
            BBigraph2CytoscapeJSON ecore2JSON = new BBigraph2CytoscapeJSON();

            PureBigraph demoBigraph = ecore2JSON.createDemoBigraph();
            if (filename != null && !filename.isEmpty()) {
//                System.out.println("filename" + filename);
                Resource resource = bigraphModelFileStorageService.loadFileAsResource(filename);
                SignatureEntity signatureEntity = signatureEntityRepository.findById(1L).get();//TODO
                DefaultDynamicSignature sig = SignatureEntity.convert(signatureEntity);
                EPackage orGetBigraphMetaModel = createOrGetBigraphMetaModel(sig);
                PureBigraphBuilder<DefaultDynamicSignature> defaultDynamicSignaturePureBigraphBuilder = PureBigraphBuilder.create(sig, orGetBigraphMetaModel, resource.getFile().getAbsolutePath());
                demoBigraph = defaultDynamicSignaturePureBigraphBuilder.createBigraph();
            }

            String json = ecore2JSON.parseBigraph(demoBigraph);
            return json;
        } catch (InvalidConnectionException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (TypeNotExistsException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    //TODO could be put in MainController and URL remove projects/
    @PostMapping(value = "/projects/convert/bigraph", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity sendBigraph(@RequestBody String requestBody) {
        //public boolean greeting2() {
        System.out.println("running post function");

        try {
//            JSONObject jsonObject = new JSONObject(requestBody);
            CytoscapeJSON2BBigraph converter = new CytoscapeJSON2BBigraph(requestBody);
            PureBigraph bigraph = converter.convert();
            String filename = "/tmp/instance-model.xmi";
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, fileOutputStream);
            return new ResponseEntity<>("{\"filename\":\"" + filename + "\"}", new HttpHeaders(), HttpStatus.OK);
//                    ResponseEntity.ok()
//                    .contentType(MediaType.valueOf("application/json; charset=utf-8"))
//                    .contentType(MediaType.asMediaType(MimeType.))application/json
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
//                    .body("{filename: \"" + filename + "\"}");
        } catch (IOException | ParsingException e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }
}

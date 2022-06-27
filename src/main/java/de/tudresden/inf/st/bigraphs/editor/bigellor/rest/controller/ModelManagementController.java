package de.tudresden.inf.st.bigraphs.editor.bigellor.rest.controller;

import de.tudresden.inf.st.bigraphs.core.BigraphFileModelManagement;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.conversion.cytoscape.BBigraph2CytoscapeJSON;
import de.tudresden.inf.st.bigraphs.editor.bigellor.persistence.conversion.cytoscape.CytoscapeJSON2BBigraph;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.AbstractController;
import de.tudresden.inf.st.bigraphs.editor.bigellor.service.ProjectFileLocationService;
import nu.xom.ParsingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileOutputStream;
import java.io.IOException;

//@Controller
@RestController
public class ModelManagementController extends AbstractController {

    @Autowired
    ProjectFileLocationService projectFileLocationService;

    @RequestMapping(value = "/demobigraph", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String loadBigraphFromFilesystem(
            @RequestParam(required = false, defaultValue = "", value = "filename") String filename,
            @RequestParam(required = true, value = "id") long projectId) {
        try {
            BBigraph2CytoscapeJSON ecore2JSON = new BBigraph2CytoscapeJSON();

            PureBigraph demoBigraph = ecore2JSON.createDemoBigraph();
            if (filename != null && !filename.isEmpty()) {
//                System.out.println("filename" + filename);
                ProjectFileLocationService.LoadedModelResult result = projectFileLocationService.loadBigraphModelbyFilename(projectId, filename);
                demoBigraph = result.getBigraph();
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

package de.tudresden.inf.st.bigraphs.editor.bigellor.rest.controller;

import de.tudresden.inf.st.bigraphs.editor.bigellor.service.CDOServerService;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.response.CdoStatusResponse;
import de.tudresden.inf.st.bigraphs.editor.bigellor.rest.AbstractController;
import de.tudresden.inf.st.bigraphs.editor.bigellor.service.ProjectFileLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;

//@Controller
@RestController
public class MainController extends AbstractController {



    public MainController() {
//        this.cdoServerService = cdoServerService;
    }

    @GetMapping(value = {"/index", "/"})
    public ModelAndView index(@ModelAttribute("flashAttributesMap") Object flashAttributesMap,
                              ModelAndView modelAndView) {
        if (flashAttributesMap instanceof Map) {
            addFlashAttributes(flashAttributesMap, modelAndView);
        } else {
            setDefaultModelViewContent(modelAndView);
        }
        setDefaultModelViewObjects(modelAndView);
        return modelAndView;
    }


    // see https://mkyong.com/spring-boot/spring-boot-webflux-server-sent-events-example/
    @CrossOrigin(origins = "http://localhost:8080")
    @GetMapping(value = "cdo-server-status-updates.do", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<CdoStatusResponse> cdoStatus() {
        return Flux
                .fromStream(
                        Stream.generate(new Supplier<CdoStatusResponse>() {
                            AtomicBoolean tmp = new AtomicBoolean(false);

                            @Override
                            public CdoStatusResponse get() {
//                                return new CdoStatusResponse("CDO Server running: " + tmp.get(), tmp.getAndSet(!tmp.get()));
                                return new CdoStatusResponse(cdoServerService.isConnected());
                            }
                        })
                )
//        Streaming through a reactive type requires an Executor to write to the response.
//                Please, configure a TaskExecutor in the MVC config under "async support".
//                The SimpleAsyncTaskExecutor currently in use is not suitable under load.
//                .onBackpressureDrop()
                .delayElements(Duration.ofSeconds(1)) // update every 1 second
                ;
    }

    private void addFlashAttributes(Object flashAttributesMap, ModelAndView modelAndView) {
        Set set = ((Map) flashAttributesMap).entrySet();
        for (Object each : set) {
            Map.Entry tmp = (Map.Entry) each;
            modelAndView.addObject((String) tmp.getKey(), tmp.getValue());
        }
    }

}
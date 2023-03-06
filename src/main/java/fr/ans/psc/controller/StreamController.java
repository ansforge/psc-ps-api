package fr.ans.psc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ans.psc.delegate.PsApiDelegateImpl;
import fr.ans.psc.model.Ps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.stream.Stream;

@RestController("/api")
@Slf4j
public class StreamController {

    @Autowired
    PsApiDelegateImpl delegate;

    @GetMapping("/api/v2/stream-all")
    public void streamAllPs(HttpServletResponse response) throws IOException {
        log.info("in controller");
        try (final Stream<Ps> stream = delegate.streamAllPs()) {
            final Writer writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
            new ObjectMapper().writerFor(Iterator.class).writeValue(writer, stream.iterator());
            log.info("Ps streaming complete");
        }
    }
}

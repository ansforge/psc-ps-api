package fr.ans.psc.pscapimajv2.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.jupiter.tools.spring.test.mongo.annotation.ExpectedMongoDataSet;
import com.jupiter.tools.spring.test.mongo.annotation.MongoDataSet;
import fr.ans.psc.delegate.ToggleApiDelegateImpl;
import fr.ans.psc.repository.PsRefRepository;
import fr.ans.psc.pscapimajv2.utils.MemoryAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ToggleOperationTest extends BaseOperationTest {

    @Autowired
    private PsRefRepository psRefRepository;

    @BeforeEach
    public void setUp(WebApplicationContext context, RestDocumentationContextProvider restDocProvider) {
        // LOG APPENDER
        Logger logger = (Logger) LoggerFactory.getLogger(ToggleApiDelegateImpl.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.INFO);
        logger.addAppender(memoryAppender);
        memoryAppender.start();

        // REST DOCS
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(documentationConfiguration(restDocProvider))
                .build();
    }

    @Test
    @DisplayName(value = "should toggle PsRef, nominal case")
    @MongoDataSet(value = "/dataset/before_toggle.json", cleanBefore = true, cleanAfter = true)
    @ExpectedMongoDataSet(value = "/dataset/after_toggle.json")
    public void togglePsRef() throws Exception {
        assertEquals(psRefRepository.findPsRefByNationalIdRef("01").getNationalId(), "01");
        assertEquals(psRefRepository.findPsRefByNationalIdRef("81").getNationalId(), "81");

        ResultActions toggleOperation = mockMvc.perform(put("/api/v3/toggle").header("Accept", "application/json")
                .contentType("application/json").content("{\"nationalIdRef\": \"01\", \"nationalId\": \"81\"}"))
                .andExpect(status().is(200));

        toggleOperation.andDo(document("ToggleOperationTest/toggle_psref"));

        assertThat(memoryAppender.contains("Ps 01 successfully removed", Level.INFO)).isTrue();
        assertThat(memoryAppender.contains("PsRef 01 is now referencing Ps 81", Level.INFO)).isTrue();
        assertEquals(psRefRepository.findPsRefByNationalIdRef("01").getNationalId(), "81");
        assertEquals(psRefRepository.findPsRefByNationalIdRef("81").getNationalId(), "81");
    }

    @Test
    @DisplayName(value = "should not toggle PsRef if it's already done")
    @MongoDataSet(value = "/dataset/after_toggle.json", cleanBefore = true, cleanAfter = true)
    @ExpectedMongoDataSet(value = "/dataset/after_toggle.json")
    public void alreadyDoneToggleFailed() throws Exception {
        mockMvc.perform(put("/api/v3/toggle").header("Accept", "application/json")
                .contentType("application/json").content("{\"nationalIdRef\": \"01\", \"nationalId\": \"81\"}"))
                .andExpect(status().is(409));

        assertThat(memoryAppender.contains("Ps 01 successfully removed", Level.INFO)).isFalse();
        assertThat(memoryAppender.contains("PsRef 01 is now referencing Ps 81", Level.INFO)).isFalse();
        assertThat(memoryAppender.contains("PsRef 01 already references Ps 81, no need to toggle", Level.INFO)).isTrue();
    }

    @Test
    @DisplayName(value = "should not toggle PsRef if target Ps does not exist")
    @MongoDataSet(value = "/dataset/before_toggle.json", cleanBefore = true, cleanAfter = true)
    public void absentTargetPsToggleFailed() throws Exception {
        mockMvc.perform(put("/api/v3/toggle").header("Accept", "application/json")
                .contentType("application/json").content("{\"nationalIdRef\": \"01\", \"nationalId\": \"89\"}"))
                .andExpect(status().is(410));

        assertThat(memoryAppender.contains("Ps 01 successfully removed", Level.INFO)).isFalse();
        assertThat(memoryAppender.contains("PsRef 01 is now referencing Ps 81", Level.INFO)).isFalse();
        assertThat(memoryAppender.contains("Could not toggle PsRef 01 on Ps 89 because this Ps does not exist", Level.ERROR)).isTrue();
    }

    @Test
    @DisplayName(value = "toggle should failed if malformed request body")
    @MongoDataSet(value = "/dataset/before_toggle.json", cleanBefore = true, cleanAfter = true)
    public void malformedPsRefToggleFailed() throws Exception {
        // with blank nationalIdRef
        mockMvc.perform(put("/api/v3/toggle").header("Accept", "application/json")
                .contentType("application/json").content("{\"nationalIdRef\": \"\", \"nationalId\": \"81\"}"))
                .andExpect(status().is(400));

        // with blank nationalId
        mockMvc.perform(put("/api/v3/toggle").header("Accept", "application/json")
                .contentType("application/json").content("{\"nationalIdRef\": \"01\", \"nationalId\": \"\"}"))
                .andExpect(status().is(400));
        // without nationalId
        mockMvc.perform(put("/api/v3/toggle").header("Accept", "application/json")
                .contentType("application/json").content("{\"nationalIdRef\": \"01\"}"))
                .andExpect(status().is(400));
    }
}

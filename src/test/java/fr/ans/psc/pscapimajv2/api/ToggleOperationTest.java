/**
 * Copyright (C) 2022-2023 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.pscapimajv2.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.jupiter.tools.spring.test.mongo.annotation.ExpectedMongoDataSet;
import com.jupiter.tools.spring.test.mongo.annotation.MongoDataSet;
import fr.ans.psc.delegate.ToggleApiDelegateImpl;
import fr.ans.psc.model.Ps;
import fr.ans.psc.pscapimajv2.utils.MemoryAppender;
import fr.ans.psc.repository.PsRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ToggleOperationTest extends BaseOperationTest {

    @Autowired
    private PsRepository psRepository;

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
        Ps ps1 = psRepository.findByIdsContaining("01");
        Ps ps2 = psRepository.findByIdsContaining("81");
        assertTrue(ps1.getIds().contains("01"));
        assertTrue(ps2.getIds().contains("81"));

        ResultActions toggleOperation = mockMvc.perform(put("/api/v2/toggle").header("Accept", "application/json")
                .contentType("application/json").content("{\"nationalIdRef\": \"01\", \"nationalId\": \"81\"}"))
                .andExpect(status().is(200));

        toggleOperation.andDo(document("ToggleOperationTest/toggle_psref"));

        assertThat(memoryAppender.contains("Ps 01 successfully removed", Level.INFO)).isTrue();
        assertThat(memoryAppender.contains("PsRef 01 is now referencing Ps 81", Level.INFO)).isTrue();
        Ps finalPs = psRepository.findByNationalId("81");
        assertTrue(finalPs.getIds().contains("01"));
        assertTrue(finalPs.getIds().contains("81"));
    }

    @Test
    @DisplayName(value = "should not toggle PsRef if it's already done")
    @MongoDataSet(value = "/dataset/after_toggle.json", cleanBefore = true, cleanAfter = true)
    @ExpectedMongoDataSet(value = "/dataset/after_toggle.json")
    public void alreadyDoneToggleFailed() throws Exception {
        mockMvc.perform(put("/api/v2/toggle").header("Accept", "application/json")
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
        mockMvc.perform(put("/api/v2/toggle").header("Accept", "application/json")
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
        mockMvc.perform(put("/api/v2/toggle").header("Accept", "application/json")
                .contentType("application/json").content("{\"nationalIdRef\": \"\", \"nationalId\": \"81\"}"))
                .andExpect(status().is(400));

        // with blank nationalId
        mockMvc.perform(put("/api/v2/toggle").header("Accept", "application/json")
                .contentType("application/json").content("{\"nationalIdRef\": \"01\", \"nationalId\": \"\"}"))
                .andExpect(status().is(400));
        // without nationalId
        mockMvc.perform(put("/api/v2/toggle").header("Accept", "application/json")
                .contentType("application/json").content("{\"nationalIdRef\": \"01\"}"))
                .andExpect(status().is(400));
    }

    @Test
    @DisplayName(value = "should untoggle PsRef, nominal case")
    @MongoDataSet(value = "/dataset/before_untoggle.json", cleanBefore = true, cleanAfter = true)
    @ExpectedMongoDataSet(value = "/dataset/after_untoggle.json")
    public void untogglePsRef() throws Exception {
        Ps ps = psRepository.findByIdsContaining("81");
        assertTrue(ps.getIds().contains("01"));
        assertTrue(ps.getIds().contains("81"));

        ResultActions untoggleOperation = mockMvc.perform(delete("/api/v2/toggle").header("Accept", "application/json")
                        .contentType("application/json").content("{\"nationalIdRef\": \"01\", \"nationalId\": \"81\"}"))
                .andExpect(status().is(200));

        untoggleOperation.andDo(document("ToggleOperationTest/toggle_psref"));

        assertThat(memoryAppender.contains("Ps 81 is no longer referencing PsRef 01", Level.INFO)).isTrue();
        Ps finalPs = psRepository.findByNationalId("81");
        assertFalse(finalPs.getIds().contains("01"));
        assertTrue(finalPs.getIds().contains("81"));
        assertEquals(1, finalPs.getIds().size());
    }

    @Test
    @DisplayName(value = "untoggle should fail if PsRef is not toggled on Ps")
    @MongoDataSet(value = "/dataset/after_untoggle.json", cleanBefore = true, cleanAfter = true)
    @ExpectedMongoDataSet(value = "/dataset/after_untoggle.json")
    public void notReferencedUntoggleFailed() throws Exception {
        mockMvc.perform(delete("/api/v2/toggle").header("Accept", "application/json")
                        .contentType("application/json").content("{\"nationalIdRef\": \"01\", \"nationalId\": \"81\"}"))
                .andExpect(status().is(404));

        assertThat(memoryAppender.contains("Ps 81 is no longer referencing PsRef 01", Level.INFO)).isFalse();
        assertThat(memoryAppender.contains("Ps 81 does not reference PsRef 01", Level.ERROR)).isTrue();
    }

    @Test
    @DisplayName(value = "untoggle should fail if target Ps does not exist")
    @MongoDataSet(value = "/dataset/before_untoggle.json", cleanBefore = true, cleanAfter = true)
    public void absentTargetPsUntoggleFailed() throws Exception {
        mockMvc.perform(delete("/api/v2/toggle").header("Accept", "application/json")
                        .contentType("application/json").content("{\"nationalIdRef\": \"01\", \"nationalId\": \"89\"}"))
                .andExpect(status().is(410));

        assertThat(memoryAppender.contains("Ps 81 is no longer referencing PsRef 01", Level.INFO)).isFalse();
        assertThat(memoryAppender.contains("Could not remove PsRef 01 from Ps 89 because this Ps does not exist", Level.ERROR)).isTrue();
    }

    @Test
    @DisplayName(value = "untoggle should fail if there is a malformed request body")
    @MongoDataSet(value = "/dataset/before_toggle.json", cleanBefore = true, cleanAfter = true)
    public void malformedPsRefUntoggleFailed() throws Exception {
        // with blank nationalIdRef
        mockMvc.perform(delete("/api/v2/toggle").header("Accept", "application/json")
                        .contentType("application/json").content("{\"nationalIdRef\": \"\", \"nationalId\": \"81\"}"))
                .andExpect(status().is(400));

        // with blank nationalId
        mockMvc.perform(delete("/api/v2/toggle").header("Accept", "application/json")
                        .contentType("application/json").content("{\"nationalIdRef\": \"01\", \"nationalId\": \"\"}"))
                .andExpect(status().is(400));
        // without nationalId
        mockMvc.perform(delete("/api/v2/toggle").header("Accept", "application/json")
                        .contentType("application/json").content("{\"nationalIdRef\": \"01\"}"))
                .andExpect(status().is(400));
    }
}

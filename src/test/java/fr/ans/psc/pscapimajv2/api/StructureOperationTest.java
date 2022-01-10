package fr.ans.psc.pscapimajv2.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.jupiter.tools.spring.test.mongo.annotation.MongoDataSet;
import fr.ans.psc.delegate.StructureApiDelegateImpl;
import fr.ans.psc.model.Structure;
import fr.ans.psc.repository.StructureRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class StructureOperationTest extends BaseOperationTest {

    @Autowired
    private StructureRepository structureRepository;
    @BeforeEach
    public void setUp(WebApplicationContext context, RestDocumentationContextProvider restDocProvider) {
        // LOG APPENDER
        Logger logger = (Logger) LoggerFactory.getLogger(StructureApiDelegateImpl.class);
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
    @DisplayName(value = "should get a structure by its technical id")
    @MongoDataSet(value = "/dataset/structure.json", cleanBefore = true, cleanAfter = true)
    public void getStructureById() throws Exception {
        Structure structure = structureRepository.findByStructureTechnicalId("1");
        String structureAsJsonString = objectWriter.writeValueAsString(structure);

        ResultActions getStructure = mockMvc.perform(get("/api/v2/structure/1").header("Accept", "application/json"))
                .andExpect(status().is(200)).andExpect(content().json(structureAsJsonString));

        assertThat(memoryAppender.contains("Structure 1 has been found", Level.INFO)).isTrue();
        getStructure.andDo(document("StructureOperationTest/get_structure_by_id"));
    }

    @Test
    @DisplayName(value = "should not get a structure if it doesn't exist")
    @MongoDataSet(value = "/dataset/structure.json", cleanBefore = true, cleanAfter = true)
    public void getStructureByIdFailed() throws Exception {
        mockMvc.perform(get("/api/v2/structure/3").header("Accept", "application/json"))
                .andExpect(status().is(410));

        assertThat(memoryAppender.contains("Structure 3 not found", Level.WARN)).isTrue();
    }

    @Test
    @DisplayName(value = "should get Structure if missing header")
    @MongoDataSet(value = "/dataset/structure.json", cleanBefore = true, cleanAfter = true)
    public void getStructureWithoutJsonAcceptHeader() throws Exception {
        mockMvc.perform(get("/api/v2/structure/2"))
                .andExpect(status().is(200));
    }

    @Test
    @DisplayName(value = "should not get Structure if wrong accept header")
    @MongoDataSet(value = "/dataset/structure.json", cleanBefore = true, cleanAfter = true)
    public void getStructureWithWrongHeaderFailed() throws Exception {
        mockMvc.perform(get("/api/v2/structure/2").header("Accept","application/xml"))
                .andExpect(status().is(406));
    }

    @Test
    @DisplayName(value = "should create a new structure")
    public void createNewStructure() throws Exception {
        ResultActions createdStructure = mockMvc.perform(post("/api/v2/structure").header("Accept", "application/json")
                .contentType("application/json").content("{\"_id\":\"61b0debad9b1af764debd6fa\",\"siteSIRET\":\"125 137 196 15574\"," +
                        "\"siteSIREN\":\"125 137 196\",\"siteFINESS\":null,\"legalEstablishmentFINESS\":null,\"structureTechnicalId\":\"1\","+
                        "\"legalCommercialName\":\"Structure One\",\"publicCommercialName\":\"Structure One\",\"recipientAdditionalInfo\":\"info +\","+
                        "\"geoLocationAdditionalInfo\":\"geoloc info +\",\"streetNumber\":\"1\",\"streetNumberRepetitionIndex\":\"bis\","+
                        "\"streetCategoryCode\":\"rue\",\"streetLabel\":\"Zorro\",\"distributionMention\":\"c/o Bernardo\",\"cedexOffice\":\"75117\","+
                        "\"postalCode\":\"75017\",\"communeCode\":\"75\",\"countryCode\":\"FR\",\"phone\":\"0123456789\",\"phone2\":\"0623456789\","+
                        "\"fax\":\"0198765432\",\"email\":\"structure@one.fr\",\"departmentCode\":\"99\",\"oldStructureId\":\"101\",\"registrationAuthority\":\"CIA\"}"))
                .andExpect(status().is(201));

        assertEquals(structureRepository.count(), 1);
        assertEquals(structureRepository.findByStructureTechnicalId("1").getLegalCommercialName(),"Structure One");
        assertThat(memoryAppender.contains("New structure created with structure technical id 1", Level.INFO)).isTrue();

        createdStructure.andDo(document("StructureOperationTest/create_new_structure"));
    }

    @Test
    @DisplayName(value = "should not create a structure if exists already")
    @MongoDataSet(value = "/dataset/structure.json", cleanBefore = true, cleanAfter = true)
    public void createExistingStructureFailed() throws Exception {
        mockMvc.perform(post("/api/v2/structure").header("Accept", "application/json")
                .contentType("application/json").content("{\"siteSIRET\":\"125 137 196 15574\",\"siteSIREN\":\"125 137 196\",\"siteFINESS\":null,"+
                        "\"legalEstablishmentFINESS\":null,\"structureTechnicalId\":\"1\",\"legalCommercialName\":\"Structure One\","+
                        "\"publicCommercialName\":\"Structure One\",\"recipientAdditionalInfo\":\"info +\",\"geoLocationAdditionalInfo\":\"geoloc info +\"," +
                        "\"streetNumber\":\"1\",\"streetNumberRepetitionIndex\":\"bis\",\"streetCategoryCode\":\"rue\",\"streetLabel\":\"Zorro\"," +
                        "\"distributionMention\":\"c/o Bernardo\",\"cedexOffice\":\"75117\",\"postalCode\":\"75017\",\"communeCode\":\"75\"," +
                        "\"countryCode\":\"FR\",\"phone\":\"0123456789\",\"phone2\":\"0623456789\",\"fax\":\"0198765432\","+
                        "\"email\":\"structure@one.fr\",\"departmentCode\":\"99\",\"oldStructureId\":\"101\",\"registrationAuthority\":\"CIA\"}"))
                .andExpect(status().is(409));

        assertEquals(structureRepository.count(), 2);
        assertThat(memoryAppender.contains("New structure created with structure technical id 1", Level.INFO)).isFalse();
        assertThat(memoryAppender.contains("Structure 1 exists already", Level.WARN)).isTrue();
    }

    @Test
    @DisplayName(value = "should not create a structure if malformed request body")
    public void createMalformedStructureFailed() throws Exception {
        mockMvc.perform(post("/api/v2/structure").header("Accept", "application/json")
                .contentType("application/json").content("{\"structureTechnicalId\":\"\",\"legalCommercialName\":\"Structure One\"}"))
                .andExpect(status().is(400));

        mockMvc.perform(post("/api/v2/structure").header("Accept", "application/json")
                .contentType("application/json").content("{\"legalCommercialName\":\"Structure One\"}"))
                .andExpect(status().is(400));
    }

    @Test
    @DisplayName(value = "should update a structure")
    @MongoDataSet(value = "/dataset/structure.json", cleanBefore = true, cleanAfter = true)
    public void updateStructure() throws Exception {
        Structure structure = structureRepository.findByStructureTechnicalId("1");
        assertEquals(structure.getLegalCommercialName(), "Structure One");

        ResultActions updatedStructure = mockMvc.perform(put("/api/v2/structure").header("Accept", "application/json")
                .contentType("application/json").content("{\"structureTechnicalId\":\"1\",\"legalCommercialName\":\"Structure updated\"}"))
                .andExpect(status().is(200));

        assertThat(memoryAppender.contains("Structure 1 successfully updated", Level.INFO)).isTrue();
        structure = structureRepository.findByStructureTechnicalId("1");
        assertEquals(structure.getLegalCommercialName(), "Structure updated");

        updatedStructure.andDo(document("StructureOperationTest/update_structure"));
    }

    @Test
    @DisplayName(value = "should not update a structure if not exists")
    @MongoDataSet(value = "/dataset/structure.json", cleanBefore = true, cleanAfter = true)
    public void updateAbsentStructureFailed() throws Exception {
        mockMvc.perform(put("/api/v2/structure").header("Accept", "application/json")
                .contentType("application/json").content("{\"structureTechnicalId\":\"1000\",\"legalCommercialName\":\"Structure One\"}"))
                .andExpect(status().is(410));

        assertThat(memoryAppender.contains("Structure 1000 successfully updated", Level.INFO)).isFalse();
        assertThat(memoryAppender.contains("Structure 1000 not found", Level.WARN)).isTrue();
    }

    @Test
    @DisplayName(value = "should delete a structure")
    @MongoDataSet(value = "/dataset/structure.json", cleanBefore = true, cleanAfter = true)
    public void deleteStructureById() throws Exception {
        ResultActions deletedStructure = mockMvc.perform(delete("/api/v2/structure/1").header("Accept", "application/json"))
                .andExpect(status().is(204));

        assertThat(memoryAppender.contains("Structure 1 successfully removed", Level.INFO)).isTrue();
        assertEquals(structureRepository.count(), 1);

        deletedStructure.andDo(document("StructureOperationTest/delete_structure_by_id"));
    }

    @Test
    @DisplayName(value = "should not delete a structure if not exists")
    @MongoDataSet(value = "/dataset/structure.json", cleanBefore = true, cleanAfter = true)
    public void deleteAbsentStructureFailed() throws Exception {
        mockMvc.perform(delete("/api/v2/structure/3").header("Accept", "application/json"))
                .andExpect(status().is(410));

        assertThat(memoryAppender.contains("Structure 3 successfully removed", Level.INFO)).isFalse();
        assertThat(memoryAppender.contains("Structure 3 not found", Level.WARN)).isTrue();
        assertEquals(structureRepository.count(), 2);
    }
}

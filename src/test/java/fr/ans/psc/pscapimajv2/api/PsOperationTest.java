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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import com.jupiter.tools.spring.test.mongo.annotation.ExpectedMongoDataSet;
import com.jupiter.tools.spring.test.mongo.annotation.MongoDataSet;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import fr.ans.psc.delegate.PsApiDelegateImpl;
import fr.ans.psc.model.Ps;
import fr.ans.psc.pscapimajv2.utils.MemoryAppender;
import fr.ans.psc.repository.PsRepository;
import fr.ans.psc.utils.ApiUtils;

public class PsOperationTest extends BaseOperationTest {

    @Autowired
    private PsRepository psRepository;

    @BeforeEach
    public void setUp(WebApplicationContext context, RestDocumentationContextProvider restDocProvider) {
        // LOG APPENDER
        Logger logger = (Logger) LoggerFactory.getLogger(PsApiDelegateImpl.class);
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
    @DisplayName(value = "should get Ps by id, nominal case")
    @MongoDataSet(value = "/dataset/ps_2_psref_entries.json", cleanBefore = true, cleanAfter = true)
    public void getPsById() throws Exception {

        Ps storedPs = psRepository.findByNationalId("800000000001");
        String psAsJsonString = objectWriter.writeValueAsString(storedPs);

        ResultActions firstPsRefRequest = mockMvc.perform(get("/api/v2/ps/800000000001")
                .header("Accept", "application/json"))
                .andExpect(status().is(200));

        firstPsRefRequest.andExpect(content().json(psAsJsonString));
        assertThat(memoryAppender.contains("Ps 800000000001 has been found", Level.INFO)).isTrue();

        firstPsRefRequest.andDo(document("PsOperationTest/get_Ps_by_id"));

        ResultActions secondPsRefRequest = mockMvc.perform(get("/api/v2/ps/800000000011")
                .header("Accept", "application/json"))
                .andExpect(status().is(200));

        secondPsRefRequest.andExpect(content().json(psAsJsonString));
        assertThat(memoryAppender.contains("Ps 800000000001 has been found", Level.INFO)).isTrue();

    }

    @Test
    @DisplayName(value = "should get a list of Ps objects from specified page")
    @MongoDataSet(value = "/dataset/before_unwind.json", cleanBefore = true, cleanAfter = true)
    public void getAllActivePs() throws Exception {

        ResultActions psRefRequest = mockMvc.perform(get("/api/v2/ps?page=0")
                        .header("Accept", "application/json"))
                .andExpect(status().is(200))
                .andDo(print());

        String json = psRefRequest.andReturn().getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        List<Ps> psList = objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, Ps.class));
        assertEquals(3, psList.size());

        for(Ps ps : psList){
            System.out.println(ps);
        }

        assertTrue(psList.stream().anyMatch(
            ps -> ps.getProfessions().get(0).getExpertises().get(0).getExpertiseId().equals("1.1")
            && ps.getProfessions().get(0).getWorkSituations().get(0).getSituId().equals("1.1")));
        assertTrue(psList.stream().anyMatch(
            ps -> ps.getProfessions().get(0).getExpertises().get(0).getExpertiseId().equals("1.1")
            && ps.getProfessions().get(0).getWorkSituations().get(1).getSituId().equals("1.2")));
        assertTrue(psList.stream().anyMatch(
            ps -> ps.getProfessions().get(0).getExpertises().get(0).getExpertiseId().equals("1.1")
            && ps.getProfessions().get(0).getWorkSituations().get(2).getSituId().equals("1.3")));
        assertTrue(psList.stream().anyMatch(
            ps -> ps.getProfessions().get(0).getExpertises().get(1).getExpertiseId().equals("1.2")
            && ps.getProfessions().get(0).getWorkSituations().get(0).getSituId().equals("1.1")));
        assertTrue(psList.stream().anyMatch(
            ps -> ps.getProfessions().get(0).getExpertises().get(1).getExpertiseId().equals("1.2")
            && ps.getProfessions().get(0).getWorkSituations().get(1).getSituId().equals("1.2")));
        assertTrue(psList.stream().anyMatch(
            ps -> ps.getProfessions().get(0).getExpertises().get(1).getExpertiseId().equals("1.2")
            && ps.getProfessions().get(0).getWorkSituations().get(2).getSituId().equals("1.3")));
    }

    @Test
    @DisplayName("check encoded url")
    @MongoDataSet(value = "/dataset/psEncodedId.json", cleanBefore = true, cleanAfter = true)
    public void getPsByEncodedId() throws Exception {
        Ps storedPs = psRepository.findByNationalId("80000000000/1");
        String psAsJsonString = objectWriter.writeValueAsString(storedPs);
        ResultActions psRefRequest = mockMvc.perform(get("/api/v2/ps/80000000000%2F1")
                .header("Accept", "application/json"))
                .andExpect(status().is(200))
                .andDo(print());

        psRefRequest.andExpect(content().json(psAsJsonString));
    }

    @Test
    @DisplayName(value = "should get Ps if missing header")
    @MongoDataSet(value = "/dataset/ps_2_psref_entries.json", cleanBefore = true, cleanAfter = true)
    public void getPsWithoutJsonAcceptHeader() throws Exception {
        mockMvc.perform(get("/api/v2/ps/800000000001"))
                .andExpect(status().is(200));
    }

    @Test
    @DisplayName(value = "should not get Ps if wrong accept header")
    @MongoDataSet(value = "/dataset/ps_2_psref_entries.json", cleanBefore = true, cleanAfter = true)
    public void getPsWithWrongHeaderFailed() throws Exception {
        mockMvc.perform(get("/api/v2/ps/800000000001").header("Accept","application/xml"))
                .andExpect(status().is(406));
    }

    @Test
    @DisplayName(value = "should not get Ps if deactivated")
    @MongoDataSet(value = "/dataset/deactivated_ps.json", cleanBefore = true, cleanAfter = true)
    public void getPsDeactivated() throws Exception {
        mockMvc.perform(get("/api/v2/ps/800000000002")
                .header("Accept", "application/json"))
                .andExpect(status().is(410));
        assertThat(memoryAppender.contains("Ps 800000000002 is deactivated", Level.WARN)).isTrue();
    }

    @Test
    @DisplayName(value = "should not get Ps if not exist")
    @MongoDataSet(value = "/dataset/ps_2_psref_entries.json", cleanBefore = true, cleanAfter = true)
    public void getNotExistingPs() throws Exception {
        mockMvc.perform(get("/api/v2/ps/800000000003")
                .header("Accept", "application/json"))
                .andExpect(status().is(410));
        assertThat(memoryAppender.contains("No Ps found with nationalIdRef 800000000003", Level.WARN)).isTrue();
    }

    @Test
    @DisplayName(value = "should create a brand new Ps")
    public void createNewPs() throws Exception {

        ResultActions createdPs = mockMvc.perform(post("/api/v2/ps").header("Accept", "application/json")
                .contentType("application/json").content("{\"idType\":\"8\",\"id\":\"00000000001\"," +
                        "\"nationalId\":\"800000000001\",\"lastName\":\"DUPONT\",\"firstNames\":[{\"firstName\":\"JIMMY\",\"order\":1}],\"dateOfBirth\":\"17/12/1983\"," +
                        "\"birthAddressCode\":\"57463\",\"birthCountryCode\":\"99000\",\"birthAddress\":\"METZ\",\"genderCode\":\"M\"," +
                        "\"phone\":\"0601020304\",\"email\":\"toto57@hotmail.fr\",\"salutationCode\":\"MME\",\"professions\":[{\"exProId\":\"50C\"," +
                        "\"code\":\"50\",\"categoryCode\":\"C\",\"salutationCode\":\"M\",\"lastName\":\"DUPONT\",\"firstName\":\"JIMMY\"," +
                        "\"expertises\":[{\"expertiseId\":\"SSM69\",\"typeCode\":\"S\",\"code\":\"SM69\"}],\"workSituations\":[{\"situId\":\"SSA04\"," +
                        "\"modeCode\":\"S\",\"activitySectorCode\":\"SA04\",\"pharmacistTableSectionCode\":\"AC36\",\"roleCode\":\"12\"," +
                        "\"registrationAuthority\":\"ARS/ARS/ARS\",\"structure\":{\"siteSIRET\":\"125 137 196 15574\",\"siteSIREN\":\"125 137 196\"," +
                        "\"siteFINESS\":null,\"legalEstablishmentFINESS\":null,\"structureTechnicalId\":\"1\"," +
                        "\"legalCommercialName\":\"Structure One\",\"publicCommercialName\":\"Structure One\",\"recipientAdditionalInfo\":\"info +\"," +
                        "\"geoLocationAdditionalInfo\":\"geoloc info +\",\"streetNumber\":\"1\",\"streetNumberRepetitionIndex\":\"bis\"," +
                        "\"streetCategoryCode\":\"rue\",\"streetLabel\":\"Zorro\",\"distributionMention\":\"c/o Bernardo\",\"cedexOffice\":\"75117\"," +
                        "\"postalCode\":\"75017\",\"communeCode\":\"75\",\"countryCode\":\"FR\",\"phone\":\"0123456789\",\"phone2\":\"0623456789\"," +
                        "\"fax\":\"0198765432\",\"email\":\"structure@one.fr\",\"departmentCode\":\"99\",\"oldStructureId\":\"101\"," +
                        "\"registrationAuthority\":\"CIA\"}}]}],\"psRefs\":[{\"nationalIdRef\": \"800000000001\",\"nationalId\": \"800000000001\","+
                        "\"activated\": 1638791221}]}"))
                .andExpect(status().is(201));
        
        ResultActions createdPs2 = mockMvc.perform(post("/api/v2/ps").header("Accept", "application/json")
                .contentType("application/json").content("{\"idType\":\"8\",\"id\":\"00000000002\"," +"\"ids\":[\"800000000002\",\"855e8700-e29b-41d4-a716-44665544111\"],"+  
                		"\"alternativeIds\":[{\"identifier\":\"800000000002\",\"origine\":\"RPPS\",\"quality\":1},{\"identifier\":\"855e8700-e29b-41d4-a716-44665544111\",\"origine\":\"PSI\",\"quality\":2}]," + "\"quality\":0," +
                        "\"nationalId\":\"855e8700-e29b-41d4-a716-44665544111\",\"lastName\":\"DUPONT\",\"firstNames\":[{\"firstName\":\"JIMMY\",\"order\":1}],\"dateOfBirth\":\"17/12/1983\"," +
                        "\"birthAddressCode\":\"57463\",\"birthCountryCode\":\"99000\",\"birthAddress\":\"METZ\",\"genderCode\":\"M\"," +
                        "\"phone\":\"0601020304\",\"email\":\"toto57@hotmail.fr\",\"salutationCode\":\"MME\",\"professions\":[{\"exProId\":\"50C\"," +
                        "\"code\":\"50\",\"categoryCode\":\"C\",\"salutationCode\":\"M\",\"lastName\":\"DUPONT\",\"firstName\":\"JIMMY\"," +
                        "\"expertises\":[{\"expertiseId\":\"SSM69\",\"typeCode\":\"S\",\"code\":\"SM69\"}],\"workSituations\":[{\"situId\":\"SSA04\"," +
                        "\"modeCode\":\"S\",\"activitySectorCode\":\"SA04\",\"pharmacistTableSectionCode\":\"AC36\",\"roleCode\":\"12\"," +
                        "\"registrationAuthority\":\"ARS/ARS/ARS\",\"structure\":{\"siteSIRET\":\"125 137 196 15574\",\"siteSIREN\":\"125 137 196\"," +
                        "\"siteFINESS\":null,\"legalEstablishmentFINESS\":null,\"structureTechnicalId\":\"1\"," +
                        "\"legalCommercialName\":\"Structure One\",\"publicCommercialName\":\"Structure One\",\"recipientAdditionalInfo\":\"info +\"," +
                        "\"geoLocationAdditionalInfo\":\"geoloc info +\",\"streetNumber\":\"1\",\"streetNumberRepetitionIndex\":\"bis\"," +
                        "\"streetCategoryCode\":\"rue\",\"streetLabel\":\"Zorro\",\"distributionMention\":\"c/o Bernardo\",\"cedexOffice\":\"75117\"," +
                        "\"postalCode\":\"75017\",\"communeCode\":\"75\",\"countryCode\":\"FR\",\"phone\":\"0123456789\",\"phone2\":\"0623456789\"," +
                        "\"fax\":\"0198765432\",\"email\":\"structure@one.fr\",\"departmentCode\":\"99\",\"oldStructureId\":\"101\"," +
                        "\"registrationAuthority\":\"CIA\"}}]}],\"psRefs\":[{\"nationalIdRef\": \"800000000002\",\"nationalId\": \"800000000002\","+
                        "\"activated\": 1638791221}]}"))
                .andExpect(status().is(201));
        
        assertThat(memoryAppender.contains("Ps 800000000001 successfully stored or updated", Level.INFO)).isTrue();
        assertThat(memoryAppender.contains("PsRef 800000000001 has been reactivated", Level.INFO)).isFalse();

        // Take the default value
        Ps storedPs = psRepository.findByNationalId("800000000001");        
        // assertTrue(storedPs.getAlternativeIds().isEmpty());
        
        // Overrides the default value
        Ps storedPs2 = psRepository.findByNationalId("855e8700-e29b-41d4-a716-44665544111");
        assertTrue(storedPs2.getAlternativeIds().stream().anyMatch(id -> id.getQuality() == 1 && "800000000002".equals(id.getIdentifier()) && "RPPS".equals(id.getOrigine() )));
        assertTrue(storedPs2.getAlternativeIds().stream().anyMatch(id -> id.getQuality() == 2 && "855e8700-e29b-41d4-a716-44665544111".equals(id.getIdentifier()) && "PSI".equals(id.getOrigine() )));
        
        String psAsJsonString = objectWriter.writeValueAsString(storedPs);
        System.out.println("---------------  RESULT  -------------");
        System.out.println(psAsJsonString);
        
        psAsJsonString = objectWriter.writeValueAsString(storedPs2);
        System.out.println("---------------  RESULT  -------------");
        System.out.println(psAsJsonString);

        createdPs.andDo(document("PsOperationTest/create_new_Ps"));
    }

    @Test
    @DisplayName(value = "should create a brand new Ps")
    public void createNewPsFromPSI() throws Exception {

        ResultActions createdPs = mockMvc.perform(post("/api/v2/ps").header("Accept", "application/json")
                .contentType("application/json")
                .content("{\"idType\":\"8\",\"id\":\"00000000001\","
                        + "\"nationalId\":\"800000000001\",\"lastName\":\"DUPONT\",\"firstNames\":[{\"firstName\":\"JIMMY\",\"order\":1}],\"dateOfBirth\":\"17/12/1983\","
                        + "\"birthAddressCode\":\"57463\",\"birthCountryCode\":\"99000\",\"birthAddress\":\"METZ\",\"genderCode\":\"M\","
                        + "\"phone\":\"0601020304\",\"email\":\"toto57@hotmail.fr\",\"salutationCode\":\"MME\",\"professions\":[{\"exProId\":\"50C\","
                        + "\"code\":\"50\",\"categoryCode\":\"C\",\"salutationCode\":\"M\",\"lastName\":\"DUPONT\",\"firstName\":\"JIMMY\","
                        + "\"expertises\":[{\"expertiseId\":\"SSM69\",\"typeCode\":\"S\",\"code\":\"SM69\"}],\"workSituations\":[{\"situId\":\"SSA04\","
                        + "\"modeCode\":\"S\",\"activitySectorCode\":\"SA04\",\"pharmacistTableSectionCode\":\"AC36\",\"roleCode\":\"12\","
                        + "\"registrationAuthority\":\"ARS/ARS/ARS\",\"structure\":{\"siteSIRET\":\"125 137 196 15574\",\"siteSIREN\":\"125 137 196\","
                        + "\"siteFINESS\":null,\"legalEstablishmentFINESS\":null,\"structureTechnicalId\":\"1\","
                        + "\"legalCommercialName\":\"Structure One\",\"publicCommercialName\":\"Structure One\",\"recipientAdditionalInfo\":\"info +\","
                        + "\"geoLocationAdditionalInfo\":\"geoloc info +\",\"streetNumber\":\"1\",\"streetNumberRepetitionIndex\":\"bis\","
                        + "\"streetCategoryCode\":\"rue\",\"streetLabel\":\"Zorro\",\"distributionMention\":\"c/o Bernardo\",\"cedexOffice\":\"75117\","
                        + "\"postalCode\":\"75017\",\"communeCode\":\"75\",\"countryCode\":\"FR\",\"phone\":\"0123456789\",\"phone2\":\"0623456789\","
                        + "\"fax\":\"0198765432\",\"email\":\"structure@one.fr\",\"departmentCode\":\"99\",\"oldStructureId\":\"101\","
                        + "\"registrationAuthority\":\"CIA\"}}]}],\"psRefs\":[{\"nationalIdRef\": \"800000000001\",\"nationalId\": \"800000000001\","
                        + "\"activated\": 1638791221}]}"))
                .andExpect(status().is(201));
        assertThat(memoryAppender.contains("Ps 800000000001 successfully stored or updated", Level.INFO)).isTrue();
        assertThat(memoryAppender.contains("PsRef 800000000001 has been reactivated", Level.INFO)).isFalse();

        Ps storedPs = psRepository.findByNationalId("800000000001");
        String psAsJsonString = objectWriter.writeValueAsString(storedPs);
        System.out.println("---------------  RESULT  -------------");
        System.out.println(psAsJsonString);

        createdPs.andDo(document("PsOperationTest/create_new_Ps"));
    }

    @Test
    @DisplayName(value = "should reject post request if wrong content-type")
    public void createPsWrongContentTypeFailed() throws Exception {
        mockMvc.perform(post("/api/v2/ps").header("Accept", "application/json")
                .contentType("application/xml").content("{\"idType\":\"8\",\"id\":\"00000000001\"," +
                        "\"nationalId\":\"800000000001\"}"))
                .andExpect(status().is(415));
        assertThat(memoryAppender.contains("Ps 800000000001 successfully stored or updated", Level.INFO)).isFalse();
        assertThat(memoryAppender.contains("PsRef 800000000001 has been reactivated", Level.INFO)).isFalse();
    }

    @Test
    @DisplayName(value = "should reject post request if content-type absent")
    public void createPsAbsentContentTypeFailed() throws Exception {
        mockMvc.perform(post("/api/v2/ps").header("Accept", "application/json")
                .content("{\"idType\":\"8\",\"id\":\"00000000001\"," +
                        "\"nationalId\":\"800000000001\"}"))
                .andExpect(status().is(415));
        assertThat(memoryAppender.contains("Ps 800000000001 successfully stored or updated", Level.INFO)).isFalse();
        assertThat(memoryAppender.contains("PsRef 800000000001 has been reactivated", Level.INFO)).isFalse();
    }

    @Test
    @DisplayName(value = "should not create a Ps if already exists and still activated")
    @MongoDataSet(value = "/dataset/ps_2_psref_entries.json", cleanBefore = true, cleanAfter = true)
    public void createStillActivatedPsFailed() throws Exception {
        mockMvc.perform(post("/api/v2/ps").header("Accept", "application/json")
                .contentType("application/json").content("{\n" +
                        "\"idType\": \"8\",\n" +
                        "\"id\": \"00000000001\",\n" +
                        "\"nationalId\": \"800000000001\"\n" +
                        "}"))
                .andExpect(status().is(409));
        assertThat(memoryAppender.contains("Ps 800000000001 already exists and is activated, will not be updated", Level.WARN)).isTrue();
        assertThat(memoryAppender.contains("Ps 800000000001 successfully stored or updated", Level.INFO)).isFalse();
    }

    @Test
    @DisplayName(value = "should reactivate Ps if already exists")
    @MongoDataSet(value = "/dataset/deactivated_ps.json", cleanBefore = true, cleanAfter = true)
    public void reactivateExistingPs() throws Exception {
        mockMvc.perform(post("/api/v2/ps").header("Accept", "application/json")
                .contentType("application/json").content("{\n" +
                        "\"idType\": \"8\",\n" +
                        "\"id\": \"00000000002\",\n" +
                        "\"nationalId\": \"800000000002\"\n" +
                        "}"))
                .andExpect(status().is(201));
        assertThat(memoryAppender.contains("Ps 800000000002 successfully stored or updated", Level.INFO)).isTrue();
        assertThat(memoryAppender.contains("Ps 800000000002 has been reactivated", Level.INFO)).isTrue();
    }

    @Test
    @DisplayName(value = "should not create Ps if malformed request body")
    public void createMalformedPsFailed() throws Exception {
        mockMvc.perform(post("/api/v2/ps").header("Accept", "application/json")
                .contentType("application/json").content("{\"toto\":\"titi\"}"))
                .andExpect(status().is(400));
    }


    @Test
    @DisplayName(value = "should delete Ps by Id")
    @MongoDataSet(value = "/dataset/ps_2_psref_entries.json", cleanBefore = true, cleanAfter = true)
    public void deletePsById() throws Exception {
        ResultActions deletedPs = mockMvc.perform(delete("/api/v2/ps/800000000001"))
                .andExpect(status().is(204));

        assertThat(memoryAppender.contains("No Ps found with nationalId 800000000001, will not be deleted", Level.WARN)).isFalse();
        assertThat(memoryAppender.contains("Ps 800000000001 successfully deleted", Level.INFO)).isTrue();
        assertThat(memoryAppender.contains("Ps 800000000011 successfully deleted", Level.INFO)).isTrue();

        Ps ps = psRepository.findByIdsContaining("800000000001");
        Ps ps2 = psRepository.findByIdsContaining("800000000011");

        assertThat(ApiUtils.isPsActivated(ps)).isFalse();
        assertThat(ApiUtils.isPsActivated(ps2)).isFalse();

        deletedPs.andDo(document("PsOperationTest/delete_Ps_by_id"));
    }

    @Test
    @DisplayName(value = "should not delete Ps if not exists")
    @MongoDataSet(value = "/dataset/ps_2_psref_entries.json", cleanBefore = true, cleanAfter = true)
    @ExpectedMongoDataSet(value = "/dataset/ps_2_psref_entries.json")
    public void deletePsFailed() throws Exception {
        mockMvc.perform(delete("/api/v2/ps/800000000003")
                .header("Accept", "application/json"))
                .andExpect(status().is(410));

        assertThat(memoryAppender.contains("No Ps found with nationalId 800000000003, will not be deleted", Level.WARN)).isTrue();
        assertThat(memoryAppender.contains("Ps 800000000003 successfully deleted", Level.INFO)).isFalse();
    }
 
    @Test
    @DisplayName(value = "Search a PS by identity")
    @MongoDataSet(value = "/dataset/ps_search_by_identity.json", cleanBefore = true, cleanAfter = true)
    public void searchPs() throws Exception {
    	
    	// Only required criterias (and only one firstName)
    	ResultActions result = mockMvc.perform(
    	        get("/api/v2/ps/search")
                .header("Accept", "application/json")
                .param("lastName", "DUPONT")
                .param("firstNames", "JIMMY")
                .param("genderCode", "M")
                .param("birthdate", "1983-12-17")
        )
        .andExpect(status().is(200));
    	String responseBody = result.andReturn().getResponse().getContentAsString();

        assertTrue(responseBody.contains("800000000001") && responseBody.contains("800000000002"));
 
        // Only required criterias (and all firstnames)
    	result = mockMvc.perform(
    	        get("/api/v2/ps/search")
                .header("Accept", "application/json")
                .param("lastName", "DUPONT")
                .param("firstNames", "JIMMY BOB")
                .param("genderCode", "M")
                .param("birthdate", "1983-12-17")
        )
        .andExpect(status().is(200));
    	responseBody = result.andReturn().getResponse().getContentAsString();

        assertTrue(responseBody.contains("800000000001"));
        
        // Only required criterias + birthCountryCode
    	result = mockMvc.perform(
    	        get("/api/v2/ps/search")
                .header("Accept", "application/json")
                .param("lastName", "DUPONT")
                .param("firstNames", "JIMMY BOB")
                .param("genderCode", "M")
                .param("birthdate", "1983-12-17")
                .param("birthCountryCode","99000")
        )
        .andExpect(status().is(200));
    	responseBody = result.andReturn().getResponse().getContentAsString();

        assertTrue(responseBody.contains("800000000001"));
        
        // Only required criterias + birthCountryCode + birthAddressCode
    	result = mockMvc.perform(
    	        get("/api/v2/ps/search")
                .header("Accept", "application/json")
                .param("lastName", "DUPONT")
                .param("firstNames", "JIMMY BOB")
                .param("genderCode", "M")
                .param("birthdate", "1983-12-17")
                .param("birthCountryCode","99000")
                .param("birthTownCode","57463")
        )
        .andExpect(status().is(200));
    	responseBody = result.andReturn().getResponse().getContentAsString();

        assertTrue(responseBody.contains("800000000001"));
        
        // All criterias (+ optionals birthCountryCode, birthAddressCode, birthAddress)
    	result = mockMvc.perform(
    	        get("/api/v2/ps/search")
                .header("Accept", "application/json")
                .param("lastName", "DUPONT")
                .param("firstNames", "JIMMY BOB")
                .param("genderCode", "M")
                .param("birthdate", "1983-12-17")
                .param("birthCountryCode","99000")
                .param("birthTownCode","57463")
                .param("birthplace","METZ")
        )
        .andExpect(status().is(200));
    	responseBody = result.andReturn().getResponse().getContentAsString();

        assertTrue(responseBody.contains("800000000001"));
        
        // All criterias, fake firstname
    	result = mockMvc.perform(
    	        get("/api/v2/ps/search")
                .header("Accept", "application/json")
                .param("lastName", "DUPONT")
                .param("firstNames", "JIMMY BOB TOTO")
                .param("genderCode", "M")
                .param("birthdate", "1983-12-17")
                .param("birthCountryCode","99000")
                .param("birthTownCode","57463")
                .param("birthplace","METZ")
        )
        .andExpect(status().is(200));
    	responseBody = result.andReturn().getResponse().getContentAsString();

        assertEquals("[]", responseBody);
        
        // All criterias, fake birthCountryCode
    	result = mockMvc.perform(
    	        get("/api/v2/ps/search")
                .header("Accept", "application/json")
                .param("lastName", "DUPONT")
                .param("firstNames", "JIMMY BOB")
                .param("genderCode", "M")
                .param("birthdate", "1983-12-17")
                .param("birthCountryCode","99001")
                .param("birthTownCode","57463")
                .param("birthplace","METZ")
        )
        .andExpect(status().is(200));
    	responseBody = result.andReturn().getResponse().getContentAsString();

    	assertEquals("[]", responseBody);
    	
        // All criterias, fake birthCountryCode
    	result = mockMvc.perform(
    	        get("/api/v2/ps/search")
                .header("Accept", "application/json")
                .param("lastName", "DUPONT")
                .param("firstNames", "JIMMY BOB")
                .param("genderCode", "M")
                .param("birthdate", "1983-12-17")
                .param("birthCountryCode","99000")
                .param("birthTownCode","57464")
                .param("birthplace","METZ")
        )
        .andExpect(status().is(200));
    	responseBody = result.andReturn().getResponse().getContentAsString();

    	assertEquals("[]", responseBody);
    }

    @Test
    @DisplayName(value = "should update Ps")
    @MongoDataSet(value = "/dataset/ps_2_psref_entries.json", cleanBefore = true, cleanAfter = true)
    public void updatePs() throws Exception {
        Ps storedPs = psRepository.findByNationalId("800000000001");

        ResultActions updatedPs = mockMvc.perform(put("/api/v2/ps").header("Accept", "application/json")
                .contentType("application/json").content("{\n" +
                        "\"idType\": \"8\",\n" +
                        "\"id\": \"00000000001\",\n" +
                        "\"nationalId\": \"800000000001\"\n" +
                        "}"))
                .andExpect(status().is(200));

        Ps afterUpdatePs = psRepository.findByNationalId("800000000001");

        assertEquals(storedPs.getActivated(), afterUpdatePs.getActivated());
        assertThat(memoryAppender.contains("No Ps found with nationalId 800000000001, can not update it", Level.WARN)).isFalse();
        assertThat(memoryAppender.contains("Ps 800000000001 successfully updated", Level.INFO)).isTrue();

        updatedPs.andDo(document("PsOperationTest/update_Ps"));
    }

    @Test
    @DisplayName(value = "should retrieve all Ps")
    @MongoDataSet(value = "/dataset/ps_2_psref_entries.json", cleanBefore = true, cleanAfter = true)
    public void getPsByIdsContaining() {
        Ps storedPs = psRepository.findByIdsContaining("800000000001");
        System.out.println(storedPs);
    }

    @Test
    @DisplayName(value = "should not update Ps if not exists")
    public void updateAbsentPsFailed() throws Exception {
        mockMvc.perform(put("/api/v2/ps").header("Accept", "application/json")
                .contentType("application/json").content("{\n" +
                        "\"idType\": \"8\",\n" +
                        "\"id\": \"00000000001\",\n" +
                        "\"nationalId\": \"800000000001\"\n" +
                        "}"))
                .andExpect(status().is(410));

        assertThat(memoryAppender.contains("No Ps found with nationalId 800000000001, can not update it", Level.WARN)).isTrue();
        assertThat(memoryAppender.contains("Ps 800000000001 successfully updated", Level.INFO)).isFalse();
    }

    @Test
    @DisplayName(value = "should not update Ps if deactivated")
    @MongoDataSet(value = "/dataset/deactivated_ps.json", cleanBefore = true, cleanAfter = true)
    public void updateDeactivatedPsFailed() throws Exception {
        mockMvc.perform(put("/api/v2/ps").header("Accept", "application/json")
                .contentType("application/json").content("{\n" +
                        "\"idType\": \"8\",\n" +
                        "\"id\": \"00000000002\",\n" +
                        "\"nationalId\": \"800000000002\"\n" +
                        "}"))
                .andExpect(status().is(410));

        assertThat(memoryAppender.contains("Ps 800000000002 is deactivated, can not update it", Level.WARN)).isTrue();
        assertThat(memoryAppender.contains("Ps 800000000002 successfully updated", Level.INFO)).isFalse();
    }

    @Test
    @DisplayName(value = "should not update Ps if malformed request body")
    @MongoDataSet(value = "/dataset/ps_2_psref_entries.json", cleanBefore = true, cleanAfter = true)
    public void updateMalformedPsFailed() throws Exception {
        // Id not present
        mockMvc.perform(put("/api/v2/ps").header("Accept", "application/json")
                .contentType("application/json").content("{\n" +
                        "\"idType\": \"8\",\n" +
                        "\"id\": \"00000000001\",\n" +
                        "}"))
                .andExpect(status().is(400));

        // Id is blank
        mockMvc.perform(put("/api/v2/ps").header("Accept", "application/json")
                .contentType("application/json").content("{\n" +
                        "\"idType\": \"8\",\n" +
                        "\"id\": \"00000000001\",\n" +
                        "\"nationalId\": \"\"\n" +
                        "}"))
                .andExpect(status().is(400));
    }

    @Test
    @DisplayName(value = "should physically delete Ps")
    @MongoDataSet(value = "/dataset/3_ps_before_delete.json", cleanBefore = true, cleanAfter = true)
    @ExpectedMongoDataSet(value = "/dataset/1_ps_after_delete.json")
    public void physicalDeleteById() throws Exception {
        mockMvc.perform(delete("/api/v2/ps/force/800000000001")
                .header("Accept", "application/json"))
                .andExpect(status().is(204));

        assertThat(memoryAppender.contains("No Ps found with id 800000000001, could not delete it", Level.WARN)).isFalse();
        assertThat(memoryAppender.contains("Ps 800000000001 successfully deleted", Level.INFO)).isTrue();
        assertThat(memoryAppender.contains("Ps 800000000002 successfully deleted", Level.INFO)).isFalse();

        Integer psRefCount = 0;
        for (Ps ps : psRepository.findAll()) {
            if (ps.getIds() != null)
                psRefCount += ps.getIds().size();
        }

        assertEquals(psRefCount, 2);
        assertEquals(psRepository.count(), 2);

        // physical delete of deactivated Ps
        mockMvc.perform(delete("/api/v2/ps/force/800000000002")
                .header("Accept", "application/json"))
                .andExpect(status().is(204));

        assertThat(memoryAppender.contains("Ps 800000000002 successfully deleted", Level.INFO)).isTrue();

        psRefCount = 0;
        for (Ps ps : psRepository.findAll()) {
            psRefCount += ps.getIds().size();
        }

        assertEquals(psRefCount, 1);
        assertEquals(psRepository.count(), 1);
    }

    @Test
    @DisplayName(value = "should update Ps and merge RPPS professional information in the PS")
    @MongoDataSet(value = "/dataset/updatePSIAndMerge_RPPS_example.json", cleanBefore = true, cleanAfter = true)
    public void updatePsAndMerge() throws Exception {


        ResultActions updatedPs = mockMvc
                .perform(put("/api/v2/ps").param("extraId", "800000000001").header("Accept", "application/json")
                        .contentType("application/json")
                        .content("{\n" + "\"idType\": \"8\",\n" + "\"id\": \"855e8700-e29b-41d4-a716-44665544111\",\n"
                                + "\"lastName\": \"MUNOZ\",\n"
                                + "\"nationalId\": \"855e8700-e29b-41d4-a716-44665544111\"\n" + "}"))
                .andExpect(status().is(200));

        Ps finalPs = psRepository.findByNationalId("855e8700-e29b-41d4-a716-44665544111");

        assertTrue(finalPs.getIds().contains("855e8700-e29b-41d4-a716-44665544111"));
        assertTrue(finalPs.getIds().contains("800000000001"));
        assertTrue("MUNOZ".equals(finalPs.getLastName()));
        assertTrue("".equals(finalPs.getIdType()));
        assertTrue("12".equals(finalPs.getProfessions().get(0).getCode()));

        updatedPs.andDo(document("PsOperationTest/update_Ps"));
    }

    @Test
    @DisplayName(value = "should update Psi with new Psi informations")
    @MongoDataSet(value = "/dataset/updatePsiWithSamePsiExtraId.json", cleanBefore = true, cleanAfter = true)
    public void updatePsiWithSamePsiExtraId() throws Exception {

        ResultActions updatedPs = mockMvc
                .perform(put("/api/v2/ps").param("extraId", "855e8700-e29b-41d4-a716-44665544111")
                        .header("Accept", "application/json").contentType("application/json")
                        .content("{\n" + "\"idType\": \"10\",\n" + "\"id\": \"855e8700-e29b-41d4-a716-44665544111\",\n"
                                + "\"lastName\": \"toto\",\n"
                                + "\"nationalId\": \"855e8700-e29b-41d4-a716-44665544111\"\n" + "}"))
                .andExpect(status().is(200));

        Ps finalPs = psRepository.findByNationalId("855e8700-e29b-41d4-a716-44665544111");

        assertTrue(finalPs.getIds().contains("855e8700-e29b-41d4-a716-44665544111"));
        assertTrue("toto".equals(finalPs.getLastName()));
        assertTrue("10".equals(finalPs.getIdType()));
        assertTrue("EVRARD".equals(finalPs.getProfessions().get(0).getLastName()));

        updatedPs.andDo(document("PsOperationTest/update_Ps"));
    }

    // ─── Itération 2 : PUT /v2/ps/{psId}/activity (upsert single profession) ───

    @Test
    @DisplayName(value = "upsertPsActivity: ajoute une nouvelle practice quand le sourceId n'existe pas")
    @MongoDataSet(value = "/dataset/ps_2_psref_entries.json", cleanBefore = true, cleanAfter = true)
    public void upsertPsActivity_addsNewWhenSourceIdAbsent() throws Exception {
        // Le dataset initial a 1 practice tagged sourceId=null. On ajoute une practice sourceId=NEW_RPPS.
        String newProfessionJson = "{"
                + "\"sourceId\":\"810099999999\","
                + "\"code\":\"21\","
                + "\"lastName\":\"DUPONT\","
                + "\"firstName\":\"JIMMY\","
                + "\"workSituations\":[{\"modeCode\":\"L\",\"activitySectorCode\":\"SA07\"}]"
                + "}";

        mockMvc.perform(put("/api/v2/ps/800000000001/activity")
                        .header("Accept", "application/json")
                        .contentType("application/json")
                        .content(newProfessionJson))
                .andExpect(status().is(200));

        Ps stored = psRepository.findByNationalId("800000000001");
        assertEquals(2, stored.getProfessions().size());
        assertThat(memoryAppender.contains("Activity upserted on PS 800000000001 for sourceId 810099999999", Level.INFO)).isTrue();
    }

    @Test
    @DisplayName(value = "upsertPsActivity: remplace la practice existante quand le sourceId match")
    @MongoDataSet(value = "/dataset/ps_with_rpps_practice.json", cleanBefore = true, cleanAfter = true)
    public void upsertPsActivity_replacesExistingWhenSourceIdMatches() throws Exception {
        // ps_with_rpps_practice.json a 1 practice tagged sourceId=810000000001 + 1 sourceId=510000000002.
        String updatedProfessionJson = "{"
                + "\"sourceId\":\"810000000001\","
                + "\"code\":\"99\","
                + "\"lastName\":\"DUPONT\","
                + "\"firstName\":\"NOUVEAU\","
                + "\"workSituations\":[{\"modeCode\":\"L\",\"activitySectorCode\":\"SA77\"}]"
                + "}";

        mockMvc.perform(put("/api/v2/ps/800000000001/activity")
                        .header("Accept", "application/json")
                        .contentType("application/json")
                        .content(updatedProfessionJson))
                .andExpect(status().is(200));

        Ps stored = psRepository.findByNationalId("800000000001");
        // Toujours 2 practices (la SIRET intacte, la RPPS remplacée)
        assertEquals(2, stored.getProfessions().size());
        // La practice avec sourceId=810000000001 a maintenant code=99 et firstName=NOUVEAU
        boolean rppsReplaced = stored.getProfessions().stream()
                .anyMatch(p -> "810000000001".equals(p.getSourceId())
                        && "99".equals(p.getCode())
                        && "NOUVEAU".equals(p.getFirstName()));
        assertTrue(rppsReplaced, "La practice sourceId=810000000001 doit avoir été remplacée");
        // La practice SIRET reste intacte
        boolean siretUntouched = stored.getProfessions().stream()
                .anyMatch(p -> "510000000002".equals(p.getSourceId()));
        assertTrue(siretUntouched, "La practice sourceId=510000000002 doit rester intacte");
    }

    @Test
    @DisplayName(value = "upsertPsActivity: 400 si sourceId manquant dans le body")
    @MongoDataSet(value = "/dataset/ps_2_psref_entries.json", cleanBefore = true, cleanAfter = true)
    public void upsertPsActivity_rejectsMissingSourceId() throws Exception {
        String noSourceIdJson = "{\"code\":\"99\",\"lastName\":\"DUPONT\"}";

        mockMvc.perform(put("/api/v2/ps/800000000001/activity")
                        .header("Accept", "application/json")
                        .contentType("application/json")
                        .content(noSourceIdJson))
                .andExpect(status().is(400));
    }

    @Test
    @DisplayName(value = "upsertPsActivity: 400 si sourceId est un UUID (PSI-source interdit)")
    @MongoDataSet(value = "/dataset/ps_2_psref_entries.json", cleanBefore = true, cleanAfter = true)
    public void upsertPsActivity_rejectsUuidSourceId() throws Exception {
        String uuidSourceIdJson = "{"
                + "\"sourceId\":\"019ce28d-aa83-7c4b-b7bf-afc3ef900cf8\","
                + "\"code\":\"99\""
                + "}";

        mockMvc.perform(put("/api/v2/ps/800000000001/activity")
                        .header("Accept", "application/json")
                        .contentType("application/json")
                        .content(uuidSourceIdJson))
                .andExpect(status().is(400));
    }

    @Test
    @DisplayName(value = "upsertPsActivity: 410 si compte introuvable")
    public void upsertPsActivity_returnsGoneWhenPsNotFound() throws Exception {
        String validJson = "{\"sourceId\":\"810099999999\",\"code\":\"21\"}";

        mockMvc.perform(put("/api/v2/ps/999999999999/activity")
                        .header("Accept", "application/json")
                        .contentType("application/json")
                        .content(validJson))
                .andExpect(status().is(410));
    }

    // ─── Fallback usualLastName à la création (règle de gestion validée) ───

    @Test
    @DisplayName(value = "createNewPs PSI: usualLastName non fourni → fallback sur lastName")
    public void createNewPsi_defaultsUsualLastNameToLastName() throws Exception {
        // PSI account (UUID nationalId), pas de usualLastName fourni.
        String psiJson = "{"
                + "\"nationalId\":\"019ce28d-aa83-7c4b-b7bf-afc3ef900cf8\","
                + "\"lastName\":\"DUPONT\","
                + "\"firstNames\":[{\"firstName\":\"JIMMY\",\"order\":1}]"
                + "}";

        mockMvc.perform(post("/api/v2/ps")
                        .header("Accept", "application/json")
                        .contentType("application/json")
                        .content(psiJson))
                .andExpect(status().is(201));

        Ps stored = psRepository.findByNationalId("019ce28d-aa83-7c4b-b7bf-afc3ef900cf8");
        assertEquals("DUPONT", stored.getUsualLastName(),
                "PSI sans usualLastName doit fallback sur lastName");
    }

    @Test
    @DisplayName(value = "createNewPs non-PSI: usualLastName non fourni → fallback sur lastName de la 1ʳᵉ practice RPPS (sourceId commence par 8)")
    public void createNewPsNonPsi_defaultsUsualLastNameToFirstRppsPractice() throws Exception {
        // Non-PSI (RPPS), avec lastName="LEGAL" mais professions[0].lastName="USAGE".
        // sourceId est auto-tagué à partir du nationalId "800000000099" → commence par "8" → RPPS.
        // Doit fallback sur le professional lastName de cette practice.
        String nonPsiJson = "{"
                + "\"idType\":\"8\","
                + "\"id\":\"00000000099\","
                + "\"nationalId\":\"800000000099\","
                + "\"lastName\":\"LEGAL\","
                + "\"firstNames\":[{\"firstName\":\"JANE\",\"order\":1}],"
                + "\"professions\":[{"
                + "  \"code\":\"50\",\"lastName\":\"USAGE\",\"firstName\":\"JANE\","
                + "  \"workSituations\":[{\"modeCode\":\"L\",\"activitySectorCode\":\"SA04\"}]"
                + "}]"
                + "}";

        mockMvc.perform(post("/api/v2/ps")
                        .header("Accept", "application/json")
                        .contentType("application/json")
                        .content(nonPsiJson))
                .andExpect(status().is(201));

        Ps stored = psRepository.findByNationalId("800000000099");
        assertEquals("USAGE", stored.getUsualLastName(),
                "Non-PSI sans usualLastName doit fallback sur le lastName de la 1ʳᵉ practice RPPS");
    }

    @Test
    @DisplayName(value = "createNewPs non-PSI: usualLastName non fourni avec mix de sourceIds → priorité à la practice RPPS (sourceId commence par 8)")
    public void createNewPsNonPsi_defaultsUsualLastNamePrioritisesRppsPractice() throws Exception {
        // Compte non-PSI avec 2 professions :
        //   - sourceId "300000123456" (FINESS, commence par "3") → lastName="FINESS_NAME"
        //   - sourceId "810000123456" (RPPS, commence par "8") → lastName="RPPS_NAME"
        // La règle prend la 1ʳᵉ practice dont le sourceId commence par "8" → "RPPS_NAME".
        // Le nationalId du compte est "300000123456" (FINESS), donc le sourceId par défaut
        // serait "3..." mais on en force un explicitement à "8..." dans le body.
        String nonPsiJson = "{"
                + "\"idType\":\"3\","
                + "\"id\":\"00000123456\","
                + "\"nationalId\":\"300000123456\","
                + "\"lastName\":\"LEGAL\","
                + "\"firstNames\":[{\"firstName\":\"JANE\",\"order\":1}],"
                + "\"professions\":["
                + "  {"
                + "    \"code\":\"10\",\"lastName\":\"FINESS_NAME\",\"firstName\":\"JANE\","
                + "    \"sourceId\":\"300000123456\","
                + "    \"workSituations\":[{\"modeCode\":\"L\",\"activitySectorCode\":\"SA05\"}]"
                + "  },"
                + "  {"
                + "    \"code\":\"50\",\"lastName\":\"RPPS_NAME\",\"firstName\":\"JANE\","
                + "    \"sourceId\":\"810000123456\","
                + "    \"workSituations\":[{\"modeCode\":\"L\",\"activitySectorCode\":\"SA04\"}]"
                + "  }"
                + "]"
                + "}";

        mockMvc.perform(post("/api/v2/ps")
                        .header("Accept", "application/json")
                        .contentType("application/json")
                        .content(nonPsiJson))
                .andExpect(status().is(201));

        Ps stored = psRepository.findByNationalId("300000123456");
        assertEquals("RPPS_NAME", stored.getUsualLastName(),
                "Avec mix de sourceIds, la practice RPPS (sourceId commence par 8) doit avoir priorité");
    }

    @Test
    @DisplayName(value = "createNewPs non-PSI: usualLastName non fourni et aucune practice RPPS → fallback sur lastName")
    public void createNewPsNonPsi_noRppsPractice_fallbackOnLastName() throws Exception {
        // Compte FINESS pur (sourceId auto-tagué "3...") sans aucune practice RPPS.
        // Aucune règle spécifique ne match → fallback final sur lastName.
        String nonPsiJson = "{"
                + "\"idType\":\"3\","
                + "\"id\":\"00000654321\","
                + "\"nationalId\":\"300000654321\","
                + "\"lastName\":\"LEGAL\","
                + "\"firstNames\":[{\"firstName\":\"JANE\",\"order\":1}],"
                + "\"professions\":[{"
                + "  \"code\":\"10\",\"lastName\":\"FINESS_NAME\",\"firstName\":\"JANE\","
                + "  \"workSituations\":[{\"modeCode\":\"L\",\"activitySectorCode\":\"SA05\"}]"
                + "}]"
                + "}";

        mockMvc.perform(post("/api/v2/ps")
                        .header("Accept", "application/json")
                        .contentType("application/json")
                        .content(nonPsiJson))
                .andExpect(status().is(201));

        Ps stored = psRepository.findByNationalId("300000654321");
        assertEquals("LEGAL", stored.getUsualLastName(),
                "Sans practice RPPS, usualLastName doit fallback sur lastName");
    }

    @Test
    @DisplayName(value = "createNewPs: usualLastName fourni explicitement → respecté (pas de fallback)")
    public void createNewPs_explicitUsualLastNameIsKept() throws Exception {
        String json = "{"
                + "\"idType\":\"8\","
                + "\"id\":\"00000000098\","
                + "\"nationalId\":\"800000000098\","
                + "\"lastName\":\"LEGAL\","
                + "\"usualLastName\":\"EXPLICIT\","
                + "\"firstNames\":[{\"firstName\":\"JANE\",\"order\":1}],"
                + "\"professions\":[{"
                + "  \"code\":\"50\",\"lastName\":\"PROF_NAME\","
                + "  \"workSituations\":[{\"modeCode\":\"L\",\"activitySectorCode\":\"SA04\"}]"
                + "}]"
                + "}";

        mockMvc.perform(post("/api/v2/ps")
                        .header("Accept", "application/json")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().is(201));

        Ps stored = psRepository.findByNationalId("800000000098");
        assertEquals("EXPLICIT", stored.getUsualLastName(),
                "usualLastName fourni doit être préservé (pas écrasé par le fallback)");
    }
}

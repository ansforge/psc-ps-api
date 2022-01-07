package fr.ans.psc.delegate;

import fr.ans.psc.api.StructureApiDelegate;
import fr.ans.psc.model.Structure;
import fr.ans.psc.repository.StructureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class StructureApiDelegateImpl implements StructureApiDelegate {

    private final StructureRepository structureRepository;
    private final MongoTemplate mongoTemplate;

    public StructureApiDelegateImpl(StructureRepository structureRepository, MongoTemplate mongoTemplate) {
        this.structureRepository = structureRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public ResponseEntity<Structure> getStructureById(String encodedStructureId) {

        String structureId = URLDecoder.decode(encodedStructureId, StandardCharsets.UTF_8);
        Structure structure = structureRepository.findByStructureTechnicalId(structureId);
        if (structure == null) {
            log.warn("Structure {} not found", structureId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            log.info("Structure {} has been found", structureId);
            return new ResponseEntity<>(structure, HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<Void> createNewStructure(Structure structure) {
        Structure storedStructure = structureRepository.findByStructureTechnicalId(structure.getStructureTechnicalId());

        if (storedStructure != null) {
            log.warn("Structure {} exists already", structure.getStructureTechnicalId());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        mongoTemplate.save(structure);
        log.info("New structure created with structure technical id {}", structure.getStructureTechnicalId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteStructureByStructureId(String encodedStructureId) {
        String structureId = URLDecoder.decode(encodedStructureId, StandardCharsets.UTF_8);
        Structure storedStructure = structureRepository.findByStructureTechnicalId(structureId);

        if (storedStructure == null) {
            log.warn("Structure {} not found", structureId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        mongoTemplate.remove(storedStructure);
        log.info("Structure {} successfully removed", structureId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> updateStructure(Structure structure) {
        Structure storedStructure = structureRepository.findByStructureTechnicalId(structure.getStructureTechnicalId());

        if (storedStructure == null) {
            log.warn("Structure {} not found", structure.getStructureTechnicalId());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        structure.set_id(storedStructure.get_id());
        mongoTemplate.save(structure);
        log.info("Structure {} successfully updated", structure.getStructureTechnicalId());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

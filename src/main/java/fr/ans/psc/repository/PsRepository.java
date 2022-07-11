package fr.ans.psc.repository;

import fr.ans.psc.model.Ps;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PsRepository extends MongoRepository<Ps, String> {

    Ps findByNationalId(String nationalId);

    Ps findByIdsContaining(String id);

    @Aggregation(pipeline = {
        "{$match: {$expr: {$gt: ['$activated', '$deactivated']}}}",
        "{$unwind: {path: '$professions', preserveNullAndEmptyArrays: true}}",
        "{$unwind: {path: '$professions.expertises', preserveNullAndEmptyArrays: true}}",
        "{$unwind: {path: '$professions.workSituations', preserveNullAndEmptyArrays: true}}",
        "{$unwind: {path: '$professions.workSituations.structure', preserveNullAndEmptyArrays: true}}",
    })
    Page<Ps> aggregateForExtraction(Pageable pageable);
}

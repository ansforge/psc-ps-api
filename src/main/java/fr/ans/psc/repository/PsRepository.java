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
        "{$project: {"+
            "_id: 0,"+
            "idType: '$idType',"+
            "id: '$id',"+
            "nationalId: '$nationalId',"+
            "lastName: '$lastName',"+
            "firstNames: '$firstNames',"+
            "dateOfBirth: '$dateOfBirth',"+
            "birthAddressCode: '$birthAddressCode',"+
            "birthCountryCode: '$birthCountryCode',"+
            "birthAddress: '$birthAddress',"+
            "genderCode: '$genderCode',"+
            "phone: '$phone',"+
            "email: '$email',"+
            "salutationCode: '$salutationCode',"+
            "professions: '$professions',"+
            "ids: {" +
                "$reduce: {" +
                    "input: '$otherIds', initialValue: '', in: {" +
                        "$concat: ['$$value',' ','$$this']" +
                    "}" +
                "}" +
            "}," +
            "activated: '$activated'," +
            "deactivated: '$deactivated'"+
        "}}"
    })
    Page<Ps> aggregateForExtraction(Pageable pageable);
}

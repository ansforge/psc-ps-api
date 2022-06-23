package fr.ans.psc.repository;

import fr.ans.psc.model.Ps;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PsRepository extends MongoRepository<Ps, String> {

    Ps findByNationalId(String nationalId);

    Ps findByIdsContaining(String id);

}

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
        "{$unwind: {path:'$firstNames'}}",
        "{$sort: {'firstNames.order':1}}",
        "{$group: {"+
            "'_id': '$_id',"+
            "'idType':{'$first':'$idType'},"+
            "'id':{'$first':'$id'},"+
            "'nationalId':{'$first':'$nationalId'},"+
            "'lastName':{'$first':'$lastName'},"+
            "'firstNames': {$push: '$firstNames'},"+
            "'dateOfBirth':{'$first':'$dateOfBirth'},"+
            "'birthAddressCode':{'$first':'$birthAddressCode'},"+
            "'birthCountryCode':{'$first':'$birthCountryCode'},"+
            "'birthAddress':{'$first':'$birthAddress'},"+
            "'genderCode':{'$first':'$genderCode'},"+
            "'phone':{'$first':'$phone'},"+
            "'email':{'$first':'$email'},"+
            "'salutationCode':{'$first':'$salutationCode'},"+
            "'professions':{'$first':'$professions'},"+
            "'otherIds':{$first:'$ids'}" +
        "}}",
        "{$addFields: { " +
            "firstNamesString: {" +
                "$reduce: {" +
                    "input: '$firstNames',"+
                    "initialValue: '',"+
                    "in: {" +
                        "$concat: ['$$value', '$$this.firstName',\"'\"]" +
                    "}"+
                "}" +
            "}" +
        "}}",
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
            "firstName: {$substrCP: ['$firstNamesString',0,{$add:[{$strLenCP:'$firstNamesString'},-1]}]},"+
            "dateOfBirth: '$dateOfBirth',"+
            "birthAddressCode: '$birthAddressCode',"+
            "birthCountryCode: '$birthCountryCode',"+
            "birthAddress: '$birthAddress',"+
            "genderCode: '$genderCode',"+
            "phone: '$phone',"+
            "email: '$email',"+
            "salutationCode: '$salutationCode',"+
            "profession_code: '$professions.code',"+
            "profession_categoryCode: '$professions.categoryCode',"+
            "profession_salutationCode: '$professions.salutationCode',"+
            "profession_lastName: '$professions.lastName',"+
            "profession_firstName: '$professions.firstName',"+
            "profession_expertise_typeCode: '$professions.expertises.typeCode',"+
            "profession_expertise_code: '$professions.expertises.code',"+
            "profession_situation_modeCode: '$professions.workSituations.modeCode',"+
            "profession_situation_activitySectorCode: '$professions.workSituations.activitySectorCode',"+
            "profession_situation_pharmacistTableSectionCode: '$professions.workSituations.pharmacistTableSectionCode',"+
            "profession_situation_roleCode: '$professions.workSituations.roleCode',"+
            "structure_siteSIRET: '$professions.workSituations.structure.siteSIRET',"+
            "structure_siteSIREN: '$professions.workSituations.structure.siteSIREN',"+
            "structure_siteFINESS: '$professions.workSituations.structure.siteFINESS',"+
            "structure_legalEstablishmentFINESS: '$professions.workSituations.structure.legalEstablishmentFINESS',"+
            "structure_structureTechnicalId: '$professions.workSituations.structure.structureTechnicalId',"+
            "structure_legalCommercialName: '$professions.workSituations.structure.legalCommercialName',"+
            "structure_publicCommercialName: '$professions.workSituations.structure.publicCommercialName',"+
            "structure_recipientAdditionalInfo: '$professions.workSituations.structure.recipientAdditionalInfo',"+
            "structure_geoLocationAdditionalInfo: '$professions.workSituations.structure.geoLocationAdditionalInfo',"+
            "structure_streetNumber: '$professions.workSituations.structure.streetNumber',"+
            "structure_streetNumberRepetitionIndex: '$professions.workSituations.structure.streetNumberRepetitionIndex',"+
            "structure_streetCategoryCode: '$professions.workSituations.structure.streetCategoryCode',"+
            "structure_streetLabel: '$professions.workSituations.structure.streetLabel',"+
            "structure_distributionMention: '$professions.workSituations.structure.distributionMention',"+
            "structure_cedexOffice: '$professions.workSituations.structure.cedexOffice',"+
            "structure_postalCode: '$professions.workSituations.structure.postalCode',"+
            "structure_communeCode: '$professions.workSituations.structure.communeCode',"+
            "structure_countryCode: '$professions.workSituations.structure.countryCode',"+
            "structure_phone: '$professions.workSituations.structure.phone',"+
            "structure_phone2: '$professions.workSituations.structure.phone2',"+
            "structure_fax: '$professions.workSituations.structure.fax',"+
            "structure_email: '$professions.workSituations.structure.email',"+
            "structure_departmentCode: '$professions.workSituations.structure.departmentCode',"+
            "structure_oldStructureId: '$professions.workSituations.structure.oldStructureId',"+
            "profession_situation_registrationAuthority: '$professions.workSituations.registrationAuthority',"+
            "profession_situation_activityKindCode: '$professions.workSituations.activityKindCode',"+
            "otherIds: {" +
                "$reduce: {" +
                    "input: '$otherIds', initialValue: '', in: {" +
                        "$concat: ['$$value',' ','$$this']" +
                    "}" +
                "}" +
            "}"+
        "}}"
    })
    Page<Ps> aggregateForExtraction(Pageable pageable);
}

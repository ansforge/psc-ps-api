# Changelog — psc-ps-api

## [2026-04-01]

### Modifié

#### `GET /v2/ps/search/name` (`rechercherParNomPrenom`)
- La recherche par nom couvre désormais à la fois le nom d'état civil (`lastNameLower`) et le nom d'exercice (`professions.lastNameLower`) via un `$or` MongoDB
- Remplacement de la recherche par regex (insensible à la casse) par une recherche par égalité (`is()`) sur `professions.lastNameLower` — plus performant, utilise l'index
- Ajout du champ calculé `lastNameLower` dans `fr.ans.psc.model.Profession`, alimenté automatiquement par `setLastName()`

### Migration base de données requise
- Créer les index : `search_nom_prenom_idx` (`lastNameLower + firstNamesLowerArray`) et `search_profession_lastname_idx` (`professions.lastNameLower`)
- Exécuter le script `migration-scripts/03-migrate-profession-lastname-lower.js` pour renseigner `professions.lastNameLower` sur les documents existants

---

## [2026-03-31]

### Modifié

#### `GET /v2/ps/search/name` (`rechercherParNomPrenom`)
- La réponse retourne désormais `List<PsSearchResult>` au lieu de `List<PsNameSearchResult>`
- Chaque résultat contient :
  - `nationalId` : identifiant national du PS
  - `professionCode` : code de la première profession du PS
  - `workLocations` : liste des lieux d'exercice de la première profession, chacun avec :
    - `companyName` : raison sociale (`legalCommercialName`)
    - `companyCedexOffice` : code cedex (`cedexOffice`)

#### `GET /v2/ps/search` (`rechercherNationalIdParTraitsIdentite`)
- Inchangé — retourne toujours `List<String>` (nationalIds)

### Ajouté
- `fr.ans.psc.model.PsSearchResult` — nouveau DTO de réponse pour `/v2/ps/search/name`
- `fr.ans.psc.model.WorkLocation` — DTO représentant un lieu d'exercice

### Supprimé
- `fr.ans.psc.model.PsNameSearchResult` — remplacé par `PsSearchResult`
- Schéma `PsNameSearchResult` retiré du fichier `psc-api-maj-v2.yaml`

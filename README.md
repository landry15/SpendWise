# SpendWise — Gestion de dépenses personnelles

Application Android native (100 % hors-ligne) permettant de suivre ses dépenses quotidiennes.
Projet d'examen de Programmation mobile BAC3, réalisé en groupe de 4 étudiants.


## 1. Informations générales

| Élément | Détail |
|---|---|
| Groupe | **[À COMPLÉTER : numéro / nom du groupe]** |
| Établissement | **[À COMPLÉTER]** |
| Langage | **[À COMPLÉTER : Kotlin ou Java]** |
| SDK minimum | API 24 (Android 7.0) |
| Stockage | Base locale **[À COMPLÉTER : Room ou SQLite]**, aucune API distante |
| Architecture | **[À COMPLÉTER : MVVM ou MVC]** |

---

## 2. Fonctionnalités (4 modules)

| Module | Contenu | Responsable |
|---|---|---|
| 1 — Authentification & Profil | Inscription (nom, email, mot de passe), connexion, stockage sécurisé (hash du mot de passe), écran de profil (modification du nom, déconnexion) | **[À COMPLÉTER]** |
| 2 — Gestion des dépenses (CRUD) | Ajout, liste, modification, suppression ; validation (montant > 0, date valide, etc.) | **[À COMPLÉTER]** |
| 3 — Tableau de bord & statistiques | Total du mois, répartition par catégorie, filtres jour / semaine / mois, budget mensuel restant | **[À COMPLÉTER]** |
| 4 — Export & paramètres | Export CSV et partage, paramètres (devise, thème clair/sombre, réinitialisation des données), gestion des catégories | **[À COMPLÉTER]** |

---

## 3. Prérequis

- Android Studio : **[À COMPLÉTER : version utilisée]**
- JDK : **[À COMPLÉTER : version]**
- Android SDK : plateforme **[À COMPLÉTER]**, outils de build **[À COMPLÉTER]**
- Gradle : via le wrapper fourni (`gradlew`), version **[À COMPLÉTER]**
- Dépendances principales (voir `app/build.gradle`) : **[À COMPLÉTER : Room, ViewModel/LiveData, etc.]**
- Appareil cible : émulateur ou téléphone Android 7.0 (API 24) minimum

---

## 4. Compiler le projet pas à pas

1. Cloner le dépôt :
   ```bash
   git clone [À COMPLÉTER : URL du dépôt]
   cd SpendWise
   ```
2. Ouvrir le dossier dans Android Studio (**File > Open**).
3. Attendre la fin de la synchronisation Gradle (**Sync Project with Gradle Files** si besoin).
4. Générer l'APK :
   - Depuis Android Studio : **Build > Build Bundle(s) / APK(s) > Build APK(s)**
   - Ou en ligne de commande :
     ```bash
     ./gradlew assembleDebug
     ```
5. L'APK est produit dans :
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
   L'APK debug est signé automatiquement avec la clé de debug, ce qui le rend installable.

---

## 5. Installer et lancer l'APK

**Sur émulateur ou appareil physique (depuis Android Studio)**
1. Créer un émulateur (API 24 ou plus) via **Device Manager**, ou brancher un téléphone avec le
   débogage USB activé.
2. Cliquer sur **Run ▶** pour installer et lancer l'application.

**Avec l'APK directement**
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Sur un téléphone, copier l'APK puis autoriser l'installation depuis des sources inconnues si Android le demande.

---

## 6. Comptes de test

| Nom | Email | Mot de passe | Remarque |
|---|---|---|---|
| **[À COMPLÉTER]** | **[À COMPLÉTER]** | **[À COMPLÉTER]** | **[À COMPLÉTER]** |

*Indiquer uniquement les comptes réellement créés pour la démonstration. S'il n'y en a pas, expliquer
comment créer un compte depuis l'écran d'inscription.*

---

## 7. Structure du projet

```
SpendWise/
├── app/
│   └── src/main/
│       ├── java/ (ou kotlin/)  → **[À COMPLÉTER : packages réels, ex. data, ui, viewmodel]**
│       └── res/                → layouts, strings, thèmes
├── docs/                       → diagrammes, maquettes
├── README.md
└── build.gradle
```

*Adapter l'arbre à la structure réelle du code.*

---

## 8. Documentation et livrables

Dossier de rendu : `SpendWise_GroupeX/`

| Fichier | Contenu |
|---|---|
| `app-debug.apk` | APK fonctionnel, signé et testé |
| `doc_conception.pdf` | Cas d'utilisation (UML), schéma de la base, maquettes, architecture, choix techniques |
| `doc_installation.pdf` | Prérequis, procédure de build, installation, comptes de test, limitations |
| `contribution.xlsx` | Fiche de contribution individuelle signée par tous les membres |
| Code source | Dépôt Git avec historique (ou archive ZIP) |

---

## 9. Organisation du travail avec Git

- Une branche par module (par exemple `module-auth`, `module-depenses`, `module-stats`, `module-export`).
- Commits clairs, identifiables par auteur.
- Fusion sur `main` en fin de séance, pas de développement direct sur `main`.

---

## 10. Équipe et contributions

La somme des pourcentages doit faire 100 %. Les contributions peuvent être inégales si elles sont
justifiées, et chaque membre doit pouvoir expliquer oralement la partie qu'il a réalisée.

| Nom & Prénom | Module(s) réalisé(s) | Tâches concrètes effectuées | % de contribution |
|---|---|---|---|
| **[À COMPLÉTER]** | | | |
| **[À COMPLÉTER]** | | | |
| **[À COMPLÉTER]** | | | |
| **[À COMPLÉTER]** | | | |
| **Total** | | | **100 %** |

---

## 11. Problèmes connus et limitations

- **[À COMPLÉTER : lister honnêtement ce qui ne fonctionne pas encore ou reste limité]**

---

## 12. Checklist avant dépôt

- [ ] APK installé et testé sur émulateur/appareil
- [ ] Les 4 modules sont accessibles et fonctionnels
- [ ] Documentation de conception complète (UML, BDD, maquettes, architecture)
- [ ] Documentation d'installation pas à pas
- [ ] Fiche de contribution remplie, signée, somme = 100 %
- [ ] Comptes de test fournis dans la documentation
- [ ] Code source inclus (archive ZIP) avec historique Git si utilisé

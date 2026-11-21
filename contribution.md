# SpendWise — Fiche de Contribution Individuelle & Guide de Soutenance

**Établissement / Promotion :** BAC3 — Examen de Programmation Mobile  
**Projet :** SpendWise (Application Android hors-ligne)  
**Dépôt Git recommandé :** 1 branche principale (`main`) + 4 branches fonctionnelles (`feat/auth-profile`, `feat/expenses-crud`, `feat/dashboard-stats`, `feat/export-settings`) fusionnées par Pull Requests.

---

## 1. Tableau de répartition des contributions (Total = 100 %)

> *Complétez les noms, prénoms et signatures des 4 membres du groupe avant remise du PDF.*

| Membre (Nom & Prénom) | Branche Git & Module(s) réalisé(s) | Tâches concrètes effectuées | % Contribution | Signature |
| :--- | :--- | :--- | :---: | :---: |
| **Étudiant 1 :** *[Landry Mulumeoderhwa]* | `feat/auth-profile`<br>**Module 1 : Authentification & Profil** + Socle Room/DataStore | Conception du schéma `UserEntity`/`UserDao`, hachage salé PBKDF2 (`PasswordHasher`), gestion de session DataStore, écrans `LoginScreen`, `RegisterScreen`, `ProfileScreen`, pré-remplissage démo (`DatabaseSeeder`). | **25 %** | _________________ |
| **Étudiant 2 :** *[Landry Mulumeoderhwa]* | `feat/expenses-crud`<br>**Module 2 : Gestion des Dépenses (CRUD)** | Modèles `ExpenseEntity`, `ExpenseWithCategory`, `ExpenseDao`, `ExpenseRepository`, validation des champs (`ValidationUtils`), `ExpensesViewModel`, écran `ExpensesScreen`, dialogue `AddEditExpenseDialog` avec `DatePickerDialog` et confirmation de suppression. | **25 %** | _________________ |
| **Étudiant 3 :** *[Kupa Mangondo Pascal]* | `feat/dashboard-stats`<br>**Module 3 : Tableau de Bord & Statistiques** | Agrégation réactive dans `DashboardViewModel` (totaux jour/semaine/mois, budget restant), graphique circulaire `SpendingDonutChart` (Compose `Canvas`), barres de répartition, filtres par période et catégorie, configuration du budget mensuel. | **25 %** | _________________ |
| **Étudiant 4 :** *[Kupa Mangondo Pascal]* | `feat/export-settings`<br>**Module 4 : Export CSV, Paramètres & Documentation** | `CsvExporter` (UTF-8 BOM, protection formules, `FileProvider` + `Intent.ACTION_SEND`), gestion des catégories personnalisées (`CategoryRepository`), changement de thème/devise, réinitialisation sécurisée, tests Robolectric et rédaction `doc_conception.md` / `doc_installation.md`. | **25 %** | _________________ |
| **TOTAL GROUPE** | **4 Modules intégrés** | **Application complète testée & APK compilé** | **100 %** | — |

---

## 2. Organisation Git recommandée pour l'examen

```bash
git checkout -b feat/auth-profile      # Module 1 (Étudiant 1)
git checkout -b feat/expenses-crud     # Module 2 (Étudiant 2)
git checkout -b feat/dashboard-stats   # Module 3 (Étudiant 3)
git checkout -b feat/export-settings   # Module 4 (Étudiant 4)
```

Exemples de messages de commit clairs :
- `feat(auth): implement PBKDF2 salted password hashing and Room UserDao`
- `feat(expenses): add CRUD screen, M3 DatePicker and input validation`
- `feat(dashboard): add Canvas donut chart, period filters and budget card`
- `feat(settings): add UTF-8 CSV export with FileProvider and custom categories`

---

## 3. Guide rapide de préparation à la soutenance orale individuelle

Chaque étudiant doit pouvoir démontrer sa maîtrise technique du module qu'il a développé face au jury :

### Questions probables — Étudiant 1 (Module 1 : Authentification & Profil)
- **Pourquoi ne pas stocker le mot de passe en clair dans SQLite ?**  
  Pour protéger la confidentialité même en cas d'extraction du fichier `spendwise_offline.db`. Nous utilisons `PasswordHasher` avec un sel aléatoire unique de 16 octets par utilisateur et 65 536 itérations PBKDF2 (`PBKDF2WithHmacSHA256`, avec repli `PBKDF2WithHmacSHA1` sur Android 7.0 API 24–25) ainsi qu'une comparaison à temps constant (`MessageDigest.isEqual`).
- **Comment la session utilisateur survit-elle au redémarrage de l'application ?**  
  L'identifiant utilisateur actif (`logged_in_user_id`) est stocké dans Jetpack DataStore (`UserPreferencesRepository`) et observé réactivement dans `AuthRepository.currentUser`.

### Questions probables — Étudiant 2 (Module 2 : CRUD Dépenses)
- **Comment assurez-vous la mise à jour instantanée de la liste après un ajout ou une modification ?**  
  `ExpenseDao.observeExpensesWithCategory()` retourne un `Flow<List<ExpenseWithCategory>>` que Room émet automatiquement à chaque transaction sur les tables `expenses` ou `categories`, collecté dans Compose via `collectAsStateWithLifecycle()`.
- **Quelles validations empêchent les données incohérentes ?**  
  `ValidationUtils` vérifie que le montant est numérique et strictement `> 0`, que la description est non vide (2 à 120 caractères), qu'une catégorie est sélectionnée et que la date issue du `DatePickerDialog` est valide.

### Questions probables — Étudiant 3 (Module 3 : Tableau de bord & Statistiques)
- **Comment les statistiques sont-elles recalculées lors du changement de période (Jour / Semaine / Mois) ?**  
  `DashboardViewModel` combine (`combine(...)`) le flux de dépenses Room avec les `StateFlow` de filtres (`selectedPeriod`, `selectedCategoryId`) et les bornes calendaires calculées dans `FormatUtils` (`startOfDayMillis`, `startOfWeekMillis`, `startOfMonthMillis`).
- **Comment le graphique circulaire est-il dessiné sans librairie externe ?**  
  Le composable `SpendingDonutChart` utilise le `Canvas` natif de Jetpack Compose (`drawArc`) avec des angles proportionnels au pourcentage de chaque catégorie et une animation fluide (`animateFloatAsState`).

### Questions probables — Étudiant 4 (Module 4 : Export CSV, Catégories & Paramètres)
- **Comment fonctionne le partage du fichier CSV sur Android récent sans demander de permission de stockage ?**  
  Le fichier est écrit dans le répertoire privé interne (`context.filesDir/exports/`), puis exposé temporairement et en lecture seule via `androidx.core.content.FileProvider` (`FLAG_GRANT_READ_URI_PERMISSION`) à un `Intent(Intent.ACTION_SEND)` de type `text/csv`.
- **Que se passe-t-il si l'utilisateur supprime une catégorie personnalisée déjà utilisée par des dépenses ?**  
  `CategoryRepository.deleteCustomCategory()` réaffecte d'abord toutes les dépenses associées vers la catégorie par défaut *« Autre »* via `ExpenseDao.reassignCategory()`, évitant ainsi toute perte de dépense ou erreur de clé étrangère.

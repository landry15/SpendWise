# SpendWise — Documentation d'Installation & Exploitation

**Application :** SpendWise (v1.0)  
**Identifiant d'application :** `com.aistudio.spendwise.kxmrpz`  
**SDK Minimum :** Android 7.0 (API 24) — **SDK Cible :** API 36  

---

## 1. Prérequis techniques

- **IDE** : Android Studio Meerkat / Ladybug (ou version plus récente supportant AGP 9.x).
- **JDK** : Java Development Kit **17** ou **21** (compilation cible Java 11 dans Gradle).
- **Android SDK** :
  - `compileSdk` : 36
  - `minSdk` : 24
  - `targetSdk` : 36
- **Gradle & Plugins** (gérés via `gradle/libs.versions.toml`) :
  - Android Gradle Plugin (AGP) `9.1.1`
  - Kotlin `2.4.21` + Compose Compiler
  - KSP `2.3.5` (pour `androidx.room:room-compiler:2.7.0`)
- **Dépendances principales** :
  - Jetpack Compose BOM `2024.09.00` (Material 3, Material Icons Extended)
  - `androidx.navigation:navigation-compose:2.8.9`
  - `androidx.room:room-runtime` & `room-ktx` `2.7.0`
  - `androidx.datastore:datastore-preferences:1.1.7`
  - `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2`
  - Tests locaux : `junit`, `robolectric:4.16.1`, `kotlinx-coroutines-test`

---

## 2. Procédure de build (compilation et génération de l'APK)

### Option A — Via Android Studio
1. Ouvrir le dossier racine du projet dans Android Studio (**File > Open...**).
2. Laisser Gradle synchroniser automatiquement les dépendances (aucune clé API ni fichier `google-services.json` requis : l'application est 100 % hors-ligne).
3. Dans le menu supérieur, sélectionner **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Une fois terminé, cliquer sur **locate** dans la notification pour accéder à l'APK généré dans :
   `app/build/outputs/apk/debug/app-debug.apk`.

### Option B — Via ligne de commande (Terminal)
Depuis la racine du projet :

```bash
# 1. Exécuter les tests unitaires et Robolectric
./gradlew :app:testDebugUnitTest
# (ou `gradle :app:testDebugUnitTest` si Gradle est installé globalement)

# 2. Compiler et générer l'APK Debug signé
./gradlew :app:assembleDebug
```

L'APK généré est disponible à l'emplacement :
`app/build/outputs/apk/debug/app-debug.apk`

---

## 3. Procédure d'installation sur émulateur ou appareil physique

### Sur émulateur Android Studio
1. Créer ou démarrer un appareil virtuel (**Device Manager**, ex. Pixel 7 — Android API 34 ou API 24+).
2. Cliquer sur **Run ▶** (`Shift + F10`) dans Android Studio, **ou** installer l'APK via ADB :
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### Sur appareil physique Android
1. Activer les **Options pour les développeurs** et le **Débogage USB** (ou transférer `app-debug.apk` par câble / partage local).
2. Avec ADB :
   ```bash
   adb devices
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
3. Sans ADB : ouvrir le fichier `app-debug.apk` depuis le gestionnaire de fichiers du téléphone et autoriser l'installation depuis cette source.

---

## 4. Comptes de test et données de démonstration

Au premier lancement de l'application, `DatabaseSeeder` crée automatiquement un compte de démonstration accompagné de 6 catégories par défaut et de 8 dépenses réparties sur le jour, la semaine et le mois en cours :

| Champ | Valeur de démonstration |
| :--- | :--- |
| **Nom** | `Étudiant Démo` |
| **E-mail** | `demo@spendwise.app` |
| **Mot de passe** | `SpendWise2026!` |
| **Budget mensuel initial** | `1200.00 €` |

> **Astuce soutenance :** Sur l'écran de connexion, un bouton rapide **« Compte démo »** pré-remplit automatiquement ces identifiants. Vous pouvez également créer de nouveaux comptes indépendants via **« Créer un compte »**.

---

## 5. Export CSV et localisation des fichiers

- Les exports CSV générés dans **Paramètres > Export des dépenses (CSV)** sont enregistrés dans le stockage privé de l'application (`files/exports/spendwise_depenses_YYYYMMDD_HHmmss.csv`).
- Le partage vers d'autres applications (Drive, Gmail, Files, WhatsApp, etc.) s'appuie sur `androidx.core.content.FileProvider` (`${applicationId}.fileprovider`) sans nécessiter de permission d'accès global au stockage.

---

## 6. Problèmes connus & limitations

- **Données strictement locales** : conformément au cahier des charges 100 % hors-ligne, la désinstallation de l'application ou l'effacement du stockage système entraîne la suppression de la base SQLite locale (pensez à exporter en CSV avant désinstallation).
- **Absence de récupération de mot de passe par e-mail** : sans serveur distant ni service SMTP, la réinitialisation de mot de passe par e-mail n'est pas applicable dans une architecture purement hors-ligne.
- **Émulateur sans applications tierces de partage** : sur un émulateur minimal sans client mail ou gestionnaire de fichiers compatible `ACTION_SEND`, le bouton **« Sauver »** permet tout de même de confirmer la création locale du fichier CSV.

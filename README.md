# CRFC Pointage Mobile Natif

Application Android 100% native en `Kotlin + Jetpack Compose` pour le pointage quotidien CRFC, reimplementee a partir de la version Expo/React Native.

## Stack

- `Kotlin`
- `Jetpack Compose Material 3`
- `Navigation Compose`
- `Room`
- `DataStore`
- `Apache POI` pour l'export Excel
- `PdfDocument` Android pour l'export PDF
- `GitHub Actions` pour CI et smoke tests Android

## Fonctionnalites v1

- authentification locale `ADMIN` / `AGENT`
- seed admin et seed employes CRFC
- creation du rapport du jour
- saisie des retards, absences et visiteurs
- absences recurrentes
- historique filtre
- statistiques
- gestion des employes
- gestion du profil et des utilisateurs
- export PDF et export Excel avec partage Android natif

## Build sans Android Studio

Le projet est volontairement structure pour fonctionner avec `Gradle Wrapper` et `GitHub Actions`.

### Local

Il faut simplement un JDK recent, puis :

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

### GitHub Actions

- `Android CI` lance les tests unitaires, `lint` et produit un APK debug en artifact.
- `Android Emulator Smoke` est declenche manuellement et lance le smoke test Compose sur emulateur.

## CI/CD release plus tard

Pour preparer une release signee plus tard dans GitHub Actions, il faudra ajouter ces secrets GitHub :

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Le keystore devra etre converti en Base64 avant ajout dans les secrets.

## Notes

- le stockage est `local-first`
- aucune migration automatique des anciennes donnees Expo n'est incluse dans cette v1
- la synchro cloud pourra etre ajoutee plus tard en gardant les repositories actuels

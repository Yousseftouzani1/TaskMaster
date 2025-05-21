# Task Master

Application Android de gestion de tâches avec synchronisation en temps réel et vue calendrier.

## Installation Rapide

### Prérequis
- Android Studio (dernière version)
- JDK 11+
- Un appareil Android (API 23+)
- Compte Google pour Firebase

### Étapes d'Installation

1. **Cloner le projet**
```bash
git clone https://github.com/votre-username/task-master.git
cd task-master
```

2. **Configurer Firebase**
   - Allez sur [Firebase Console](https://console.firebase.google.com)
   - Créez un nouveau projet
   - Ajoutez une application Android avec :
     - Package name: `com.example.devmob`
     - Téléchargez `google-services.json`
   - Placez `google-services.json` dans le dossier `app/`

3. **Ouvrir dans Android Studio**
   - Lancez Android Studio
   - Sélectionnez "Open an existing project"
   - Naviguez vers le dossier du projet
   - Attendez la synchronisation Gradle

4. **Configurer l'authentification Firebase**
   - Dans Firebase Console, allez dans "Authentication"
   - Activez "Email/Password" et "Google Sign-in"
   - Pour Google Sign-in, configurez le SHA-1 de votre projet

5. **Configurer la base de données**
   - Dans Firebase Console, allez dans "Realtime Database"
   - Créez une base de données en mode test
   - Copiez les règles de sécurité suivantes :
```json
{
  "rules": {
    "tasks": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

## Exécution

1. **Sur un émulateur**
   - Dans Android Studio, cliquez sur "Run" (triangle vert)
   - Sélectionnez un émulateur ou créez-en un nouveau
   - L'application se lancera automatiquement

2. **Sur un appareil physique**
   - Activez le "Mode développeur" sur votre appareil
   - Activez le "Débogage USB"
   - Connectez votre appareil via USB
   - Dans Android Studio, sélectionnez votre appareil
   - Cliquez sur "Run"

## Dépannage

### Problèmes courants

1. **Erreur de synchronisation Gradle**
```bash
./gradlew clean
./gradlew build
```

2. **Erreur de connexion Firebase**
   - Vérifiez que `google-services.json` est bien placé
   - Vérifiez votre connexion Internet
   - Vérifiez les règles de sécurité Firebase

3. **Erreur d'authentification Google**
   - Vérifiez que le SHA-1 est correctement configuré
   - Vérifiez que l'authentification Google est activée

## Support

Pour toute question ou problème :
- Ouvrez une issue sur GitHub
- Consultez la [documentation Firebase](https://firebase.google.com/docs)
- Contactez l'équipe de développement

## Licence

MIT License - Voir le fichier `LICENSE` pour plus de détails. 
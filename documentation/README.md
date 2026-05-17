# Documentation Technique - PI Trading Platform

## Contenu

Ce dossier contient la documentation complète du projet PI Trading Platform au format LaTeX.

### Fichiers

- `rapport-technique.tex` : Rapport complet (13 chapitres, ~150 pages quand compilé)

## Chapitres Inclus

1. **Résumé Exécutif** : Vision et objectifs principaux
2. **Cahier de Charge** : Fonctionnalités requises et contraintes
3. **Stack Technologique** : Technologies utilisées et justification
4. **Architecture Système** : Diagrammes et flux de données
5. **Documentation des Endpoints** : Tous les endpoints API avec exemples
6. **Système de Messagerie Kafka** : Topics, formats, garanties
7. **Justifications des Choix Techniques** : Pourquoi microservices, Kafka, ARIMA, etc.
8. **Implémentation Détaillée** : Code source des composants clés
9. **Déploiement Docker** : Docker Compose et containerization
10. **Kubernetes - Implémentation Future** : Manifests K8s complets
11. **Pipeline CI/CD Jenkins** : Jenkinsfile et architecture
12. **Monitoring et Observabilité** : Prometheus, Grafana, ELK
13. **Conclusion** : Résumé et améliorations futures

## Compilation

### Prérequis

Installer une distribution LaTeX complète :

**Ubuntu/Debian:**
```bash
sudo apt-get install texlive-full
```

**macOS (via Homebrew):**
```bash
brew install basictex
```

**Windows:**
Télécharger et installer MiKTeX : https://miktex.org/

### Compiler le document

```bash
cd documentation
pdflatex rapport-technique.tex
# ou
xelatex rapport-technique.tex
```

Pour générer la table des matières et les références (2 passes requises):
```bash
pdflatex rapport-technique.tex
pdflatex rapport-technique.tex
```

Ou utiliser un script automatisé :
```bash
latexmk -pdf rapport-technique.tex
```

### Résultat

Un fichier `rapport-technique.pdf` sera généré dans le même dossier.

## Format et Style

- **Classe** : `report` (chapitres numérotés)
- **Langue** : Français (babel)
- **Encodage** : UTF-8
- **Feuille de style** : A4, marges 1 inch, en-têtes/pieds de page personnalisés

## Contenu Spécifique

### Diagrammes

Le rapport inclut des diagrammes ASCII pour :
- Architecture générale du système
- Flux de trading complet
- Architecture Kubernetes proposée

### Code Examples

Code sources incluant :
- Java Spring Boot (Services, ConsensusService)
- Python (ARIMA FastAPI)
- YAML (Kubernetes manifests)
- Groovy (Jenkins pipeline)

### Tableaux

Documentation des endpoints avec :
- Méthode HTTP
- Chemin complet
- Paramètres
- Exemples de requête/réponse
- Codes de statut

### Topics Kafka

Formats JSON complètes pour chacun des 6 topics principaux.

## Intégration au Rapport Scolaire

Ce rapport LaTeX peut être :
- Compilé directement en PDF pour remise
- Intégré à un projet plus large (main.tex)
- Customisé (logo, en-tête, couleurs)
- Utilisé comme base pour d'autres rapports techniques

## Questions Fréquentes

**Q: Le document est très long?**
A: Oui, ~15,000 lignes de contenu technique. C'est intentionnel pour démontrer la profondeur du projet.

**Q: Puis-je modifier les chapitres?**
A: Absolument! Le format LaTeX est texte brut. Éditez `rapport-technique.tex` avec n'importe quel éditeur.

**Q: Comment ajouter des images?**
A: Utilisez `\includegraphics{path/to/image.png}` après `\usepackage{graphicx}`.

**Q: Puis-je ajouter des diagrammes PlantUML?**
A: Oui, générez des PDFs via PlantUML puis intégrez-les comme images.

## Compilation Automatique (CI)

Dans votre pipeline Jenkins futur :
```bash
stage('Documentation') {
    steps {
        dir('documentation') {
            sh 'latexmk -pdf rapport-technique.tex'
            archiveArtifacts artifacts: 'rapport-technique.pdf'
        }
    }
}
```

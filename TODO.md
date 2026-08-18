# TODO

## Priorité haute

- Afficher dans l'UI import le détail complet de toutes les erreurs CSV (pas seulement la première).
- Ajouter des tests frontend ciblés (Dashboard, Import CSV, formulaire de dérogation).

## Qualité backend

- Ajouter des tests d'intégration Spring Boot pour le flux complet :
  import CSV -> persistance PostgreSQL -> lecture dashboard.
- Ajouter des tests d'API REST (codes HTTP + structure des réponses d'erreur).

## Améliorations produit (hors scope prioritaire)

- Export CSV des demandes de dérogation validées/rejetées.
- Monitoring applicatif (Actuator + healthchecks enrichis).

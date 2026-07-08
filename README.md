# DGuard

**Régions protégées façon WorldGuard**, réservées aux admins (`dguard.admin`,
`op` par défaut). Une région se crée directement à partir d'une sélection
[DWorldEdit](../DWorldEdit) (via [DAPI](../DAPI)) : sélectionne une zone à la
baguette, `/rg create <nom>`, et configure ce qui y est autorisé ou non.

## Fonctionnalités

- **`/region` ou `/rg`** (les deux marchent, comme le vrai WorldGuard).
- **`/rg create <nom>`** : crée une région à partir de ta sélection DWorldEdit
  courante. Tout est **refusé par défaut** (zone entièrement protégée dès la
  création) — à toi de rouvrir ce qui doit l'être avec `/rg perm`.
- **`/rg delete <nom>`**, **`/rg list`**, **`/rg info <nom>`**.
- **`/rg perm <nom> <flag> <allow|deny>`** : configure un flag. Flags
  disponibles : `break` (alias `griefing`), `place`, `explosion`, `mob-spawn`,
  `pvp`, `interact` (coffres/fours/tables de craft...), `fire-spread`,
  `entry` (entrée dans la région).
- **`/rg priority <nom> <valeur>`** : les régions **peuvent se chevaucher** —
  en cas de chevauchement, la région avec la priorité la plus haute
  l'emporte (à égalité, la plus récemment créée gagne).
- Les joueurs avec `dguard.admin` **bypassent toujours** tous les flags
  (jamais bloqués dans leurs propres régions).
- Stockage YAML local par défaut, ou MySQL — même pattern que DFaction/DTicket.

## Intégration DAPI

DGuard **dépend directement** de DAPI (`depend: [DAPI]`, pas juste
`softdepend`) : contrairement à DWorldEdit/DTicket, sa fonction principale
(`/rg create`) a réellement besoin d'une sélection, donc de DAPI.

- **Consomme `SelectionService`** (fourni par DWorldEdit) pour récupérer la
  sélection courante du joueur lors de `/rg create`. Si aucun plugin ne
  fournit ce service (DWorldEdit non installé), `/rg create` échoue avec un
  message clair plutôt que de planter.
- **Consomme `FactionService`** (fourni par DFaction, optionnel) : avertit
  (sans bloquer) si la région créée chevauche le territoire claim d'une
  faction — configurable via `warn-on-faction-claim` dans `config.yml`.
- **Fournit `RegionService`** : un futur plugin (ex: `DShop`) pourrait
  vérifier `isAllowed(location, "place")` avant d'autoriser la pose d'un
  shop, sans dépendre de DGuard.

### Autres idées d'interconnexion possibles

- Un futur `DTicket` pourrait proposer une catégorie de ticket "signaler du
  grief dans une région protégée", en citant automatiquement le nom de la
  région via `RegionService`.
- Un futur système d'arène de combat pourrait activer/désactiver le flag
  `pvp` d'une région par commande, pour des événements ponctuels.

## Commandes

| Commande | Description |
|---|---|
| `/rg create <nom>` | Crée une région à partir de la sélection DWorldEdit courante. |
| `/rg delete <nom>` | Supprime une région. |
| `/rg list` | Liste toutes les régions. |
| `/rg info <nom>` | Détail d'une région (bornes, priorité, flags). |
| `/rg perm <nom> <flag> <allow\|deny>` | Configure un flag. |
| `/rg priority <nom> <valeur>` | Priorité en cas de chevauchement. |

## Permissions

| Permission | Défaut | Description |
|---|---|---|
| `dguard.admin` | `op` | Toutes les commandes `/rg`, et bypass de tous les flags. |

## Configuration (`config.yml`)

```yaml
storage:
  type: local   # local ou mysql
  mysql:
    host: localhost
    port: 3306
    database: dguard
    username: root
    password: ""

warn-on-faction-claim: true
```

## Compiler le projet

Dépend de l'API Spigot 26.1.2 et, en `provided`, de DAPI :

```
cd ../DAPI && mvn install
cd ../DGuard && mvn clean package
```

Pour tester `/rg create`, il faut aussi [DWorldEdit](../DWorldEdit) installé
sur le serveur (c'est lui qui fournit la sélection). Voir
[`libs/README.md`](libs/README.md) pour la mise en place de l'API Spigot.

## Roadmap / idées d'extension

- Régions non-cuboïdes (polygones).
- Membres/propriétaires par région (pour une délégation partielle, pas
  seulement `dguard.admin`).
- Messages d'entrée/sortie personnalisables par région (greeting/farewell).
- Téléportation à l'entrée d'une région (spawn-in point).

## Licence

MIT — voir [`LICENSE`](LICENSE).

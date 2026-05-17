# Git Intro

## Feature Branch erstellen

Eine Feature Branch wird verwendet, um an einer neuen Funktion zu arbeiten, ohne direkt die dev-Branch zu verändern.

### 1. Aktuelle dev-Branch holen

Wechsle zuerst auf die dev-Branch:

```bash
git checkout dev
```

Hole danach die neuesten Änderungen:

```bash
git pull origin dev
```

### 2. Neue Feature Branch erstellen

Erstelle eine neue Branch für dein Feature. Der Branch-Name soll dem Schema `FR-##/name` folgen:

```bash
git checkout -b FR-##/name
```

Beispiel:

```bash
git checkout -b FR-42/login-page
```

### 3. Änderungen committen

Nachdem du Dateien geändert hast, prüfe den Status:

```bash
git status
```

Füge deine Änderungen hinzu:

```bash
git add .
```

Erstelle einen Commit:

```bash
git commit -m "Add login page"
```

### 4. Feature Branch hochladen

Lade die Branch zum Remote-Repository hoch:

```bash
git push -u origin FR-##/name
```

Beispiel:

```bash
git push -u origin FR-42/login-page
```

Danach kannst du im Repository einen Merge Request oder Pull Request erstellen.

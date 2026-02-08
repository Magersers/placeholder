# ClassSidebar (Paper 1.21.1 + PlaceholderAPI 2.12.2)

Лёгкий плагин, который показывает аккуратный `sidebar` (справа по центру интерфейса Minecraft) с:

- ником игрока;
- основным классом и его ключом/уровнем;
- боевым классом и его ключом/уровнем.

## Плейсхолдеры

Используются значения:

- `%classlevel_main_class%`
- `%classlevel_main_class_key%`
- `%classlevel_main_level%`
- `%classlevel_combat_class%`
- `%classlevel_combat_class_key%`
- `%classlevel_combat_level%`

## Сборка

```bash
mvn package
```

Готовый jar: `target/class-sidebar-1.0.0.jar`

## Установка

1. Установи на сервер:
   - Paper/Spigot 1.21.1
   - PlaceholderAPI 2.12.2+
2. Положи jar этого плагина в папку `plugins/`.
3. Перезапусти сервер.
4. При необходимости измени оформление в `plugins/ClassSidebar/config.yml`.

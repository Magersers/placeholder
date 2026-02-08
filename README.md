# ClassSidebar (Paper 1.21.1 + PlaceholderAPI 2.12.2)

Компактный и современный правый sidebar для Minecraft сервера.

Что показывает:
- ник игрока;
- основной класс + уровень;
- боевой класс + уровень.

Особенности:
- без "ключей" класса (как просили);
- более узкое и аккуратное меню;
- подсветка классов разными цветами;
- если уровень `>= 10`, число уровня переливается анимированным градиентом;
- попытка скрыть цифровые значения справа (для современных версий API).

## Используемые placeholders

- `%classlevel_main_class%`
- `%classlevel_main_level%`
- `%classlevel_combat_class%`
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
4. Настрой `plugins/ClassSidebar/config.yml` при необходимости.

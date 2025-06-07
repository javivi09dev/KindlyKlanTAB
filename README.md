# KindlyKlanTAB

Un mod server-side para Fabric 1.21.1 que permite personalizar completamente el TAB (lista de jugadores) y gestionar anuncios automáticos en el chat.

## ✨ Características

### 📋 Gestión del TAB
- **Header y Footer personalizables** con soporte para placeholders
- **Nombres personalizados** con prefijos y sufijos por grupos
- **Actualización automática** en tiempo real
- **Sistema de prioridades** para grupos de jugadores
- **Soporte para colores** y formato de Minecraft
- **Integración completa con LuckPerms**

### 📢 Sistema de Anuncios
- **Anuncios automáticos** en intervalos configurables
- **Anuncios manuales** mediante comandos
- **Orden secuencial o aleatorio**
- **Permisos por anuncio** (opcional)
- **Prefijo personalizable**

### 🔐 Integración con LuckPerms
- **Detección automática** de LuckPerms
- **Prefijos y sufijos** desde LuckPerms
- **Sistema de pesos** para ordenamiento automático
- **Permisos avanzados** basados en grupos
- **Metadatos personalizados** en el TAB
- **Fallback automático** si LuckPerms no está disponible

### 🔧 Placeholders Disponibles

#### Generales del Servidor
- `{PLAYER_COUNT}` - Jugadores conectados
- `{MAX_PLAYERS}` - Máximo de jugadores
- `{TPS}` - Ticks por segundo actual
- `{MSPT}` - Milisegundos por tick
- `{TIME}` - Hora actual (HH:mm:ss)
- `{DATE}` - Fecha actual (dd/MM/yyyy)
- `{UPTIME}` - Tiempo de actividad del servidor
- `{SERVER_NAME}` - Nombre del servidor

#### Específicos del Jugador
- `{PLAYER_NAME}` - Nombre del jugador
- `{PLAYER_UUID}` - UUID del jugador
- `{PLAYER_PING}` - Latencia del jugador
- `{PLAYER_WORLD}` - Mundo donde está el jugador
- `{PLAYER_X}`, `{PLAYER_Y}`, `{PLAYER_Z}` - Coordenadas del jugador

#### LuckPerms (cuando está disponible)
- `{LUCKPERMS_PREFIX}` - Prefijo del grupo principal
- `{LUCKPERMS_SUFFIX}` - Sufijo del grupo principal
- `{LUCKPERMS_GROUP}` - Nombre del grupo principal
- `{LUCKPERMS_WEIGHT}` - Peso del grupo principal
- `{LUCKPERMS_META_<key>}` - Valor de meta personalizada
- `{PREFIX}`, `{SUFFIX}`, `{GROUP}` - Aliases de compatibilidad

## 📦 Instalación

### Instalación Básica
1. Descarga el archivo `.jar` del mod
2. Colócalo en la carpeta `mods/` de tu servidor Fabric 1.21.1
3. Inicia el servidor
4. Los archivos de configuración se generarán automáticamente en `config/kindlyklantab/`

### 🔐 Instalación con LuckPerms (Recomendado)
1. Instala **LuckPerms para Fabric** desde [su sitio oficial](https://luckperms.net/download)
2. Opcionalmente, instala **Fabric PlaceholderAPI** para funcionalidad extendida
3. Instala **KindlyKlanTAB**
4. Sigue la [Guía de Integración con LuckPerms](LUCKPERMS_INTEGRATION.md)

## ⚙️ Configuración

### TAB (config/kindlyklantab/tab.json)

#### Configuración básica:
```json
{
  "enabled": true,
  "updateInterval": 20,
  "header": "§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\\n§e§l                      ¡Bienvenido al Servidor!                      \\n§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
  "footer": "§7Jugadores conectados: §a{PLAYER_COUNT}§7 | TPS: §a{TPS}\\n§7Discord: §bdiscord.gg/servidor §7| Web: §bwww.servidor.com"
}
```

#### Con integración LuckPerms:
```json
{
  "enabled": true,
  "nameFormat": "{LUCKPERMS_PREFIX}{PLAYER_NAME}{LUCKPERMS_SUFFIX}",
  "footer": "§7Tu grupo: §a{LUCKPERMS_GROUP}§7 | Peso: §e{LUCKPERMS_WEIGHT}"
}
```

### Anuncios (config/kindlyklantab/announcements.json)

```json
{
  "enabled": true,
  "interval": 6000,
  "randomOrder": false,
  "prefix": "§8[§6Anuncio§8] §r",
  "announcements": [
    {
      "message": "§e¡Bienvenido al servidor! §7Disfruta tu estadía.",
      "enabled": true,
      "permission": ""
    }
  ]
}
```

## 🕹️ Comandos

### Comandos del TAB (`/kktab`)
**Requiere nivel de OP 3 o superior**

- `/kktab reload` - Recarga la configuración del TAB
- `/kktab update` - Actualiza el TAB manualmente
- `/kktab header <texto>` - Cambia el header del TAB
- `/kktab footer <texto>` - Cambia el footer del TAB
- `/kktab toggle` - Activa/desactiva el TAB personalizado
- `/kktab info` - Muestra información del estado del TAB
- `/kktab test` - Probar actualización manual del TAB
- `/kktab debug <jugador>` - Ver información detallada de un jugador

### Comandos de Anuncios (`/kkannounce`)
**Requiere nivel de OP 3 o superior**

- `/kkannounce send <mensaje>` - Envía un anuncio inmediato
- `/kkannounce add <mensaje>` - Añade un anuncio a la lista automática
- `/kkannounce remove <índice>` - Elimina un anuncio por índice
- `/kkannounce list` - Lista todos los anuncios configurados
- `/kkannounce reload` - Recarga la configuración de anuncios
- `/kkannounce toggle` - Activa/desactiva los anuncios automáticos
- `/kkannounce interval <ticks>` - Cambia el intervalo de anuncios
- `/kkannounce info` - Muestra información del sistema de anuncios
- `/kkannounce testannounce <modo> <mensaje>` - Probar anuncio en modo específico

## 🎨 Códigos de Color

### Códigos Legacy (Minecraft clásico)
- `§0` Negro - `§1` Azul oscuro - `§2` Verde oscuro - `§3` Cian oscuro
- `§4` Rojo oscuro - `§5` Púrpura - `§6` Dorado - `§7` Gris
- `§8` Gris oscuro - `§9` Azul - `§a` Verde - `§b` Cian
- `§c` Rojo - `§d` Rosa - `§e` Amarillo - `§f` Blanco

### Simplified Text Format (LuckPerms)
```
<red>, <blue>, <green>, <yellow>, <gold>, <gray>
<bold>, <italic>, <underlined>, <strikethrough>
<gradient:red:blue>Texto</gradient>
<rainbow>Texto arcoíris</rainbow>
```

## 🔌 Sistemas de Permisos Soportados

### 🔥 LuckPerms (Recomendado)
- **Detección automática** al iniciar el servidor
- **Integración completa** con grupos, pesos y metadatos
- **Soporte para placeholders** avanzados
- **Ordenamiento automático** por peso de grupo
- **Ver [guía completa](LUCKPERMS_INTEGRATION.md)**

### Sistema Básico
- Fallback automático si LuckPerms no está disponible
- Basado en niveles de OP del servidor
- Grupos configurables manualmente en `tab.json`

## 🚀 Ejemplos de Configuración

### Servidor con LuckPerms
```bash
# Configurar grupos en LuckPerms
/lp creategroup owner
/lp group owner setweight 1000
/lp group owner meta setprefix "<dark_red>[Owner] "
/lp user TuNombre parent add owner
```

### TAB con información avanzada
```json
{
  "header": "<gold><bold>🏰 Mi Servidor RPG 🏰</bold></gold>\\n<gray>Conectados: {PLAYER_COUNT}/{MAX_PLAYERS}</gray>",
  "footer": "<yellow>Tu rango: {LUCKPERMS_GROUP}</yellow>\\n<gray>Kills: {LUCKPERMS_META_kills} | Nivel: {LUCKPERMS_META_nivel}</gray>",
  "nameFormat": "{LUCKPERMS_PREFIX}{PLAYER_NAME} <gray>[Nv.{LUCKPERMS_META_nivel}]"
}
```

## 📈 Características Avanzadas

### Metadatos Personalizados
```bash
# En LuckPerms
/lp user JugadorX meta set "kills" "1337"
/lp user JugadorX meta set "nivel" "50"

# En el TAB
"footer": "Kills: {LUCKPERMS_META_kills} | Nivel: {LUCKPERMS_META_nivel}"
```

### Ordenamiento Automático
El mod ordena a los jugadores automáticamente por:
1. **Peso del grupo LuckPerms** (mayor peso = más arriba)
2. **Prioridad configurada** (si no hay LuckPerms)
3. **Orden alfabético** (como fallback)

### Rendimiento Optimizado
- **Caché inteligente** de información de LuckPerms
- **Actualizaciones incrementales** solo cuando es necesario
- **Fallback automático** sin interrupciones

## 🐛 Problemas Conocidos

- ✅ **Resuelto**: Integración con LuckPerms completamente funcional
- ✅ **Resuelto**: Soporte para metadatos personalizados

## 📝 Licencia

Todos los derechos reservados.

## 🤝 Contribuir

Las contribuciones son bienvenidas. Por favor:

1. Haz fork del proyecto
2. Crea una rama para tu feature
3. Haz commit de tus cambios
4. Haz push a la rama
5. Abre un Pull Request

## 📚 Documentación Adicional

- [🔐 Guía de Integración con LuckPerms](LUCKPERMS_INTEGRATION.md)

## 📌 Solución de Problemas

### Los anuncios no aparecen centrados

1. Verifica que `displayMode` esté en `"auto"` o `"title"`
2. Usa el comando `/kktab testannounce title §6Tu mensaje` para probar
3. Asegúrate de que `useTitleForFormatted` esté en `true`

### Los prefijos no aparecen en el TAB

1. Usa `/kktab debug <jugador>` para ver información detallada
2. Verifica que `enableCustomNames` esté en `true`
3. Usa `/kktab test` para forzar actualización del TAB
4. Revisa que el jugador tenga el permiso correcto para su grupo

### Debug Commands

```bash
# Ver información completa de un jugador
/kktab debug NombreJugador

# Probar diferentes modos de anuncios
/kktab testannounce title "§6¡Mensaje de prueba!"
/kktab testannounce actionbar "Mensaje en actionbar"
/kktab testannounce chat "Mensaje en chat"

# Forzar actualización del TAB
/kktab test
```

### Logs Útiles

El mod registra información importante en los logs del servidor:
- Inicialización de managers
- Detección de LuckPerms
- Errores de actualización del TAB
- Estados de configuración

## 📌 Integración con LuckPerms

Ver [LUCKPERMS_INTEGRATION.md](LUCKPERMS_INTEGRATION.md) para configuración detallada.

## 📌 Soporte

Para reportar bugs o solicitar características:
1. Usa los comandos de debug para recopilar información
2. Revisa los logs del servidor
3. Proporciona tu configuración JSON

## 📌 Licencia

Este proyecto está bajo la Licencia MIT.

# 🔐 Integración con LuckPerms


## 📋 Requisitos

1. **Servidor Fabric 1.21.1**
2. **KindlyKlanTAB**
3. **LuckPerms para Fabric** - [Descargar aquí](https://luckperms.net/download)


## 🚀 Instalación

### Paso 1: Instalar LuckPerms
1. Descarga **LuckPerms para Fabric** desde su sitio oficial
2. **IMPORTANTE**: Verifica que la versión sea compatible con Minecraft 1.21.1
3. Coloca el archivo `.jar` en tu carpeta `mods/`
4. Inicia el servidor una vez para generar las configuraciones
5. Detén el servidor

### Paso 2: Configurar LuckPerms
1. Edita `/config/luckperms/luckperms.conf`
2. Asegúrate de que `meta-formatting.prefix.end-spacer` esté configurado como `" "` para añadir espacio después del prefijo:

```hocon
meta-formatting {
    prefix {
        end-spacer = " "
    }
    suffix {
        start-spacer = " "
    }
}
```

### Paso 3: Instalar KindlyKlanTAB
1. Coloca el mod KindlyKlanTAB en tu carpeta `mods/`
2. Inicia el servidor
3. **Verifica en los logs** que aparezca: `✅ Integración con LuckPerms activada`
4. Si ves `⚠️ LuckPerms no encontrado`, el mod funcionará con permisos básicos (fallback, por default)

## ⚙️ Configuración

### Configurar Grupos en LuckPerms

```bash
# Crear grupos básicos
/lp creategroup owner
/lp creategroup admin  
/lp creategroup mod
/lp creategroup vip
/lp creategroup default

# Configurar pesos (mayor número = mayor prioridad)
/lp group owner setweight 1000
/lp group admin setweight 900
/lp group mod setweight 800
/lp group vip setweight 700
/lp group default setweight 100

# Configurar prefijos con colores modernos
/lp group owner meta setprefix "<dark_red>[Owner] "
/lp group admin meta setprefix "<red>[Admin] "
/lp group mod meta setprefix "<yellow>[Mod] "
/lp group vip meta setprefix "<gold>[VIP] "
/lp group default meta setprefix "<gray>"

# Configurar sufijos (opcional)
/lp group vip meta setsuffix " <gray>⭐"
/lp group owner meta setsuffix " <red>👑"

# Asignar usuarios a grupos
/lp user TuNombre parent add owner
/lp user OtroJugador parent add vip
```

### Configurar el TAB en KindlyKlanTAB

Edita `/config/kindlyklantab/tab.json`:

```json
{
  "enabled": true,
  "updateInterval": 20,
  "header": "<gold><bold>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</bold></gold>\n<yellow><bold>                      ¡Bienvenido al Servidor!                      </bold></yellow>\n<gold><bold>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</bold></gold>",
  "footer": "<gray>Jugadores: </gray><green>{PLAYER_COUNT}</green><gray>/{MAX_PLAYERS} | TPS: </gray><green>{TPS}</green>\n<gray>Tu grupo: </gray><green>{LUCKPERMS_GROUP}</green><gray> | Peso: </gray><yellow>{LUCKPERMS_WEIGHT}</yellow>",
  "enableCustomNames": true,
  "nameFormat": "{LUCKPERMS_PREFIX}{PLAYER_NAME}{LUCKPERMS_SUFFIX}",
  "groups": [
    {
      "permission": "owner",
      "prefix": "<dark_red>[Owner] ",
      "suffix": " <red>👑",
      "priority": 1
    },
    {
      "permission": "admin", 
      "prefix": "<red>[Admin] ",
      "suffix": "",
      "priority": 2
    },
    {
      "permission": "mod",
      "prefix": "<yellow>[Mod] ",
      "suffix": "",
      "priority": 3
    },
    {
      "permission": "vip",
      "prefix": "<gold>[VIP] ",
      "suffix": " <gray>⭐",
      "priority": 4
    },
    {
      "permission": "default",
      "prefix": "<gray>",
      "suffix": "",
      "priority": 5
    }
  ]
}
```

## 🏷️ Placeholders Disponibles

### Placeholders de LuckPerms
- `{LUCKPERMS_PREFIX}` - Prefijo del grupo principal
- `{LUCKPERMS_SUFFIX}` - Sufijo del grupo principal  
- `{LUCKPERMS_GROUP}` - Nombre del grupo principal
- `{LUCKPERMS_WEIGHT}` - Peso del grupo principal
- `{LUCKPERMS_META_<key>}` - Valor de meta personalizada

### Placeholders Heredados (compatibilidad)
- `{PREFIX}` - Alias para `{LUCKPERMS_PREFIX}`
- `{SUFFIX}` - Alias para `{LUCKPERMS_SUFFIX}`
- `{GROUP}` - Alias para `{LUCKPERMS_GROUP}`

## 🎨 Formato de Colores

LuckPerms usa **Simplified Text Format** en lugar de códigos legacy (`&`):

### Colores Básicos
```
<black>, <dark_blue>, <dark_green>, <dark_aqua>
<dark_red>, <dark_purple>, <gold>, <gray>
<dark_gray>, <blue>, <green>, <aqua>
<red>, <light_purple>, <yellow>, <white>
```

### Formatos
```
<bold>, <italic>, <underlined>, <strikethrough>, <obfuscated>
<reset> - Resetear formato
```

### Gradientes (LuckPerms avanzado)
```
<gradient:red:blue>Texto con gradiente</gradient>
<rainbow>Texto arcoíris</rainbow>
```

## 🔧 Comandos Útiles

### LuckPerms
```bash
# Ver información de un usuario
/lp user <nombre> info

# Añadir permiso
/lp user <nombre> permission set kktab.admin true

# Cambiar grupo principal
/lp user <nombre> parent set <grupo>

# Ver grupos
/lp listgroups

# Editar prefijo de grupo
/lp group <grupo> meta setprefix "<color>[Rango] "
```

### KindlyKlanTAB
```bash
# Recargar configuraciones
/kktab reload

# Ver información del TAB
/kktab info

# Actualizar TAB manualmente
/kktab update
```

## 🔄 Orden en la Lista del TAB

El mod ordenará automáticamente a los jugadores por:
1. **Peso del grupo** (mayor peso = más arriba)
2. **Nombre alfabético** (como fallback)

Para personalizar el orden, ajusta los pesos de los grupos:
```bash
/lp group owner setweight 1000    # Más arriba
/lp group admin setweight 900
/lp group mod setweight 800  
/lp group vip setweight 700
/lp group default setweight 100   # Más abajo
```

## 🛠️ Metadatos Personalizados

Puedes añadir metadatos personalizados que se mostrarán en el TAB:

```bash
# Añadir metadatos
/lp user <nombre> meta set "titulo" "El Elegido"
/lp user <nombre> meta set "kills" "1337"
/lp user <nombre> meta set "nivel" "50"
```

Luego úsalos en el TAB:
```json
{
  "nameFormat": "{LUCKPERMS_PREFIX}{PLAYER_NAME} <gray>[Nv.{LUCKPERMS_META_nivel}]",
  "footer": "Tu título: <yellow>{LUCKPERMS_META_titulo}</yellow>"
}
```

## ⚡ Rendimiento

- El mod cachea la información de LuckPerms automáticamente
- Las actualizaciones se sincronizan con los cambios de LuckPerms
- Usa el sistema de pesos de LuckPerms para ordenamiento eficiente

## 🐛 Solución de Problemas

### LuckPerms no se detecta
1. **Verifica la versión**: Asegúrate de que LuckPerms sea para Fabric y compatible con 1.21.1
2. **Revisa los logs**: Busca errores relacionados con LuckPerms al iniciar
3. **Instala versión compatible**: Si hay problemas, prueba con una versión más antigua de LuckPerms

### Error de dependencias al compilar
1. **El mod es independiente**: No necesitas instalar dependencias adicionales
2. **Solo LuckPerms**: El único mod adicional que necesitas es LuckPerms
3. **Compilación limpia**: Ejecuta `./gradlew clean build` para limpiar cache

### Los prefijos no aparecen
1. Verifica que LuckPerms esté funcionando: `/lp info`
2. Asegúrate de que los grupos tienen prefijos: `/lp group <grupo> info`
3. Recarga las configuraciones: `/kktab reload`
4. Verifica en logs si hay errores de LuckPerms

### Problemas de compatibilidad
1. **Usa solo Fabric**: No mezcles con NeoForge o Bukkit
2. **Versiones compatibles**: LuckPerms 5.4+ y Minecraft 1.21.1
3. **Revisa issues conocidos**: Consulta [el repositorio de LuckPerms](https://github.com/LuckPerms/LuckPerms/issues)

## ⚡ Características Técnicas

### Sistema de Detección Robusto
- ✅ **Detección por reflexión** - Carga LuckPerms de forma segura
- ✅ **Fallback automático** - Funciona sin LuckPerms
- ✅ **Manejo de errores** - No se rompe si LuckPerms falla
- ✅ **Logs informativos** - Te dice exactamente qué está pasando

### Rendimiento Optimizado
- ✅ **Sin dependencias obligatorias** - Funciona independientemente
- ✅ **Caché inteligente** - Solo consulta cuando es necesario
- ✅ **Memoria eficiente** - Usa reflexión optimizada

---

¡Con esta configuración robusta tendrás un sistema completo que funciona con o sin LuckPerms! 🎉 
# TeamCity Cached Subversion Plugin
TeamCity Cached Subversion plugin provides support of caching svn repositories on agent.
Can be useful with slow remote and/or big repositories.

# Compatibility

The plugin is compatible with [TeamCity](https://www.jetbrains.com/teamcity/download/) 10.0.x and greater (tested only 10.0.x yet).
 
# Build

execute *mvn package* in root folder

# Using
1. Install plugin
2. Write in *config/cachedSubversion.repositories* file urls you want to cache. One url one line. Urls can be to any directory in repository, only that part will be cached.
3. In build configuration with standard Subversion repository change checkout mode to *"Do not checkout files automatically"* and add build feature *"cache subversion"*.

# Features
* Flexible settings. For instance you can disable caching on local to repository agents and use shared network cache for each region
* Checkout rules to files are supported (standard agent side checkout doesn't)

# Additional Agent-Side Parameters
* cachedSubversion.disabled - if parameter exists, than agent will be used original url
* cachedSubversion.cachePath - local or network path to repositories cache. Cache can be shared

# Known Issues
* ssh keys currently are not supported
* unstable work with active TSVNCache (part of the TortoiseSVN)
# TeamCity Cached Subversion Plugin

TeamCity Cached Subversion plugin provides support of caching svn repositories on agent.
Can be useful with slow remote and/or big repositories.

# Compatibility

The plugin is compatible with [TeamCity](https://www.jetbrains.com/teamcity/download/) 10.0.x and greater (tested only 10.0.x yet).
 
# Build

execute *mvn package* in root folder

# Using

1. Install plugin
2. Write on server in *config/cachedSubversion.repositories* file urls you want to cache. One url one line. Urls can be to any directory in repository, only that part will be cached.
3. In build configuration with standard Subversion repository change checkout mode to *"Do not checkout files automatically"* and add build feature *"cache subversion"*.

# Features

* Flexible settings. For instance you can disable caching on local to repository agents and use shared network cache for each region
* Checkout rules to files are supported (standard agent side checkout doesn't)

# Known Issues

* ssh keys currently are not supported
* unstable work with active TSVNCache (part of the TortoiseSVN) 
* configurations with other vcs are not supported. Workaround: you should checkout manually or add as artifact dependency

# Additional Agent Side Parameters

* cachedSubversion.disabled - if parameter exists, than agent will be used original url
* cachedSubversion.cachePath - local or network path to all repositories cache
* cachedSubversion.cachePath.{name} - local or network or http path to specific repository cache. Overrides *cachedSubversion.cachePath* parameter 

# Server Side Configuration

File *config/cachedSubversion.repositories* on server contains urls you want to cache. Write on each line space delimited parameters:
1. source url
2. name for reference in agent parameters
3. cache path/url - default cache url for this repository. Overrides agent parameter *cachedSubversion.cachePath*, overridable by *cachedSubversion.cachePath.{name}* agent parameter   

    http://example.com/test1/trunk/subdirectory
    http://example.com/test2 test2
    http://example.com/test3 test3 //network/path/to/shared/cache 
    http://example.com/test4 test4 http://example.org/cache 

# Shared Cache

Several agents can share one cache. Shared cache can be:
1. network path
2. HTTP repository (recommended)

### Network path

* can be installed to all repositories via agent parameter *cachedSubversion.cachePath* 
* can be installed to specific repository via agent parameter *cachedSubversion.cachePath.{name}* or to all agents via server side configuration
* doesn't require any repository preparation

### HTTP repository
* can't be installed to all repositories
* can be installed to specific repository via agent parameter *cachedSubversion.cachePath.{name}* or to all agents via server side configuration
* should have same login/password
* you should manually create an empty repository and add *pre-revprop-change* hook as described [here](http://www.microhowto.info/howto/mirror_a_subversion_repository.html) or [here](http://www.cardinalpath.com/how-to-use-svnsync-to-create-a-mirror-backup-of-your-subversion-repository/) 
* you can do a full synchronization manually to not occupy agent while synchronizing
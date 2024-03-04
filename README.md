# PS data update API

## Developement

### Release procedure

Whenever a version is ready for release, run the following commands on the `main` brnahc (or on the maintenance branch if we're about to issue a production FIX). This should run on any shell, be it `bash`, `cmd` or if needed `gitbash`.

```bash
mvn release:prepare -DautoVersionSubmodules=true -DtagNameFormat=@{version}
git push
git push origin <new_version_tag>
```

where `<new_version_tag>` stands for the new version.

Eg to relase `2.0.0` :

```bash
mvn release:prepare -DautoVersionSubmodules=true -DtagNameFormat=@{version}
git push
git push origin 2.0.0
```

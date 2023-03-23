[![npm version](https://img.shields.io/npm/v/guacamole-common-js.svg)](https://www.npmjs.com/package/guacamole-common-js) [![downloads](https://img.shields.io/npm/dt/guacamole-common-js.svg)](https://www.npmjs.com/package/guacamole-common-js)

# What is guacamole-common-js?

guacamole-common-js is the core JavaScript library used by the Guacamole web
application.

guacamole-common-js provides an efficient HTTP tunnel for transporting
protocol data between JavaScript and the web application, as well as an
implementation of a Guacamole protocol client and abstract synchronized
drawing layers.

## What is this fork for?

This is a fork of [glyptodon/guacamole-client](https://github.com/glyptodon/guacamole-client)
meant for distribution via npm. Prior to a version being submitted to npm a gulp buildscript
combines all `guacamole-common-js` files and minifies them.

None of the guacamole project files besides the combined javascript sources are included in
the npm release, so you won't have to download any files you don't need. You can read which
files/folders are omitted in the `.npmignore` file.

You can install this library via npm:

```
npm install --save guacamole-common-js
```

```js
const Guacamole = require('guacamole-common-js');

// ... or ES6
import Guacamole from 'guacamole-common-js';
```

> Previously this fork was available as `padarom-guacamole-common-js`, which has been deprecated
in favor of the new version of the fork which is much easier to maintain thanks to the gulp buildscript.

#### Version numbers
I intend to keep the npm version numbers identical to the guacamole version that I publish. This doesn't always work 100% in case something is missing or at fault, so I will need to release a patch. These patch versions still keep the same version number, but add a letter designating the patch (e.g. `1.0.0-b`). In case the guacamole upstream ever releases any patches with a similar version number, I can't guarantee that the patch letter of the original Guacamole sources and the release of this fork match up for that specific version.

#### Fork origin
Apparently the original repository `[glyptodon/guacamole-client](https://github.com/glyptodon/guacamole-client)` is no longer maintained and the actual guacamole client is now maintained at `[apache/guacamole-client](https://github.com/apache/guacamole-client)`. Unfortunately there is no way to rewrite the fork origin on GitHub, so this repository will show the wrong origin. In order to update from upstream, you may do the following:

```
$ git remote add upstream git@github.com:apache/guacamole-client.git
$ git pull upstream master --rebase
```

## Documentation
Distribution-specific packages are available from the files section of the main
project page:

    http://sourceforge.net/projects/guacamole/files/

Distribution-specific documentation is provided on the Guacamole wiki:

    http://guac-dev.org/

# Reporting problems

_I am not a maintainer or contributor to the original guacamole repository. I only created this fork and published it to npm._

Please report any bugs encountered by opening a new ticket at the Trac system hosted at:

    http://guac-dev.org/trac/

Any bugs or questions to this fork specifically can be asked via issues on GitHub.

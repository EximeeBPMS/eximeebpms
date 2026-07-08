# EximeeBPMS Webapp

This is the EximeeBPMS web application source.
Clean, package and install it via [Maven](https://maven.apache.org/).

## Structure of this project

The structure is as follows:

* `assembly` - Java sources and tests for the EximeeBPMS web application based on `javax` namespace.
* `assembly-jakarta` - Java sources and tests for the EximeeBPMS web application based on `jakarta` namespace.
  * This module is created from the `assembly` module via code transformation.
* `frontend` - HTML, CSS and Javascript sources as well as Plugins and tests for the EximeeBPMS webapplications Cockpit, Tasklist and Admin.

## FRONTEND

### UI

There are 3 web applications available for the EximeeBPMS Platform :

* __cockpit__: an administration interface for processes and decisions
* __tasklist__: provides an interface to process user tasks
* __admin__: is used to administer users, groups and their authorizations

The webapps above are relying on 2 libraries:

* __eximeebpms-bpm-sdk-js__: provides tools for developers who want interact with the platform using Javascript
* __eximeebpms-commons-ui__: is a set of shared scripts, templates and assets, used in the different webapps


#### Plugins

Parts of the web applications can be extended using plugins.

See [plugin development guide](http://docs.eximeebpms.org/real-life/how-to/#cockpit-how-to-develop-a-cockpit-plugin) for details.

#### Translations

English and german translations are located in the `ui/<app>/client/locales` folders.  
Translations for other languages are available in the [eximeebpms-webapp-translations](https://github.com/camunda-community-hub/camunda-7-webapp-translations) repository.

### Libraries

#### [eximeebpms-bpm-sdk-js](https://github.com/EximeeBPMS/eximeebpms/tree/master/webapps/frontend/eximeebpms-bpm-sdk-js)

Has tools to work with the REST API and forms (included transitively via eximeebpms-commons-ui).

#### [eximeebpms-commons-ui](https://github.com/EximeeBPMS/eximeebpms/tree/master/webapps/frontend/eximeebpms-commons-ui)

Contains resources like images, [`.less`](http://lesscss.org) stylesheets as well as some [angular.js](http://angularjs.org) modules.

### Prerequisite

You need [node.js](http://nodejs.org) 20.14.0 and npm 10.7.0. Both are installed automatically by `frontend-maven-plugin` during a Maven build — manual installation is only needed for standalone frontend development.

### Setup

#### Using Webpack

Start the backend and frontend dev servers in separate terminals:

```sh
# Terminal 1 — backend (REST API + engine) on http://localhost:8080
cd webapps/assembly-jakarta
mvn jetty:run -Pdevelop

# Terminal 2 — frontend dev server on http://localhost:8081
cd webapps/frontend
npm install
npm start
```

The webapps are then available at [http://localhost:8081/eximeebpms/app/cockpit/default/](http://localhost:8081/eximeebpms/app/cockpit/default/).

Demo users created on first start (login = password): `demo`, `john`, `mary`, `peter`.

## Browsers support

The supported browsers are:

- Chrome Latest
- Firefox Latest
- Edge Latest

## Contributing

Have a look at our [contribution guide](https://github.com/EximeeBPMS/eximeebpms/blob/master/CONTRIBUTING.md) for how to contribute to this repository.


## Help and support

* [Documentation](http://docs.eximeebpms.org/manual/)
* [Stackoverflow](https://stackoverflow.com/questions/tagged/camunda)

## License

The source files in this repository are made available under the [Apache License Version 2.0](./LICENSE).

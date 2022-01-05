# signably

Demo project using [Ably](https://www.ably.com/) from ClojureScript in
the browser.

## Pre-requisites

The project is built using
[`shadow-cljs`](https://github.com/thheller/shadow-cljs) (for
ClojureScript) with [Leiningen](https://www.leiningen.org)
integration. You'll want both installed and functional to work with
the project. Note that `shadow-cljs` depends on `npm`.

## Development

First, start the backend server. Recommended way is to connect your
development tools to the project, compile the code from the
`signably.server` namespace, then eval something like the following in
your REPL:

``` clojure
(def srv (-main))
```

Note that you can kill the server with `(.stop srv)` if you need to restart.

Now startup `shadow-cljs` for the front-end code using:

``` shell
shadow-cljs watch app
```

This will trigger live code reloading in the browser. Open your
browser to http://localhost:3000/ (or whatever you changed the port
to!) and it'll connect when the compiled JS loads.

Note that `shadow-cljs` also prints a port number for nREPL, so if you
want to connect your editor REPL to the frontend too, you can.

## Building for release

Two steps:

* Compile the release JS code using `shadow-cljs`
* Compile the standalone backend jar, which will bundle release
  minified JS code along with other assets.

Easiest way to do this is:

``` shell
shadow-cljs release app && lein uberjar
```

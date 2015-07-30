# HTML in Elm

Blazing fast HTML *and* all of the benefits of Elm.

Read more about how this library works and how to use it in [this post][html].

[html]: http://elm-lang.org/blog/Blazing-Fast-Html.elm

### Get Started

Once you have the [Elm Platform][platform], use [elm-package][package] to download
elm-html. The commands would be something like this:

[platform]: https://github.com/elm-lang/elm-platform#elm-platform
[package]: https://github.com/elm-lang/elm-package#basic-usage

```
mkdir myProject
cd myProject
elm-package install evancz/elm-html
```

After that you will be able to use anything described in [the
documentation][docs].

[docs]: http://package.elm-lang.org/packages/evancz/elm-html/latest/

Another way to get started is to fork the [elm-todomvc project][todomvc] and
work from there. This will also give you a reasonable architecture for your
project.

[todomvc]: https://github.com/evancz/elm-todomvc

### Example

Try out [TodoMVC written in Elm][demo] and check out the [source code][src].

[![Live Demo](https://raw.githubusercontent.com/evancz/elm-html/master/todo.png)][demo]

[demo]: http://evancz.github.io/elm-todomvc/
[src]: https://github.com/evancz/elm-todomvc/

For information on architecting larger projects, see [this
document][architecture].

[architecture]: https://gist.github.com/evancz/2b2ba366cae1887fe621

### Performance

[The benchmarks][bench] show that elm-html is really fast when compared to
other popular alternatives.

[bench]: http://evancz.github.io/todomvc-perf-comparison/


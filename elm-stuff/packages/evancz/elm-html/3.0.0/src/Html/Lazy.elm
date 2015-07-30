module Html.Lazy where
{-| Since all Elm functions are pure we have a guarantee that the same input
will always result in the same output. This module gives us tools to be lazy
about building `Html` that utilize this fact.

Rather than immediately applying functions to their arguments, the `lazy`
functions just bundle the function and arguments up for later. When diffing
the old and new virtual DOM, it checks to see if all the arguments are equal.
If so, it skips calling the function!

This is a really cheap test and often makes things a lot faster, but definitely
benchmark to be sure!

@docs lazy, lazy2, lazy3
-}

import Html exposing (Html)
import VirtualDom


lazy : (a -> Html) -> a -> Html
lazy =
    VirtualDom.lazy

lazy2 : (a -> b -> Html) -> a -> b -> Html
lazy2 =
    VirtualDom.lazy2

lazy3 : (a -> b -> c -> Html) -> a -> b -> c -> Html
lazy3 =
    VirtualDom.lazy3

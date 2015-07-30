module VirtualDom where
{-| API to the core diffing algorithm. Can serve as a foundation for libraries
that expose more helper functions for HTML or SVG.

# Create
@docs text, node

# Conversions
@docs toElement, fromElement

# Declare Properties
@docs property

# Events
@docs on

-}

import Graphics.Element exposing (Element)
import Json.Decode as Json
import Native.VirtualDom


type Node = Node

{-| Create a DOM node with a tag name, a list of HTML properties that can
include styles and event listeners, a list of CSS properties like `color`, and
a list of child nodes.

    import Json.Encode as Json

    hello : Node
    hello =
        node "div" [] [ text "Hello!" ]

    greeting : Node
    greeting =
        node "div"
            [ property "id" (Json.string "greeting") ]
            [ text "Hello!" ]
-}
node : String -> List Property -> List Node -> Node
node =
    Native.VirtualDom.node


{-| Just put plain text in the DOM. It will escape the string so that it appears
exactly as you specify.

    text "Hello World!"
-}
text : String -> Node
text =
    Native.VirtualDom.text


{-| Embed an `Node` value in Elm's rendering system. Like any other `Element`,
this requires a known width and height, so it is not yet clear if this can be
made more convenient in the future.
-}
toElement : Int -> Int -> Node -> Element
toElement =
    Native.VirtualDom.toElement


{-| Embed an `Element` as `Html`.
-}
fromElement : Element -> Node
fromElement =
    Native.VirtualDom.fromElement


-- PROPERTIES

type Property = Property


property : String -> Json.Value -> Property
property =
    Native.VirtualDom.property


attribute : String -> String -> Property
attribute =
    Native.VirtualDom.attribute


-- EVENTS

on : String -> Json.Decoder a -> (a -> Signal.Message) -> Property
on =
    Native.VirtualDom.on


-- OPTIMIZATION

lazy : (a -> Node) -> a -> Node
lazy =
    Native.VirtualDom.lazy


lazy2 : (a -> b -> Node) -> a -> b -> Node
lazy2 =
    Native.VirtualDom.lazy2


lazy3 : (a -> b -> c -> Node) -> a -> b -> c -> Node
lazy3 =
    Native.VirtualDom.lazy3

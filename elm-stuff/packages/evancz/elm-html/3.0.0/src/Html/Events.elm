module Html.Events
    ( onBlur, onFocus, onSubmit
    , onKeyUp, onKeyDown, onKeyPress
    , onClick, onDoubleClick
    , onMouseMove
    , onMouseDown, onMouseUp
    , onMouseEnter, onMouseLeave
    , onMouseOver, onMouseOut
    , on
    , targetValue, targetChecked, keyCode
    ) where
{-|
It is often helpful to create an [Union Type][] so you can have many different kinds
of events as seen in the [TodoMVC][] example.

[Union Type]: http://elm-lang.org/learn/Union-Types.elm
[TodoMVC]: https://github.com/evancz/elm-todomvc/blob/master/Todo.elm

# Focus Helpers
@docs onBlur, onFocus, onSubmit

# Keyboard Helpers
@docs onKeyUp, onKeyDown, onKeyPress

# Mouse Helpers
@docs onClick, onDoubleClick, onMouseMove,
      onMouseDown, onMouseUp,
      onMouseEnter, onMouseLeave,
      onMouseOver, onMouseOut

# All Events
@docs on, targetValue, targetChecked, keyCode
-}

import Html exposing (Attribute)
import Json.Decode as Json exposing (..)
import VirtualDom


on : String -> Json.Decoder a -> (a -> Signal.Message) -> Attribute
on =
    VirtualDom.on


-- COMMON DECODERS

targetValue : Json.Decoder String
targetValue =
    at ["target", "value"] string


targetChecked : Json.Decoder Bool
targetChecked =
    at ["target", "checked"] bool


keyCode : Json.Decoder Int
keyCode =
    ("keyCode" := int)


-- MouseEvent

messageOn : String -> Signal.Address a -> a -> Attribute
messageOn name addr msg =
    on name value (\_ -> Signal.message addr msg)


onClick : Signal.Address a -> a -> Attribute
onClick =
    messageOn "click"


onDoubleClick : Signal.Address a -> a -> Attribute
onDoubleClick =
    messageOn "dblclick"


onMouseMove : Signal.Address a -> a -> Attribute
onMouseMove =
    messageOn "mousemove"


onMouseDown : Signal.Address a -> a -> Attribute
onMouseDown =
    messageOn "mousedown"


onMouseUp : Signal.Address a -> a -> Attribute
onMouseUp =
    messageOn "mouseup"


onMouseEnter : Signal.Address a -> a -> Attribute
onMouseEnter =
    messageOn "mouseenter"


onMouseLeave : Signal.Address a -> a -> Attribute
onMouseLeave =
    messageOn "mouseleave"


onMouseOver : Signal.Address a -> a -> Attribute
onMouseOver =
    messageOn "mouseover"


onMouseOut : Signal.Address a -> a -> Attribute
onMouseOut =
    messageOn "mouseout"



-- KeyboardEvent

onKey : String -> Signal.Address a -> (Int -> a) -> Attribute
onKey name addr handler =
    on name keyCode (\code -> Signal.message addr (handler code))


onKeyUp : Signal.Address a -> (Int -> a) -> Attribute
onKeyUp =
    onKey "keyup"


onKeyDown : Signal.Address a -> (Int -> a) -> Attribute
onKeyDown =
    onKey "keydown"


onKeyPress : Signal.Address a -> (Int -> a) -> Attribute
onKeyPress =
    onKey "keypress"


-- Simple Events

onBlur : Signal.Address a -> a -> Attribute
onBlur =
    messageOn "blur"


onFocus : Signal.Address a -> a -> Attribute
onFocus =
    messageOn "focus"


onSubmit : Signal.Address a -> a -> Attribute
onSubmit =
    messageOn "submit"

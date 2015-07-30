module TextReverser where

import Graphics.Input as Input
import Html (Html, Attribute, text, toElement, div, input)
import Html.Attributes (..)
import Html.Events (on, targetValue)
import Signal
import String


-- VIEW

view : String -> Html
view string =
    div []
        [ stringInput string
        , reversedString string
        ]


reversedString : String -> Html
reversedString string =
    div [ myStyle ] [ text (String.reverse string) ]


stringInput : String -> Html
stringInput string =
    input
        [ placeholder "Text to reverse"
        , value string
        , on "input" targetValue (Signal.send updates << identity)
        , myStyle
        ]
        []


myStyle : Attribute
myStyle = style
    [ ("width", "100%")
    , ("height", "40px")
    , ("padding", "10px 0")
    , ("font-size", "2em")
    , ("text-align", "center")
    ]


-- SIGNALS

main : Signal Html
main =
    Signal.map view (Signal.subscribe updates)

updates : Signal.Channel String
updates =
    Signal.channel ""

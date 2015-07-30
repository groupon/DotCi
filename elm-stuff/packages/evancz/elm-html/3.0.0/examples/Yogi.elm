
import Html (Html, toElement, img)
import Html.Attributes (src, style)
import Signal
import Time (fps)
import Window


-- VIEW

view : Int -> Html
view n =
    img [ src "http://elm-lang.org/yogi.jpg"
        , style
            [ ("width", toString n ++ "px")
            , ("height", toString n ++ "px")
            ]
        ]
        []


-- SIGNALS

main : Signal Html
main =
    Signal.map view size

size : Signal Int
size =
    fps 30
      |> Signal.foldp (+) 0
      |> Signal.map (\t -> round (200 + 100 * sin (degrees t / 10)))

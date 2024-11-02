package krasa.editorGroups.support

import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.util.ui.ColorIcon
import java.awt.Color
import javax.swing.Icon

@Suppress("UseJBColor")
val colorMap: HashMap<String, Color> = hashMapOf(
  "black" to Color(0x000000),
  "deepskyblue" to Color(0x00bfff),
  "mediumblue" to Color(0x0000cd),
  "darkturquoise" to Color(0x00ced1),
  "mediumspringgreen" to Color(0x00fa9a),
  "blue" to Color(0x0000ff),
  "lime" to Color(0x00ff00),
  "springgreen" to Color(0x00ff7f),
  "aqua" to Color(0x00ffff),
  "cyan" to Color(0x00ffff),
  "dodgerblue" to Color(0x1e90ff),
  "seagreen" to Color(0x2e8b57),
  "darkslategray" to Color(0x2f4f4f),
  "darkslategrey" to Color(0x2f4f4f),
  "mediumseagreen" to Color(0x3cb371),
  "indigo" to Color(0x4b0082),
  "cadetblue" to Color(0x5f9ea0),
  "slateblue" to Color(0x6a5acd),
  "olivedrab" to Color(0x6b8e23),
  "mediumslateblue" to Color(0x7b68ee),
  "lawngreen" to Color(0x7cfc00),
  "chartreuse" to Color(0x7fff00),
  "aquamarine" to Color(0x7fffd4),
  "blueviolet" to Color(0x8a2be2),
  "darkblue" to Color(0x00008b),
  "darkred" to Color(0x8b0000),
  "darkcyan" to Color(0x008b8b),
  "darkmagenta" to Color(0x8b008b),
  "saddlebrown" to Color(0x8b4513),
  "darkseagreen" to Color(0x8fbc8f),
  "yellowgreen" to Color(0x9acd32),
  "lightseagreen" to Color(0x20b2aa),
  "limegreen" to Color(0x32cd32),
  "turquoise" to Color(0x40e0d0),
  "mediumturquoise" to Color(0x48d1cc),
  "mediumaquamarine" to Color(0x66cdaa),
  "navy" to Color(0x000080),
  "skyblue" to Color(0x87ceeb),
  "lightskyblue" to Color(0x87cefa),
  "lightgreen" to Color(0x90ee90),
  "palegreen" to Color(0x98fb98),
  "forestgreen" to Color(0x228b22),
  "darkslateblue" to Color(0x483d8b),
  "darkolivegreen" to Color(0x556b2f),
  "royalblue" to Color(0x4169e1),
  "steelblue" to Color(0x4682b4),
  "darkgreen" to Color(0x006400),
  "cornflowerblue" to Color(0x6495ed),
  "green" to Color(0x008000),
  "teal" to Color(0x008080),
  "mediumpurple" to Color(0x9370d8),
  "darkviolet" to Color(0x9400d3),
  "darkorchid" to Color(0x9932cc),
  "midnightblue" to Color(0x191970),
  "dimgray" to Color(0x696969),
  "dimgrey" to Color(0x696969),
  "slategray" to Color(0x708090),
  "slategrey" to Color(0x708090),
  "lightslategray" to Color(0x778899),
  "lightslategrey" to Color(0x778899),
  "maroon" to Color(0x800000),
  "purple" to Color(0x800080),
  "olive" to Color(0x808000),
  "gray" to Color(0x808080),
  "grey" to Color(0x808080),
  "darkgray" to Color(0xa9a9a9),
  "darkgrey" to Color(0xa9a9a9),
  "brown" to Color(0xa52a2a),
  "sienna" to Color(0xa0522d),
  "lightblue" to Color(0xadd8e6),
  "greenyellow" to Color(0xadff2f),
  "paleturquoise" to Color(0xafeeee),
  "lightsteelblue" to Color(0xb0c4de),
  "powderblue" to Color(0xb0e0e6),
  "darkgoldenrod" to Color(0xb8860b),
  "firebrick" to Color(0xb22222),
  "mediumorchid" to Color(0xba55d3),
  "rosybrown" to Color(0xbc8f8f),
  "darkkhaki" to Color(0xbdb76b),
  "silver" to Color(0xc0c0c0),
  "mediumvioletred" to Color(0xc71585),
  "indianred" to Color(0xcd5c5c),
  "peru" to Color(0xcd853f),
  "tan" to Color(0xd2b48c),
  "lightgray" to Color(0xd3d3d3),
  "lightgrey" to Color(0xd3d3d3),
  "thistle" to Color(0xd8bfd8),
  "chocolate" to Color(0xd2691e),
  "palevioletred" to Color(0xd87093),
  "orchid" to Color(0xda70d6),
  "goldenrod" to Color(0xdaa520),
  "crimson" to Color(0xdc143c),
  "gainsboro" to Color(0xdcdcdc),
  "plum" to Color(0xdda0dd),
  "burlywood" to Color(0xdeb887),
  "lightcyan" to Color(0xe0ffff),
  "lavender" to Color(0xe6e6fa),
  "darksalmon" to Color(0xe9967a),
  "violet" to Color(0xee82ee),
  "palegoldenrod" to Color(0xeee8aa),
  "khaki" to Color(0xf0e68c),
  "aliceblue" to Color(0xf0f8ff),
  "honeydew" to Color(0xf0fff0),
  "azure" to Color(0xf0ffff),
  "sandybrown" to Color(0xf4a460),
  "wheat" to Color(0xf5deb3),
  "beige" to Color(0xf5f5dc),
  "whitesmoke" to Color(0xf5f5f5),
  "mintcream" to Color(0xf5fffa),
  "ghostwhite" to Color(0xf8f8ff),
  "lightcoral" to Color(0xf08080),
  "salmon" to Color(0xfa8072),
  "antiquewhite" to Color(0xfaebd7),
  "linen" to Color(0xfaf0e6),
  "lightgoldenrodyellow" to Color(0xfafad2),
  "oldlace" to Color(0xfdf5e6),
  "red" to Color(0xff0000),
  "fuchsia" to Color(0xff00ff),
  "magenta" to Color(0xff00ff),
  "coral" to Color(0xff7f50),
  "darkorange" to Color(0xff8c00),
  "hotpink" to Color(0xff69b4),
  "deeppink" to Color(0xff1493),
  "orangered" to Color(0xff4500),
  "tomato" to Color(0xff6347),
  "lightsalmon" to Color(0xffa07a),
  "orange" to Color(0xffa500),
  "lightpink" to Color(0xffb6c1),
  "pink" to Color(0xffc0cb),
  "gold" to Color(0xffd700),
  "peachpuff" to Color(0xffdab9),
  "navajowhite" to Color(0xffdead),
  "moccasin" to Color(0xffe4b5),
  "bisque" to Color(0xffe4c4),
  "mistyrose" to Color(0xffe4e1),
  "blanchedalmond" to Color(0xffebcd),
  "papayawhip" to Color(0xffefd5),
  "lavenderblush" to Color(0xfff0f5),
  "seashell" to Color(0xfff5ee),
  "cornsilk" to Color(0xfff8dc),
  "lemonchiffon" to Color(0xfffacd),
  "floralwhite" to Color(0xfffaf0),
  "snow" to Color(0xfffafa),
  "rebeccapurple" to Color(0x663399),
  "yellow" to Color(0xffff00),
  "lightyellow" to Color(0xffffe0),
  "ivory" to Color(0xfffff0),
  "white" to Color(0xffffff)
)

val colorSet: MutableSet<String> = colorMap.keys

/**
 * Retrieves the Color instance based on the specified color name and modifiers.
 *
 * @param color the color name with optional modifiers. Format: "colorName[+/-tones]". Examples: "red", "blue+2", "green-1".
 * @return the Color instance corresponding to the provided color name and modifiers, or null if not found.
 */
fun getColorInstance(color: String): Color? {
  var colorName = color.lowercase()
  var modifier = CharArray(0)

  val lighterIndex = color.indexOf("-")
  if (lighterIndex > 0) {
    colorName = color.substring(0, lighterIndex)
    modifier = color.substring(lighterIndex).toCharArray()
  }

  val darkerIndex = color.indexOf("+")
  if (darkerIndex > 0) {
    colorName = color.substring(0, darkerIndex)
    modifier = color.substring(darkerIndex).toCharArray()
  }

  var myColor = colorMap[colorName]
  var number = ""

  modifier.indices.reversed().forEach { i ->
    val c = modifier[i]
    when {
      Character.isDigit(c) -> number += c
      c == '+'             -> {
        var tones = 1
        if (number.isNotEmpty()) {
          tones = number.toInt()
          number = ""
        }
        myColor = ColorUtil.brighter(myColor!!, tones)
      }

      c == '-'             -> {
        var tones = 1
        if (number.isNotEmpty()) {
          tones = number.toInt()
          number = ""
        }
        myColor = ColorUtil.darker(myColor!!, tones)
      }
    }
  }
  return myColor
}

/**
 * Creates an icon with the specified color and size for use in the gutter.
 *
 * @param color The color to be used for the icon.
 * @param size The size of the icon, default is 12.
 * @return An Icon instance with the specified color and size.
 */
fun gutterColorIcon(color: Color, size: Int = 12): Icon {
  val borderColor = JBColor(Color.black, Color.white)
  val arc = 24
  return ColorIcon(
    /* width = */ size,
    /* height = */ size,
    /* colorWidth = */ size,
    /* colorHeight = */ size,
    /* color = */ color,
    /* borderColor = */ borderColor,
    /* arc = */ arc
  )
}

/**
 * Converts a Color object to its hexadecimal string representation.
 *
 * @return A string representing the color in hexadecimal format.
 */
fun Color.toHex(): String = ColorUtil.toHex(this)

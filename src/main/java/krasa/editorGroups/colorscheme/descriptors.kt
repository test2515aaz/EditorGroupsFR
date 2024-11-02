package krasa.editorGroups.colorscheme

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

val KEYWORD: TextAttributesKey = DefaultLanguageHighlighterColors.KEYWORD
val METADATA: TextAttributesKey = DefaultLanguageHighlighterColors.METADATA
val MACRO: TextAttributesKey = DefaultLanguageHighlighterColors.STATIC_METHOD
val COMMENT: TextAttributesKey = DefaultLanguageHighlighterColors.LINE_COMMENT
val STRING: TextAttributesKey = DefaultLanguageHighlighterColors.STRING
val CONSTANT: TextAttributesKey = DefaultLanguageHighlighterColors.CONSTANT
val PREDEFINED_SYMBOL: TextAttributesKey = DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL

val EG_COMMENT: TextAttributesKey = TextAttributesKey.createTextAttributesKey("EDITOR_GROUPS.COMMENT", COMMENT)
val EG_PATH: TextAttributesKey = TextAttributesKey.createTextAttributesKey("EDITOR_GROUPS.PATH", STRING)
val EG_KEYWORD: TextAttributesKey = TextAttributesKey.createTextAttributesKey("EDITOR_GROUPS.KEYWORD", KEYWORD)
val EG_METADATA: TextAttributesKey = TextAttributesKey.createTextAttributesKey("EDITOR_GROUPS.METADATA", METADATA)
val EG_MACRO: TextAttributesKey = TextAttributesKey.createTextAttributesKey("EDITOR_GROUPS.MACRO", MACRO)
val EG_CONSTANT: TextAttributesKey = TextAttributesKey.createTextAttributesKey("EDITOR_GROUPS.CONSTANT", CONSTANT)
val EG_COLOR: TextAttributesKey = TextAttributesKey.createTextAttributesKey("EDITOR_GROUPS.COLOR", PREDEFINED_SYMBOL)

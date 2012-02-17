module util::SyntaxHighlightingTemplates

import vis::Figure;
import util::IDE;

public Contribution getSolarizedLightCategories() {
	// colors would be even better if background was #FDF6E3 and text #FDF6E3
	return categories((
		// default categories
		//"Normal": font(),
		"Type": font({}, rgb(0x74,0x8B,0x00)),
		"Identifier": font({}, rgb(0x48,0x5A,0x62)),
		"Variable": font({}, rgb(0x26,0x8B,0xD2)),
		//"Constant": font({}, rgb(0xD3,0x36,0x82)),
		"Constant": font({}, rgb(0xCB,0x4B,0x16)),
		"Comment": font({italic()}, rgb(0x8a,0x8a,0x8a)),
		"Todo": font({bold()}, rgb(0xaf,0x00,0x00)),
		//"Quote": font(), // no idea what this category means?
		"MetaAmbiguity": font({bold(), italic()}, rgb(0xaf,0x00,0x00)),
		"MetaVariable": font({}, rgb(0x00,0x87,0xff)),
		"MetaKeyword": font({}, rgb(0x85,0x99,0x00)),
		// new categories
		"StringLiteral": font({}, rgb(0x2A,0xA1,0x98))
	));
}
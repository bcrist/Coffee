package com.magicmoremagic.coffee.parser;

public abstract class StringUtil {

	private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	
	public static String toLiteral(String terminal) {
		if (terminal == null)
			return null;
		
		StringBuilder sb = new StringBuilder((int)(terminal.length() * 1.2f + 2));
		sb.append('"');
		
		final int len = terminal.length();
		char surrogate = 0;
		for (int i = 0; i < len; ++i) {
			char cu = terminal.charAt(i);
			surrogate = tryLiteralEncode(sb, cu, surrogate);
		}
		if (surrogate != 0) {
			encodeUtf16(sb, surrogate);
		}
		
		sb.append('"');
		return sb.toString();
	}
	
	public static String toIdentifier(String name) {
		if (name == null)
			return null;
		
		StringBuilder sb = new StringBuilder((int)(name.length() * 1.05f + 1));
		
		final int len = name.length();
		char surrogate = 0;
		for (int i = 0; i < len; ++i) {
			char cu = name.charAt(i);
			surrogate = (i == 0 ? tryIdentifierEncodeFirst(sb, cu, surrogate) : 
				tryIdentifierEncode(sb, cu, surrogate));
		}
		if (surrogate != 0) {
			encodeUtf16(sb, surrogate);
		}		
		
		return sb.toString();
	}
	
	private static char tryLiteralEncode(StringBuilder sb, char cu, char surrogate) {
		if (surrogate != 0) {
			if (cu >= 0xDC00 && cu <= 0xDFFF) {
				int cp = ((surrogate - 0xD800) << 10) | (cu - 0xDC00);
				cp += 0x100000;
				encodeUtf32(sb, cp);
			} else {
				encodeUtf16(sb, surrogate);
			}
		}
		
		if (cu >= '#' && cu <= '[' ||
			cu >= ']' && cu <= '~' ||
			cu == ' ' || cu == '!') {
			sb.append(cu);
		} else switch (cu) {
			case '"':	sb.append("\\\""); break;
			case '\\':	sb.append("\\\\"); break;
			case '\t': 	sb.append("\\t"); break;
			case '\n':	sb.append("\\n"); break;
			case '\r':	sb.append("\\r"); break;
			case '\b':	sb.append("\\b"); break;
			case '\f':	sb.append("\\f"); break;
			case 11:	sb.append("\\v"); break;
			default:
				if (cu >= 0xD800 && cu <= 0xDBFF) {
					return cu;
				} else {
					encodeUtf16(sb, cu);
				}
		}
		return 0;
	}
	
	private static char tryIdentifierEncodeFirst(StringBuilder sb, char cu, char surrogate) {
		if (surrogate != 0) {
			if (cu >= 0xDC00 && cu <= 0xDFFF) {
				int cp = ((surrogate - 0xD800) << 10) | (cu - 0xDC00);
				cp += 0x100000;
				encodeUtf32(sb, cp);
			} else {
				encodeUtf16(sb, surrogate);
			}
		}
		
		if (cu >= 'a' && cu <= 'z' ||
			cu >= 'A' && cu <= 'Z' ||
			cu == '_' || cu == '$') {
			sb.append(cu);
		} else switch (cu) {
			case '\\':	sb.append("\\\\"); break;
			case '\t': 	sb.append("\\t"); break;
			case '\n':	sb.append("\\n"); break;
			case '\r':	sb.append("\\r"); break;
			case '\b':	sb.append("\\b"); break;
			case '\f':	sb.append("\\f"); break;
			case 11:	sb.append("\\v"); break;
			default:
				if (cu >= 0xD800 && cu <= 0xDBFF) {
					return cu;
				} else {
					encodeUtf16(sb, cu);
				}
		}
		return 0;
	}
	
	private static char tryIdentifierEncode(StringBuilder sb, char cu, char surrogate) {
		if (surrogate != 0) {
			if (cu >= 0xDC00 && cu <= 0xDFFF) {
				int cp = ((surrogate - 0xD800) << 10) | (cu - 0xDC00);
				cp += 0x100000;
				encodeUtf32(sb, cp);
			} else {
				encodeUtf16(sb, surrogate);
			}
		}
		
		if (cu >= 'a' && cu <= 'z' ||
			cu >= 'A' && cu <= 'Z' ||
			cu >= '0' && cu <= '9' ||
			cu == '_' || cu == '$') {
			sb.append(cu);
		} else switch (cu) {
			case '\\':	sb.append("\\\\"); break;
			case '\t': 	sb.append("\\t"); break;
			case '\n':	sb.append("\\n"); break;
			case '\r':	sb.append("\\r"); break;
			case '\b':	sb.append("\\b"); break;
			case '\f':	sb.append("\\f"); break;
			case 11:	sb.append("\\v"); break;
			default:
				if (cu >= 0xD800 && cu <= 0xDBFF) {
					return cu;
				} else {
					encodeUtf16(sb, cu);
				}
		}
		return 0;
	}
		
	private static void encodeUtf16(StringBuilder sb, char cu) {
		if (cu < 256) {
			sb.append("\\x");
			sb.append(HEX_DIGITS[cu >> 4]);
			sb.append(HEX_DIGITS[cu & 0xF]);
		} else {
			sb.append("\\u");
			sb.append(HEX_DIGITS[cu >> 12]);
			sb.append(HEX_DIGITS[(cu >> 8) & 0xF]);
			sb.append(HEX_DIGITS[(cu >> 4) & 0xF]);
			sb.append(HEX_DIGITS[cu & 0xF]);
		}
	}
	
	private static void encodeUtf32(StringBuilder sb, int cp) {
		sb.append("\\U");
		sb.append(HEX_DIGITS[cp >> 28]);
		sb.append(HEX_DIGITS[(cp >> 24) & 0xF]);
		sb.append(HEX_DIGITS[(cp >> 20) & 0xF]);
		sb.append(HEX_DIGITS[(cp >> 16) & 0xF]);
		sb.append(HEX_DIGITS[(cp >> 12) & 0xF]);
		sb.append(HEX_DIGITS[(cp >> 8) & 0xF]);
		sb.append(HEX_DIGITS[(cp >> 4) & 0xF]);
		sb.append(HEX_DIGITS[cp & 0xF]);
	}
}

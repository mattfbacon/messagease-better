package nz.felle.messageasebetter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Decides from the text before the cursor whether a typed . , ; : ! ? ends a sentence, so that the
 * automatic space and the automatic upper case only follow a real sentence end ("128.32.",
 * "user.name@gmail." and ":)" are not).
 */
final class SentenceContext {
    /** What follows the punctuation symbol. */
    enum Spacing {
        NORMAL,     // an automatic space, and upper case after . ! ?
        NO_CAP,     // an automatic space, but no upper case ("e.g. the", "vs. the")
        WAIT,       // no automatic space; upper case waits for a space that is typed ("a@b.com.")
        NO_SPACE    // the word goes on: no space and no upper case ("user.name@gmail.", ":)")
    }

    static final int FIELD_TEXT = 0, FIELD_EMAIL = 1, FIELD_URI = 2,
        FIELD_OTHER = 3;   // passwords, numbers, terminals: no automatic upper case

    private static final Set<String> TLDS = new HashSet<String>(Arrays.asList(
        "com", "org", "net", "edu", "gov", "mil", "int", "info", "biz", "io", "co", "il", "uk",
        "de", "fr", "ru", "us", "me", "app", "dev", "ai", "tv", "ca", "au", "nl", "it", "es",
        "ch", "se", "no", "jp", "cn", "in", "br", "gr", "pl", "cz", "at", "be", "dk", "fi", "ie",
        "nz", "za", "xyz", "online", "site", "tech"));
    private static final Pattern SCHEME = Pattern.compile(
        "(?i)(?:https?|ftps?|sftp|file|mailto|ssh|git|tel)");
    private static final Pattern NUMBER = Pattern.compile("\\d+(?:\\.\\d+)+$");           // 128.32 v1.2.3
    private static final String[] NO_CAP_ABBREVIATIONS = {"e.g", "i.e", "vs", "cf", "viz", "approx"};
    private static final String[] DOTTED_ABBREVIATIONS = {"e.g.", "i.e.", "a.m.", "p.m.", "u.s.", "u.k.",
        "a.k.a.", "ph.d.", "b.c.", "a.d.", "p.s.", "n.b."};
    private static final Locale LOCALE = Locale.ENGLISH;
    private static final Pattern INITIALS = Pattern.compile("(?:^|[^\\p{L}.])\\p{L}(?:\\.\\p{L})+$"); // U.S

    private static final Pattern NO_CAP_WORD;
    static {
        final StringBuilder sb = new StringBuilder("(?i)(?:^|[^\\p{L}])(?:");
        for (int i = 0; i < NO_CAP_ABBREVIATIONS.length; i++) {
            sb.append(i > 0 ? "|" : "").append(Pattern.quote(NO_CAP_ABBREVIATIONS[i]));
        }
        NO_CAP_WORD = Pattern.compile(sb.append(")$").toString());
    }

    private static Pattern noCapWord() {
        return NO_CAP_WORD;
    }

    private SentenceContext() {}

    /**
     * The letter {@code next} continues a dotted abbreviation ("e." + "g") whose automatic space
     * was just entered; {@code before} is the text up to the cursor, ending with that space.
     */
    static boolean continuesAbbreviation(String before, String next) {
        if (next.length() != 1 || !Character.isLetter(next.charAt(0)) || !before.endsWith(". ")) {
            return false;
        }
        final String token = lastToken(before.substring(0, before.length() - 1));
        final String typed = (token + next).toLowerCase(LOCALE);
        for (String abbr : DOTTED_ABBREVIATIONS) {
            if (abbr.startsWith(typed) && typed.length() < abbr.length()
                && (before.length() - 1 == token.length()
                    || !Character.isLetter(before.charAt(before.length() - 2 - token.length())))) {
                return true;
            }
        }
        return false;
    }

    /** The letters typed so far in the abbreviation are all lower case: "e." but not "U.". */
    static boolean isLowerAbbreviation(String before) {
        final String token = lastToken(before.substring(0, before.length() - 1));
        return token.equals(token.toLowerCase(LOCALE));
    }

    /** The text after the last white space. */
    static String lastToken(String before) {
        int i = before.length();
        while (i > 0 && !Character.isWhitespace(before.charAt(i - 1))) {
            i--;
        }
        return before.substring(i);
    }

    /** What follows the punctuation symbol about to be entered after the text {@code before}. */
    static Spacing after(String before, String symbol, int field) {
        if (symbol.length() != 1) {
            return Spacing.NORMAL;
        }
        final char s = symbol.charAt(0);
        final String token = lastToken(before);
        if (s == '.' && (field == FIELD_EMAIL || field == FIELD_URI)) {
            return Spacing.NO_SPACE;                    // an address or a URL is being typed
        }
        if (s == ':' && (field == FIELD_URI || SCHEME.matcher(token).matches())) {
            return Spacing.NO_SPACE;                    // http:
        }
        if ((s == ':' || s == ';') && token.isEmpty()) {
            return Spacing.NO_SPACE;                    // an emoticon: :) ;) :-)
        }
        if (s != '.') {
            return Spacing.NORMAL;
        }
        final Spacing host = hostSpacing(token);
        if (host != null) {
            return host;
        }
        if (noCapWord().matcher(token).find() || token.endsWith(".")) {
            return Spacing.NO_CAP;                      // e.g. vs. and an ellipsis: lower case goes on
        }
        if (NUMBER.matcher(token).find() || INITIALS.matcher(token).find()) {
            return Spacing.WAIT;                        // 128.32. U.S.
        }
        return Spacing.NORMAL;
    }

    /** A dot in an e-mail address or a web address: the domain goes on unless it ends in a TLD. */
    private static Spacing hostSpacing(String token) {
        String host;
        final int at = token.lastIndexOf('@');
        final int scheme = token.indexOf("://");
        if (at > 0) {
            host = token.substring(at + 1);
        }
        else if (scheme > 0) {
            host = token.substring(scheme + 3);
        }
        else if (token.equalsIgnoreCase("www")) {
            return Spacing.NO_SPACE;
        }
        else if (token.regionMatches(true, 0, "www.", 0, 4)) {
            host = token;
        }
        else {
            return null;
        }
        if (host.indexOf('/') >= 0) {
            return Spacing.WAIT;                        // a path: the sentence may end here
        }
        final String label = host.substring(host.lastIndexOf('.') + 1).toLowerCase();
        return TLDS.contains(label) ? Spacing.WAIT : Spacing.NO_SPACE;
    }

    private static final Pattern SENTENCE_END = Pattern.compile(
        "[.!?][\"'\u201d\u2019)\\]}]*\\s+$");
    private static final Pattern LIST_MARKER = Pattern.compile("(?:^|\n)[ \t]*(?:[-*\u2022]|\\d+[.)])[ \t]+$");

    /** The text without its trailing white space. */
    static String rtrim(String s) {
        int n = s.length();
        while (n > 0 && Character.isWhitespace(s.charAt(n - 1))) {
            n--;
        }
        return s.substring(0, n);
    }

    /**
     * Should the next letter be upper case, judging by the text before the cursor: at the start of
     * the text or of a line (or of a list item), after a sentence end and a space. {@code capWords}
     * (a name field) also capitalizes after every space.
     */
    static boolean shouldCapitalize(String before, int field, boolean capWords) {
        if (field != FIELD_TEXT) {
            return false;
        }
        if (rtrim(before).isEmpty() || LIST_MARKER.matcher(before).find()) {
            return true;                                // the start of the text or of a line
        }
        final char last = before.charAt(before.length() - 1);
        if (!Character.isWhitespace(last)) {
            return false;
        }
        int n = before.length();
        while (n > 0 && (before.charAt(n - 1) == ' ' || before.charAt(n - 1) == '\t')) {
            n--;
        }
        if (capWords || (n > 0 && before.charAt(n - 1) == '\n')) {
            return true;                                // after the indentation of a new line
        }
        return SENTENCE_END.matcher(before).find() && endsSentence(before);
    }

    /** The punctuation before the white space really ends a sentence (not 5.3. or e.g.). */
    private static boolean endsSentence(String before) {
        final String text = rtrim(before);
        int i = text.length() - 1;
        while (i >= 0 && ".!?".indexOf(text.charAt(i)) < 0) {
            i--;                                        // skip closing quotes and brackets
        }
        if (text.charAt(i) != '.') {
            return true;
        }
        final Spacing sp = after(text.substring(0, i), ".", FIELD_TEXT);
        return sp == Spacing.NORMAL || sp == Spacing.WAIT;
    }

    /** "..", "?!", "!?", "!!", "??": the second symbol replaces the automatic space of the first. */
    static boolean repeatsSentenceEnd(String prev, String next) {
        if (prev.length() != 1 || next.length() != 1) {
            return false;
        }
        if (prev.equals(".")) {
            return next.equals(".");
        }
        return "!?".contains(prev) && "!?".contains(next);
    }

    /**
     * The automatic space after a sentence end turned out to be wrong because of what is typed
     * next: 128. + 3 (a number), ". + )" (a closing bracket or quote).
     * {@code prevChar} is the character before the symbol, {@code before} the text up to the cursor.
     */
    static boolean joinsAutoSpace(String prevSymbol, char prevChar, String before, String next) {
        if (next.length() != 1) {
            return false;
        }
        final char c = next.charAt(0);
        if (Character.isDigit(c)) {
            return ".,:".contains(prevSymbol) && Character.isDigit(prevChar);    // 128.32 1,000 12:30
        }
        if (c == ')' || c == ']' || c == '}') {
            return true;
        }
        if (prevSymbol.equals(".") && continuesAbbreviation(before, next)) {
            return true;                                // e. + g
        }
        if (c == '"') {
            int quotes = 0;
            for (int i = 0; i < before.length(); i++) {
                if (before.charAt(i) == '"') {
                    quotes++;
                }
            }
            return quotes % 2 == 1;                     // a closing quote: she said "no."
        }
        return false;
    }
}

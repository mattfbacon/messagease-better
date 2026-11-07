package nz.felle.messageasebetter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * English word prediction from the dictionary res/raw/en_dict (word frequencies and next-word
 * pairs, ported from DovUniStroke): completions of the word being typed, ranked by word
 * frequency plus a bonus for words that often follow the previous word, and next-word
 * suggestions when no word is being typed.
 */
final class Predictor {
    /** The context of the first word of a sentence. */
    static final String SENTENCE_START = "<s>";

    private static final int FOLLOWER_BONUS = 90;
    private static final int FOLLOWER_STEP = 10;

    private final String[] mWords;                  // sorted
    private final int[] mFreq;                      // 1..255, parallel to mWords
    private final Map<String, String[]> mFollowers; // previous word -> likely next words
    private final String[] mCommon;                 // most frequent words, best first

    /** A suggestion; fixed ones keep their case (Grobgeld, an address), the others take the typed one. */
    private static final class Cand {
        final String text;
        final boolean fixed;
        final int score;

        Cand(String text, boolean fixed, int score) {
            this.text = text;
            this.fixed = fixed;
            this.score = score;
        }
    }

    private Predictor(String[] words, int[] freq, Map<String, String[]> followers) {
        mWords = words;
        mFreq = freq;
        mFollowers = followers;
        Integer[] order = new Integer[words.length];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (a, b) -> freq[b] - freq[a]);
        mCommon = new String[Math.min(12, words.length)];
        for (int i = 0; i < mCommon.length; i++) {
            mCommon[i] = words[order[i]];
        }
    }

    static Predictor load(InputStream in) throws IOException {
        ArrayList<String> words = new ArrayList<>();
        ArrayList<Integer> freq = new ArrayList<>();
        HashMap<String, String[]> followers = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] f = line.split("\t");
                if (f.length != 3) {
                    continue;
                }
                if (f[0].equals("W")) {
                    words.add(f[1]);
                    freq.add(Integer.parseInt(f[2]));
                }
                else if (f[0].equals("B")) {
                    followers.put(f[1], f[2].split(" "));
                }
            }
        }
        finally {
            reader.close();
        }
        int[] fr = new int[freq.size()];
        for (int i = 0; i < fr.length; i++) {
            fr[i] = freq.get(i);
        }
        return new Predictor(words.toArray(new String[0]), fr, followers);
    }

    /**
     * Split the text before the cursor into {previous word, word being typed}. The word being
     * typed is a token of letters, digits and . @ _ + ' (so that an address is one), or empty.
     * Null where no word is being typed: after punctuation, or when the token ends in it.
     *
     * @param wholeField the text starts at the beginning of the field (so a sentence starts there)
     */
    static String[] context(String before, boolean wholeField) {
        int s = before.length();
        while (s > 0 && isTokenChar(before, s - 1)) {
            s--;
        }
        String prefix = before.substring(s);
        if (!prefix.isEmpty()) {
            if (!Character.isLetterOrDigit(prefix.charAt(prefix.length() - 1))) {
                return null;   // foo. or a@
            }
            if (s > 0) {
                char c = before.charAt(s - 1);
                if (!Character.isWhitespace(c) && "(\"'“‘[-—".indexOf(c) < 0) {
                    return null;
                }
            }
        }
        int p = s;
        boolean newline = false;
        while (p > 0 && Character.isWhitespace(before.charAt(p - 1))) {
            newline |= before.charAt(p - 1) == '\n';
            p--;
        }
        if (prefix.isEmpty() && s > 0 && p == s) {
            return null;
        }
        String prev;
        if (p == 0) {
            prev = wholeField ? SENTENCE_START : "";
        }
        else if (newline || ".!?".indexOf(before.charAt(p - 1)) >= 0) {
            prev = SENTENCE_START;
        }
        else if (isTokenChar(before, p - 1)) {
            int q = p;
            while (q > 0 && isTokenChar(before, q - 1)) {
                q--;
            }
            prev = key(before.substring(q, p));
        }
        else {
            prev = "";
        }
        return new String[] {prev, prefix};
    }

    private static boolean isTokenChar(String text, int i) {
        char c = text.charAt(i);
        if (Character.isLetterOrDigit(c)) {
            return true;
        }
        return (isApostrophe(c) || "._@+".indexOf(c) >= 0) && i > 0 && Character.isLetterOrDigit(text.charAt(i - 1));
    }

    private static boolean isWordPrefix(String prefix) {
        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            if (!Character.isLetter(c) && !isApostrophe(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isApostrophe(char c) {
        return c == '\'' || c == '’';
    }

    /** The dictionary form of a word: lower case, with the typographic apostrophe made plain. */
    private static String key(String word) {
        return word.toLowerCase(Locale.US).replace('’', '\'');
    }

    boolean isWord(String word) {
        return Arrays.binarySearch(mWords, key(word)) >= 0;
    }

    /**
     * Up to max suggestions, best first.
     *
     * @param prev   the lower case word before the one being typed, SENTENCE_START, or "" if unknown
     * @param prefix the token typed so far, possibly empty (then the next word is predicted)
     */
    List<String> suggest(String prev, String prefix, int max) {
        ArrayList<Cand> out = new ArrayList<>();
        String[] next = mFollowers.get(prev);

        if (prefix.isEmpty()) {
            if (next != null) {
                for (String w : next) {
                    add(out, new Cand(w, false, 0), max);
                }
            }
            for (String w : mCommon) {
                add(out, new Cand(w, false, 0), max);
            }
            return display(out, prev.equals(SENTENCE_START), prefix);
        }

        String lower = key(prefix);
        if (isWordPrefix(prefix)) {
            int lo = Arrays.binarySearch(mWords, lower);
            if (lo < 0) {
                lo = -lo - 1;
            }
            // Keep the best max candidates of the matching range, in a small sorted list
            int[] bestIdx = new int[max];
            int[] bestScore = new int[max];
            int n = 0;
            for (int i = lo; i < mWords.length && mWords[i].startsWith(lower); i++) {
                int score = mFreq[i] + bonus(next, mWords[i]);
                if (n == max && score <= bestScore[n - 1]) {
                    continue;
                }
                int j = n < max ? n++ : n - 1;
                while (j > 0 && bestScore[j - 1] < score) {
                    bestScore[j] = bestScore[j - 1];
                    bestIdx[j] = bestIdx[j - 1];
                    j--;
                }
                bestScore[j] = score;
                bestIdx[j] = i;
            }
            for (int i = 0; i < n; i++) {
                out.add(new Cand(mWords[bestIdx[i]], false, bestScore[i]));
            }
        }
        out.sort((a, b) -> b.score - a.score);
        ArrayList<Cand> best = new ArrayList<>();
        for (Cand c : out) {
            add(best, c, max);
        }
        return display(best, false, prefix);
    }

    private static int bonus(String[] next, String word) {
        if (next != null) {
            for (int i = 0; i < next.length; i++) {
                if (next[i].equals(word)) {
                    return FOLLOWER_BONUS - FOLLOWER_STEP * i;
                }
            }
        }
        return 0;
    }

    private static void add(List<Cand> out, Cand c, int max) {
        if (c == null || out.size() >= max) {
            return;
        }
        for (Cand o : out) {
            if (o.text.equalsIgnoreCase(c.text)) {
                return;
            }
        }
        out.add(c);
    }

    /** "i" and its contractions are always capitalized. */
    private static boolean alwaysCapital(String w) {
        return w.equals("i") || w.startsWith("i'");
    }

    /** Give the suggestions the case of what is typed: Th -> The, TH -> THE, and I, I'm. */
    private static List<String> display(List<Cand> cands, boolean sentenceStart, String prefix) {
        boolean allUpper = prefix.length() > 1 && prefix.equals(prefix.toUpperCase(Locale.US));
        boolean capital = sentenceStart
            || (!prefix.isEmpty() && Character.isUpperCase(prefix.charAt(0)));
        ArrayList<String> out = new ArrayList<>(cands.size());
        for (Cand c : cands) {
            String w = c.text;
            if (allUpper && w.indexOf('@') < 0) {
                w = w.toUpperCase(Locale.US);
            }
            else if (!c.fixed && (capital || alwaysCapital(w))) {
                w = Character.toUpperCase(w.charAt(0)) + w.substring(1);
            }
            out.add(w);
        }
        return out;
    }
}

package edu.macalester.mscs.characters;

import edu.macalester.mscs.utils.EntryComparator;
import edu.macalester.mscs.utils.Logger;
import edu.macalester.mscs.utils.WordUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * CharacterFinder provides lots of different utility for generating a list of characters from a text.
 * The basis of the algorithm is to find capitalized words that don't start sentences.
 *
 * It takes into its constructor a list of capitalized words that should be ignored and a list of
 * capitalized words that should not be unique (such as titles, ie. President in President Obama).
 * It also takes in a string of characters that should define a sentence start. These should normally
 * include a period, question mark, exclamation mark, and whatever form of double quote is used to
 * indicate the beginning of a dialogue sentence.
 *
 * The class has two underlying data structures: a counter and an instance of CharacterGroups.
 * The counter is used initially to accumulate capitalized words and phrases, and maintains a count
 * of each of their occurrences. A capitalized phrase is a sequence of capitalized words seperated by
 * only spaces and the words "of" and "the".
 *
 * The counter can be built up manually using increment word, or by using the countCapitalized method
 * which takes a list of strings representing lines in a text file and processes them automatically.
 * The two methods can be used in combination for precise control. The class similarly provides a
 * removeWords method to remove words that may be undesired.
 *
 * The class then provides a sequence of methods that return different sets of strings. These include
 * titled names (names generated by looking at words that come after general words), pluralized words
 * (one indication of surnames or non-name words), surnames (words that appear as the second word in
 * more than one pair of words), places (words that appear following "of" in a name), and lonely
 * words (words that do not appear in capitalized phrases). There is also a method that derives names
 * by finding word pairs that end in a word from a list of surnames, as well as a method that derives
 * first names from a list of names (it just takes the first word of each name).
 *
 * For further pruning the counter, there are methods removeWords, removeWordsBelowThreshold,
 * removePlaces, and removeTitles. The first two remove words from a specific subset of words, where
 * one requires the word to appear in the text less than a specified threshold of times. The latter
 * two remove redundant words that contain place names or titles which do not add descriptors. For
 * example, if both "President Barack Obama" and "Barack Obama" were present, removeTitles would
 * remove "President Barack Obama" because it is redundant give "Barack Obama".
 *
 * At this point, focus shifts to the second data structure, an instance of CharacterGroups. The
 * buildCharacterGroups method is used to construct the instance from the counter. This method takes
 * a set of nondescriptive strings that should not be grouped on. A user can then manually combine
 * groups using the various combineGroups methods.
 *
 * The groups can then be converted to String lists using the getNameList and getFirstNameList
 * methods. The former method takes in an (optional) list of names and returns a list of strings
 * representing the groups associated with those names, with the specified name first. If no list of
 * names is specified, all groups are returned. The latter method also takes a list of names, but
 * only returns significant single-word names from the various groups.
 *
 * @author Ari Weiland
 */
public class CharacterFinder {

    private final Set<String> ignoredWords;
    private final Set<String> titleWords;
    private final Set<String> generalWords;
    private final String punctuation;

    private Map<String, Integer> counter = new HashMap<>();
    private CharacterGroups characterGroups;

    public CharacterFinder(Set<String> ignoredWords, Set<String> titleWords, Set<String> generalWords, String punctuation) {
        this.ignoredWords = ignoredWords;
        this.titleWords = titleWords;
        this.generalWords = generalWords;
        this.punctuation = punctuation;
    }

    public Set<String> getIgnoredWords() {
        return ignoredWords;
    }

    public Set<String> getTitleWords() {
        return titleWords;
    }

    public Set<String> getGeneralWords() {
        return generalWords;
    }

    public String getPunctuation() {
        return punctuation;
    }

    public Map<String, Integer> getCounter() {
        return counter;
    }

    public CharacterGroups getCharacterGroups() {
        return characterGroups;
    }

    private boolean isIgnoredWord(String part) {
        return ignoredWords.contains(part);
    }

    private boolean isTitleWord(String s) {
        return titleWords.contains(s);
    }

    private boolean isGeneralWord(String s) {
        return isTitleWord(s) || generalWords.contains(s);
    }

    /**
     * Clears the counter and sets the characterGroups to null
     */
    public void clear() {
        counter.clear();
        characterGroups = null;
    }

    /**
     * Builds up the counter from lines of a text
     * @param lines
     */
    public void countCapitalized(List<String> lines) {
        for (String line : lines) {
            List<String> parts = breakLine(line);
            StringBuilder phrase = null;
            String toAdd = null;
            // find capitals that don't start sentences
            for (int i = 0; i < parts.size(); i++) {
                String part = parts.get(i);
                if (i > 0 && !WordUtils.precedesSentenceStart(parts.get(i - 1), punctuation)) {
                    if (WordUtils.isCapitalized(part) && !isIgnoredWord(part)) {
                        if (!isGeneralWord(part)) {
                            incrementName(part, 0);
                        }
                        if (phrase == null) {
                            phrase = new StringBuilder(part);
                        } else {
                            phrase.append(part);
                        }
                        toAdd = phrase.toString();
                    } else {
                        if (phrase != null) {
                            if (part.equals(" ") || part.equals("of") || part.equals("the")) {
                                phrase.append(part);
                            } else {
                                if (!isGeneralWord(toAdd)) {
                                    incrementName(toAdd, 1);
                                }
                                phrase = null;
                            }
                        }
                    }
                }
            }
            // go back and get sentence starters that we've already seen
            phrase = null;
            for (int i = 0; i < parts.size(); i++) {
                String part = parts.get(i);
                if (phrase != null || counter.containsKey(part) && (i == 0 || WordUtils.precedesSentenceStart(parts.get(i - 1), punctuation))) {
                    if (part.length() > 1 && WordUtils.isCapitalized(part) && !isIgnoredWord(part)) {
                        if (!isGeneralWord(part)) {
                            incrementName(part, 0);
                        }
                        if (phrase == null) {
                            phrase = new StringBuilder(part);
                        } else {
                            phrase.append(part);
                        }
                        toAdd = phrase.toString();
                    } else {
                        if (phrase != null) {
                            if (part.equals(" ") || part.equals("of") || part.equals("the")) {
                                phrase.append(part);
                            } else {
                                if (!isGeneralWord(toAdd)) {
                                    incrementName(toAdd, 1);
                                }
                                phrase = null;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Manually increment a name some amount
     * If the name is not in the counter, it gets added
     * @param name
     * @param increment
     */
    public void incrementName(String name, int increment) {
        if (counter.containsKey(name)) {
            counter.put(name, counter.get(name) + increment);
        } else {
            counter.put(name, increment);
        }
    }

    /**
     * If the first word of name is a title (in generalWords), it gets removed.
     * Otherwise returns name.
     * @param name
     * @return
     */
    public String stripTitle(String name) {
        String[] split = name.split(" ");
        if (split.length > 1 && isTitleWord(split[0]) && WordUtils.isCapitalized(split[1])) {
            return StringUtils.substringAfter(name, " ");
        } else {
            return name;
        }
    }

    /**
     * Returns a set of names that occur in a word triplet preceded by an ignored word
     * @return
     */
    public Set<String> getTitledNames() {
        Set<String> words = counter.keySet();
        Set<String> names = new HashSet<>();
        Set<String> partNames = new HashSet<>();
        for (String cap : words) {
            String[] split = cap.split(" ");
            String name = StringUtils.substringAfter(cap, " ");
            if (split.length > 1 && isTitleWord(split[0]) && WordUtils.isCapitalized(split[1]) && !isGeneralWord(split[1])) {
                if (split.length == 3 || (split[0].equals("Ko") || split[0].equals("Khal"))) {
                    names.add(name);
                } else {
                    partNames.add(split[1]);
                }
            }
        }
        for (String name : partNames) {
            boolean isUnique = true;
            for (String cap : words) {
                // cut off after "of" and "the" because anything preceding is either still unique, or inherently not unique
                cap = StringUtils.substringBefore(cap, " of ");
                cap = StringUtils.substringBefore(cap, " the ");
                String[] split = cap.split(" ");
                if (split.length == 2) {
                    // not unique if first in a pair, or if second in a pair and not preceded by a general word
                    if (name.equals(split[0]) || (!isGeneralWord(split[0]) && name.equals(split[1]))) {
                        isUnique = false;
                    }
                    // if something contains name and another non-general word, it's a name TODO this part is suspect
                    if (!isGeneralWord(split[0]) && !isGeneralWord(split[1])
                            && (name.equals(split[0]) || name.equals(split[1]))) {
                        names.add(cap);
                    }
                } else if (split.length > 2) {
                    for (String s : split) {
                        // if it comes up as part of anything else, or pluralized, its not unique
                        if (name.equals(s) || name.equals(s + "s")) {
                            isUnique = false;
                        }
                    }
                }
            }
            if (isUnique) {
                names.add(name);
            }
        }
        return names;
    }

    /**
     * Returns words that exist in words and phrases as both "xxxx" and "xxxxs" and at least once do not follow "the"
     * Ignores words found in generalWords
     * @return
     */
    public Set<String> getPluralizedNames() {
        Set<String> words = counter.keySet();
        Set<String> pluralized = new HashSet<>();
        for (String cap : words) {
            String[] split = cap.split(" ");
            for (int i = 1; i < split.length; i++) {
                String s = split[i];
                if (!isGeneralWord(s) && !split[i-1].equalsIgnoreCase("the")
                        && words.contains(s) && words.contains(s + "s")) {
                    pluralized.add(s);
                }
            }
        }
        return pluralized;
    }

    /**
     * Returns words that come second in multiple word pairs
     * Ignores words found in generalWords
     * @return
     */
    public Set<String> getSurnames() {
        Set<String> words = counter.keySet();
        Set<String> surnames = new HashSet<>();
        Set<String> once = new HashSet<>();
        for (String cap : words) {
            String name = stripTitle(cap);
            String[] split = name.split(" ");
            if (split.length == 2 && !isGeneralWord(split[0]) && !isGeneralWord(split[1])) {
                if (once.contains(split[1])) {
                    surnames.add(split[1]);
                } else {
                    once.add(split[1]);
                }
            }
        }
        return surnames;
    }

    /**
     * Returns phrases that follow "of" or "of the" in the capitalized phrases
     * @return
     * @param notPlaces
     */
    public Set<String> getPlaces(Collection<String> notPlaces) {
        Set<String> words = counter.keySet();
        Set<String> places = new HashSet<>();
        String notPlacesString = notPlaces.toString();
        for (String cap : words) {
            if (cap.contains(" of ")) {
                String place = cap.substring(cap.indexOf(" of") + 4);
                if (place.startsWith("the")) {
                    place = place.substring(4);
                }
                if (!notPlacesString.contains(stripTitle(place))) {
                    places.add(place);
                }
            }
        }
        return places;
    }

    /**
     * Returns all words that are not compound or part of compound phrases
     * @return
     */
    public Set<String> getLonelyWords() {
        Set<String> words = counter.keySet();
        Set<String> lonely = new HashSet<>(words);
        for (String cap : words) {
            if (cap.contains(" ")) {
                lonely.remove(cap);
                for (String s : cap.split(" ")) {
                    lonely.remove(s);
                }
            }
        }
        return lonely;
    }

    /**
     * Returns all word pairs whose second word is in surnames
     * Ignores pairs with the first word in generalWords
     * @param surnames
     * @return
     */
    public Set<String> getNamesBySurname(Collection<String> surnames) {
        Set<String> words = counter.keySet();
        Set<String> names = new HashSet<>();
        for (String cap : words) {
            String name = stripTitle(cap);
            String[] split = name.split(" ");
            if (split.length == 2 && !isGeneralWord(split[0]) && surnames.contains(split[1])) {
                if (words.contains(name)) {
                    names.add(name);
                } else {
                    names.add(cap);
                }
            }
        }
        return names;
    }

    /**
     * Returns a set of the first words in each of names
     * @param names
     * @return
     */
    public Set<String> getFirstNames(Collection<String> names) {
        Set<String> firstNames = new HashSet<>();
        for (String cap : names) {
            firstNames.add(StringUtils.substringBefore(stripTitle(cap), " "));
        }
        return firstNames;
    }

    /**
     * Returns a new set that is the intersection of two sets, via the Set.retainAll method
     * @param set1
     * @param set2
     * @param <T>
     * @return
     */
    public static <T> Set<T> intersectSets(Set<T> set1, Set<T> set2) {
        Set<T> intersection = new HashSet<>(set2);
        intersection.retainAll(set1);
        return intersection;
    }

    /**
     * Removes phrases ending in places that are redundant, meaning the primary name
     * (string preceding "of") is not unique
     */
    public void removePlaces() {
        Map<String, Integer> reducedCounter = new HashMap<>();
        for (String cap : counter.keySet()) {
            String noPlace = cap;
            // strip "of..." bits
            if (noPlace.contains(" of ")) {
                String temp = StringUtils.substringBefore(noPlace, " of ");
                if (!isGeneralWord(temp)) {
                    noPlace = temp;
                }
            }
            if (!reducedCounter.containsKey(noPlace)) {
                reducedCounter.put(noPlace, 0);
            }
            reducedCounter.put(noPlace, reducedCounter.get(noPlace) + counter.get(cap));
        }
        counter = reducedCounter;
    }

    /**
     * Removes phrases starting with titles (elements of generalWords) that are not unique,
     * meaning the primary name following any set of initial titles. The primary name must
     * be compound (contain a space)
     */
    public void removeTitles() {
        Map<String, Integer> reducedCounter = new HashMap<>();
        for (String cap : counter.keySet()) {
            String noTitle = cap;
            for (int i = StringUtils.countMatches(noTitle, ' '); i > 1; i--) {
                noTitle = stripTitle(noTitle);
            }
            if (!reducedCounter.containsKey(noTitle)) {
                reducedCounter.put(noTitle, 0);
            }
            reducedCounter.put(noTitle, reducedCounter.get(noTitle) + counter.get(cap));
        }
        counter = reducedCounter;
    }

    /**
     * Removes certain words from the counter
     * @param words
     */
    public void removeWords(String... words) {
        removeWords(Arrays.asList(words));
    }

    /**
     * Removes certain words from the counter
     * @param words
     */
    public void removeWords(Collection<String> words) {
        removeWordsBelowThreshold(words, Integer.MAX_VALUE);
    }

    /**
     * Removes certain words from the counter only if their occurrence is less than threshold
     * @param words
     * @param threshold
     */
    public void removeWordsBelowThreshold(Collection<String> words, int threshold) {
        Map<String, Integer> reducedCounter = new HashMap<>();
        for (String cap : counter.keySet()) {
            if (!words.contains(cap) || counter.get(cap) >= threshold) {
                reducedCounter.put(cap, counter.get(cap));
            }
        }
        counter = reducedCounter;
    }

    /**
     * Builds the internal CharacterGroups data structure
     * @param nondescriptors
     */
    public void buildCharacterGroups(Set<String> nondescriptors) {
        characterGroups = new CharacterGroups(counter, nondescriptors);
    }

    /**
     * Manually combine the groups containing each of the specified names
     * @param names
     */
    public void combineGroups(String... names) {
        combineGroups(Arrays.asList(names));
    }

    /**
     * Manually combine the groups containing each of the specified names
     * @param names
     */
    public void combineGroups(Collection<String> names) {
        String first = names.iterator().next();
        for (String s : names) {
            characterGroups.combineGroups(first, s);
        }
    }

    /**
     * Returns a list of strings, each of which corresponds to one of the CharacterGroups
     * Each line contains all the aliases in that group, and each line is one group
     * @return
     */
    public List<String> getNameList() {
        return getNameList(characterGroups.getPrimaryAliases());
    }

    /**
     * Returns a list of strings, each of which corresponds to one of the CharacterGroups
     * Each line contains all the aliases in that group, and each line is one group
     * Each line corresponds (in iteration order) to the group associated with the name in names
     * @return
     */
    public List<String> getNameList(Collection<String> names) {
        Map<List<String>, Integer> groupMap = new HashMap<>();
        for (String s : names) {
            if (characterGroups.isAlias(s)) {
                List<String> list = new ArrayList<>();
                list.add(s);
                list.addAll(characterGroups.getGroup(s));
                groupMap.put(list, characterGroups.getAliasCount(s));
            } else {
                System.out.println(s);
            }
        }

        List<Map.Entry<List<String>, Integer>> groups = new ArrayList<>(groupMap.entrySet());
        groups.sort(EntryComparator.DESCENDING);
        List<String> lines = new ArrayList<>();
        for (Map.Entry<List<String>, Integer> group : groups) {
            System.out.println(group.getValue() + "\t" + group.getKey());
            StringBuilder line = new StringBuilder();
            boolean first = true;
            for (String name : group.getKey()) {
                if (first) {
                    first = false;
                } else {
                    line.append(",");
                }
                line.append(name);
            }
            lines.add(line.toString());
        }
        System.out.println();
        System.out.println(groups.size());
        System.out.println();
        return lines;
    }

    /**
     * Returns a list of strings, each of which corresponds to one of the CharacterGroups
     * Each line contains all single-word aliases in that group, and each line is one group
     * Each line corresponds (in iteration order) to the group associated with the name in names
     * @return
     */
    public List<String> getFirstNameList(Collection<String> names) {
        Map<List<String>, Integer> groupMap = new HashMap<>();
        for (String name : names) {
            if (characterGroups.isAlias(name)) {
                List<String> list = new ArrayList<>();
                list.add(StringUtils.substringBefore(stripTitle(name), " "));
                Set<String> set = new HashSet<>();
                for (String s : characterGroups.getGroup(name)) {
                    String firstName = StringUtils.substringBefore(stripTitle(s), " ");
                    if (!isGeneralWord(firstName)) {
                        set.add(firstName);
                    }
                }
                list.addAll(set);
                groupMap.put(list, characterGroups.getAliasCount(name));
            } else {
                System.out.println(name);
            }
        }

        List<Map.Entry<List<String>, Integer>> groups = new ArrayList<>(groupMap.entrySet());
        groups.sort(EntryComparator.DESCENDING);
        List<String> lines = new ArrayList<>();
        for (Map.Entry<List<String>, Integer> group : groups) {
            System.out.println(group.getValue() + "\t" + group.getKey());
            StringBuilder line = new StringBuilder();
            boolean first = true;
            for (String name : group.getKey()) {
                if (first) {
                    first = false;
                } else {
                    line.append(",");
                }
                line.append(name);
            }
            lines.add(line.toString());
        }
        System.out.println();
        System.out.println(groups.size());
        System.out.println();
        return lines;
    }

    /**
     * Prints out the counter in a nice format to the console,
     * and returns the strings as a Logger
     */
    public Logger printCounter() {
        Logger logger = new Logger();
        List<Map.Entry<String, Integer>> caps = new ArrayList<>(counter.entrySet());
        caps.sort(EntryComparator.DESCENDING);
        for (Map.Entry<String, Integer> cap : caps) {
            logger.log(cap.getKey() + "," + cap.getValue());
        }
        logger.log();
        logger.log(counter.size());
        return logger;
    }

    /**
     * Breaks a line of text into chunks
     * Each chunk is either a contiguous sequence of alphabetic characters, or
     * a filler sequence of non-alphabetic characters
     * Every character in the line occurs in the output (as distinct from
     * String.split, which removes instances of the splitting character)
     * @param line
     * @return
     */
    private static List<String> breakLine(String line) {
        List<String> pieces = new ArrayList<>();
        StringBuilder sb = null;
        boolean letters = true;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (Character.isAlphabetic(c)) {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                if (letters) {
                    sb.append(c);
                } else {
                    pieces.add(sb.toString());
                    letters = true;
                    sb = new StringBuilder("" + c);
                }
            } else {
                if (sb != null) { // ignore leading punctuation
                    if (!letters) {
                        sb.append(c);
                    } else {
                        pieces.add(sb.toString());
                        letters = false;
                        sb = new StringBuilder("" + c);
                    }
                }
            }
        }
        if (sb != null) {
            pieces.add(sb.toString());
        }
        return pieces;
    }

}

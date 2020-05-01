package com.ess.anime.wallpaper.website.search;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 搜索下拉提示TagJson解析器（Konachan、Yande、Lolibooru通用）
// 参考js地址：http://konachan.com/assets/moe-legacy/application-ce3dc3e48d9ceff5fb362df103a23576.js
public class GeneralAutoCompleteParser {

    /*
     * Return an array of completions for a tag.  Tag types of returned tags will be
     * registered in Post.tag_types, if necessary.
     *
     * options = {
     *   max_results: 10
     * }
     *
     * [["tag1", "tag2", "tag3"], 1]
     *
     * The value 1 is the number of results from the beginning which come from recent_tags,
     * rather than tag_data.
     */
    public static List<String> getSearchAutoCompleteListFromDB(String tagJson, String search) {
        int maxResults = 15;

        /* Make a list of all results; this will be ordered recent tags first, other tags
         * sorted by tag count.  Request more results than we need, since we'll reorder
         * them below before cutting it off. */
        String regex = createTagSearchRegex(search, false);
        List<String> resultList = retrieveTagSearch(tagJson, regex, maxResults);

        /*
         * Contents matches (t*g*m -> tagme) are lower priority than other results.  Within
         * each search type (recent and main), sort them to the bottom.
         */
        resultList = reorderSearchResults(search, resultList);
        if (resultList.size() > maxResults) {
            resultList = resultList.subList(0, maxResults);
        }

        return resultList;
    }

    private static String createTagSearchRegex(String search, boolean isTopResultsOnly) {
        /* Split the tag by character. */
        String[] letters = search.split("");

        /*
         * We can do a few search methods:
         *
         * 1: Ordinary prefix search.
         * 2: Name search. "aaa_bbb" -> "aaa*_bbb*|bbb*_aaa*".
         * 3: Contents search; "tgm" -> "t*g*m*" -> "tagme".  The first character is still always
         * matched exactly.
         *
         * Avoid running multiple expressions.  Instead, combine these into a single one, then run
         * each part on the results to determine which type of result it is.  Always show prefix and
         * name results before contents results.
         */
        List<String> regexParts = new ArrayList<>();

        /* Allow basic word prefix matches.  "tag" matches at the beginning of any word
         * in a tag, eg. both "tagme" and "dont_tagme". */
        /* Add the regex for ordinary prefix matches. */
        StringBuilder s = new StringBuilder("(([^`]*_)?");
        for (String letter : letters) {
            s.append(regExpEscape(letter));
        }
        s.append(")");
        regexParts.add(s.toString());

        /* Allow "fir_las" to match both "first_last" and "last_first". */
        if (search.contains("_")) {
            String first = search.split("_")[0];
            String last = search.substring(first.length() + 1);

            first = regExpEscape(first);
            last = regExpEscape(last);

            String str = "(";
            str += "(" + first + "[^`]*_" + last + ")";
            str += "|";
            str += "(" + last + "[^`]*_" + first + ")";
            str += ")";
            regexParts.add(str);
        }

        /* Allow "tgm" to match "tagme".  If isTopResultsOnly is true, we only want primary results,
         * so omit this match. */
        if (isTopResultsOnly) {
            s = new StringBuilder("(");
            for (String letter : letters) {
                s.append(regExpEscape(letter)).append("[^`]*");
            }
            s.append(")");
            regexParts.add(s.toString());
        }

        /* The space is included in the result, so the result tags can be matched with the
         * same regexes, for in reorder_search_results.
         *
         * (\d)+  match the alias ID                      1`
         * [^ ]*: start at the beginning of any alias     1`foo`bar`
         * ... match ...
         * [^`]*` all matches are prefix matches          1`foo`bar`tagme`
         * [^ ]*  match any remaining aliases             1`foo`bar`tagme`tag_me`
         */

        String regex = TextUtils.join("|", regexParts);
        regex = "(\\d+)[^ ]*`(" + regex + ")[^`]*`[^ ]* ";
        return regex;
    }

    private static List<String> retrieveTagSearch(String tagJson, String regex, int maxResults) {
        List<String> tagList = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tagJson);
        while (matcher.find() && tagList.size() < maxResults) {
            String tag = matcher.group();
            /* Ignore this tag.  We need a better way to blackhole tags. */
            if (tag.contains(":deletethistag:"))
                continue;
            String[] items = tag.split("`");
            if (items.length > 1) {
                if (!tagList.contains(tag)) {
                    tagList.add(items[1]);
                }
            }
        }
        return tagList;
    }

    private static List<String> reorderSearchResults(String search, List<String> resultList) {
        String regex = createTagSearchRegex(search, true);
        List<String> topList = new ArrayList<>();
        List<String> bottomList = new ArrayList<>();

        Pattern regExp = Pattern.compile(regex);
        for (String tag : resultList) {
            if (regExp.matcher(tag).find()) {
                topList.add(tag);
            } else {
                bottomList.add(tag);
            }
        }
        topList.addAll(bottomList);
        return topList;
    }

    private static String regExpEscape(String str) {
        return str.replaceAll("/([.*+?^=!:${}()|[\\\\]\\\\/\\\\])/g", "\\\\$1");
    }

}

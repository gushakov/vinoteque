package vinoteque.utils;

import java.io.File;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comparator for {@link File} instances which imposes an order based
 * on the value of the first matching group of the specified {@link Pattern}.
 * Returns zero for the files with names which do not match the pattern or
 * if the pattern does not contain a match group. 
 * @author gushakov
 */
public class RegexGroupComparator implements Comparator<File> {
    private Pattern pattern;
    
    public RegexGroupComparator(Pattern pattern) {
        this.pattern = pattern;
        
    }

    @Override
    public int compare(File file1, File file2) {
        int answer = 0;
        Matcher matcher1 = pattern.matcher(file1.getName());
        Matcher matcher2 = pattern.matcher(file2.getName());
        if (matcher1.matches() && matcher2.matches()){
            try {
                if ((pattern.flags() & Pattern.CASE_INSENSITIVE) > 0){
                    answer = matcher1.group(1).compareToIgnoreCase(matcher2.group(1));    
                }
                else {
                    answer = matcher1.group(1).compareTo(matcher2.group(1));
                }                
            } catch (IndexOutOfBoundsException e) {
                //ignore, no matching group in the pattern
            }
        }
        return answer;
    }
    
    
    
}

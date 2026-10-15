package zone.vao.nexoAddon.populators;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record PopulatorRange(int min, int max) {

  private static final Pattern RANGE = Pattern.compile("(-?\\d+)\\s*-\\s*(-?\\d+)");

  public static PopulatorRange parse(Object value, boolean allowScan) {
    String text = String.valueOf(value).trim();
    Matcher matcher = RANGE.matcher(text);
    int first;
    int second;
    if (matcher.matches()) {
      first = Integer.parseInt(matcher.group(1));
      second = Integer.parseInt(matcher.group(2));
    } else {
      first = Integer.parseInt(text);
      second = first;
    }
    int min = Math.min(first, second);
    int max = Math.max(first, second);
    if (allowScan && min == max && min < 0) return new PopulatorRange(-1, -1);
    if (min < 0 || max > 100000) throw new IllegalArgumentException("Range must be between 0 and 100000");
    return new PopulatorRange(min, max);
  }

  public int sample(Random random) {
    return min == max ? min : random.nextInt(max - min + 1) + min;
  }

  public boolean scansChunk() {
    return min < 0;
  }
}

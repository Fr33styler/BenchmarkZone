package ro.fr33styler.benchmarkzone;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.regex.Pattern;

public class Main {

    private static final Pattern FROM_HEX_PATTERN = Pattern.compile("&(#[a-fA-F0-9]{6})");
    private static final Pattern TO_HEX_PATTERN =
            Pattern.compile("[§&]x[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])");

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Main.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public void fromHexBenchmark() {
        fromHexPattern("Hello World! Today we're testing &#aabbcc, &#af0123 and &#231278");
    }

    @Benchmark
    public void fromHexOldBenchmark() {
        fromHex("Hello World! Today we're testing &#aabbcc, &#af0123 and &#231278");
    }

    public static String toHexPattern(String text) {
        return TO_HEX_PATTERN.matcher(text).replaceAll("#$1$2$3$4$5$6");
    }

    public static String fromHexPattern(String text) {
        return FROM_HEX_PATTERN.matcher(text).replaceAll(result -> {
            String hex = result.group(1);

            char[] chars = new char[14];
            chars[0] = '§';
            chars[1] = 'x';
            for (int i = 1; i < hex.length(); i++) {
                chars[i * 2] = '§';
                chars[i * 2 + 1] = hex.charAt(i);
            }

            return new String(chars);
        });
    }

    public static String toHex(String text) {
        if (text.isEmpty()) return text;

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);

            if (character == '§' && i + 14 <= text.length() && text.charAt(i + 1) == 'x') {

                builder.append('#');

                for (int j = 2; j < 14; j++) {
                    char hex = text.charAt(i + j);
                    if (j % 2 == 0 && hex == '§') continue;

                    if (Character.digit(hex, 16) == -1) {
                        builder.setLength(builder.length() - j >> 1);
                        builder.append(character);
                        break;
                    }

                    builder.append(Character.toUpperCase(hex));
                    if (j == 13) i += j;
                }
            } else {
                builder.append(character);
            }
        }

        return builder.toString();
    }

    public static String fromHex(String text) {
        if (text.isEmpty()) return text;

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);

            if (character == '#' && i + 7 <= text.length()) {

                if (i > 0 && text.charAt(i - 1) == '&') {
                    builder.setCharAt(builder.length() - 1, '§');
                } else {
                    builder.append('§');
                }

                builder.append('x');

                for (int j = 1; j < 7; j++) {
                    char hex = Character.toLowerCase(text.charAt(i + j));

                    if (Character.digit(hex, 16) == -1) {
                        builder.setLength(builder.length() - j * 2);
                        if (i > 0 && text.charAt(i - 1) == '&') {
                            builder.append('&');
                        }
                        builder.append(character);
                        break;
                    }

                    builder.append('§');
                    builder.append(hex);
                    if (j == 6) i += j;
                }
            } else {
                builder.append(character);
            }
        }

        return builder.toString();
    }

}